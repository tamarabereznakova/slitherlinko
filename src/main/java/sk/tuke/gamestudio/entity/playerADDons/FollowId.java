package sk.tuke.gamestudio.entity.playerADDons;

import java.io.Serializable;
import java.util.Objects;

public class FollowId implements Serializable {
    private String followerName;
    private String followingName;

    public FollowId() {
    }

    public FollowId(String followerName, String followingName) {
        this.followerName = followerName;
        this.followingName = followingName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FollowId)) return false;
        final FollowId that = (FollowId) o;
        return Objects.equals(followerName, that.followerName)
                && Objects.equals(followingName, that.followingName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(followerName, followingName);
    }
}