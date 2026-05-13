package sk.tuke.gamestudio.server.controller.helpers.multiplayer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.WebApplicationContext;
import sk.tuke.gamestudio.entity.playerADDons.multiplayer.Lobby;
import sk.tuke.gamestudio.entity.playerADDons.multiplayer.LobbyMember;
import sk.tuke.gamestudio.entity.playerADDons.multiplayer.MultiplayerScore;
import sk.tuke.gamestudio.game.slitherlink.core.bricks.Field;
import sk.tuke.gamestudio.game.slitherlink.core.bricks.FieldState;
import sk.tuke.gamestudio.game.slitherlink.core.logic.Generator;
import sk.tuke.gamestudio.game.slitherlink.core.logic.Play;
import sk.tuke.gamestudio.game.slitherlink.core.logic.PredefinedMaps;
import sk.tuke.gamestudio.server.controller.helpers.SlitherlinkControllerHelper;
import sk.tuke.gamestudio.server.controller.helpers.SlitherlinkSession;
import sk.tuke.gamestudio.server.controller.helpers.notification.AchievementChecker;
import sk.tuke.gamestudio.service.entity.CommentService;
import sk.tuke.gamestudio.service.entity.RatingService;
import sk.tuke.gamestudio.service.entity.ScoreService;
import sk.tuke.gamestudio.service.entity.playerADDons.FollowService;
import sk.tuke.gamestudio.service.entity.playerADDons.LobbyService;
import sk.tuke.gamestudio.service.entity.playerADDons.MultiplayerScoreService;
import java.util.*;

@Controller
@Scope(WebApplicationContext.SCOPE_SESSION)
public class RaceController {

    @Autowired
    private SlitherlinkSession session;
    @Autowired
    private LobbyService lobbyService;
    @Autowired
    private RaceSessionRegistry raceRegistry;
    @Autowired
    private MultiplayerScoreService multiplayerScoreService;
    @Autowired
    private ScoreService scoreService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private RatingService ratingService;
    @Autowired
    private FollowService followService;
    @Autowired
    private AchievementChecker achievementChecker;

    @GetMapping("/slitherlink/lobby/{token}/start-race")
    public String startRace(@PathVariable String token) {
        if (session.getPlayerName() == null) return "redirect:/";

        final Lobby lobby = lobbyService.findByToken(token);
        if (lobby == null) return "redirect:/slitherlink/level";
        if (!"RACE".equals(lobby.getMode())) return "redirect:/slitherlink/lobby/coop/" + token;

        final List<String> accepted = new ArrayList<>();
        for (final LobbyMember m : lobbyService.getMembers(token)) {
            if ("ACCEPTED".equals(m.getStatus())) accepted.add(m.getUsername());
        }
        if (accepted.size() != 2) {
            return "redirect:/slitherlink/lobby/race/" + token + "?msg=notenough";
        }

        if (!accepted.contains(session.getPlayerName())) {
            return "redirect:/slitherlink/lobby/race/" + token;
        }

        if (raceRegistry.get(token) != null) {
            return "redirect:/slitherlink/race/" + token;
        }

        final int[][] map = PredefinedMaps.getMap(lobby.getMapIndex());
        final Field fieldA = new Generator(0, 0).generateFromMap(map);
        final Field fieldB = new Generator(0, 0).generateFromMap(map);
        final Play playA = new Play(fieldA, accepted.get(0), null,
                lobby.getLevel(), lobby.getMapIndex());
        final Play playB = new Play(fieldB, accepted.get(1), null,
                lobby.getLevel(), lobby.getMapIndex());

        raceRegistry.start(new RaceSessionRegistry.RaceGame(
                token, accepted.get(0), accepted.get(1),
                lobby.getLevel(), lobby.getMapIndex(),
                fieldA, playA, fieldB, playB));

        lobbyService.setState(token, "PLAYING");
        return "redirect:/slitherlink/race/" + token;
    }

    @GetMapping("/slitherlink/race/{token}")
    public String racePage(@PathVariable String token, Model model) {
        if (session.getPlayerName() == null) return "redirect:/";
        final RaceSessionRegistry.RaceGame game = raceRegistry.get(token);
        if (game == null) {
            return "redirect:/slitherlink/lobby/race/" + token;
        }
        if (!game.states.containsKey(session.getPlayerName())) {
            return "redirect:/slitherlink/level";
        }

        model.addAttribute("token", token);
        model.addAttribute("opponentName", game.opponentOf(session.getPlayerName()));
        model.addAttribute("levelName", SlitherlinkControllerHelper.getLevelName(game.level));
        SlitherlinkControllerHelper.addCommonAttributes(model, session, scoreService, commentService, ratingService, followService);
        return "race_game";
    }

