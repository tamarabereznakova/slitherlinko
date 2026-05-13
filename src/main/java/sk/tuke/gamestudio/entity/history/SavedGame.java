package sk.tuke.gamestudio.entity.history;

import javax.persistence.*;
import java.util.Date;
import java.io.Serializable;

@Entity
@Table(name = "saved_game")
@IdClass(SavedGameId.class)
public class SavedGame implements Serializable {

    @Id
    private String player;

    @Id
    private int level;

    @Column(length = 10000)
    private String edges;

    @Column(length = 10000)
    private String undoBuffer;

    @Column(length = 10000)
    private String redoBuffer;

    private int moves;
    private int elapsed;
    private Date savedOn;
    private int mapIndex;

    public SavedGame() {
    }

    public SavedGame(String player, int level, String edges, int moves,
                     int elapsed, String undoBuffer, String redoBuffer, int mapIndex) {
        this.player = player;
        this.level = level;
        this.edges = edges;
        this.moves = moves;
        this.elapsed = elapsed;
        this.undoBuffer = undoBuffer;
        this.redoBuffer = redoBuffer;
        this.mapIndex = mapIndex;
        this.savedOn = new Date();
    }

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getEdges() {
        return edges;
    }

    public void setEdges(String edges) {
        this.edges = edges;
    }

    public int getMoves() {
        return moves;
    }

    public void setMoves(int moves) {
        this.moves = moves;
    }

    public int getElapsed() {
        return elapsed;
    }

    public void setElapsed(int elapsed) {
        this.elapsed = elapsed;
    }

    public Date getSavedOn() {
        return savedOn;
    }

    public void setSavedOn(Date savedOn) {
        this.savedOn = savedOn;
    }

    public String getUndoBuffer() {
        return undoBuffer;
    }

    public void setUndoBuffer(String undoBuffer) {
        this.undoBuffer = undoBuffer;
    }

    public String getRedoBuffer() {
        return redoBuffer;
    }

    public void setRedoBuffer(String redoBuffer) {
        this.redoBuffer = redoBuffer;
    }

    public int getMapIndex() {
        return mapIndex;
    }

    public void setMapIndex(int mapIndex) {
        this.mapIndex = mapIndex;
    }
}