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
public class CoopController {

    @Autowired
    private SlitherlinkSession session;
    @Autowired
    private LobbyService lobbyService;
    @Autowired
    private CoopSessionRegistry coopRegistry;
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

    @GetMapping("/slitherlink/lobby/{token}/start")
    public String startGame(@PathVariable String token) {
        if (session.getPlayerName() == null) return "redirect:/";

        final Lobby lobby = lobbyService.findByToken(token);
        if (lobby == null) return "redirect:/slitherlink/level";
        if (!session.getPlayerName().equals(lobby.getCreator()))
            return "redirect:/slitherlink/lobby/coop/" + token;

        if (!"COOP".equals(lobby.getMode())) {
            return "redirect:/slitherlink/lobby/race/" + token;
        }

        if (coopRegistry.get(token) != null) {
            return "redirect:/slitherlink/coop/" + token;
        }

        final Set<String> players = new HashSet<>();
        for (final LobbyMember m : lobbyService.getMembers(token)) {
            if ("ACCEPTED".equals(m.getStatus())) players.add(m.getUsername());
        }
        if (players.size() < 2) {
            return "redirect:/slitherlink/lobby/coop/" + token + "?msg=notenough";
        }

        final Field field = new Generator(0, 0).generateFromMap(PredefinedMaps.getMap(lobby.getMapIndex()));
        final Play play = new Play(field, lobby.getCreator(), null, lobby.getLevel(), lobby.getMapIndex());

        coopRegistry.start(new CoopSessionRegistry.CoopGame(token, lobby.getCreator(), lobby.getLevel(), lobby.getMapIndex(), players, field, play));
        lobbyService.setState(token, "PLAYING");

        return "redirect:/slitherlink/coop/" + token;
    }

    @GetMapping("/slitherlink/coop/{token}")
    public String coopPage(@PathVariable String token, Model model) {
        if (session.getPlayerName() == null) return "redirect:/";

        final CoopSessionRegistry.CoopGame game = coopRegistry.get(token);
        if (game == null) {
            return "redirect:/slitherlink/lobby/coop/" + token;
        }
        if (!game.players.contains(session.getPlayerName())) {
            return "redirect:/slitherlink/level";
        }

        model.addAttribute("token", token);
        model.addAttribute("creator", game.creator);
        model.addAttribute("isCreator", session.getPlayerName().equals(game.creator));
        model.addAttribute("levelName", SlitherlinkControllerHelper.getLevelName(game.level));
        SlitherlinkControllerHelper.addCommonAttributes(model, session, scoreService, commentService, ratingService, followService);
        return "multiplayer/coop_game";
    }

    @GetMapping("/api/coop/{token}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> coopApi(@PathVariable String token) {
        final Map<String, Object> data = new HashMap<>();
        if (session.getPlayerName() == null) {
            data.put("status", "auth");
            return ResponseEntity.ok(data);
        }

        final CoopSessionRegistry.CoopGame game = coopRegistry.get(token);
        if (game == null) {
            data.put("status", "ended");
            return ResponseEntity.ok(data);
        }
        if (!game.players.contains(session.getPlayerName())) {
            data.put("status", "notmember");
            return ResponseEntity.ok(data);
        }

        coopRegistry.touch(token, session.getPlayerName());

        if (!game.isCreatorOnline()
                && !session.getPlayerName().equals(game.creator)) {
            coopRegistry.remove(token);
            lobbyService.setState(token, "WAITING");
            data.put("status", "creator_left");
            return ResponseEntity.ok(data);
        }

        if (game.field.getState() == FieldState.SOLVED) {
            if (!game.finished) {
                saveScoreOnce(game);
                game.finished = true;
                game.finishedAt = System.currentTimeMillis();
                lobbyService.setState(token, "WAITING");
            }
            data.put("status", "won");
            data.put("moves", game.play.getMoves());
            data.put("duration", game.getDuration());
            data.put("players", String.join(",", game.players));
            return ResponseEntity.ok(data);
        }
        if (game.finished && (System.currentTimeMillis() - game.finishedAt) > 15_000) {
            coopRegistry.remove(token);
        }

        final List<Map<String, Object>> activeList = new ArrayList<>();
        final long now = System.currentTimeMillis();
        for (final String p : game.players) {
            final Long t = game.lastSeen.get(p);
            final boolean active = t != null && (now - t) < 3_000;
            final Map<String, Object> mm = new HashMap<>();
            mm.put("username", p);
            mm.put("active", active);
            activeList.add(mm);
        }

        data.put("status", "playing");
        data.put("htmlField", SlitherlinkControllerHelper.getHtmlField(game.field, true));
        data.put("moves", game.play.getMoves());
        data.put("elapsed", game.getDuration());
        data.put("creator", game.creator);
        data.put("creatorOnline", game.isCreatorOnline());
        data.put("activePlayers", activeList);
        return ResponseEntity.ok(data);
    }

