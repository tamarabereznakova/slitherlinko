package sk.tuke.gamestudio.service.jpa;

import org.springframework.stereotype.Component;
import sk.tuke.gamestudio.entity.playerADDons.Follow;
import sk.tuke.gamestudio.entity.playerADDons.FollowId;
import sk.tuke.gamestudio.service.entity.playerADDons.FollowService;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;

@Component
@Transactional
public class FollowServiceJPA implements FollowService {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void follow(String follower, String following) {
        if (follower == null || following == null) return;
        if (follower.equals(following)) return;
        if (isFollowing(follower, following)) return;
        entityManager.persist(new Follow(follower, following));
    }

    @Override
    public void unfollow(String follower, String following) {
        final Follow existing = entityManager.find(Follow.class, new FollowId(follower, following));
        if (existing != null) {
            entityManager.remove(existing);
        }
    }

    @Override
    public boolean isFollowing(String follower, String following) {
        final Long count = (Long) entityManager.createNamedQuery("Follow.exists").setParameter("follower", follower).setParameter("following", following).getSingleResult();
        return count > 0;
    }

    @Override
    public List<Follow> getFollowing(String follower) {
        return entityManager.createNamedQuery("Follow.findFollowing", Follow.class).setParameter("follower", follower).getResultList();
    }

    @Override
    public List<Follow> getFollowers(String following) {
        return entityManager.createNamedQuery("Follow.findFollowers", Follow.class).setParameter("following", following).getResultList();
    }

    @Override
    public long countFollowing(String follower) {
        final Object result = entityManager.createNamedQuery("Follow.countFollowing").setParameter("follower", follower).getSingleResult();
        return result == null ? 0L : (Long) result;
    }

    @Override
    public long countFollowers(String following) {
        final Object result = entityManager.createNamedQuery("Follow.countFollowers").setParameter("following", following).getSingleResult();
        return result == null ? 0L : (Long) result;
    }

    @Override
    public void deleteByPlayer(String player) {
        entityManager.createNamedQuery("Follow.deleteByPlayer").setParameter("player", player).executeUpdate();
    }
}