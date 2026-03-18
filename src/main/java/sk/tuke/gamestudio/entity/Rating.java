package sk.tuke.gamestudio.entity;

import java.util.Date;

public class Rating {
    private String game;
    private String player;
    private int stars;
    private Date ratedOn;

    public Rating(String game, String player, int stars, Date ratedOn) {
        this.game = game;
        this.player = player;
        this.stars = stars;
        this.ratedOn = ratedOn;
    }

    public String getGame() { return game; }
    public void setGame(String game) { this.game = game; }

    public String getPlayer() { return player; }
    public void setPlayer(String player) { this.player = player; }

    public int getStars() { return stars; }
    public void setStars(int stars) { this.stars = stars; }

    public Date getRatedOn() { return ratedOn; }
    public void setRatedOn(Date ratedOn) { this.ratedOn = ratedOn; }

    @Override
    public String toString() {
        return "Rating{game='" + game + "', player='" + player + "', stars=" + stars + ", ratedOn=" + ratedOn + '}';
    }
}
