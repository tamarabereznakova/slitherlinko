package sk.tuke.gamestudio.server.controller.helpers.slitherfriends;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.WebApplicationContext;
import sk.tuke.gamestudio.entity.history.SavedGame;
import sk.tuke.gamestudio.entity.playerADDons.GameInvite;
import sk.tuke.gamestudio.entity.playerADDons.Spectate;
import sk.tuke.gamestudio.server.controller.helpers.SlitherlinkControllerHelper;
import sk.tuke.gamestudio.server.controller.helpers.SlitherlinkSession;
import sk.tuke.gamestudio.service.entity.CommentService;
import sk.tuke.gamestudio.service.entity.PlayerService;
import sk.tuke.gamestudio.service.entity.RatingService;
import sk.tuke.gamestudio.service.entity.ScoreService;
import sk.tuke.gamestudio.service.entity.history.SavedGameService;
import sk.tuke.gamestudio.service.entity.playerADDons.FollowService;
import sk.tuke.gamestudio.service.entity.playerADDons.GameInviteService;
import sk.tuke.gamestudio.service.entity.playerADDons.SpectateService;
import java.util.HashMap;
import java.util.Map;

@Controller
@Scope(WebApplicationContext.SCOPE_SESSION)
public class SpectateController {

    @Autowired
    private SlitherlinkSession session;
    @Autowired
    private SpectateService spectateService;
    @Autowired
    private GameInviteService gameInviteService;
    @Autowired
    private SavedGameService savedGameService;
    @Autowired
    private PlayerService playerService;
    @Autowired
    private ScoreService scoreService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private RatingService ratingService;
    @Autowired
    private FollowService followService;

    private static final java.util.Map<String, java.util.Map<String, Long>> watchers = new java.util.concurrent.ConcurrentHashMap<>();

    @PostMapping("/slitherlink/invite")
    public String sendInvite(@RequestParam(value = "toPlayer", required = false) String toPlayer) {
        if (session.getPlayerName() == null) return "redirect:/";
        if (session.getField() == null || session.getPlay() == null) return "redirect:/slitherlink/level";
        final String trimmed = (toPlayer != null) ? toPlayer.trim() : "";
        final String recipient = (!trimmed.isEmpty() && playerService.exists(trimmed) && !trimmed.equals(session.getPlayerName())) ? trimmed : null;
        final String token = spectateService.createInvite(session.getPlayerName(), recipient, session.getCurrentLevel(), session.getCurrentMapIndex());

        if (!trimmed.isEmpty() && !playerService.exists(trimmed)) {
            return "redirect:/slitherlink?spectateToken=" + token + "&spectateMsg=notfound";
        }
        if (trimmed.equals(session.getPlayerName())) {
            return "redirect:/slitherlink?spectateToken=" + token + "&spectateMsg=self";
        }
        if (recipient != null) {
            gameInviteService.sendInvite(new GameInvite(session.getPlayerName(), recipient, token, "SPECTATE"));
            return "redirect:/slitherlink?spectateToken=" + token + "&spectateMsg=sent";
        }
        return "redirect:/slitherlink?spectateToken=" + token;
    }

