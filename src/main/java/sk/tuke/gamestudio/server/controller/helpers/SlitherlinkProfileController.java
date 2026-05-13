package sk.tuke.gamestudio.server.controller.helpers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.WebApplicationContext;
import sk.tuke.gamestudio.entity.Comment;
import sk.tuke.gamestudio.entity.playerADDons.PlayerSettings;
import sk.tuke.gamestudio.entity.Rating;
import sk.tuke.gamestudio.entity.Score;
import sk.tuke.gamestudio.entity.history.Replay;
import sk.tuke.gamestudio.game.slitherlink.core.bricks.Field;
import sk.tuke.gamestudio.game.slitherlink.core.logic.Generator;
import sk.tuke.gamestudio.game.slitherlink.core.logic.Play;
import sk.tuke.gamestudio.game.slitherlink.core.logic.PredefinedMaps;
import sk.tuke.gamestudio.server.controller.helpers.multiplayer.CoopSessionRegistry;
import sk.tuke.gamestudio.server.controller.helpers.multiplayer.RaceSessionRegistry;
import sk.tuke.gamestudio.service.entity.CommentService;
import sk.tuke.gamestudio.service.entity.PlayerService;
import sk.tuke.gamestudio.service.entity.RatingService;
import sk.tuke.gamestudio.service.entity.ScoreService;
import sk.tuke.gamestudio.service.entity.history.ReplayService;
import sk.tuke.gamestudio.service.entity.history.SavedGameService;
import sk.tuke.gamestudio.service.entity.playerADDons.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@Scope(WebApplicationContext.SCOPE_SESSION)
public class SlitherlinkProfileController {

    @Autowired
    private SlitherlinkSession session;
    @Autowired
    private ScoreService scoreService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private RatingService ratingService;
    @Autowired
    private PlayerService playerService;
    @Autowired
    private PlayerSettingsService playerSettingsService;
    @Autowired
    private SavedGameService savedGameService;
    @Autowired
    private ReplayService replayService;
    @Autowired
    private GameInviteService gameInviteService;
    @Autowired
    private SpectateService spectateService;
    @Autowired
    FollowService followService;
    @Autowired
    NotificationService notificationService;
    @Autowired
    private MultiplayerScoreService multiplayerScoreService;
    @Autowired
    private LobbyService lobbyService;
    @Autowired
    private CoopSessionRegistry coopRegistry;
    @Autowired
    private RaceSessionRegistry raceRegistry;

    @RequestMapping("/slitherlink/result")
    public String result(@RequestParam(value = "save", required = false) String save, Model model) {
        if (session.getPlayerName() == null) return "redirect:/";

        if (save != null) {
            if (save.equals("yes")) saveScoreAndReplay();
            savedGameService.deleteGame(session.getPlayerName(), session.getCurrentLevel());
            session.setField(null);
            session.setPlay(null);
            return "redirect:/slitherlink/level";
        }

        model.addAttribute("lastMoves", session.getLastMoves());
        model.addAttribute("lastDuration", session.getLastDuration());
        model.addAttribute("inTop10", session.isInTop10());
        model.addAttribute("levelName", SlitherlinkControllerHelper.getLevelName(session.getCurrentLevel()));
        SlitherlinkControllerHelper.addCommonAttributes(model, session, scoreService, commentService, ratingService, followService);
        return "game/result";
    }

    private void saveScoreAndReplay() {
        final Score score = new Score("slitherlink", session.getPlayerName(), session.getLastMoves(), new Date(), session.getLastDuration(), session.getCurrentLevel(), session.getCurrentMapIndex());
        scoreService.addScore(score);

        final List<Score> scores = scoreService.getScoresByPlayer("slitherlink", session.getPlayerName(), session.getCurrentLevel());
        if (scores.isEmpty()) return;

        final Score latestScore = scores.stream().max(java.util.Comparator.comparing(Score::getPlayedOn)).orElse(null);
        if (latestScore == null) return;

        final int scoreIdent = latestScore.getIdent();
        final List<Replay> replayMoves = new ArrayList<>();
        final List<int[]> history = session.getPlay().getMoveHistory();
        for (int i = 0; i < history.size(); i++) {
            final int[] m = history.get(i);
            replayMoves.add(new Replay(scoreIdent, i, m[0], m[1], (char) m[2]));
        }
        replayService.saveReplay(replayMoves);
    }

    @RequestMapping("/slitherlink/comment")
    public String addComment(@RequestParam(value = "text") String text, @RequestParam(value = "from", defaultValue = "/slitherlink/level") String from) {
        if (session.getPlayerName() != null && session.isHasPlayed() && text != null && !text.trim().isEmpty()) commentService.addComment(new Comment("slitherlink", session.getPlayerName(), text.trim(), new Date()));
        return "redirect:" + from;
    }

