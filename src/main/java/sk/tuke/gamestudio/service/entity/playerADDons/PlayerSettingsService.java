package sk.tuke.gamestudio.service.entity.playerADDons;

import sk.tuke.gamestudio.entity.playerADDons.PlayerSettings;

public interface PlayerSettingsService {
    PlayerSettings getSettings(String username);
    void saveSettings(PlayerSettings settings);
    void deleteSettings(String username);
}