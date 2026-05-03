package sk.tuke.gamestudio.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Date;

@Entity
@NamedQueries({
        @NamedQuery(name = "Achievement.getByPlayer",
                query = "SELECT a FROM Achievement a WHERE a.player = :player ORDER BY a.earnedOn ASC"),
        @NamedQuery(name = "Achievement.findByPlayerAndType",
                query = "SELECT a FROM Achievement a WHERE a.player = :player AND a.type = :type"),
        @NamedQuery(name = "Achievement.reset",
                query = "DELETE FROM Achievement a")
})
public class Achievement implements Serializable {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private int ident;

    private String player;
    private String type;
    private Date earnedOn;

    public Achievement() {}

    public Achievement(String player, String type, Date earnedOn) {
        this.player = player;
        this.type = type;
        this.earnedOn = earnedOn;
    }

    public int getIdent() { return ident; }
    public void setIdent(int ident) { this.ident = ident; }

    public String getPlayer() { return player; }
    public void setPlayer(String player) { this.player = player; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Date getEarnedOn() { return earnedOn; }
    public void setEarnedOn(Date earnedOn) { this.earnedOn = earnedOn; }

    @Override
    public String toString() {
        return "Achievement{player='" + player + "', type='" + type + "', earnedOn=" + earnedOn + '}';
    }
}
