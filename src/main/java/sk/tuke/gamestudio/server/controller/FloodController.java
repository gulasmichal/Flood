package sk.tuke.gamestudio.server.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import sk.tuke.gamestudio.entity.Comment;
import sk.tuke.gamestudio.entity.Rating;
import sk.tuke.gamestudio.entity.Score;
import sk.tuke.gamestudio.game.flood.core.Field;
import sk.tuke.gamestudio.game.flood.core.GameState;
import sk.tuke.gamestudio.game.flood.core.TileColor;
import sk.tuke.gamestudio.game.flood.core.TileState;
import sk.tuke.gamestudio.service.*;

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

    // ===================== GAME PAGE =====================
    @GetMapping("/")
    public String game(HttpSession session, Model model,
                       @RequestParam(required = false) String ld) {
        String player = (String) session.getAttribute("player");
        if (player == null) return "redirect:/login";

        Field field = (Field) session.getAttribute("field");
        String difficulty = (String) session.getAttribute("difficulty");
        if (field == null) {
            field = newField("medium");
            difficulty = "medium";
            session.setAttribute("field", field);
            session.setAttribute("difficulty", difficulty);
            session.setAttribute("gameEndRecorded", false);
            session.setAttribute("gameStartMs", null);   // timer not yet started
            session.setAttribute("gameDurationSeconds", 0);
            session.setAttribute("moveHistory", new ArrayList<String>());
        }

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
                    try {
                        scoreService.addScore(new Score(GAME_NAME + "-" + difficulty, player, points, durationSecs, new Date()));
                    } catch (Exception ignored) {}
                }

                session.setAttribute("gamesPlayed", gamesPlayed);
                session.setAttribute("gamesWon", gamesWon);
                session.setAttribute("bestScore", bestScore);
                session.setAttribute("gameEndRecorded", true);
            }
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
        TileColor hint   = showHint ? field.getBestHintColor() : null;

        Long gameStartMs = (Long) session.getAttribute("gameStartMs");
        boolean timerStarted = gameStartMs != null;

        String difficultyLabel = switch (difficulty) {
            case "easy" -> "Jednoduchá";
            case "hard" -> "Ťažká";
            default     -> "Stredná";
        };

        model.addAttribute("field", field);
        model.addAttribute("board", board);
        model.addAttribute("boardJson", boardJson.toString());
        model.addAttribute("floodedJson", floodedJson.toString());
        model.addAttribute("player", player);
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

        // Leaderboard difficulty can be overridden by ?ld= param (independent of game)
        String scoresDifficulty = (ld != null && List.of("easy","medium","hard").contains(ld)) ? ld : difficulty;
        String scoresDifficultyLabel = switch (scoresDifficulty) {
            case "easy" -> "Jednoduchá";
            case "hard" -> "Ťažká";
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
        try { model.addAttribute("myRating", ratingService.getRating(GAME_NAME, player)); }
        catch (Exception e) { model.addAttribute("myRating", 0); }

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
            return "redirect:/login?error=Meno+nesmie+byť+prázdne&tab=register";
        }
        if (password.length() < 3) {
            return "redirect:/login?error=Heslo+musí+mať+aspoň+3+znaky&tab=register";
        }
        if (!password.equals(password2)) {
            return "redirect:/login?error=Heslá+sa+nezhodujú&tab=register";
        }
        try {
            userService.register(username.trim(), password);
        } catch (UserException e) {
            return "redirect:/login?error=" + e.getMessage().replace(" ", "+") + "&tab=register";
        }
        return "redirect:/login?success=Registrácia+úspešná.+Prihláste+sa.&tab=login";
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

    private int getInt(HttpSession session, String key) {
        Object val = session.getAttribute(key);
        return val instanceof Integer i ? i : 0;
    }
}
