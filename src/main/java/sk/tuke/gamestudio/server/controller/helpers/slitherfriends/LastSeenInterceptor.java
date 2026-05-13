package sk.tuke.gamestudio.server.controller.helpers.slitherfriends;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import sk.tuke.gamestudio.server.controller.helpers.SlitherlinkSession;
import sk.tuke.gamestudio.service.entity.PlayerService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class LastSeenInterceptor implements HandlerInterceptor {

    @Autowired
    private SlitherlinkSession session;
    @Autowired
    private PlayerService playerService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (session.getPlayerName() != null) {
            try {
                playerService.touchLastSeen(session.getPlayerName());
            } catch (Exception e) {

            }
        }
        return true;
    }
}