    @PostMapping("/api/coop/{token}/click")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> coopClick(@PathVariable String token, @RequestParam("row") int row, @RequestParam("col") int col, @RequestParam("side") String side) {

        final Map<String, Object> data = new HashMap<>();
        if (session.getPlayerName() == null) {
            data.put("ok", false);
            return ResponseEntity.ok(data);
        }

        final CoopSessionRegistry.CoopGame game = coopRegistry.get(token);
        if (game == null) {
            data.put("ok", false);
            return ResponseEntity.ok(data);
        }
        if (!game.players.contains(session.getPlayerName())) {
            data.put("ok", false);
            return ResponseEntity.ok(data);
        }

        synchronized (game) {
            if (game.field.getState() == FieldState.PLAYING) {
                game.play.handleClick(row, col, side.charAt(0));
            }
        }
        coopRegistry.touch(token, session.getPlayerName());
        data.put("ok", true);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/slitherlink/coop/{token}/leave")
    public String leaveGame(@PathVariable String token) {
        if (session.getPlayerName() == null) return "redirect:/";
        final CoopSessionRegistry.CoopGame game = coopRegistry.get(token);
        if (game == null) return "redirect:/slitherlink/level";

        if (session.getPlayerName().equals(game.creator)) {
            coopRegistry.remove(token);
            lobbyService.setState(token, "WAITING");
        } else {
            game.players.remove(session.getPlayerName());
            game.lastSeen.remove(session.getPlayerName());
        }
        return "redirect:/slitherlink/lobby/coop/" + token;
    }

    @GetMapping("/slitherlink/coop/{token}/end")
    public String endGame(@PathVariable String token) {
        if (session.getPlayerName() == null) return "redirect:/";
        final CoopSessionRegistry.CoopGame game = coopRegistry.get(token);
        if (game == null) return "redirect:/slitherlink/level";
        if (!session.getPlayerName().equals(game.creator))
            return "redirect:/slitherlink/coop/" + token;

        coopRegistry.remove(token);
        lobbyService.setState(token, "WAITING");
        return "redirect:/slitherlink/lobby/coop/" + token;
    }

    @GetMapping("/slitherlink/lobby/{token}/join")
    public String joinRunningGame(@PathVariable String token) {
        if (session.getPlayerName() == null) return "redirect:/";
        final CoopSessionRegistry.CoopGame game = coopRegistry.get(token);
        if (game == null) return "redirect:/slitherlink/lobby/coop/" + token;
        game.players.add(session.getPlayerName());
        return "redirect:/slitherlink/coop/" + token;
    }

    private final Set<String> savedTokens = java.util.Collections.newSetFromMap(new java.util.concurrent.ConcurrentHashMap<>());

    private void saveScoreOnce(CoopSessionRegistry.CoopGame game) {
        if (savedTokens.contains(game.lobbyToken)) return;
        savedTokens.add(game.lobbyToken);
        multiplayerScoreService.addScore(new MultiplayerScore("COOP", game.level, game.mapIndex, String.join(",", game.players), game.play.getMoves(), game.getDuration(), null));
        for (final String player : game.players) {
            achievementChecker.check(player);
        }
    }
}