package sk.tuke.gamestudio.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sk.tuke.gamestudio.entity.Score;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ScoreServiceJDBCTest {

    private ScoreService service;

    @BeforeEach
    void setUp() {
        service = new ScoreServiceJDBC();
        service.reset();
    }

    @Test
    void topScoresAreEmptyAfterReset() {
        List<Score> scores = service.getTopScores("flood");
        assertTrue(scores.isEmpty());
    }

    @Test
    void addedScoreAppearsInTopScores() {
        service.addScore(new Score("flood", "Alice", 20, new Date()));
        List<Score> scores = service.getTopScores("flood");
        assertEquals(1, scores.size());
        assertEquals("Alice", scores.get(0).getPlayer());
        assertEquals(20, scores.get(0).getPoints());
    }

    @Test
    void topScoresAreOrderedByPointsDescending() {
        service.addScore(new Score("flood", "Alice", 10, new Date()));
        service.addScore(new Score("flood", "Bob", 25, new Date()));
        service.addScore(new Score("flood", "Carol", 15, new Date()));
        List<Score> scores = service.getTopScores("flood");
        assertEquals("Bob", scores.get(0).getPlayer());
        assertEquals("Carol", scores.get(1).getPlayer());
        assertEquals("Alice", scores.get(2).getPlayer());
    }

    @Test
    void topScoresLimitedToTen() {
        for (int i = 1; i <= 15; i++) {
            service.addScore(new Score("flood", "Player" + i, i, new Date()));
        }
        List<Score> scores = service.getTopScores("flood");
        assertEquals(10, scores.size());
    }

    @Test
    void scoresFilteredByGame() {
        service.addScore(new Score("flood", "Alice", 10, new Date()));
        service.addScore(new Score("minesweeper", "Bob", 50, new Date()));
        List<Score> scores = service.getTopScores("flood");
        assertEquals(1, scores.size());
        assertEquals("Alice", scores.get(0).getPlayer());
    }

    @Test
    void resetClearsAllScores() {
        service.addScore(new Score("flood", "Alice", 10, new Date()));
        service.reset();
        assertTrue(service.getTopScores("flood").isEmpty());
    }

    @Test
    void scoreHasCorrectGame() {
        service.addScore(new Score("flood", "Alice", 10, new Date()));
        Score s = service.getTopScores("flood").get(0);
        assertEquals("flood", s.getGame());
    }

    @Test
    void scoreHasPlayedOnDate() {
        service.addScore(new Score("flood", "Alice", 10, new Date()));
        Score s = service.getTopScores("flood").get(0);
        assertNotNull(s.getPlayedOn());
    }
}