    @GetMapping("/api/spectate/{token}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> spectateApi(@PathVariable String token) {
        final Spectate invite = spectateService.findByToken(token);
        if (invite == null) {
            final Map<String, Object> gone = new HashMap<>();
            gone.put("status", "ended");
            return ResponseEntity.ok(gone);
        }

        final SavedGame saved = savedGameService.loadGame(invite.getFromPlayer(), invite.getLevel());
        if (saved == null) {
            final Map<String, Object> waiting = new HashMap<>();
            waiting.put("status", "waiting");
            waiting.put("player", invite.getFromPlayer());
            waiting.put("levelName", SlitherlinkControllerHelper.getLevelName(invite.getLevel()));
            return ResponseEntity.ok(waiting);
        }

        final sk.tuke.gamestudio.game.slitherlink.core.bricks.Field field = new sk.tuke.gamestudio.game.slitherlink.core.logic.Generator(0, 0).generateFromMap(sk.tuke.gamestudio.game.slitherlink.core.logic.PredefinedMaps.getMap(saved.getMapIndex()));
        SlitherlinkControllerHelper.deserializeEdges(field, saved.getEdges());
        final Map<String, Object> data = new HashMap<>();
        data.put("status", "playing");
        data.put("player", invite.getFromPlayer());
        data.put("levelName", SlitherlinkControllerHelper.getLevelName(invite.getLevel()));
        data.put("moves", saved.getMoves());
        data.put("elapsed", saved.getElapsed());
        data.put("htmlField", SlitherlinkControllerHelper.getHtmlField(field, false));

        final javax.servlet.http.HttpSession httpSession = ((org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes()).getRequest().getSession();
        final String sessionId = httpSession.getId();
        final long now = System.currentTimeMillis();
        watchers.computeIfAbsent(token, k -> new java.util.concurrent.ConcurrentHashMap<>()).put(sessionId, now);

        final java.util.Map<String, Long> tokenWatchers = watchers.get(token);
        if (tokenWatchers != null) {
            tokenWatchers.entrySet().removeIf(e -> (now - e.getValue()) > 10_000);
        }
        data.put("watcherCount", tokenWatchers != null ? tokenWatchers.size() : 0);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/spectate/{token}")
    public String spectatePage(@PathVariable String token, Model model) {

        if (session.getPlayerName() == null) {
            return "redirect:/home?spectate=" + token;
        }
        session.setSpectatorMode(true);
        gameInviteService.getInbox(session.getPlayerName()).stream().filter(g -> token.equals(g.getToken()) && !g.isRead()).forEach(g -> gameInviteService.markAsRead(g.getIdent()));

        final Spectate invite = spectateService.findByToken(token);
        if (invite == null) {
            model.addAttribute("error", "expired");
            SlitherlinkControllerHelper.addCommonAttributes(model, session, scoreService, commentService, ratingService, followService);
            return "spectate";
        }

        model.addAttribute("token", token);
        model.addAttribute("spectatedPlayer", invite.getFromPlayer());
        model.addAttribute("levelName", SlitherlinkControllerHelper.getLevelName(invite.getLevel()));
        SlitherlinkControllerHelper.addCommonAttributes(model, session, scoreService, commentService, ratingService, followService);
        return "spectate";
    }

    @GetMapping("/slitherlink/inbox")
    public String inbox(Model model) {
        if (session.getPlayerName() == null) return "redirect:/";
        model.addAttribute("messages", gameInviteService.getInbox(session.getPlayerName()));
        model.addAttribute("isInbox", true);
        SlitherlinkControllerHelper.addCommonAttributes(model, session, scoreService, commentService, ratingService, followService);
        return "mailbox";
    }

    @GetMapping("/slitherlink/outbox")
    public String outbox(Model model) {
        if (session.getPlayerName() == null) return "redirect:/";
        model.addAttribute("messages", gameInviteService.getOutbox(session.getPlayerName()));
        model.addAttribute("isInbox", false);
        SlitherlinkControllerHelper.addCommonAttributes(model, session, scoreService, commentService, ratingService, followService);
        return "mailbox";
    }

    @GetMapping("/slitherlink/deleteinvite")
    public String deleteInvite(@RequestParam(value = "ident") int ident, @RequestParam(value = "box", defaultValue = "inbox") String box) {
        if (session.getPlayerName() != null) {
            gameInviteService.deleteInviteForUser(ident, session.getPlayerName());
        }
        return "redirect:/slitherlink/" + box;
    }

    @GetMapping("/spectate/leave")
    public String leaveSpectate() {
        session.setSpectatorMode(false);
        return "redirect:/slitherlink/level";
    }

    @GetMapping("/slitherlink/stopspectate")
    public String stopSpectate() {
        if (session.getPlayerName() == null) return "redirect:/";
        spectateService.deleteByPlayerAndLevel(session.getPlayerName(), session.getCurrentLevel());
        return "redirect:/slitherlink";
    }

    @GetMapping("/api/my-watchers")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> myWatchers() {
        final Map<String, Object> data = new HashMap<>();
        if (session.getPlayerName() == null) {
            data.put("count", 0);
            return ResponseEntity.ok(data);
        }
        final long now = System.currentTimeMillis();
        int total = 0;

        for (final java.util.Map.Entry<String, java.util.Map<String, Long>> e : watchers.entrySet()) {
            e.getValue().entrySet().removeIf(w -> (now - w.getValue()) > 10_000);

            final sk.tuke.gamestudio.entity.playerADDons.Spectate spec =
                    spectateService.findByToken(e.getKey());
            if (spec != null && session.getPlayerName().equals(spec.getFromPlayer()) && spec.getLevel() == session.getCurrentLevel()) {
                total += e.getValue().size();
            }
        }
        data.put("count", total);
        return ResponseEntity.ok(data);
    }
}