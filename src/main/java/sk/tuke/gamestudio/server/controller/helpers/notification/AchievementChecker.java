package sk.tuke.gamestudio.server.controller.helpers.notification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sk.tuke.gamestudio.entity.Comment;
import sk.tuke.gamestudio.entity.Score;
import sk.tuke.gamestudio.entity.playerADDons.Follow;
import sk.tuke.gamestudio.entity.playerADDons.multiplayer.MultiplayerScore;
import sk.tuke.gamestudio.entity.playerADDons.Notification;
import sk.tuke.gamestudio.service.entity.CommentService;
import sk.tuke.gamestudio.service.entity.ScoreService;
import sk.tuke.gamestudio.service.entity.playerADDons.FollowService;
import sk.tuke.gamestudio.service.entity.playerADDons.MultiplayerScoreService;
import sk.tuke.gamestudio.service.entity.playerADDons.NotificationService;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AchievementChecker {

    @Autowired
    private NotificationService notificationService;
    @Autowired
    private ScoreService scoreService;
    @Autowired
    private MultiplayerScoreService multiplayerScoreService;
    @Autowired
    private FollowService followService;
    @Autowired
    private CommentService commentService;


    public void check(String username) {
        if (username == null) return;

        final List<Score> allSoloScores = scoreService.getTopScores("slitherlink").stream().filter(s -> username.equals(s.getPlayer())).collect(Collectors.toList());
        final List<MultiplayerScore> mpForCalc = multiplayerScoreService.getScoresForPlayer(username);
        final List<Follow> followingList = followService.getFollowing(username);
        final List<Follow> followersList = followService.getFollowers(username);
        final List<Comment> allComments = commentService.getComments("slitherlink");
        final List<Comment> commentsByUser = allComments.stream().filter(c -> username.equals(c.getPlayer())).collect(Collectors.toList());
        final List<AchievementCalculator.Achievement> achievements = AchievementCalculator.calculate(username, allSoloScores, mpForCalc, followingList, followersList, commentsByUser);

        synchronized (("achievement_" + username).intern()) {
            final Set<String> alreadyNotified = new HashSet<>();
            for (final Notification n : notificationService.getAll(username)) {
                if ("ACHIEVEMENT".equals(n.getType()) && n.getPayload() != null) {
                    alreadyNotified.add(n.getPayload());
                }
            }

            final List<AchievementCalculator.Achievement> newOnes =
                    AchievementCalculator.findNew(achievements, alreadyNotified);

            for (final AchievementCalculator.Achievement a : newOnes) {
                final Notification n = new Notification();
                n.setRecipient(username);
                n.setActor(username);
                n.setType("ACHIEVEMENT");
                n.setPayload(a.code);
                n.setCreatedOn(new Date());
                n.setSeen(false);
                notificationService.create(n);
            }
        }
    }
}