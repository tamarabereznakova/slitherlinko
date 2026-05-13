package sk.tuke.gamestudio.server.controller.helpers.slitherfriends;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.WebApplicationContext;
import sk.tuke.gamestudio.entity.Comment;
import sk.tuke.gamestudio.entity.Score;
import sk.tuke.gamestudio.entity.playerADDons.*;
import sk.tuke.gamestudio.entity.playerADDons.multiplayer.MultiplayerScore;
import sk.tuke.gamestudio.server.controller.helpers.SlitherlinkControllerHelper;
import sk.tuke.gamestudio.server.controller.helpers.SlitherlinkSession;
import sk.tuke.gamestudio.server.controller.helpers.notification.AchievementCalculator;
import sk.tuke.gamestudio.server.controller.helpers.notification.AchievementChecker;
import sk.tuke.gamestudio.service.entity.CommentService;
import sk.tuke.gamestudio.service.entity.PlayerService;
import sk.tuke.gamestudio.service.entity.RatingService;
import sk.tuke.gamestudio.service.entity.ScoreService;
import sk.tuke.gamestudio.service.entity.playerADDons.*;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@Scope(WebApplicationContext.SCOPE_SESSION)
public class SlitherFriendsController {

    @Autowired
    private SlitherlinkSession session;
    @Autowired
    private FollowService followService;
    @Autowired
    private PlayerService playerService;
    @Autowired
    private ScoreService scoreService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private RatingService ratingService;
    @Autowired
    private SpectateService spectateService;
    @Autowired
    NotificationService notificationService;
    @Autowired
    private MultiplayerScoreService multiplayerScoreService;
    @Autowired
    private PlayerSettingsService playerSettingsService;
    @Autowired
    private AchievementChecker achievementChecker;

    private static String profileGame(String username) {
        return "profile:" + username;
    }

    @GetMapping("/slitherlink/profile/{username}")
    public String userProfile(@PathVariable String username, Model model) {
        if (session.getPlayerName() == null) return "redirect:/";
        if (!playerService.exists(username)) return "redirect:/slitherlink/friends?error=notfound";

        final boolean isOwnProfile = username.equals(session.getPlayerName());
        final List<Score> userTopScores0 = filterTopScores(username, 0);
        final List<Score> userTopScores1 = filterTopScores(username, 1);
        final List<Score> userTopScores2 = filterTopScores(username, 2);
        final List<Score> userTopScores3 = filterTopScores(username, 3);
        final List<Comment> profileComments = commentService.getComments(profileGame(username));

        model.addAttribute("profileUser", username);
        model.addAttribute("isOwnProfile", isOwnProfile);
        model.addAttribute("createdOn", playerService.getCreatedOn(username));
        model.addAttribute("isFollowing", !isOwnProfile && followService.isFollowing(session.getPlayerName(), username));
        model.addAttribute("followersCount", followService.countFollowers(username));
        model.addAttribute("followingCount", followService.countFollowing(username));
        model.addAttribute("userTopScores0", userTopScores0);
        model.addAttribute("userTopScores1", userTopScores1);
        model.addAttribute("userTopScores2", userTopScores2);
        model.addAttribute("userTopScores3", userTopScores3);
        model.addAttribute("profileComments", profileComments);
        SlitherlinkControllerHelper.addCommonAttributes(model, session, scoreService, commentService, ratingService, followService);

        final PlayerSettings profileSettings = playerSettingsService.getSettings(username);
        final boolean showMultiplayer = profileSettings == null || profileSettings.isShowMultiplayerScores();

        if (showMultiplayer) {
            model.addAttribute("multiplayerScores", multiplayerScoreService.getScoresForPlayer(username));
        } else {
            model.addAttribute("multiplayerScores", java.util.Collections.emptyList());
        }
        model.addAttribute("showMultiplayer", showMultiplayer);


        final List<Score> allSoloScores = scoreService.getTopScores("slitherlink").stream().filter(s -> username.equals(s.getPlayer())).collect(Collectors.toList());
        final List<MultiplayerScore> mpForCalc = multiplayerScoreService.getScoresForPlayer(username);
        final List<Follow> followingList2 = followService.getFollowing(username);
        final List<Follow> followersList2 = followService.getFollowers(username);
        final List<Comment> allComments = commentService.getComments("slitherlink");
        final List<Comment> commentsByUser = allComments.stream().filter(c -> username.equals(c.getPlayer())).collect(Collectors.toList());
        final List<AchievementCalculator.Achievement> achievements = AchievementCalculator.calculate(username, allSoloScores, mpForCalc, followingList2, followersList2, commentsByUser);

        model.addAttribute("achievements", achievements);
        model.addAttribute("achievementsUnlocked", AchievementCalculator.countUnlocked(achievements));
        model.addAttribute("achievementsTotal", achievements.size());

        if (session.getPlayerName() != null && session.getPlayerName().equals(username)) {
            achievementChecker.check(username);
        }
        return "slitherfriends_profile";
    }

