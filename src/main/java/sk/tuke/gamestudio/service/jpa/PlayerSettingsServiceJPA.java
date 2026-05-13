package sk.tuke.gamestudio.service.jpa;

import org.springframework.stereotype.Component;
import sk.tuke.gamestudio.entity.playerADDons.PlayerSettings;
import sk.tuke.gamestudio.service.entity.playerADDons.PlayerSettingsService;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;

@Component
@Transactional
public class PlayerSettingsServiceJPA implements PlayerSettingsService {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public PlayerSettings getSettings(final String username) {
        final List<PlayerSettings> result = entityManager .createQuery("SELECT s FROM PlayerSettings s WHERE s.username = :username", PlayerSettings.class).setParameter("username", username).getResultList();
        return result.isEmpty() ? new PlayerSettings(username) : result.get(0);
    }

    @Override
    public void saveSettings(final PlayerSettings settings) {
        final PlayerSettings existing = entityManager.find(PlayerSettings.class, settings.getUsername());
        if (existing != null) {
            existing.setPreferredTheme(settings.getPreferredTheme());
            existing.setPreferredLevel(settings.getPreferredLevel());
            existing.setMoveLimit(settings.getMoveLimit());
            existing.setTimeLimit(settings.getTimeLimit());
            existing.setShowMultiplayerScores(settings.isShowMultiplayerScores());
        } else {
            entityManager.persist(settings);
        }
    }

    @Override
    public void deleteSettings(final String username) {
        final PlayerSettings existing = entityManager.find(PlayerSettings.class, username);
        if (existing != null) entityManager.remove(existing);
    }
}