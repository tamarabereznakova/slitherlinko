package sk.tuke.gamestudio.service.jpa;

import org.springframework.stereotype.Component;
import sk.tuke.gamestudio.entity.Rating;
import sk.tuke.gamestudio.service.entity.RatingException;
import sk.tuke.gamestudio.service.entity.RatingService;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

@Component
@Transactional
public class RatingServiceJPA implements RatingService {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void addRating(Rating rating) throws RatingException {
        final var existing = entityManager.createNamedQuery("Rating.getRating").setParameter("game", rating.getGame()).setParameter("player", rating.getPlayer()).getResultList();
        if (!existing.isEmpty()) {
            entityManager.remove(existing.get(0));
            entityManager.flush();
        }
        entityManager.persist(rating);
    }

    @Override
    public int getRating(String game, String player) throws RatingException {
        final var result = entityManager.createNamedQuery("Rating.getRating").setParameter("game", game).setParameter("player", player).getResultList();
        if (result.isEmpty()) return 0;
        return ((Rating) result.get(0)).getRating();
    }

    @Override
    public int getAverageRating(String game) throws RatingException {
        final var result = entityManager.createNamedQuery("Rating.getAverageRating").setParameter("game", game).getSingleResult();
        if (result == null) return 0;
        return ((Double) result).intValue();
    }

    @Override
    public void reset() throws RatingException {
        entityManager.createNamedQuery("Rating.reset").executeUpdate();
    }

    @Override
    public void deleteRating(String game, String player) {
        entityManager.createQuery("DELETE FROM Rating r WHERE r.game = :game AND r.player = :player").setParameter("game", game).setParameter("player", player).executeUpdate();
    }
}