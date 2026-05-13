package sk.tuke.gamestudio.entity.playerADDons;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "notification")
@NamedQuery(name = "Notification.findByRecipient", query = "SELECT n FROM Notification n WHERE n.recipient = :recipient ORDER BY n.createdOn DESC")
@NamedQuery(name = "Notification.findUnseenByRecipient", query = "SELECT n FROM Notification n WHERE n.recipient = :recipient AND n.seen = false ORDER BY n.createdOn DESC")
@NamedQuery(name = "Notification.markAllSeen", query = "UPDATE Notification n SET n.seen = true WHERE n.recipient = :recipient")
@NamedQuery(name = "Notification.deleteByPlayer", query = "DELETE FROM Notification n WHERE n.recipient = :player OR n.actor = :player")

public class Notification implements Serializable {

    @Id
    @GeneratedValue
    private int ident;

    private String recipient;
    private String actor;
    private String type;
    @Column(length = 200)
    private String payload;
    private Date createdOn;
    private boolean seen;

    public Notification() {
    }

    public Notification(String recipient, String actor, String type, String payload) {
        this.recipient = recipient;
        this.actor = actor;
        this.type = type;
        this.payload = payload;
        this.createdOn = new Date();
        this.seen = false;
    }

    public int getIdent() {
        return ident;
    }
    public void setIdent(int ident) {
        this.ident = ident;
    }

    public String getRecipient() {
        return recipient;
    }
    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getActor() {
        return actor;
    }
    public void setActor(String actor) {
        this.actor = actor;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public String getPayload() {
        return payload;
    }
    public void setPayload(String payload) {
        this.payload = payload;
    }

    public Date getCreatedOn() {
        return createdOn;
    }
    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public boolean isSeen() {
        return seen;
    }
    public void setSeen(boolean seen) {
        this.seen = seen;
    }
}