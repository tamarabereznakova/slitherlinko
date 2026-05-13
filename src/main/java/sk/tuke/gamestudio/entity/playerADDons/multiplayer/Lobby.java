package sk.tuke.gamestudio.entity.playerADDons.multiplayer;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "lobby")
@NamedQuery(name = "Lobby.findByToken",
        query = "SELECT l FROM Lobby l WHERE l.token = :token")
@NamedQuery(name = "Lobby.findByCreator",
        query = "SELECT l FROM Lobby l WHERE l.creator = :creator ORDER BY l.createdOn DESC")
@NamedQuery(name = "Lobby.deleteByToken",
        query = "DELETE FROM Lobby l WHERE l.token = :token")
@NamedQuery(name = "Lobby.deleteByCreator",
        query = "DELETE FROM Lobby l WHERE l.creator = :creator")
public class Lobby implements Serializable {

    @Id
    private String token;
    private String creator;
    private String mode;
    private int level;
    private int mapIndex;
    private String state;
    private Date createdOn;

    public Lobby() {
    }

    public Lobby(String token, String creator, String mode, int level, int mapIndex) {
        this.token = token;
        this.creator = creator;
        this.mode = mode;
        this.level = level;
        this.mapIndex = mapIndex;
        this.state = "WAITING";
        this.createdOn = new Date();
    }

    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }

    public String getCreator() {
        return creator;
    }
    public void setCreator(String creator) {
        this.creator = creator;
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

    public String getState() {
        return state;
    }
    public void setState(String state) {
        this.state = state;
    }

    public Date getCreatedOn() {
        return createdOn;
    }
    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }
}