    @RequestMapping("/slitherlink/rate")
    public String rate(@RequestParam(value = "rating") int rating, @RequestParam(value = "from", defaultValue = "/slitherlink/level") String from) {
        if (session.getPlayerName() != null && session.isHasPlayed())
            ratingService.addRating(new Rating("slitherlink", session.getPlayerName(), rating, new Date()));
        return "redirect:" + from;
    }

    @RequestMapping("/slitherlink/profile")
    public String profile(@RequestParam(value = "error", required = false) String error, @RequestParam(value = "success", required = false) String success, Model model) {
        if (session.getPlayerName() == null) return "redirect:/";

        model.addAttribute("createdOn", playerService.getCreatedOn(session.getPlayerName()));
        model.addAttribute("settings", session.getSettings() != null ? session.getSettings() : new PlayerSettings(session.getPlayerName()));
        model.addAttribute("error", error);
        model.addAttribute("success", success);
        model.addAttribute("myComments", commentService.getCommentsByPlayer("slitherlink", session.getPlayerName()));
        model.addAttribute("myScores0", scoreService.getScoresByPlayer("slitherlink", session.getPlayerName(), 0));
        model.addAttribute("myScores1", scoreService.getScoresByPlayer("slitherlink", session.getPlayerName(), 1));
        model.addAttribute("myScores2", scoreService.getScoresByPlayer("slitherlink", session.getPlayerName(), 2));
        model.addAttribute("myScores3", scoreService.getScoresByPlayer("slitherlink", session.getPlayerName(), 3));
        model.addAttribute("saved0", savedGameService.loadGame(session.getPlayerName(), 0));
        model.addAttribute("saved1", savedGameService.loadGame(session.getPlayerName(), 1));
        model.addAttribute("saved2", savedGameService.loadGame(session.getPlayerName(), 2));
        model.addAttribute("saved3", savedGameService.loadGame(session.getPlayerName(), 3));
        model.addAttribute("unreadCount", gameInviteService.countUnread(session.getPlayerName()));
        addTopPositions(model);
        SlitherlinkControllerHelper.addCommonAttributes(model, session, scoreService, commentService, ratingService, followService);
        return "player/profile";
    }

    private void addTopPositions(Model model) {
        final Map<Integer, Integer> topPositions = new HashMap<>();
        final List<Score> top0 = scoreService.getTopScoresByLevel("slitherlink", 0);
        final List<Score> top1 = scoreService.getTopScoresByLevel("slitherlink", 1);
        final List<Score> top2 = scoreService.getTopScoresByLevel("slitherlink", 2);
        final List<Score> top3 = scoreService.getTopScoresByLevel("slitherlink", 3);
        for (int i = 0; i < top0.size(); i++) topPositions.put(top0.get(i).getIdent(), i + 1);
        for (int i = 0; i < top1.size(); i++) topPositions.put(top1.get(i).getIdent(), i + 1);
        for (int i = 0; i < top2.size(); i++) topPositions.put(top2.get(i).getIdent(), i + 1);
        for (int i = 0; i < top3.size(); i++) topPositions.put(top3.get(i).getIdent(), i + 1);
        model.addAttribute("topPositions", topPositions);
        model.addAttribute("topIdents", topPositions.keySet());
    }

    @PostMapping("/slitherlink/changepassword")
    public String changePassword(@RequestParam(value = "oldPassword") String oldPassword, @RequestParam(value = "newPassword") String newPassword) {
        if (session.getPlayerName() == null) return "redirect:/";
        if (newPassword.length() < 4 || newPassword.length() > 10)
            return "redirect:/slitherlink/profile?error=invalidpassword";
        if (!playerService.login(session.getPlayerName(), oldPassword))
            return "redirect:/slitherlink/profile?error=wrongpassword";
        if (oldPassword.equals(newPassword))
            return "redirect:/slitherlink/profile?error=samepassword";
        playerService.changePassword(session.getPlayerName(), newPassword);
        return "redirect:/slitherlink/profile?success=passwordchanged";
    }

    @RequestMapping("/slitherlink/deletescore")
    public String deleteScore(@RequestParam(value = "ident") int ident) {
        if (session.getPlayerName() != null) {
            replayService.deleteReplay(ident);
            scoreService.deleteScore("slitherlink", session.getPlayerName(), ident);
        }
        return "redirect:/slitherlink/profile";
    }

    @RequestMapping("/slitherlink/deletecomment")
    public String deleteComment(@RequestParam(value = "ident") int ident) {
        if (session.getPlayerName() != null)
            commentService.deleteComment(ident, session.getPlayerName());
        return "redirect:/slitherlink/profile";
    }

