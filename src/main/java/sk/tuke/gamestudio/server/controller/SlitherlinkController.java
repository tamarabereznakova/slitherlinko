package sk.tuke.gamestudio.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import sk.tuke.gamestudio.entity.Score;
import sk.tuke.gamestudio.entity.history.SavedGame;
import sk.tuke.gamestudio.game.slitherlink.core.bricks.FieldState;
import sk.tuke.gamestudio.server.controller.helpers.SlitherlinkControllerHelper;
import sk.tuke.gamestudio.server.controller.helpers.SlitherlinkSession;
import sk.tuke.gamestudio.service.entity.CommentService;
import sk.tuke.gamestudio.service.entity.RatingService;
import sk.tuke.gamestudio.service.entity.ScoreService;
import sk.tuke.gamestudio.service.entity.history.SavedGameService;
import sk.tuke.gamestudio.service.entity.playerADDons.FollowService;
import sk.tuke.gamestudio.service.entity.playerADDons.SpectateService;
import java.util.List;

@Controller
//@Scope(WebApplicationContext.SCOPE_SESSION)
public class SlitherlinkController {

    @Autowired
    private SlitherlinkSession session;
    @Autowired
    private ScoreService scoreService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private RatingService ratingService;
    @Autowired
    private SavedGameService savedGameService;
    @Autowired
    SpectateService spectateService;
    @Autowired
    private FollowService followService;

    @RequestMapping("/slitherlink/undo")
    public String undo() {
        if (session.getPlayerName() == null) return "redirect:/";
        if (session.getField() == null) return "redirect:/slitherlink/level";
        if (session.getPlay() != null) session.getPlay().undo();
        return "redirect:/slitherlink";
    }

    @RequestMapping("/slitherlink/redo")
    public String redo() {
        if (session.getPlayerName() == null) return "redirect:/";
        if (session.getField() == null) return "redirect:/slitherlink/level";
        if (session.getPlay() != null) session.getPlay().redo();
        return "redirect:/slitherlink";
    }