    private List<Score> filterTopScores(String username, int level) {
        final List<Score> top10 = scoreService.getTopScoresByLevel("slitherlink", level);
        final List<Score> filtered = new ArrayList<>();
        for (final Score s : top10) {
            if (username.equals(s.getPlayer())) filtered.add(s);
        }
        return filtered;
    }

    @GetMapping("/slitherlink/friends")
    public String friendsPage(@RequestParam(value = "search", required = false) String search, Model model) {

        if (session.getPlayerName() == null) return "redirect:/";
        final List<Follow> following = followService.getFollowing(session.getPlayerName());
        final List<Follow> followers = followService.getFollowers(session.getPlayerName());

        String searchResult = null;
        boolean searchFound = false;
        if (search != null && !search.trim().isEmpty()) {
            final String trimmed = search.trim();
            if (playerService.exists(trimmed)) {
                searchFound = true;
                searchResult = trimmed;
            } else {
                searchResult = trimmed;
            }
        }

        model.addAttribute("following", following);
        model.addAttribute("followers", followers);
        model.addAttribute("search", search);
        model.addAttribute("searchResult", searchResult);
        model.addAttribute("searchFound", searchFound);

        SlitherlinkControllerHelper.addCommonAttributes(model, session, scoreService, commentService, ratingService, followService);
        return "slitherfriends";
    }

    @GetMapping("/slitherlink/follow")
    public String follow(@RequestParam("user") String user, @RequestParam(value = "from", defaultValue = "/slitherlink/friends") String from) {
        if (session.getPlayerName() == null) return "redirect:/";
        followService.follow(session.getPlayerName(), user);
        notificationService.create(new sk.tuke.gamestudio.entity.playerADDons.Notification(user, session.getPlayerName(), "FOLLOW", null));
        achievementChecker.check(session.getPlayerName());
        return "redirect:" + from;
    }

    @GetMapping("/slitherlink/unfollow")
    public String unfollow(@RequestParam("user") String user, @RequestParam(value = "from", defaultValue = "/slitherlink/friends") String from) {
        if (session.getPlayerName() == null) return "redirect:/";
        followService.unfollow(session.getPlayerName(), user);
        notificationService.create(new sk.tuke.gamestudio.entity.playerADDons.Notification(user, session.getPlayerName(), "UNFOLLOW", null));
        achievementChecker.check(session.getPlayerName());
        return "redirect:" + from;
    }

    @PostMapping("/slitherlink/profile/{username}/comment")
    public String addProfileComment(@PathVariable String username, @RequestParam("text") String text) {
        if (session.getPlayerName() == null) return "redirect:/";
        if (!playerService.exists(username)) return "redirect:/slitherlink/friends";
        if (text != null && !text.trim().isEmpty()) {
            commentService.addComment(new Comment(profileGame(username), session.getPlayerName(), text.trim(), new Date()));
            notificationService.create(new sk.tuke.gamestudio.entity.playerADDons.Notification(username, session.getPlayerName(), "COMMENT", text.trim()));
            achievementChecker.check(session.getPlayerName());
        }
        return "redirect:/slitherlink/profile/" + username;
    }

    @GetMapping("/slitherlink/profile/{username}/deletecomment")
    public String deleteProfileComment(@PathVariable String username, @RequestParam("ident") int ident) {
        if (session.getPlayerName() == null) return "redirect:/";
        commentService.deleteComment(ident, session.getPlayerName());
        return "redirect:/slitherlink/profile/" + username;
    }

    public static Set<String> getFollowingNames(FollowService followService, String username) {
        final Set<String> names = new HashSet<>();
        if (username == null) return names;
        for (final Follow f : followService.getFollowing(username)) {
            names.add(f.getFollowingName());
        }
        return names;
    }
}