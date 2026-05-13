package sk.tuke.gamestudio.entity;

import java.util.Date;
import javax.persistence.*;
import java.io.Serializable;

@Entity
@NamedQuery(name = "Comment.getComments",
        query = "SELECT c FROM Comment c WHERE c.game=:game ORDER BY c.commentedOn DESC")
@NamedQuery(name = "Comment.reset",
        query = "DELETE FROM Comment")
@NamedQuery(name = "Comment.getCommentsByPlayer",
        query = "SELECT c FROM Comment c WHERE c.game=:game AND c.player=:player ORDER BY c.commentedOn DESC")
@NamedQuery(name = "Comment.deleteByIdent",
        query = "DELETE FROM Comment c WHERE c.ident = :ident AND c.player = :player")

public class Comment implements Serializable {
    @Id
    @GeneratedValue
    private int ident;

    private String game;
    private String player;
    private String text;
    private Date commentedOn;

    public Comment() {}

    public Comment(String game, String player, String text, Date commentedOn) {
        this.game = game;
        this.player = player;
        this.text = text;
        this.commentedOn = commentedOn;
    }

    public String getGame() {
        return game;
    }
    public void setGame(String game) {
        this.game = game;
    }

    public String getPlayer() {
        return player;
    }
    public void setPlayer(String player) {
        this.player = player;
    }

    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }

    public Date getCommentedOn() {
        return commentedOn;
    }
    public void setCommentedOn(Date commentedOn) {
        this.commentedOn = commentedOn;
    }

    public int getIdent() {
        return ident;
    }
    public void setIdent(int ident) {
        this.ident = ident;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "game='" + game + '\'' +
                ", player='" + player + '\'' +
                ", text=" + text +
                ", commentedOn=" + commentedOn +
                '}';
    }
}