    @RequestMapping("/slitherlink/replay")
    public String replay(@RequestParam(value = "ident") int ident, @RequestParam(value = "step", defaultValue = "0") int step, @RequestParam(value = "from", required = false) String from, Model model) {
        if (session.getPlayerName() == null) return "redirect:/";
        final List<Replay> moves = replayService.getReplay(ident);
        Score score = null;
        for (int lvl = 0; lvl < 4; lvl++) {
            for (final Score s : scoreService.getTopScoresByLevel("slitherlink", lvl)) {
                if (s.getIdent() == ident) {
                    score = s;
                    break;
                }
            }
            if (score != null) break;
        }
        if (score == null) return "redirect:/slitherlink/profile";

        final Field replayField = new Generator(0, 0).generateFromMap(PredefinedMaps.getMap(score.getMapIndex()));
        final Play replayPlay = new Play(replayField, score.getPlayer(), null, score.getLevel(), 0);
        final int maxStep = Math.min(step, moves.size());
        for (int i = 0; i < maxStep; i++) {
            final Replay m = moves.get(i);
            replayPlay.handleClick(m.getRowNum(), m.getColNum(), m.getSide());
        }

        model.addAttribute("replayField", SlitherlinkControllerHelper.getHtmlField(replayField, false));
        model.addAttribute("step", step);
        model.addAttribute("backUrl", from != null && !from.isEmpty() ? from : "/slitherlink/profile");
        model.addAttribute("totalSteps", moves.size());
        model.addAttribute("ident", ident);
        model.addAttribute("score", score);
        model.addAttribute("levelName", SlitherlinkControllerHelper.getLevelName(score.getLevel()));
        SlitherlinkControllerHelper.addCommonAttributes(model, session, scoreService, commentService, ratingService, followService);
        return "game/replay";
    }

    @PostMapping("/slitherlink/settings")
    public String saveSettings(@RequestParam(value = "preferredTheme", defaultValue = "neon") String theme, @RequestParam(value = "preferredLevel", defaultValue = "1") int level, @RequestParam(value = "moveLimit", defaultValue = "0") int moveLimit, @RequestParam(value = "timeLimit", defaultValue = "0") int timeLimit, @RequestParam(value = "showMultiplayerScores", defaultValue = "false") boolean showMultiplayerScores) {

        if (session.getPlayerName() == null) return "redirect:/";
        final int validMoveLimit = Math.min(Math.max(moveLimit, 0), 500);
        final int validTimeLimit = Math.min(Math.max(timeLimit, 0), 86400);
        final PlayerSettings s = new PlayerSettings(session.getPlayerName());
        s.setPreferredTheme(theme);
        s.setPreferredLevel(level);
        s.setMoveLimit(validMoveLimit);
        s.setTimeLimit(validTimeLimit);
        s.setShowMultiplayerScores(showMultiplayerScores);
        playerSettingsService.saveSettings(s);
        session.setSettings(playerSettingsService.getSettings(session.getPlayerName()));
        return "redirect:/slitherlink/profile?success=settingssaved";
    }

    @RequestMapping("/slitherlink/deleteprofile")
    public String deleteProfile() {
        if (session.getPlayerName() == null) return "redirect:/";
        final String me = session.getPlayerName();

        savedGameService.deleteGame(me, 0);
        savedGameService.deleteGame(me, 1);
        savedGameService.deleteGame(me, 2);
        savedGameService.deleteGame(me, 3);

        final List<Score> allScores = new ArrayList<>();
        allScores.addAll(scoreService.getScoresByPlayer("slitherlink", me, 0));
        allScores.addAll(scoreService.getScoresByPlayer("slitherlink", me, 1));
        allScores.addAll(scoreService.getScoresByPlayer("slitherlink", me, 2));
        allScores.addAll(scoreService.getScoresByPlayer("slitherlink", me, 3));
        for (final Score s : allScores) {
            replayService.deleteReplay(s.getIdent());
            scoreService.deleteScore("slitherlink", me, s.getIdent());
        }

        commentService.deleteCommentsByPlayer("slitherlink", me);
        commentService.deleteCommentsByPlayer("profile:" + me, me);
        for (final Comment c : commentService.getComments("profile:" + me)) {
            commentService.deleteComment(c.getIdent(), c.getPlayer());
        }
        ratingService.deleteRating("slitherlink", me);
        playerSettingsService.deleteSettings(me);
        spectateService.deleteByPlayer(me);
        gameInviteService.deleteByPlayer(me);
        followService.deleteByPlayer(me);
        notificationService.deleteByPlayer(me);
        multiplayerScoreService.deleteByPlayer(me);
        lobbyService.deleteByPlayer(me);
        coopRegistry.removePlayerFromAll(me);
        raceRegistry.removePlayerFromAll(me);
        playerService.deletePlayer(me);
        session.setPlayerName(null);
        session.setReturning(false);
        session.setHasPlayed(false);
        session.setGamesPlayed(0);
        session.setField(null);
        session.setPlay(null);
        return "redirect:/home";
    }
}