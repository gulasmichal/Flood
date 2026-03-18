package sk.tuke.gamestudio.entity;

import java.util.Date;

public class Comment {
    private String game;
    private String player;
    private String content;
    private Date commentedOn;

    public Comment(String game, String player, String content, Date commentedOn) {
        this.game = game;
        this.player = player;
        this.content = content;
        this.commentedOn = commentedOn;
    }

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
