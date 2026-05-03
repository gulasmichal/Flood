package sk.tuke.gamestudio.server.controller;

public class DifficultyStats {
    private String label;
    private int played;
    private int wins;
    private int winRatePct;
    private int bestScore;
    private double avgMoves;
    private Integer bestTime;

    public DifficultyStats(String label, int played, int wins, int winRatePct,
                           int bestScore, double avgMoves, Integer bestTime) {
        this.label = label;
        this.played = played;
        this.wins = wins;
        this.winRatePct = winRatePct;
        this.bestScore = bestScore;
        this.avgMoves = avgMoves;
        this.bestTime = bestTime;
    }

    public String getLabel() { return label; }
    public int getPlayed() { return played; }
    public int getWins() { return wins; }
    public int getWinRatePct() { return winRatePct; }
    public int getBestScore() { return bestScore; }
    public double getAvgMoves() { return avgMoves; }
    public Integer getBestTime() { return bestTime; }
}
