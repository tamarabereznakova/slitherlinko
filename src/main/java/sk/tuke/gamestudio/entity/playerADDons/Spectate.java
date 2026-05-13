package sk.tuke.gamestudio.entity.playerADDons;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "spectate")
@NamedQuery(name = "SpectateInvite.findByToken", query = "SELECT s FROM Spectate s WHERE s.token = :token")
@NamedQuery(name = "SpectateInvite.findByFromPlayer", query = "SELECT s FROM Spectate s WHERE s.fromPlayer = :fromPlayer ORDER BY s.createdOn DESC")
@NamedQuery(name = "SpectateInvite.deleteByToken", query = "DELETE FROM Spectate s WHERE s.token = :token")
@NamedQuery(name = "SpectateInvite.deleteByPlayer", query = "DELETE FROM Spectate s WHERE s.fromPlayer = :fromPlayer")
public class Spectate implements Serializable {

    @Id
    private String token;

    private String fromPlayer;
    private String toPlayer;
    private int level;
    private int mapIndex;
    private Date createdOn;

    public Spectate() {
    }

    public Spectate(String token, String fromPlayer, String toPlayer, int level, int mapIndex) {
        this.token = token;
        this.fromPlayer = fromPlayer;
        this.toPlayer = toPlayer;
        this.level = level;
        this.mapIndex = mapIndex;
        this.createdOn = new Date();
    }

    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }

    public String getFromPlayer() {
        return fromPlayer;
    }
    public void setFromPlayer(String fromPlayer) {
        this.fromPlayer = fromPlayer;
    }

    public String getToPlayer() {
        return toPlayer;
    }
    public void setToPlayer(String toPlayer) {
        this.toPlayer = toPlayer;
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

    public Date getCreatedOn() {
        return createdOn;
    }
    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    @Override
    public String toString() {
        return "SpectateInvite{token='" + token + "', from='" + fromPlayer + "', to='" + toPlayer + "', level=" + level + "}";
    }
}