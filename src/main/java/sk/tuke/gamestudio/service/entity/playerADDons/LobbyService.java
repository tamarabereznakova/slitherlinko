package sk.tuke.gamestudio.service.entity.playerADDons;

import sk.tuke.gamestudio.entity.playerADDons.multiplayer.Lobby;
import sk.tuke.gamestudio.entity.playerADDons.multiplayer.LobbyMember;

import java.util.List;

public interface LobbyService {
    String createLobby(String creator, String mode, int level, int mapIndex);
    Lobby findByToken(String token);
    void inviteMember(String token, String username);
    void acceptInvite(String token, String username);
    void declineInvite(String token, String username);
    void leaveLobby(String token, String username);
    List<LobbyMember> getMembers(String token);
    void setState(String token, String state);
    void deleteLobby(String token);
    List<Lobby> getActiveLobbiesForUser(String username);
    int cleanupExpired();
    void deleteByPlayer(String player);
}