    @RequestMapping("/slitherlink")
    public String slitherlink(@RequestParam(value = "row", required = false) String row, @RequestParam(value = "col", required = false) String col, @RequestParam(value = "side", required = false) String side, @RequestParam(value = "spectateToken", required = false, defaultValue = "") String spectateToken, @RequestParam(value = "spectateMsg", required = false, defaultValue = "") String spectateMsg, Model model) {
        final java.util.List<String> followingList = new java.util.ArrayList<>();
        for (final sk.tuke.gamestudio.entity.playerADDons.Follow f :
                followService.getFollowing(session.getPlayerName())) {
            followingList.add(f.getFollowingName());
        }

        if (session.getPlayerName() == null) return "redirect:/";
        if (session.getField() == null || session.getPlay() == null) return "redirect:/slitherlink/level";
        if (session.getField().getState() == FieldState.PLAYING && session.getPlay().isStarted()) {
            final int timeLimit = session.getSettings() != null ? session.getSettings().getTimeLimit() : 0;
            if (timeLimit > 0) {
                final int elapsed = (int) ((System.currentTimeMillis() - session.getPlay().getStartTime()) / 1000);
                if (elapsed >= timeLimit) session.getField().setState(FieldState.FAILED);
            }
            final int moveLimit = session.getSettings() != null ? session.getSettings().getMoveLimit() : 0;
            if (moveLimit > 0 && session.getPlay().getMoves() >= moveLimit) {
                session.getField().setState(FieldState.FAILED);
            }
        }

        if (session.getField().getState() == FieldState.FAILED) {
            session.setHasPlayed(true);
            session.setGamesPlayed(session.getGamesPlayed() + 1);
            savedGameService.deleteGame(session.getPlayerName(), session.getCurrentLevel());
            spectateService.deleteByPlayerAndLevel(session.getPlayerName(), session.getCurrentLevel());
            session.setField(null);
            session.setPlay(null);
            return "redirect:/slitherlink/level?failed=true";
        }

        if (row != null && col != null && side != null && session.getField().getState() == FieldState.PLAYING) {
            session.getPlay().handleClick(Integer.parseInt(row), Integer.parseInt(col), side.charAt(0));
            final int elapsed = (int) ((System.currentTimeMillis() - session.getPlay().getStartTime()) / 1000);
            savedGameService.saveGame(new SavedGame(session.getPlayerName(), session.getCurrentLevel(), SlitherlinkControllerHelper.serializeEdges(session.getField()), session.getPlay().getMoves(), elapsed, session.getPlay().serializeUndoBuffer(), session.getPlay().serializeRedoBuffer(), session.getCurrentMapIndex()
            ));
        }

        if (session.getField().getState() == FieldState.SOLVED) {
            session.setLastMoves(session.getPlay().getMoves());
            session.setLastDuration((int) ((System.currentTimeMillis() - session.getPlay().getStartTime()) / 1000));
            session.setGamesPlayed(session.getGamesPlayed() + 1);
            session.setHasPlayed(true);
            final List<Score> top = scoreService.getTopScoresByLevel("slitherlink", session.getCurrentLevel());
            session.setInTop10(top.size() < 10 || top.stream().anyMatch(s ->
                    session.getLastDuration() < s.getDuration() ||
                            (session.getLastDuration() == s.getDuration() && session.getLastMoves() <= s.getMoves())));
            spectateService.deleteByPlayerAndLevel(session.getPlayerName(), session.getCurrentLevel());
            return "redirect:/slitherlink/result";
        }

        if (session.getField().getState() == FieldState.PLAYING && session.getPlay().isStarted()) {
            final int timeLimit = session.getSettings() != null ? session.getSettings().getTimeLimit() : 0;
            if (timeLimit > 0) {
                final int elapsed = (int) ((System.currentTimeMillis() - session.getPlay().getStartTime()) / 1000);
                if (elapsed >= timeLimit) {
                    session.getField().setState(FieldState.FAILED);
                    session.setFailReason("time");
                }
            }
            final int moveLimit = session.getSettings() != null ? session.getSettings().getMoveLimit() : 0;
            if (moveLimit > 0 && session.getPlay().getMoves() >= moveLimit) {
                session.getField().setState(FieldState.FAILED);
                if (session.getFailReason() == null) session.setFailReason("moves");
            }
        }

        if (session.getField().getState() == FieldState.FAILED) {
            session.setHasPlayed(true);
            session.setGamesPlayed(session.getGamesPlayed() + 1);
            savedGameService.deleteGame(session.getPlayerName(), session.getCurrentLevel());
            spectateService.deleteByPlayerAndLevel(session.getPlayerName(), session.getCurrentLevel());
            session.setField(null);
            session.setPlay(null);
            final String reason = session.getFailReason() != null ? session.getFailReason() : "unknown";
            session.setFailReason(null);
            return "redirect:/slitherlink/level?failed=" + reason;
        }

        model.addAttribute("followingList", followingList);
        model.addAttribute("htmlField", SlitherlinkControllerHelper.getHtmlField(session.getField(), true));
        model.addAttribute("moves", session.getPlay().getMoves());
        model.addAttribute("elapsed", session.getPlay().isStarted() ? (int) ((System.currentTimeMillis() - session.getPlay().getStartTime()) / 1000) : -1);
        model.addAttribute("moveLimit", session.getSettings() != null ? session.getSettings().getMoveLimit() : 0);
        model.addAttribute("timeLimit", session.getSettings() != null ? session.getSettings().getTimeLimit() : 0);
        model.addAttribute("levelName", SlitherlinkControllerHelper.getLevelName(session.getCurrentLevel()));
        model.addAttribute("currentLevel", session.getCurrentLevel());
        SlitherlinkControllerHelper.addCommonAttributes(model, session, scoreService, commentService, ratingService, followService);
        model.addAttribute("spectateToken", spectateToken);
        model.addAttribute("hasActiveStream", spectateService.hasActiveStream(session.getPlayerName(), session.getCurrentLevel()));
        model.addAttribute("spectateMsg", spectateMsg);
        return "game/slitherlink";
    }

    @RequestMapping("/slitherlink/checktime")
    public String checkTime() {
        if (session.getPlayerName() == null) return "redirect:/";
        if (session.getField() == null || session.getPlay() == null) return "redirect:/slitherlink/level";
        session.getField().setState(FieldState.FAILED);
        session.setFailReason("time");
        session.setHasPlayed(true);
        session.setGamesPlayed(session.getGamesPlayed() + 1);
        savedGameService.deleteGame(session.getPlayerName(), session.getCurrentLevel());
        spectateService.deleteByPlayerAndLevel(session.getPlayerName(), session.getCurrentLevel());
        session.setField(null);
        session.setPlay(null);

        return "redirect:/slitherlink/level?failed=time";
    }
}