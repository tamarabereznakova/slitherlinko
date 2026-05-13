package sk.tuke.gamestudio.server.controller.helpers.multiplayer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.WebApplicationContext;
import sk.tuke.gamestudio.entity.playerADDons.*;
import sk.tuke.gamestudio.entity.playerADDons.GameInvite;
import sk.tuke.gamestudio.entity.playerADDons.multiplayer.Lobby;
import sk.tuke.gamestudio.entity.playerADDons.multiplayer.LobbyMember;
import sk.tuke.gamestudio.game.slitherlink.core.logic.PredefinedMaps;
import sk.tuke.gamestudio.server.controller.helpers.SlitherlinkControllerHelper;
import sk.tuke.gamestudio.server.controller.helpers.SlitherlinkSession;
import sk.tuke.gamestudio.service.entity.CommentService;
import sk.tuke.gamestudio.service.entity.PlayerService;
import sk.tuke.gamestudio.service.entity.RatingService;
import sk.tuke.gamestudio.service.entity.ScoreService;
import sk.tuke.gamestudio.service.entity.playerADDons.FollowService;
import sk.tuke.gamestudio.service.entity.playerADDons.GameInviteService;
import sk.tuke.gamestudio.service.entity.playerADDons.LobbyService;
import sk.tuke.gamestudio.service.entity.playerADDons.NotificationService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@Scope(WebApplicationContext.SCOPE_SESSION)
public class LobbyController {

    @Autowired
    private SlitherlinkSession session;
    @Autowired
    private LobbyService lobbyService;
    @Autowired
    private PlayerService playerService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private FollowService followService;
    @Autowired
    private ScoreService scoreService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private RatingService ratingService;
    @Autowired
    GameInviteService gameInviteService;
    @Autowired
    CoopSessionRegistry coopRegistry;

    @PostMapping("/slitherlink/lobby/create")
    public String createLobby(@RequestParam("mode") String mode, @RequestParam(value = "level", defaultValue = "1") int level) {

        if (session.getPlayerName() == null) return "redirect:/";

        if (!"RACE".equals(mode) && !"COOP".equals(mode)) {
            return "redirect:/slitherlink/level";
        }
        if (level < 0 || level > 3) level = 1;

        for (final Lobby existing : lobbyService.getActiveLobbiesForUser(session.getPlayerName())) {
            if (session.getPlayerName().equals(existing.getCreator())
                    && mode.equals(existing.getMode())) {
                if ("RACE".equals(mode)) {
                    return "redirect:/slitherlink/lobby/race/" + existing.getToken();
                } else {
                    return "redirect:/slitherlink/lobby/coop/" + existing.getToken();
                }
            }
        }

        int mapIndex;
        switch (level) {
            case 0:
                mapIndex = PredefinedMaps.testMapIndex();
                break;
            case 2:
                mapIndex = PredefinedMaps.randomLargeMapIndex();
                break;
            case 3:
                mapIndex = PredefinedMaps.randomExtraLargeMapIndex();
                break;
            default:
                mapIndex = PredefinedMaps.randomSmallMapIndex();
                break;
        }

        final String token = lobbyService.createLobby(session.getPlayerName(), mode, level, mapIndex);
        if ("RACE".equals(mode)) {
            return "redirect:/slitherlink/lobby/race/" + token;
        } else {
            return "redirect:/slitherlink/lobby/coop/" + token;
        }
    }

    @GetMapping("/slitherlink/lobby/race/{token}")
    public String lobbyRacePage(@PathVariable String token, Model model) {
        return loadLobbyPage(token, "RACE", model, "multiplayer/lobby_race");
    }

    @GetMapping("/slitherlink/lobby/coop/{token}")
    public String lobbyCoopPage(@PathVariable String token, Model model) {
        return loadLobbyPage(token, "COOP", model, "multiplayer/lobby_coop");
    }

