package sk.tuke.gamestudio.service.jpa;

import org.springframework.stereotype.Component;
import sk.tuke.gamestudio.entity.Score;
import sk.tuke.gamestudio.service.entity.ScoreException;
import sk.tuke.gamestudio.service.entity.ScoreService;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;

@Component
@Transactional
public class ScoreServiceJPA implements ScoreService {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void addScore(Score score) throws ScoreException {
        entityManager.persist(score);
    }

    @Override
    public List<Score> getTopScores(String game) throws ScoreException {
        return entityManager.createNamedQuery("Score.getTopScores").setParameter("game", game).setMaxResults(10).getResultList();
    }

    @Override
    public void reset() throws ScoreException {
        entityManager.createNamedQuery("Score.reset").executeUpdate();
    }

    @Override
    public List<Score> getTopScoresByLevel(String game, int level) {
        return entityManager.createNamedQuery("Score.getTopScoresByLevel", Score.class).setParameter("game", game).setParameter("level", level).setMaxResults(10).getResultList();
    }

    @Override
    public List<Score> getScoresByPlayer(String game, String player, int level) {
        return entityManager.createNamedQuery("Score.getScoresByPlayer", Score.class).setParameter("game", game).setParameter("player", player).setParameter("level", level).getResultList();
    }

    @Override
    public void deleteScore(String game, String player, int ident) {
        entityManager.createNamedQuery("Score.deleteByIdent").setParameter("ident", ident).setParameter("player", player).executeUpdate();
    }
}