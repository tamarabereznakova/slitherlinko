package sk.tuke.gamestudio.entity.playerADDons.multiplayer;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "lobby_member")
@IdClass(LobbyMemberId.class)
@NamedQuery(name = "LobbyMember.findByLobby", query = "SELECT m FROM LobbyMember m WHERE m.lobbyToken = :token")
@NamedQuery(name = "LobbyMember.findByPlayer", query = "SELECT m FROM LobbyMember m WHERE m.username = :username")
@NamedQuery(name = "LobbyMember.deleteByLobby", query = "DELETE FROM LobbyMember m WHERE m.lobbyToken = :token")
@NamedQuery(name = "LobbyMember.deleteByPlayer", query = "DELETE FROM LobbyMember m WHERE m.username = :username")

public class LobbyMember implements Serializable {

    @Id
    private String lobbyToken;

    @Id
    private String username;

    private String status;
    private int progress;
    private boolean finished;
    private Date joinedOn;

    public LobbyMember() {
    }

    public LobbyMember(String lobbyToken, String username, String status) {
        this.lobbyToken = lobbyToken;
        this.username = username;
        this.status = status;
        this.progress = 0;
        this.finished = false;
        this.joinedOn = new Date();
    }

    public String getLobbyToken() {
        return lobbyToken;
    }
    public void setLobbyToken(String lobbyToken) {
        this.lobbyToken = lobbyToken;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public int getProgress() {
        return progress;
    }
    public void setProgress(int progress) {
        this.progress = progress;
    }

    public boolean isFinished() {
        return finished;
    }
    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public Date getJoinedOn() {
        return joinedOn;
    }
    public void setJoinedOn(Date joinedOn) {
        this.joinedOn = joinedOn;
    }
}