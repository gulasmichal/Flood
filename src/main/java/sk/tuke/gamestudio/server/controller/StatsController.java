package sk.tuke.gamestudio.server.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import sk.tuke.gamestudio.entity.Score;
import sk.tuke.gamestudio.service.ScoreService;

import java.util.Comparator;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Stream;

@Controller
public class StatsController {

    @Autowired
    private ScoreService scoreService;

    @GetMapping("/stats")
    public String stats(HttpSession session, Model model) {
        String player = (String) session.getAttribute("player");
        if (player == null) return "redirect:/login";

        List<Score> all;
        try {
            all = scoreService.getScoresByPlayer("flood", player);
        } catch (Exception e) {
            all = List.of();
        }

        List<Score> easy   = all.stream().filter(s -> "flood-easy".equals(s.getGame())).toList();
        List<Score> medium = all.stream().filter(s -> "flood-medium".equals(s.getGame())).toList();
        List<Score> hard   = all.stream().filter(s -> "flood-hard".equals(s.getGame())).toList();

        DifficultyStats easyStats   = buildStats(easy,   "Jednoduchá", 20);
        DifficultyStats mediumStats = buildStats(medium,  "Stredná",    25);
        DifficultyStats hardStats   = buildStats(hard,    "Ťažká",      30);

        int totalPlayed = easyStats.getPlayed() + mediumStats.getPlayed() + hardStats.getPlayed();
        int totalBestScore = Math.max(Math.max(easyStats.getBestScore(), mediumStats.getBestScore()), hardStats.getBestScore());
        Integer totalBestTime = Stream.of(easyStats.getBestTime(), mediumStats.getBestTime(), hardStats.getBestTime())
                .filter(t -> t != null)
                .min(Integer::compareTo)
                .orElse(null);
        int totalWinRatePct = totalPlayed > 0 ? 100 : 0;

        List<Score> regularAll = Stream.of(easy, medium, hard).flatMap(List::stream).toList();
        double totalAvgMoves = regularAll.isEmpty() ? 0 :
                regularAll.stream().mapToDouble(s -> maxMovesForGame(s.getGame()) - s.getPoints()).average().orElse(0);

        List<Score> recentScores = regularAll.stream()
                .sorted(Comparator.comparing(Score::getPlayedOn).reversed())
                .limit(20)
                .toList();

        model.addAttribute("player", player);
        model.addAttribute("difficultyRows", List.of(easyStats, mediumStats, hardStats));
        model.addAttribute("totalPlayed", totalPlayed);
        model.addAttribute("totalBestScore", totalBestScore);
        model.addAttribute("totalBestTime", totalBestTime);
        model.addAttribute("totalWinRatePct", totalWinRatePct);
        model.addAttribute("totalAvgMoves", totalAvgMoves);
        model.addAttribute("recentScores", recentScores);

        return "stats";
    }

    private DifficultyStats buildStats(List<Score> scores, String label, int maxMoves) {
        if (scores.isEmpty()) return new DifficultyStats(label, 0, 0, 0, 0, 0, null);
        int played = scores.size();
        int bestScore = scores.stream().mapToInt(Score::getPoints).max().orElse(0);
        double avgMoves = maxMoves - scores.stream().mapToInt(Score::getPoints).average().orElse(0);
        OptionalInt best = scores.stream().filter(s -> s.getDuration() != null)
                .mapToInt(Score::getDuration).min();
        Integer bestTime = best.isPresent() ? best.getAsInt() : null;
        return new DifficultyStats(label, played, played, 100, bestScore, avgMoves, bestTime);
    }

    private int maxMovesForGame(String game) {
        if ("flood-easy".equals(game)) return 20;
        if ("flood-hard".equals(game)) return 30;
        return 25;
    }
}
