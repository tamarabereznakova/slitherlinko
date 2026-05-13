package sk.tuke.gamestudio.service.entity.history;

import sk.tuke.gamestudio.entity.history.SavedGame;

public interface SavedGameService {
    void saveGame(SavedGame savedGame);
    SavedGame loadGame(String player, int level);
    void deleteGame(String player, int level);
}