package sk.tuke.gamestudio.service.jpa;

import org.springframework.stereotype.Component;
import sk.tuke.gamestudio.entity.playerADDons.GameInvite;
import sk.tuke.gamestudio.service.entity.playerADDons.GameInviteService;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;

@Component
@Transactional
public class GameInviteServiceJPA implements GameInviteService {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void sendInvite(GameInvite invite) {
        entityManager.persist(invite);
    }

    @Override
    public List<GameInvite> getInbox(String toPlayer) {
        return entityManager.createNamedQuery("GameInvite.findByToPlayer", GameInvite.class).setParameter("toPlayer", toPlayer).getResultList();
    }

    @Override
    public List<GameInvite> getOutbox(String fromPlayer) {
        return entityManager.createNamedQuery("GameInvite.findByFromPlayer", GameInvite.class).setParameter("fromPlayer", fromPlayer).getResultList();
    }

    @Override
    public void markDelivered(int ident) {
        final List<GameInvite> result = entityManager.createNamedQuery("GameInvite.findByIdent", GameInvite.class).setParameter("ident", ident).getResultList();
        if (!result.isEmpty()) {
            result.get(0).setDelivered(true);
        }
    }

    @Override
    public void markAsRead(int ident) {
        final List<GameInvite> result = entityManager.createNamedQuery("GameInvite.findByIdent", GameInvite.class).setParameter("ident", ident).getResultList();
        if (!result.isEmpty()) {
            result.get(0).setRead(true);
        }
    }

    @Override
    public long countUnread(String toPlayer) {
        final Object result = entityManager.createNamedQuery("GameInvite.countUnread").setParameter("toPlayer", toPlayer).getSingleResult();
        return result == null ? 0L : (Long) result;
    }

    @Override
    public void deleteInvite(int ident) {
        entityManager.createNamedQuery("GameInvite.deleteByIdent").setParameter("ident", ident).executeUpdate();
    }

    @Override
    public void deleteInviteForUser(int ident, String username) {
        final List<GameInvite> result = entityManager.createNamedQuery("GameInvite.findByIdent", GameInvite.class).setParameter("ident", ident).getResultList();
        if (result.isEmpty()) return;
        final GameInvite invite = result.get(0);

        if (username.equals(invite.getFromPlayer())) {
            invite.setDeletedByFrom(true);
        }
        if (username.equals(invite.getToPlayer())) {
            invite.setDeletedByTo(true);
        }
        final boolean noRecipient = invite.getToPlayer() == null;
        if ((invite.isDeletedByFrom() && invite.isDeletedByTo()) || (invite.isDeletedByFrom() && noRecipient)) {
            entityManager.remove(invite);
        }
    }

    @Override
    public void deleteByPlayer(String player) {
        entityManager.createNamedQuery("GameInvite.deleteByPlayer").setParameter("player", player).executeUpdate();
    }
}