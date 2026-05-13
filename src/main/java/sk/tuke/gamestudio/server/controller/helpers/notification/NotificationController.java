package sk.tuke.gamestudio.server.controller.helpers.notification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import sk.tuke.gamestudio.entity.playerADDons.GameInvite;
import sk.tuke.gamestudio.entity.playerADDons.Notification;
import sk.tuke.gamestudio.server.controller.helpers.SlitherlinkSession;
import sk.tuke.gamestudio.service.entity.PlayerService;
import sk.tuke.gamestudio.service.entity.playerADDons.GameInviteService;
import sk.tuke.gamestudio.service.entity.playerADDons.NotificationService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class NotificationController {

    @Autowired
    private SlitherlinkSession session;
    @Autowired
    private GameInviteService gameInviteService;
    @Autowired
    private PlayerService playerService;
    @Autowired
    private NotificationService notificationService;

    @GetMapping("/api/notifications")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> notifications() {
        final Map<String, Object> data = new HashMap<>();

        if (session.getPlayerName() == null) {
            data.put("loggedIn", false);
            return ResponseEntity.ok(data);
        }
        data.put("loggedIn", true);
        data.put("unread", gameInviteService.countUnread(session.getPlayerName()));

        final List<GameInvite> inbox = gameInviteService.getInbox(session.getPlayerName());
        final List<Map<String, Object>> unreadAll = new ArrayList<>();
        for (final GameInvite g : inbox) {
            if (!g.isRead() && !g.isDelivered()) {
                final Map<String, Object> m = new HashMap<>();
                m.put("ident", g.getIdent());
                m.put("from", g.getFromPlayer());
                m.put("token", g.getToken());
                m.put("inviteType", g.getInviteType());
                unreadAll.add(m);
                gameInviteService.markDelivered(g.getIdent());
            }
        }
        data.put("unreadAll", unreadAll);
        if (!unreadAll.isEmpty()) {
            data.put("latestUnread", unreadAll.get(0));
        }

        final List<Notification> unseen = notificationService.getUnseen(session.getPlayerName());
        final List<Map<String, Object>> events = new ArrayList<>();
        for (final Notification n : unseen) {
            final Map<String, Object> e = new HashMap<>();
            e.put("ident", n.getIdent());
            e.put("type", n.getType());
            e.put("actor", n.getActor());
            e.put("payload", n.getPayload());
            events.add(e);
        }
        data.put("events", events);

        if (!unseen.isEmpty()) {
            notificationService.markAllSeen(session.getPlayerName());
        }

        return ResponseEntity.ok(data);
    }

    @GetMapping("/api/online")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> onlineStatus(@RequestParam("user") String user) {
        final Map<String, Object> data = new HashMap<>();
        data.put("user", user);
        data.put("online", playerService.isOnline(user));
        return ResponseEntity.ok(data);
    }
}