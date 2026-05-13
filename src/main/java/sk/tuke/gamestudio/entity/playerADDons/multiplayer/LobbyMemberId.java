package sk.tuke.gamestudio.entity.playerADDons.multiplayer;

import java.io.Serializable;
import java.util.Objects;

public class LobbyMemberId implements Serializable {
    private String lobbyToken;
    private String username;

    public LobbyMemberId() {
    }

    public LobbyMemberId(String lobbyToken, String username) {
        this.lobbyToken = lobbyToken;
        this.username = username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LobbyMemberId)) return false;
        final LobbyMemberId that = (LobbyMemberId) o;
        return Objects.equals(lobbyToken, that.lobbyToken)
                && Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lobbyToken, username);
    }
}