package sk.tuke.gamestudio.service.entity.playerADDons;

import sk.tuke.gamestudio.entity.playerADDons.GameInvite;
import java.util.List;

public interface GameInviteService {
    void sendInvite(GameInvite invite);
    List<GameInvite> getInbox(String toPlayer);
    List<GameInvite> getOutbox(String fromPlayer);
    void markDelivered(int ident);
    void markAsRead(int ident);
    long countUnread(String toPlayer);
    void deleteInvite(int ident); //asi ne
    void deleteInviteForUser(int ident, String username);
    void deleteByPlayer(String player);
}