    @PostMapping("/slitherlink/lobby/{token}/invite")
    public String invitePlayer(@PathVariable String token, @RequestParam("username") String username) {

        if (session.getPlayerName() == null) return "redirect:/";

        final Lobby lobby = lobbyService.findByToken(token);
        if (lobby == null) return "redirect:/slitherlink/level";
        if (!session.getPlayerName().equals(lobby.getCreator())) {
            return redirectToLobby(lobby);
        }

        final String trimmed = (username != null) ? username.trim() : "";
        if (trimmed.isEmpty()) return redirectToLobby(lobby) + "?msg=empty";
        if (trimmed.equals(session.getPlayerName())) return redirectToLobby(lobby) + "?msg=self";
        if (!playerService.exists(trimmed)) return redirectToLobby(lobby) + "?msg=notfound";

        final int memberLimit = "RACE".equals(lobby.getMode()) ? 2 : 4;
        final List<LobbyMember> currentMembers = lobbyService.getMembers(token);
        int acceptedCount = 0;
        for (final LobbyMember m : currentMembers) {
            if ("ACCEPTED".equals(m.getStatus())) acceptedCount++;
        }
        if (acceptedCount >= memberLimit) return redirectToLobby(lobby) + "?msg=full";
        lobbyService.inviteMember(token, trimmed);
        gameInviteService.sendInvite(new GameInvite(session.getPlayerName(), trimmed, token, "LOBBY_" + lobby.getMode()));
        return redirectToLobby(lobby) + "?msg=sent";
    }

    @GetMapping("/slitherlink/lobby/{token}/accept")
    public String acceptInvite(@PathVariable String token) {
        if (session.getPlayerName() == null) return "redirect:/";

        for (final GameInvite g : gameInviteService.getInbox(session.getPlayerName())) {
            if (token.equals(g.getToken()) && !g.isRead()) {
                gameInviteService.markAsRead(g.getIdent());
                break;
            }
        }

        final Lobby lobby = lobbyService.findByToken(token);
        if (lobby == null) {
            return "redirect:/slitherlink/lobby/race/" + token;
        }

        final int memberLimit = "RACE".equals(lobby.getMode()) ? 2 : 4;
        int acceptedCount = 0;
        boolean alreadyAccepted = false;
        for (final LobbyMember m : lobbyService.getMembers(token)) {
            if ("ACCEPTED".equals(m.getStatus())) {
                acceptedCount++;
                if (session.getPlayerName().equals(m.getUsername())) alreadyAccepted = true;
            }
        }
        if (alreadyAccepted) return redirectToLobby(lobby);
        if (acceptedCount >= memberLimit) {
            return redirectToLobby(lobby) + "?msg=full";
        }

        lobbyService.acceptInvite(token, session.getPlayerName());
        return redirectToLobby(lobby);
    }

    @GetMapping("/slitherlink/lobby/{token}/decline")
    public String declineInvite(@PathVariable String token) {
        if (session.getPlayerName() == null) return "redirect:/";
        for (final GameInvite g : gameInviteService.getInbox(session.getPlayerName())) {
            if (token.equals(g.getToken()) && !g.isRead()) {
                gameInviteService.markAsRead(g.getIdent());
                break;
            }
        }
        lobbyService.declineInvite(token, session.getPlayerName());

        for (final GameInvite g : gameInviteService.getInbox(session.getPlayerName())) {
            if (token.equals(g.getToken())) {
                gameInviteService.deleteInviteForUser(g.getIdent(), session.getPlayerName());
                break;
            }
        }
        return "redirect:/slitherlink/inbox";
    }

    @GetMapping("/slitherlink/lobby/{token}/kick")
    public String kickMember(@PathVariable String token, @RequestParam("user") String user) {
        if (session.getPlayerName() == null) return "redirect:/";
        final Lobby lobby = lobbyService.findByToken(token);
        if (lobby == null) return "redirect:/slitherlink/level";
        if (!session.getPlayerName().equals(lobby.getCreator())) return redirectToLobby(lobby);
        if (user.equals(lobby.getCreator())) return redirectToLobby(lobby);

        lobbyService.leaveLobby(token, user);
        return redirectToLobby(lobby);
    }

