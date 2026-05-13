package sk.tuke.gamestudio.service.jpa;

import org.springframework.stereotype.Component;
import sk.tuke.gamestudio.entity.playerADDons.multiplayer.MultiplayerScore;
import sk.tuke.gamestudio.service.entity.playerADDons.MultiplayerScoreService;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;

@Component
@Transactional
public class MultiplayerScoreServiceJPA implements MultiplayerScoreService {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void addScore(MultiplayerScore score) {
        entityManager.persist(score);
    }

    @Override
    public List<MultiplayerScore> getScoresForPlayer(String username) {
        return entityManager.createNamedQuery("MultiplayerScore.findByPlayer", MultiplayerScore.class).setParameter("pattern", "%" + username + "%").getResultList().stream()
                .filter(m -> {
                    final String[] names = m.getPlayers().split(",");
                    for (final String n : names) {
                        if (n.trim().equals(username)) return true;
                    }
                    return false;
                })
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public void deleteByPlayer(String username) {
        final List<MultiplayerScore> toRemove = entityManager.createNamedQuery("MultiplayerScore.findByPlayer", MultiplayerScore.class).setParameter("pattern", "%" + username + "%").getResultList().stream()
                .filter(m -> {
                    final String[] names = m.getPlayers().split(",");
                    for (final String n : names) {
                        if (n.trim().equals(username)) return true;
                    }
                    return false;
                })
                .collect(java.util.stream.Collectors.toList());
        for (final MultiplayerScore m : toRemove) {
            entityManager.remove(m);
        }
    }
}