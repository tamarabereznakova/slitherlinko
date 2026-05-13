package sk.tuke.gamestudio.service.entity.playerADDons;

import sk.tuke.gamestudio.entity.playerADDons.Follow;
import java.util.List;

public interface FollowService {
    void follow(String follower, String following);
    void unfollow(String follower, String following);
    boolean isFollowing(String follower, String following);
    List<Follow> getFollowing(String follower);
    List<Follow> getFollowers(String following);
    long countFollowing(String follower);
    long countFollowers(String following);
    void deleteByPlayer(String player);
}