    @GetMapping("/api/race/{token}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> raceApi(@PathVariable String token) {
        final Map<String, Object> data = new HashMap<>();
        if (session.getPlayerName() == null) {
            data.put("status", "auth");
            return ResponseEntity.ok(data);
        }

        final RaceSessionRegistry.RaceGame game = raceRegistry.get(token);
        if (game == null) {
            data.put("status", "ended");
            return ResponseEntity.ok(data);
        }
        final String me = session.getPlayerName();
        if (!game.states.containsKey(me)) {
            data.put("status", "notmember");
            return ResponseEntity.ok(data);
        }
        raceRegistry.touch(token, me);
        final String opponent = game.opponentOf(me);
        final RaceSessionRegistry.PlayerRaceState meState = game.states.get(me);
        final RaceSessionRegistry.PlayerRaceState oppState = game.states.get(opponent);

        if (meState.field.getState() == FieldState.SOLVED && !game.finished) {
            game.finished = true;
            game.winner = me;
            game.finishedAt = System.currentTimeMillis();
            saveScoreOnce(game);
            lobbyService.setState(token, "WAITING");
        } else if (oppState != null
                && oppState.field.getState() == FieldState.SOLVED
                && !game.finished) {
            game.finished = true;
            game.winner = opponent;
            game.finishedAt = System.currentTimeMillis();
            saveScoreOnce(game);
            lobbyService.setState(token, "WAITING");
        }

        if (!game.finished && (!game.isPlayerOnline(me) || !game.isPlayerOnline(opponent))) {
            if (!game.isPlayerOnline(opponent)) {
                raceRegistry.remove(token);
                lobbyService.setState(token, "WAITING");
                data.put("status", "opponent_left");
                data.put("opponent", opponent);
                return ResponseEntity.ok(data);
            }
        }
        if (game.finished && (System.currentTimeMillis() - game.finishedAt) > 15_000) {
            raceRegistry.remove(token);
        }

        if (game.finished) {
            data.put("status", me.equals(game.winner) ? "won" : "lost");
            data.put("winner", game.winner);
            data.put("loser", game.winner.equals(game.playerA) ? game.playerB : game.playerA);
            data.put("myMoves", meState.play.getMoves());
            data.put("opponentMoves", oppState != null ? oppState.play.getMoves() : 0);
            data.put("duration", game.getDuration());
            data.put("myProgress", game.getCorrectProgress(me));
            data.put("opponentProgress", game.getCorrectProgress(opponent));
            return ResponseEntity.ok(data);
        }

        data.put("status", "playing");
        data.put("htmlField", SlitherlinkControllerHelper.getHtmlField(meState.field, true));
        data.put("moves", meState.play.getMoves());
        data.put("elapsed", game.getDuration());
        data.put("myProgress", game.getCorrectProgress(me));
        data.put("opponentProgress", game.getCorrectProgress(opponent));
        data.put("opponentOnline", game.isPlayerOnline(opponent));
        data.put("opponentName", opponent);
        return ResponseEntity.ok(data);
    }

    @PostMapping("/api/race/{token}/click")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> raceClick(@PathVariable String token, @RequestParam("row") int row, @RequestParam("col") int col,@RequestParam("side") String side) {

        final Map<String, Object> data = new HashMap<>();
        if (session.getPlayerName() == null) {
            data.put("ok", false);
            return ResponseEntity.ok(data);
        }

        final RaceSessionRegistry.RaceGame game = raceRegistry.get(token);
        if (game == null) {
            data.put("ok", false);
            return ResponseEntity.ok(data);
        }
        final String me = session.getPlayerName();
        final RaceSessionRegistry.PlayerRaceState state = game.states.get(me);
        if (state == null) {
            data.put("ok", false);
            return ResponseEntity.ok(data);
        }

        synchronized (state) {
            if (state.field.getState() == FieldState.PLAYING) {
                state.play.handleClick(row, col, side.charAt(0));
            }
        }

        raceRegistry.touch(token, me);
        data.put("ok", true);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/slitherlink/race/{token}/leave")
    public String leaveRace(@PathVariable String token) {
        if (session.getPlayerName() == null) return "redirect:/";
        final RaceSessionRegistry.RaceGame game = raceRegistry.get(token);
        if (game != null) {
            raceRegistry.remove(token);
            lobbyService.setState(token, "WAITING");
        }
        return "redirect:/slitherlink/lobby/race/" + token;
    }

    @GetMapping("/slitherlink/lobby/{token}/join-race")
    public String joinRunningRace(@PathVariable String token) {
        if (session.getPlayerName() == null) return "redirect:/";
        final RaceSessionRegistry.RaceGame game = raceRegistry.get(token);
        if (game == null) return "redirect:/slitherlink/lobby/race/" + token;
        if (!game.states.containsKey(session.getPlayerName())) {
            return "redirect:/slitherlink/lobby/race/" + token;
        }
        return "redirect:/slitherlink/race/" + token;
    }

    private final Set<String> savedTokens = java.util.Collections.newSetFromMap(
            new java.util.concurrent.ConcurrentHashMap<>());

    private void saveScoreOnce(RaceSessionRegistry.RaceGame game) {
        if (savedTokens.contains(game.lobbyToken)) return;
        savedTokens.add(game.lobbyToken);
        final RaceSessionRegistry.PlayerRaceState winnerState = game.states.get(game.winner);
        multiplayerScoreService.addScore(new MultiplayerScore(
                "RACE", game.level, game.mapIndex,
                game.playerA + "," + game.playerB,
                winnerState.play.getMoves(),
                game.getDuration(),
                game.winner));
        achievementChecker.check(game.playerA);
        achievementChecker.check(game.playerB);
    }
}