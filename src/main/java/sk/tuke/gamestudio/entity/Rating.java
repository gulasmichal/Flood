package sk.tuke.gamestudio.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Date;

@Entity
@NamedQueries({
        @NamedQuery(name = "Rating.getAverageRating",
                query = "SELECT AVG(r.stars) FROM Rating r WHERE r.game=:game"),
        @NamedQuery(name = "Rating.getRating",
                query = "SELECT r FROM Rating r WHERE r.game=:game AND r.player=:player"),
        @NamedQuery(name = "Rating.deleteByGameAndPlayer",
                query = "DELETE FROM Rating r WHERE r.game=:game AND r.player=:player"),
        @NamedQuery(name = "Rating.resetRatings",
                query = "DELETE FROM Rating r")
})
public class Rating implements Serializable {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private int ident;

    private String game;
    private String player;
    private int stars;
    private Date ratedOn;

    public Rating() {}

    public Rating(String game, String player, int stars, Date ratedOn) {
        this.game = game;
        this.player = player;
        this.stars = stars;
        this.ratedOn = ratedOn;
    }

    public int getIdent() { return ident; }
    public void setIdent(int ident) { this.ident = ident; }

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
