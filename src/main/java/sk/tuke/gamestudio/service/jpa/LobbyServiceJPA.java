package sk.tuke.gamestudio.service.jpa;

import org.springframework.stereotype.Component;
import sk.tuke.gamestudio.entity.playerADDons.multiplayer.Lobby;
import sk.tuke.gamestudio.entity.playerADDons.multiplayer.LobbyMember;
import sk.tuke.gamestudio.entity.playerADDons.multiplayer.LobbyMemberId;
import sk.tuke.gamestudio.service.entity.playerADDons.LobbyService;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@Transactional
public class LobbyServiceJPA implements LobbyService {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public String createLobby(String creator, String mode, int level, int mapIndex) {
        final String token = UUID.randomUUID().toString();
        final Lobby lobby = new Lobby(token, creator, mode, level, mapIndex);
        entityManager.persist(lobby);
        // Tvorca je automaticky ACCEPTED
        entityManager.persist(new LobbyMember(token, creator, "ACCEPTED"));
        return token;
    }

    @Override
    public Lobby findByToken(String token) {
        if (token == null) return null;
        final List<Lobby> result = entityManager
                .createNamedQuery("Lobby.findByToken", Lobby.class)
                .setParameter("token", token)
                .getResultList();
        return result.isEmpty() ? null : result.get(0);
    }

    @Override
    public void inviteMember(String token, String username) {
        // Skontroluj ci uz nie je v lobby
        final LobbyMember existing = entityManager.find(LobbyMember.class,
                new LobbyMemberId(token, username));
        if (existing != null) return;
        entityManager.persist(new LobbyMember(token, username, "INVITED"));
    }

    @Override
    public void acceptInvite(String token, String username) {
        final LobbyMember m = entityManager.find(LobbyMember.class,
                new LobbyMemberId(token, username));
        if (m != null) m.setStatus("ACCEPTED");
    }

    @Override
    public void declineInvite(String token, String username) {
        final LobbyMember m = entityManager.find(LobbyMember.class,
                new LobbyMemberId(token, username));
        if (m != null) m.setStatus("DECLINED");
    }

    @Override
    public void leaveLobby(String token, String username) {
        final LobbyMember m = entityManager.find(LobbyMember.class,
                new LobbyMemberId(token, username));
        if (m != null) entityManager.remove(m);
    }

    @Override
    public List<LobbyMember> getMembers(String token) {
        return entityManager.createNamedQuery("LobbyMember.findByLobby", LobbyMember.class).setParameter("token", token).getResultList();
    }

    @Override
    public void setState(String token, String state) {
        final Lobby lobby = findByToken(token);
        if (lobby != null) lobby.setState(state);
    }

    @Override
    public void deleteLobby(String token) {
        entityManager.createNamedQuery("LobbyMember.deleteByLobby").setParameter("token", token).executeUpdate();
        entityManager.createNamedQuery("Lobby.deleteByToken").setParameter("token", token).executeUpdate();
    }

    @Override
    public List<Lobby> getActiveLobbiesForUser(String username) {
        final List<LobbyMember> members = entityManager.createNamedQuery("LobbyMember.findByPlayer", LobbyMember.class).setParameter("username", username).getResultList();
        final List<Lobby> result = new ArrayList<>();
        for (final LobbyMember m : members) {
            if ("ACCEPTED".equals(m.getStatus())) {
                final Lobby l = findByToken(m.getLobbyToken());
                if (l != null && !"FINISHED".equals(l.getState())) {
                    result.add(l);
                }
            }
        }
        return result;
    }

    @Override
    public int cleanupExpired() {
        final long cutoff = System.currentTimeMillis() - (24L * 60L * 60L * 1000L);
        final List<Lobby> all = entityManager.createQuery("SELECT l FROM Lobby l", Lobby.class).getResultList();
        int deleted = 0;
        for (final Lobby l : all) {
            if (l.getCreatedOn() != null && l.getCreatedOn().getTime() < cutoff) {
                deleteLobby(l.getToken());
                deleted++;
            }
        }
        return deleted;
    }

    @Override
    public void deleteByPlayer(String player) {
        final List<Lobby> created = entityManager.createNamedQuery("Lobby.findByCreator", Lobby.class).setParameter("creator", player).getResultList();
        for (final Lobby l : created) {
            deleteLobby(l.getToken());
        }
        entityManager.createNamedQuery("LobbyMember.deleteByPlayer").setParameter("username", player).executeUpdate();
    }
}