package sk.tuke.gamestudio.service.jpa;

import org.springframework.stereotype.Component;
import sk.tuke.gamestudio.entity.playerADDons.Spectate;
import sk.tuke.gamestudio.service.entity.playerADDons.SpectateService;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;

@Component
@Transactional
public class SpectateServiceJPA implements SpectateService {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public String createInvite(String fromPlayer, String toPlayer, int level, int mapIndex) {
        final String token = UUID.randomUUID().toString();
        entityManager.persist(new Spectate(token, fromPlayer, toPlayer, level, mapIndex));
        return token;
    }

    @Override
    public Spectate findByToken(String token) {
        final List<Spectate> result = entityManager.createNamedQuery("SpectateInvite.findByToken", Spectate.class).setParameter("token", token).getResultList();
        return result.isEmpty() ? null : result.get(0);
    }

    @Override
    public void deleteByToken(String token) {
        entityManager.createNamedQuery("SpectateInvite.deleteByToken").setParameter("token", token).executeUpdate();
    }

    @Override
    public void deleteByPlayer(String fromPlayer) {
        entityManager.createNamedQuery("SpectateInvite.deleteByPlayer").setParameter("fromPlayer", fromPlayer).executeUpdate();
    }

    @Override
    public void deleteByPlayerAndLevel(String fromPlayer, int level) {
        entityManager.createQuery("DELETE FROM Spectate s WHERE s.fromPlayer = :fromPlayer AND s.level = :level").setParameter("fromPlayer", fromPlayer).setParameter("level", level).executeUpdate();
    }

    @Override
    public boolean hasActiveStream(String fromPlayer, int level) {
        final Long count = entityManager.createQuery("SELECT COUNT(s) FROM Spectate s WHERE s.fromPlayer = :fromPlayer AND s.level = :level", Long.class).setParameter("fromPlayer", fromPlayer).setParameter("level", level).getSingleResult();
        return count > 0;
    }
}