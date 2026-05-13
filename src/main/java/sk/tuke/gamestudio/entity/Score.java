package sk.tuke.gamestudio.entity;

import java.util.Date;
import javax.persistence.*;
import java.io.Serializable;

@Entity
@NamedQuery(name = "Score.getTopScores",
        query = "SELECT s FROM Score s WHERE s.game=:game ORDER BY s.duration ASC, s.moves ASC, s.playedOn DESC")
@NamedQuery(name = "Score.getTopScoresByLevel",
        query = "SELECT s FROM Score s WHERE s.game=:game AND s.level=:level ORDER BY s.duration ASC, s.moves ASC, s.playedOn DESC")
@NamedQuery(name = "Score.reset",
        query = "DELETE FROM Score")
@NamedQuery(name = "Score.getScoresByPlayer",
        query = "SELECT s FROM Score s WHERE s.game=:game AND s.player=:player AND s.level=:level ORDER BY s.playedOn DESC")
@NamedQuery(name = "Score.deleteByIdent",
        query = "DELETE FROM Score s WHERE s.ident = :ident AND s.player = :player")

public class Score implements Serializable {
    @Id
    @GeneratedValue
    private int ident;

    private String game;
    private String player;
    private int moves;
    private Date playedOn;
    private int duration;
    private int level;
    private int mapIndex;

    public Score() {} //bezparam konstruktor pre jpa

    public Score(String game, String player, int moves, Date playedOn, int duration, int level, int mapIndex) {
        this.game = game;
        this.player = player;
        this.moves = moves;
        this.playedOn = playedOn;
        this.duration = duration;
        this.level = level;
        this.mapIndex = mapIndex;
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

    public int getMoves() {
        return moves;
    }
    public void setMoves(int moves) {
        this.moves = moves;
    }

    public Date getPlayedOn() {
        return playedOn;
    }
    public void setPlayedOn(Date playedOn) {
        this.playedOn = playedOn;
    }

    public int getDuration() {
        return duration;
    }
    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getIdent() {
        return ident;
    }
    public void setIdent(int ident) {
        this.ident = ident;
    }

    public int getLevel() {
        return level;
    }
    public void setLevel(int level) {
        this.level = level;
    }

    public int getMapIndex() { return mapIndex; }

    public void setMapIndex(int mapIndex) { this.mapIndex = mapIndex; }

    @Override
    public String toString() {
        return "Score{" +
                "game='" + game + '\'' +
                ", player='" + player + '\'' +
                ", points=" + moves +
                ", playedOn=" + playedOn +
                ", duration=" + duration +
                '}';
    }
}
