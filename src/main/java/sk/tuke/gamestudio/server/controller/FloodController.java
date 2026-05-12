package sk.tuke.gamestudio.server.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import sk.tuke.gamestudio.entity.Achievement;
import sk.tuke.gamestudio.entity.AchievementType;
import sk.tuke.gamestudio.entity.Comment;
import sk.tuke.gamestudio.entity.Rating;
import sk.tuke.gamestudio.entity.Score;
import sk.tuke.gamestudio.game.flood.core.Field;
import sk.tuke.gamestudio.game.flood.core.GameState;
import sk.tuke.gamestudio.game.flood.core.TileColor;
import sk.tuke.gamestudio.game.flood.core.TileState;
import sk.tuke.gamestudio.service.*;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class FloodController {

    private static final String GAME_NAME = "flood";

    @Autowired private ScoreService scoreService;
    @Autowired private CommentService commentService;
    @Autowired private RatingService ratingService;
    @Autowired private UserService userService;
    @Autowired private AchievementService achievementService;

    // ===================== GAME PAGE =====================
    @GetMapping("/")
    public String game(HttpSession session, Model model,
                       @RequestParam(required = false) String ld) {
        String player = (String) session.getAttribute("player");
        model.addAttribute("player", player);

        // Public data — always visible to everyone (logged-in and guests)
        String scoresDifficulty = (ld != null && List.of("easy","medium","hard").contains(ld)) ? ld : "medium";
        String scoresDifficultyLabel = switch (scoresDifficulty) {
            case "easy" -> "Malá";
            case "hard" -> "Veľká";
            default     -> "Stredná";
        };
        model.addAttribute("scoresDifficulty", scoresDifficulty);
        model.addAttribute("scoresDifficultyLabel", scoresDifficultyLabel);
        try { model.addAttribute("scores", scoreService.getTopScores(GAME_NAME + "-" + scoresDifficulty)); }
        catch (Exception e) { model.addAttribute("scores", List.of()); }
        try { model.addAttribute("comments", commentService.getComments(GAME_NAME)); }
        catch (Exception e) { model.addAttribute("comments", List.of()); }
        try { model.addAttribute("avgRating", ratingService.getAverageRating(GAME_NAME)); }
        catch (Exception e) { model.addAttribute("avgRating", 0); }

        Field field = (Field) session.getAttribute("field");
        String difficulty = (String) session.getAttribute("difficulty");
        if (field == null) {
            field = newField("medium");
            difficulty = "medium";
            session.setAttribute("field", field);
            session.setAttribute("difficulty", difficulty);
            session.setAttribute("gameEndRecorded", false);
            session.setAttribute("gameStartMs", null);
            session.setAttribute("gameDurationSeconds", 0);
            session.setAttribute("moveHistory", new ArrayList<String>());
            session.setAttribute("hintUsed", false);

            if (player != null) {
                try {
                    List<Score> playerScores = scoreService.getScoresByPlayer(GAME_NAME, player);
                    int wonFromDb = playerScores.size();
                    int bestFromDb = playerScores.stream().mapToInt(Score::getPoints).max().orElse(0);
                    session.setAttribute("gamesPlayed", wonFromDb);
                    session.setAttribute("gamesWon", wonFromDb);
                    session.setAttribute("bestScore", bestFromDb);
                } catch (Exception ignored) {
                    session.setAttribute("gamesPlayed", 0);
                    session.setAttribute("gamesWon", 0);
                    session.setAttribute("bestScore", 0);
                }
            }
        }

        boolean isDailyChallenge = Boolean.TRUE.equals(session.getAttribute("isDailyChallenge"));

        // Record game end once
        GameState state = field.getGameState();
        if (state == GameState.SOLVED || state == GameState.FAILED) {
            Boolean recorded = (Boolean) session.getAttribute("gameEndRecorded");
            if (recorded == null || !recorded) {
                Long startMs = (Long) session.getAttribute("gameStartMs");
                int durationSecs = startMs != null
                        ? (int) ((System.currentTimeMillis() - startMs) / 1000)
                        : 0;
                session.setAttribute("gameDurationSeconds", durationSecs);

                int gamesPlayed = getInt(session, "gamesPlayed") + 1;
                int gamesWon    = getInt(session, "gamesWon");
                int bestScore   = getInt(session, "bestScore");

                if (state == GameState.SOLVED) {
                    gamesWon++;
                    int points = field.getMaxMoves() - field.getMoveCount();
                    if (points > bestScore) bestScore = points;

                    if (player != null) {
                        String gameKey = isDailyChallenge ? todayDailyGameKey() : (GAME_NAME + "-" + difficulty);
                        try {
                            scoreService.addScore(new Score(gameKey, player, points, durationSecs, new Date()));
                        } catch (Exception ignored) {}

                        // Award achievements for regular games
                        if (!isDailyChallenge) {
                            try {
                                int totalWins = scoreService.getScoresByPlayer(GAME_NAME, player).size();
                                boolean hintUsed = Boolean.TRUE.equals(session.getAttribute("hintUsed"));
                                if (totalWins == 1) achievementService.award(player, AchievementType.FIRST_WIN);
                                if (!hintUsed) achievementService.award(player, AchievementType.HINTLESS);
                                if (field.getMoveCount() <= field.getMaxMoves() / 2) achievementService.award(player, AchievementType.PERFECTIONIST);
                                if (durationSecs > 0 && durationSecs < 60) achievementService.award(player, AchievementType.SPEEDRUNNER);
                                if ("hard".equals(difficulty)) achievementService.award(player, AchievementType.HARD_WINNER);
                                if (totalWins >= 10) achievementService.award(player, AchievementType.VETERAN);
                            } catch (Exception ignored) {}
                        } else {
                            // Award daily challenge achievements
                            try {
                                int dailyWins = scoreService.getScoresByPlayer("flood-daily", player).size();
                                achievementService.award(player, AchievementType.DAILY_FIRST);
                                if (dailyWins >= 10)  achievementService.award(player, AchievementType.DAILY_10);
                                if (dailyWins >= 50)  achievementService.award(player, AchievementType.DAILY_50);
                                if (dailyWins >= 100) achievementService.award(player, AchievementType.DAILY_100);
                            } catch (Exception ignored) {}
                        }
                    }
                }

                session.setAttribute("gamesPlayed", gamesPlayed);
                session.setAttribute("gamesWon", gamesWon);
                session.setAttribute("bestScore", bestScore);
                session.setAttribute("gameEndRecorded", true);
            }
        }

        // Daily challenge: check if already played today
        boolean dailyAlreadyPlayed = false;
        List<Score> dailyScores = List.of();
        if (isDailyChallenge) {
            try {
                List<Score> todayPlays = scoreService.getScoresByPlayer(todayDailyGameKey(), player);
                dailyAlreadyPlayed = todayPlays.stream().anyMatch(s -> todayDailyGameKey().equals(s.getGame()));
            } catch (Exception ignored) {}
            try { dailyScores = scoreService.getTopScores(todayDailyGameKey()); }
            catch (Exception ignored) {}
        }

        // Build board data
        int rows = field.getRows(), cols = field.getCols();
        List<List<String>> board = new ArrayList<>();
        StringBuilder boardJson   = new StringBuilder("[");
        StringBuilder floodedJson = new StringBuilder("[");
        for (int r = 0; r < rows; r++) {
            List<String> row = new ArrayList<>();
            if (r > 0) { boardJson.append(","); floodedJson.append(","); }
            boardJson.append("["); floodedJson.append("[");
            for (int c = 0; c < cols; c++) {
                String color = field.getTile(r, c).getColor().name().toLowerCase();
                boolean flooded = field.getTile(r, c).getState() == TileState.FLOODED;
                row.add(color);
                if (c > 0) { boardJson.append(","); floodedJson.append(","); }
                boardJson.append("\"").append(color).append("\"");
                floodedJson.append(flooded ? "true" : "false");
            }
            boardJson.append("]"); floodedJson.append("]");
            board.add(row);
        }
        boardJson.append("]"); floodedJson.append("]");

        int total   = rows * cols;
        int flooded = field.getFloodedCount();

        boolean showHint = Boolean.TRUE.equals(session.getAttribute("showHint"));
        TileColor hint   = showHint ? bestGainColor(field) : null;

        Long gameStartMs = (Long) session.getAttribute("gameStartMs");
        boolean timerStarted = gameStartMs != null;

        String difficultyLabel = switch (difficulty) {
            case "easy" -> "Malá";
            case "hard" -> "Veľká";
            default     -> "Stredná";
        };

        model.addAttribute("field", field);
        model.addAttribute("board", board);
        model.addAttribute("boardJson", boardJson.toString());
        model.addAttribute("floodedJson", floodedJson.toString());
        model.addAttribute("difficulty", difficulty);
        model.addAttribute("difficultyLabel", difficultyLabel);
        model.addAttribute("colors", TileColor.values());
        model.addAttribute("currentColor", field.getTile(0, 0).getColor().name().toLowerCase());
        model.addAttribute("hintColor", hint != null ? hint.name().toLowerCase() : "");
        model.addAttribute("showHint", showHint);
        model.addAttribute("gameState", state.name());
        model.addAttribute("flooded", flooded);
        model.addAttribute("total", total);
        model.addAttribute("floodedPct", flooded * 100 / total);
        model.addAttribute("timerStarted", timerStarted);
        model.addAttribute("gameStartMs", timerStarted ? gameStartMs : 0L);
        model.addAttribute("gameDurationSeconds", getInt(session, "gameDurationSeconds"));
        model.addAttribute("moveHistory", session.getAttribute("moveHistory"));
        model.addAttribute("gamesPlayed", getInt(session, "gamesPlayed"));
        model.addAttribute("gamesWon", getInt(session, "gamesWon"));
        model.addAttribute("bestScore", getInt(session, "bestScore"));

        // Override leaderboard difficulty per player's current game if not overridden by ?ld=
        if (ld == null) {
            String byDiff = GAME_NAME + "-" + difficulty;
            try { model.addAttribute("scores", scoreService.getTopScores(byDiff)); }
            catch (Exception e) { model.addAttribute("scores", List.of()); }
            model.addAttribute("scoresDifficulty", difficulty);
            model.addAttribute("scoresDifficultyLabel", switch (difficulty) {
                case "easy" -> "Malá";
                case "hard" -> "Veľká";
                default     -> "Stredná";
            });
        }
        if (player != null) {
            try { model.addAttribute("myRating", ratingService.getRating(GAME_NAME, player)); }
            catch (Exception e) { model.addAttribute("myRating", 0); }
        } else {
            model.addAttribute("myRating", 0);
        }

        model.addAttribute("isDailyChallenge", isDailyChallenge);
        model.addAttribute("dailyAlreadyPlayed", dailyAlreadyPlayed);
        model.addAttribute("dailyScores", dailyScores);
        model.addAttribute("todayDate", LocalDate.now(ZoneId.of("Europe/Bratislava")).toString());

        Set<String> earnedTypes = Set.of();
        int dailyWinsCount = 0;
        if (player != null) {
            try {
                List<Achievement> achievements = achievementService.getAchievements(player);
                earnedTypes = achievements.stream().map(Achievement::getType).collect(Collectors.toSet());
            } catch (Exception ignored) {}
            try {
                dailyWinsCount = scoreService.getScoresByPlayer("flood-daily", player).size();
            } catch (Exception ignored) {}
        }
        model.addAttribute("earnedTypes", earnedTypes);
        model.addAttribute("dailyWinsCount", dailyWinsCount);
        model.addAttribute("allAchievementTypes", AchievementType.values());
        model.addAttribute("totalAchievementCount", AchievementType.values().length);

        return "flood";
    }

    // ===================== NEW GAME =====================
    @PostMapping("/new")
    public String newGame(@RequestParam String difficulty, HttpSession session) {
        session.setAttribute("field", newField(difficulty));
        session.setAttribute("difficulty", difficulty);
        session.setAttribute("gameEndRecorded", false);
        session.setAttribute("gameStartMs", null);
        session.setAttribute("gameDurationSeconds", 0);
        session.setAttribute("moveHistory", new ArrayList<String>());
        session.setAttribute("isDailyChallenge", false);
        session.setAttribute("showHint", false);
        session.setAttribute("hintUsed", false);
        return "redirect:/";
    }

    // ===================== DAILY CHALLENGE =====================
    @PostMapping("/daily")
    public String startDaily(HttpSession session) {
        String player = (String) session.getAttribute("player");
        if (player == null) return "redirect:/login";
        session.setAttribute("field", newDailyField());
        session.setAttribute("difficulty", "medium");
        session.setAttribute("gameEndRecorded", false);
        session.setAttribute("gameStartMs", null);
        session.setAttribute("gameDurationSeconds", 0);
        session.setAttribute("moveHistory", new ArrayList<String>());
        session.setAttribute("isDailyChallenge", true);
        session.setAttribute("showHint", false);
        session.setAttribute("hintUsed", false);
        return "redirect:/";
    }

    // ===================== FLOOD MOVE =====================
    @SuppressWarnings("unchecked")
    @PostMapping("/flood")
    public String flood(@RequestParam String color, HttpSession session) {
        Field field = (Field) session.getAttribute("field");
        if (field != null && field.getGameState() == GameState.PLAYING) {
            // Start timer on first move
            if (session.getAttribute("gameStartMs") == null) {
                session.setAttribute("gameStartMs", System.currentTimeMillis());
            }
            field.flood(TileColor.valueOf(color.toUpperCase()));
            List<String> history = (List<String>) session.getAttribute("moveHistory");
            if (history == null) history = new ArrayList<>();
            history.add(color);
            session.setAttribute("moveHistory", history);
        }
        return "redirect:/";
    }

    // ===================== HINT TOGGLE =====================
    @PostMapping("/toggle-hint")
    public String toggleHint(HttpSession session) {
        boolean current = Boolean.TRUE.equals(session.getAttribute("showHint"));
        session.setAttribute("showHint", !current);
        if (!current) {
            session.setAttribute("hintUsed", true);
        }
        return "redirect:/";
    }

    // ===================== COMMENT =====================
    @PostMapping("/comment")
    public String comment(@RequestParam String content, HttpSession session) {
        String player = (String) session.getAttribute("player");
        if (player != null && content != null && !content.isBlank()) {
            try { commentService.addComment(new Comment(GAME_NAME, player, content.trim(), new Date())); }
            catch (Exception ignored) {}
        }
        return "redirect:/";
    }

    // ===================== RATING =====================
    @PostMapping("/rating")
    public String rating(@RequestParam int stars, HttpSession session) {
        String player = (String) session.getAttribute("player");
        if (player != null && stars >= 1 && stars <= 5) {
            try { ratingService.setRating(new Rating(GAME_NAME, player, stars, new Date())); }
            catch (Exception ignored) {}
        }
        return "redirect:/";
    }

    // ===================== LOGIN / REGISTER =====================
    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String success,
                            @RequestParam(required = false) String tab,
                            Model model) {
        model.addAttribute("error", error);
        model.addAttribute("success", success);
        model.addAttribute("tab", tab != null ? tab : "login");
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session) {
        if (username.isBlank()) {
            return "redirect:/login?error=Zadajte+meno&tab=login";
        }
        try {
            boolean ok = userService.login(username.trim(), password);
            if (!ok) return "redirect:/login?error=Nespravne+meno+alebo+heslo&tab=login";
        } catch (Exception e) {
            return "redirect:/login?error=Nespravne+meno+alebo+heslo&tab=login";
        }
        session.setAttribute("player", username.trim());
        return "redirect:/";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String password2,
                           HttpSession session) {
        if (username.isBlank()) {
            return "redirect:/login?error=" + enc("Meno nesmie byť prázdne") + "&tab=register";
        }
        if (password.length() < 3) {
            return "redirect:/login?error=" + enc("Heslo musí mať aspoň 3 znaky") + "&tab=register";
        }
        if (!password.equals(password2)) {
            return "redirect:/login?error=" + enc("Heslá sa nezhodujú") + "&tab=register";
        }
        try {
            userService.register(username.trim(), password);
        } catch (UserException e) {
            return "redirect:/login?error=" + enc(e.getMessage()) + "&tab=register";
        }
        return "redirect:/login?success=" + enc("Registrácia úspešná. Prihláste sa.") + "&tab=login";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    // ===================== HELPERS =====================
    private Field newField(String difficulty) {
        return switch (difficulty) {
            case "easy" -> new Field(6, 6, 20);
            case "hard" -> new Field(12, 12, 30);
            default     -> new Field(8, 8, 25);
        };
    }

    private String todayDailyGameKey() {
        return "flood-daily-" + LocalDate.now(ZoneId.of("Europe/Bratislava"));
    }

    private Field newDailyField() {
        long seed = LocalDate.now(ZoneId.of("Europe/Bratislava")).toEpochDay();
        int rows = 12, cols = 12;
        // Extract board as flat int arrays for fast cloning during lookahead.
        Field template = new Field(rows, cols, 500, seed);
        int n = rows * cols;
        int[] initColors  = new int[n];
        boolean[] initFlooded = new boolean[n];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int i = r * cols + c;
                initColors[i]  = template.getTile(r, c).getColor().ordinal();
                initFlooded[i] = template.getTile(r, c).getState() == TileState.FLOODED;
            }
        }
        int moves = dailySolve(initColors, initFlooded, rows, cols);
        return new Field(rows, cols, moves, seed);
    }

    // 2-step lookahead greedy on flat arrays. At each step it evaluates all
    // 5×5 = 25 two-move sequences and picks the first move of the best one,
    // giving a path significantly shorter than a 1-step greedy.
    private int dailySolve(int[] initColors, boolean[] initFlooded, int rows, int cols) {
        int n = rows * cols;
        int[] colors  = initColors.clone();
        boolean[] flooded = initFlooded.clone();
        int moves = 0;
        while (!arrSolved(flooded, n) && moves < 400) {
            int best = dailyBestColor(colors, flooded, rows, cols, n);
            if (best < 0) break;
            arrFlood(colors, flooded, best, rows, cols);
            moves++;
        }
        return moves;
    }

    private int dailyBestColor(int[] colors, boolean[] flooded, int rows, int cols, int n) {
        int cur = colors[0];
        int bestFirst = -1, bestScore = -1;
        for (int c1 = 0; c1 < 6; c1++) {
            if (c1 == cur) continue;
            int[] col1 = colors.clone();
            boolean[] fl1 = flooded.clone();
            arrFlood(col1, fl1, c1, rows, cols);
            if (arrSolved(fl1, n)) return c1;
            // Score = best 1-step gain achievable from the c1 state
            int cur1 = col1[0];
            int score = arrCountFlooded(fl1, n);
            for (int c2 = 0; c2 < 6; c2++) {
                if (c2 == cur1) continue;
                int[] col2 = col1.clone();
                boolean[] fl2 = fl1.clone();
                arrFlood(col2, fl2, c2, rows, cols);
                int s = arrCountFlooded(fl2, n);
                if (s > score) score = s;
            }
            if (score > bestScore) { bestScore = score; bestFirst = c1; }
        }
        return bestFirst;
    }

    private void arrFlood(int[] colors, boolean[] flooded, int newColor, int rows, int cols) {
        int n = rows * cols;
        for (int i = 0; i < n; i++) if (flooded[i]) colors[i] = newColor;
        Queue<Integer> q = new LinkedList<>();
        for (int i = 0; i < n; i++) if (flooded[i]) q.add(i);
        while (!q.isEmpty()) {
            int pos = q.poll();
            int r = pos / cols, c = pos % cols;
            int[] nr = {r-1, r+1, r,   r  };
            int[] nc = {c,   c,   c-1, c+1};
            for (int k = 0; k < 4; k++) {
                if (nr[k]<0||nr[k]>=rows||nc[k]<0||nc[k]>=cols) continue;
                int ni = nr[k]*cols+nc[k];
                if (!flooded[ni] && colors[ni] == newColor) {
                    flooded[ni] = true;
                    q.add(ni);
                }
            }
        }
    }

    private boolean arrSolved(boolean[] flooded, int n) {
        for (int i = 0; i < n; i++) if (!flooded[i]) return false;
        return true;
    }

    private int arrCountFlooded(boolean[] flooded, int n) {
        int c = 0;
        for (int i = 0; i < n; i++) if (flooded[i]) c++;
        return c;
    }

    // Used for in-game hints (Field-based BFS gain).
    private TileColor bestGainColor(Field sim) {
        TileColor current = sim.getTile(0, 0).getColor();
        TileColor best = null;
        int bestGain = -1;
        for (TileColor color : TileColor.values()) {
            if (color == current) continue;
            int gain = computeFloodGain(sim, color);
            if (gain > bestGain) { bestGain = gain; best = color; }
        }
        return best;
    }

    // BFS from flooded border: count connected component of `color` that would
    // be absorbed — NOT just the number of bordering edges.
    private int computeFloodGain(Field sim, TileColor color) {
        int rows = sim.getRows(), cols = sim.getCols();
        boolean[][] seen = new boolean[rows][cols];
        Queue<int[]> q = new LinkedList<>();
        int[][] dirs = {{-1,0},{1,0},{0,-1},{0,1}};
        int count = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (sim.getTile(r, c).getState() == TileState.FLOODED) {
                    for (int[] d : dirs) {
                        int nr = r+d[0], nc = c+d[1];
                        if (nr>=0 && nr<rows && nc>=0 && nc<cols
                                && !seen[nr][nc]
                                && sim.getTile(nr, nc).getState() == TileState.NOT_FLOODED
                                && sim.getTile(nr, nc).getColor() == color) {
                            seen[nr][nc] = true;
                            q.add(new int[]{nr, nc});
                            count++;
                        }
                    }
                }
            }
        }
        while (!q.isEmpty()) {
            int[] p = q.poll();
            for (int[] d : dirs) {
                int nr = p[0]+d[0], nc = p[1]+d[1];
                if (nr>=0 && nr<rows && nc>=0 && nc<cols
                        && !seen[nr][nc]
                        && sim.getTile(nr, nc).getState() == TileState.NOT_FLOODED
                        && sim.getTile(nr, nc).getColor() == color) {
                    seen[nr][nc] = true;
                    q.add(new int[]{nr, nc});
                    count++;
                }
            }
        }
        return count;
    }

    private int getInt(HttpSession session, String key) {
        Object val = session.getAttribute(key);
        return val instanceof Integer i ? i : 0;
    }

    private static String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
