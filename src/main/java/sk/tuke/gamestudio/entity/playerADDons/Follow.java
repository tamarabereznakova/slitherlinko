package sk.tuke.gamestudio.entity.playerADDons;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "follow")
@IdClass(FollowId.class)
@NamedQuery(name = "Follow.findFollowing", query = "SELECT f FROM Follow f WHERE f.followerName = :follower ORDER BY f.followedOn DESC")
@NamedQuery(name = "Follow.findFollowers", query = "SELECT f FROM Follow f WHERE f.followingName = :following ORDER BY f.followedOn DESC")
@NamedQuery(name = "Follow.exists", query = "SELECT COUNT(f) FROM Follow f WHERE f.followerName = :follower AND f.followingName = :following")
@NamedQuery(name = "Follow.countFollowers", query = "SELECT COUNT(f) FROM Follow f WHERE f.followingName = :following")
@NamedQuery(name = "Follow.countFollowing", query = "SELECT COUNT(f) FROM Follow f WHERE f.followerName = :follower")
@NamedQuery(name = "Follow.deleteByPlayer", query = "DELETE FROM Follow f WHERE f.followerName = :player OR f.followingName = :player")
public class Follow implements Serializable {

    @Id
    private String followerName;

    @Id
    private String followingName;

    private Date followedOn;

    public Follow() {
    }

    public Follow(String followerName, String followingName) {
        this.followerName = followerName;
        this.followingName = followingName;
        this.followedOn = new Date();
    }

    public String getFollowerName() {
        return followerName;
    }
    public void setFollowerName(String followerName) {
        this.followerName = followerName;
    }

    public String getFollowingName() {
        return followingName;
    }
    public void setFollowingName(String followingName) {
        this.followingName = followingName;
    }

    public Date getFollowedOn() {
        return followedOn;
    }
    public void setFollowedOn(Date followedOn) {
        this.followedOn = followedOn;
    }
}