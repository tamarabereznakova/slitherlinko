package sk.tuke.gamestudio.server.controller.helpers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.WebApplicationContext;
import sk.tuke.gamestudio.entity.history.SavedGame;
import sk.tuke.gamestudio.entity.playerADDons.multiplayer.Lobby;
import sk.tuke.gamestudio.game.slitherlink.core.logic.Generator;
import sk.tuke.gamestudio.game.slitherlink.core.logic.Play;
import sk.tuke.gamestudio.game.slitherlink.core.logic.PredefinedMaps;
import sk.tuke.gamestudio.server.controller.helpers.notification.AchievementChecker;
import sk.tuke.gamestudio.service.entity.CommentService;
import sk.tuke.gamestudio.service.entity.RatingService;
import sk.tuke.gamestudio.service.entity.ScoreService;
import sk.tuke.gamestudio.service.entity.history.SavedGameService;
import sk.tuke.gamestudio.service.entity.playerADDons.FollowService;
import sk.tuke.gamestudio.service.entity.playerADDons.LobbyService;
import sk.tuke.gamestudio.service.entity.playerADDons.SpectateService;

@Controller
@Scope(WebApplicationContext.SCOPE_SESSION)
public class SlitherlinkGameController {

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
    @Autowired
    LobbyService lobbyService;
    @Autowired
    private AchievementChecker achievementChecker;

    @RequestMapping("/slitherlink/level")
    public String levelSelect(Model model) {
        if (session.getPlayerName() == null) return "redirect:/";
        achievementChecker.check(session.getPlayerName());
        if (session.getField() != null) {
            spectateService.deleteByPlayerAndLevel(session.getPlayerName(), session.getCurrentLevel());
        }

        model.addAttribute("saved0", savedGameService.loadGame(session.getPlayerName(), 0));
        model.addAttribute("saved0", savedGameService.loadGame(session.getPlayerName(), 0));
        model.addAttribute("saved1", savedGameService.loadGame(session.getPlayerName(), 1));
        model.addAttribute("saved2", savedGameService.loadGame(session.getPlayerName(), 2));
        model.addAttribute("saved3", savedGameService.loadGame(session.getPlayerName(), 3));
        SlitherlinkControllerHelper.addCommonAttributes(model, session, scoreService, commentService, ratingService, followService);
        final java.util.List<Lobby> myLobbies = lobbyService.getActiveLobbiesForUser(session.getPlayerName());
        model.addAttribute("myLobbies", myLobbies);
        Lobby myRaceLobby = null;
        Lobby myCoopLobby = null;
        for (final Lobby l :
                lobbyService.getActiveLobbiesForUser(session.getPlayerName())) {
            if (session.getPlayerName().equals(l.getCreator())) {
                if ("RACE".equals(l.getMode())) myRaceLobby = l;
                else if ("COOP".equals(l.getMode())) myCoopLobby = l;
            }
        }
        model.addAttribute("myRaceLobby", myRaceLobby);
        model.addAttribute("myCoopLobby", myCoopLobby);
        return "level";
    }

    @RequestMapping("/slitherlink/new")
    public String newGame(@RequestParam(value = "level", defaultValue = "1") String level, @RequestParam(value = "fresh", required = false) String fresh) {

        if (session.getPlayerName() == null) return "redirect:/";
        spectateService.deleteByPlayerAndLevel(session.getPlayerName(), session.getCurrentLevel());
        session.setCurrentLevel(Integer.parseInt(level));
        switch (session.getCurrentLevel()) {
            case 0:
                session.setCurrentMapIndex(PredefinedMaps.testMapIndex());
                break;
            case 2:
                session.setCurrentMapIndex(PredefinedMaps.randomLargeMapIndex());
                break;
            case 3:
                session.setCurrentMapIndex(PredefinedMaps.randomExtraLargeMapIndex());
                break;
            default:
                session.setCurrentMapIndex(PredefinedMaps.randomSmallMapIndex());
                break;
        }

        session.setField(new Generator(0, 0).generateFromMap(PredefinedMaps.getMap(session.getCurrentMapIndex())));
        session.setPlay(new Play(session.getField(), session.getPlayerName(), null, session.getCurrentLevel(), session.getCurrentMapIndex()));
        if (fresh == null) loadSavedGame();
        else savedGameService.deleteGame(session.getPlayerName(), session.getCurrentLevel());
        return "redirect:/slitherlink";
    }

    private void loadSavedGame() {
        final SavedGame saved = savedGameService.loadGame(session.getPlayerName(), session.getCurrentLevel());
        if (saved == null) return;
        session.setCurrentMapIndex(saved.getMapIndex());
        session.setField(new Generator(0, 0).generateFromMap(PredefinedMaps.getMap(session.getCurrentMapIndex())));
        session.setPlay(new Play(session.getField(), session.getPlayerName(), null, session.getCurrentLevel(), session.getCurrentMapIndex()));
        SlitherlinkControllerHelper.deserializeEdges(session.getField(), saved.getEdges());
        session.getPlay().setMoves(saved.getMoves());
        session.getPlay().setStartTime(System.currentTimeMillis() - (saved.getElapsed() * 1000L));
        if (saved.getUndoBuffer() != null) session.getPlay().deserializeUndoBuffer(saved.getUndoBuffer());
        if (saved.getRedoBuffer() != null) session.getPlay().deserializeRedoBuffer(saved.getRedoBuffer());
    }
}