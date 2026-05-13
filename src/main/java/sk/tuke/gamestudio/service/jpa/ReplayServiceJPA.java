package sk.tuke.gamestudio.service.jpa;

import org.springframework.stereotype.Component;
import sk.tuke.gamestudio.entity.history.Replay;
import sk.tuke.gamestudio.service.entity.history.ReplayService;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;

@Component
@Transactional
public class ReplayServiceJPA implements ReplayService {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void saveReplay(List<Replay> moves) {
        if (moves.isEmpty()) return;
        final int scoreIdent = moves.get(0).getScoreIdent();
        entityManager.createQuery("DELETE FROM Replay r WHERE r.scoreIdent = :ident").setParameter("ident", scoreIdent).executeUpdate();
        entityManager.flush();
        for (final Replay move : moves) {
            entityManager.persist(move);
        }
    }

    @Override
    public List<Replay> getReplay(int scoreIdent) {
        return entityManager.createQuery("SELECT r FROM Replay r WHERE r.scoreIdent = :ident ORDER BY r.moveOrder ASC", Replay.class).setParameter("ident", scoreIdent).getResultList();
    }

    @Override
    public void deleteReplay(int scoreIdent) {
        entityManager.createQuery("DELETE FROM Replay r WHERE r.scoreIdent = :ident").setParameter("ident", scoreIdent).executeUpdate();
    }
}