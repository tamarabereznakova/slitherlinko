package sk.tuke.gamestudio.service.jpa;

import org.springframework.stereotype.Component;
import sk.tuke.gamestudio.entity.playerADDons.Notification;
import sk.tuke.gamestudio.service.entity.playerADDons.NotificationService;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;

@Component
@Transactional
public class NotificationServiceJPA implements NotificationService {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void create(Notification notification) {
        if (notification.getRecipient() == null) return;
        if (notification.getRecipient().equals(notification.getActor()) && !"ACHIEVEMENT".equals(notification.getType())) return;
        entityManager.persist(notification);
    }

    @Override
    public List<Notification> getUnseen(String recipient) {
        return entityManager.createNamedQuery("Notification.findUnseenByRecipient", Notification.class).setParameter("recipient", recipient).getResultList();
    }

    @Override
    public List<Notification> getAll(String recipient) {
        return entityManager
                .createNamedQuery("Notification.findByRecipient", Notification.class)
                .setParameter("recipient", recipient)
                .getResultList();
    }

    @Override
    public void markAllSeen(String recipient) {
        entityManager.createNamedQuery("Notification.markAllSeen").setParameter("recipient", recipient).executeUpdate();
    }

    @Override
    public void deleteByPlayer(String player) {
        entityManager.createNamedQuery("Notification.deleteByPlayer").setParameter("player", player).executeUpdate();
    }
}