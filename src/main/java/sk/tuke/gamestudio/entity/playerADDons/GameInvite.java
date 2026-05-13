package sk.tuke.gamestudio.entity.playerADDons;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "game_invite")
@NamedQuery(name = "GameInvite.findByToPlayer", query = "SELECT g FROM GameInvite g WHERE g.toPlayer = :toPlayer AND g.deletedByTo = false ORDER BY g.sentOn DESC")
@NamedQuery(name = "GameInvite.findByFromPlayer", query = "SELECT g FROM GameInvite g WHERE g.fromPlayer = :fromPlayer AND g.deletedByFrom = false ORDER BY g.sentOn DESC")
@NamedQuery(name = "GameInvite.findByIdent", query = "SELECT g FROM GameInvite g WHERE g.ident = :ident")
@NamedQuery(name = "GameInvite.deleteByIdent", query = "DELETE FROM GameInvite g WHERE g.ident = :ident")
@NamedQuery(name = "GameInvite.deleteByPlayer", query = "DELETE FROM GameInvite g WHERE g.fromPlayer = :player OR g.toPlayer = :player")
@NamedQuery(name = "GameInvite.countUnread", query = "SELECT COUNT(g) FROM GameInvite g WHERE g.toPlayer = :toPlayer AND g.read = false AND g.deletedByTo = false")

public class GameInvite implements Serializable {

    @Id
    @GeneratedValue
    private int ident;

    private String fromPlayer;
    private String toPlayer;
    private String token;
    private Date sentOn;
    private boolean delivered = false;
    private boolean read;
    private boolean deletedByFrom = false;
    private boolean deletedByTo = false;
    private String inviteType;

    public GameInvite() {
    }

    public GameInvite(String fromPlayer, String toPlayer, String token, String inviteType) {
        this.fromPlayer = fromPlayer;
        this.toPlayer = toPlayer;
        this.token = token;
        this.sentOn = new Date();
        this.read = false;
        this.deletedByFrom = false;
        this.deletedByTo = false;
        this.inviteType = inviteType;
        this.delivered = false;
    }

    public int getIdent() {
        return ident;
    }
    public void setIdent(int ident) {
        this.ident = ident;
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

    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }

    public Date getSentOn() {
        return sentOn;
    }
    public void setSentOn(Date sentOn) {
        this.sentOn = sentOn;
    }

    public boolean isDelivered() {
        return delivered;
    }
    public void setDelivered(boolean delivered) {
        this.delivered = delivered;
    }

    public boolean isRead() {
        return read;
    }
    public void setRead(boolean read) {
        this.read = read;
    }

    public boolean isDeletedByFrom() {
        return deletedByFrom;
    }
    public void setDeletedByFrom(boolean deletedByFrom) {
        this.deletedByFrom = deletedByFrom;
    }

    public boolean isDeletedByTo() {
        return deletedByTo;
    }
    public void setDeletedByTo(boolean deletedByTo) {
        this.deletedByTo = deletedByTo;
    }

    public String getInviteType() {
        return inviteType;
    }
    public void setInviteType(String inviteType) {
        this.inviteType = inviteType;
    }

    @Override
    public String toString() {
        return "GameInvite{ident=" + ident + ", from='" + fromPlayer + "', to='" + toPlayer + "', token='" + token + "', read=" + read + "}";
    }
}