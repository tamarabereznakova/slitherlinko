package sk.tuke.gamestudio.service.entity.playerADDons;

import sk.tuke.gamestudio.entity.playerADDons.multiplayer.MultiplayerScore;
import java.util.List;

public interface MultiplayerScoreService {
    void addScore(MultiplayerScore score);
    List<MultiplayerScore> getScoresForPlayer(String username);
    void deleteByPlayer(String username);
}