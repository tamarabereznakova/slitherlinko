package sk.tuke.gamestudio.service.entity.playerADDons;

import sk.tuke.gamestudio.entity.playerADDons.Spectate;

public interface SpectateService {
    String createInvite(String fromPlayer, String toPlayer, int level, int mapIndex);
    Spectate findByToken(String token);
    void deleteByToken(String token);
    void deleteByPlayer(String fromPlayer);
    void deleteByPlayerAndLevel(String fromPlayer, int level);
    boolean hasActiveStream(String fromPlayer, int level);
}