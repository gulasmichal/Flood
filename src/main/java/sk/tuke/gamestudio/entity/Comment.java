package sk.tuke.gamestudio.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Date;

@Entity
@NamedQueries({
        @NamedQuery(name = "Comment.getComments",
                query = "SELECT c FROM Comment c WHERE c.game=:game ORDER BY c.commentedOn DESC"),
        @NamedQuery(name = "Comment.resetComments",
                query = "DELETE FROM Comment c")
})
public class Comment implements Serializable {

    @Id
    @GeneratedValue
    private int ident;

    private String game;
    private String player;
    private String content;
    private Date commentedOn;

    public Comment() {}

    public Comment(String game, String player, String content, Date commentedOn) {
        this.game = game;
        this.player = player;
        this.content = content;
        this.commentedOn = commentedOn;
    }

    public int getIdent() { return ident; }
    public void setIdent(int ident) { this.ident = ident; }

    public String getGame() { return game; }
    public void setGame(String game) { this.game = game; }

    public String getPlayer() { return player; }
    public void setPlayer(String player) { this.player = player; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Date getCommentedOn() { return commentedOn; }
    public void setCommentedOn(Date commentedOn) { this.commentedOn = commentedOn; }

    @Override
    public String toString() {
        return "Comment{game='" + game + "', player='" + player + "', content='" + content + "', commentedOn=" + commentedOn + '}';
    }
}