    @GetMapping("/slitherlink/lobby/{token}/leave")
    public String leaveLobby(@PathVariable String token) {
        if (session.getPlayerName() == null) return "redirect:/";
        final Lobby lobby = lobbyService.findByToken(token);
        if (lobby == null) return "redirect:/slitherlink/level";

        if (session.getPlayerName().equals(lobby.getCreator())) {
            lobbyService.deleteLobby(token);
        } else {
            lobbyService.leaveLobby(token, session.getPlayerName());
        }
        return "redirect:/slitherlink/level";
    }

    @GetMapping("/slitherlink/lobby/{token}/cancel")
    public String cancelLobby(@PathVariable String token) {
        if (session.getPlayerName() == null) return "redirect:/";
        final Lobby lobby = lobbyService.findByToken(token);
        if (lobby == null) return "redirect:/slitherlink/level";
        if (!session.getPlayerName().equals(lobby.getCreator()))
            return redirectToLobby(lobby);

        coopRegistry.remove(token);
        lobbyService.deleteLobby(token);
        return "redirect:/slitherlink/level";
    }

    @GetMapping("/api/lobby/{token}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> lobbyApi(@PathVariable String token) {
        final Map<String, Object> data = new HashMap<>();
        final Lobby lobby = lobbyService.findByToken(token);
        if (lobby == null) {
            data.put("status", "expired");
            return ResponseEntity.ok(data);
        }

        data.put("status", lobby.getState());
        data.put("mode", lobby.getMode());
        data.put("creator", lobby.getCreator());
        data.put("level", lobby.getLevel());

        final List<Map<String, Object>> membersOut = new ArrayList<>();
        for (final LobbyMember m : lobbyService.getMembers(token)) {
            final Map<String, Object> mm = new HashMap<>();
            mm.put("username", m.getUsername());
            mm.put("status", m.getStatus());
            mm.put("progress", m.getProgress());
            mm.put("finished", m.isFinished());
            mm.put("online", playerService.isOnline(m.getUsername()));
            membersOut.add(mm);
        }
        data.put("members", membersOut);
        return ResponseEntity.ok(data);
    }

    private String loadLobbyPage(String token, String expectedMode, Model model, String view) {
        if (session.getPlayerName() == null) return "redirect:/";

        final Lobby lobby = lobbyService.findByToken(token);
        if (lobby == null) {
            model.addAttribute("error", "expired");
            SlitherlinkControllerHelper.addCommonAttributes(model, session,
                    scoreService, commentService, ratingService, followService);
            return view;
        }

        final boolean creatorOffline = !playerService.isOnline(lobby.getCreator());
        model.addAttribute("creatorOffline", creatorOffline);

        final CoopSessionRegistry.CoopGame runningGame = coopRegistry.get(token);
        model.addAttribute("gameRunning", runningGame != null);

        final List<LobbyMember> members = lobbyService.getMembers(token);
        boolean isMember = false;
        for (final LobbyMember m : members) {
            if (session.getPlayerName().equals(m.getUsername())) {
                isMember = true;
                break;
            }
        }
        if (!isMember) {
            model.addAttribute("error", "notmember");
            SlitherlinkControllerHelper.addCommonAttributes(model, session, scoreService, commentService, ratingService, followService);
            return view;
        }

        model.addAttribute("lobby", lobby);
        model.addAttribute("members", members);
        model.addAttribute("isCreator", session.getPlayerName().equals(lobby.getCreator()));
        model.addAttribute("levelName", SlitherlinkControllerHelper.getLevelName(lobby.getLevel()));

        final List<String> followingList = new ArrayList<>();
        for (final Follow f : followService.getFollowing(session.getPlayerName())) {
            followingList.add(f.getFollowingName());
        }
        model.addAttribute("followingList", followingList);

        SlitherlinkControllerHelper.addCommonAttributes(model, session, scoreService, commentService, ratingService, followService);
        return view;
    }

    private String redirectToLobby(Lobby lobby) {
        final String prefix = "RACE".equals(lobby.getMode()) ? "/slitherlink/lobby/race/" : "/slitherlink/lobby/coop/";
        return "redirect:" + prefix + lobby.getToken();
    }
}