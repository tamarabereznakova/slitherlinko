package sk.tuke.gamestudio.entity.playerADDons.multiplayer;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "multiplayer_score")
@NamedQuery(name = "MultiplayerScore.findByPlayer", query = "SELECT m FROM MultiplayerScore m WHERE m.players LIKE :pattern ORDER BY m.playedOn DESC")
@NamedQuery(name = "MultiplayerScore.deleteByPlayer", query = "DELETE FROM MultiplayerScore m WHERE m.players LIKE :pattern")
public class MultiplayerScore implements Serializable {

    @Id
    @GeneratedValue
    private int ident;

    private String mode;
    private int level;
    private int mapIndex;
    @Column(length = 500)
    private String players;
    private int moves;
    private int duration;
    private Date playedOn;
    private String winner;

    public MultiplayerScore() {
    }

    public MultiplayerScore(String mode, int level, int mapIndex, String players, int moves, int duration, String winner) {
        this.mode = mode;
        this.level = level;
        this.mapIndex = mapIndex;
        this.players = players;
        this.moves = moves;
        this.duration = duration;
        this.winner = winner;
        this.playedOn = new Date();
    }

    public int getIdent() {
        return ident;
    }
    public void setIdent(int ident) {
        this.ident = ident;
    }

    public String getMode() {
        return mode;
    }
    public void setMode(String mode) {
        this.mode = mode;
    }

    public int getLevel() {
        return level;
    }
    public void setLevel(int level) {
        this.level = level;
    }

    public int getMapIndex() {
        return mapIndex;
    }
    public void setMapIndex(int mapIndex) {
        this.mapIndex = mapIndex;
    }

    public String getPlayers() {
        return players;
    }
    public void setPlayers(String players) {
        this.players = players;
    }

    public int getMoves() {
        return moves;
    }
    public void setMoves(int moves) {
        this.moves = moves;
    }

    public int getDuration() {
        return duration;
    }
    public void setDuration(int duration) {
        this.duration = duration;
    }

    public Date getPlayedOn() {
        return playedOn;
    }
    public void setPlayedOn(Date playedOn) {
        this.playedOn = playedOn;
    }

    public String getWinner() {
        return winner;
    }
    public void setWinner(String winner) {
        this.winner = winner;
    }
}