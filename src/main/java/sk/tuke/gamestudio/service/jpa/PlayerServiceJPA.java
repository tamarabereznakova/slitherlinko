package sk.tuke.gamestudio.service.jpa;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import sk.tuke.gamestudio.entity.Player;
import sk.tuke.gamestudio.service.entity.PlayerService;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

@Component
@Transactional
public class PlayerServiceJPA implements PlayerService {

    @PersistenceContext
    private EntityManager entityManager;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public boolean register(String username, String password) {
        if (exists(username)) return false;
        final String hash = encoder.encode(password);
        entityManager.persist(new Player(username, hash));
        return true;
    }

    @Override
    public boolean login(String username, String password) {
        final List<Player> result = entityManager.createNamedQuery("Player.findByUsername", Player.class).setParameter("username", username).getResultList();
        if (result.isEmpty()) return false;
        return encoder.matches(password, result.get(0).getPasswordHash());
    }

    @Override
    public boolean exists(String username) {
        return !entityManager.createNamedQuery("Player.findByUsername", Player.class).setParameter("username", username).getResultList().isEmpty();
    }

    @Override
    public void changePassword(String username, String newPassword) {
        final List<Player> result = entityManager.createNamedQuery("Player.findByUsername", Player.class).setParameter("username", username).getResultList();
        if (!result.isEmpty()) {
            result.get(0).setPasswordHash(encoder.encode(newPassword));
        }
    }

    @Override
    public Date getCreatedOn(String username) {
        final List<Player> result = entityManager.createNamedQuery("Player.findByUsername", Player.class).setParameter("username", username).getResultList();
        if (result.isEmpty()) return null;
        return result.get(0).getCreatedOn();
    }

    @Override
    public void deletePlayer(String username) {
        final List<Player> result = entityManager.createNamedQuery("Player.findByUsername", Player.class).setParameter("username", username).getResultList();
        if (!result.isEmpty()) {
            entityManager.remove(result.get(0));
        }
    }

    @Override
    public void touchLastSeen(String username) {
        final List<Player> result = entityManager.createNamedQuery("Player.findByUsername", Player.class).setParameter("username", username).getResultList();
        if (!result.isEmpty()) {
            result.get(0).setLastSeen(new Date());
        }
    }

    @Override
    public boolean isOnline(String username) {
        final List<Player> result = entityManager.createNamedQuery("Player.findByUsername", Player.class).setParameter("username", username).getResultList();
        if (result.isEmpty()) return false;
        final Date lastSeen = result.get(0).getLastSeen();
        if (lastSeen == null) return false;
        return (System.currentTimeMillis() - lastSeen.getTime()) < 10_000;
    }
}