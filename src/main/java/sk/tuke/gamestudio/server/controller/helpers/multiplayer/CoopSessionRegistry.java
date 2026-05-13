package sk.tuke.gamestudio.server.controller.helpers.multiplayer;

import org.springframework.stereotype.Component;
import sk.tuke.gamestudio.game.slitherlink.core.bricks.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import sk.tuke.gamestudio.game.slitherlink.core.logic.Play;

@Component
public class CoopSessionRegistry {

    public static class CoopGame {
        public final String lobbyToken;
        public final String creator;
        public final int level;
        public final int mapIndex;
        public final Set<String> players;
        public final long startTime;
        public Field field;
        public Play play;
        public boolean finished = false;
        public long finishedAt = 0;
        public final Map<String, Long> lastSeen = new ConcurrentHashMap<>();

        public CoopGame(String lobbyToken, String creator, int level, int mapIndex, Set<String> players, Field field, Play play) {
            this.lobbyToken = lobbyToken;
            this.creator = creator;
            this.level = level;
            this.mapIndex = mapIndex;
            this.players = new HashSet<>(players);
            this.startTime = System.currentTimeMillis();
            this.field = field;
            this.play = play;
        }

        public int getDuration() {
            return (int)((System.currentTimeMillis() - startTime) / 1000);
        }

        public boolean isCreatorOnline() {
            final Long t = lastSeen.get(creator);
            if (t == null) return false;
            return (System.currentTimeMillis() - t) < 3_000;
        }
    }

    private final Map<String, CoopGame> games = new ConcurrentHashMap<>();

    public void start(CoopGame game) {
        games.put(game.lobbyToken, game);
    }

    public CoopGame get(String lobbyToken) {
        return games.get(lobbyToken);
    }

    public void remove(String lobbyToken) {
        games.remove(lobbyToken);
    }

    public void touch(String lobbyToken, String username) {
        final CoopGame g = games.get(lobbyToken);
        if (g != null) {
            g.lastSeen.put(username, System.currentTimeMillis());
        }
    }

    public void removePlayerFromAll(String username) {
        for (final CoopGame g : games.values()) {
            g.players.remove(username);
            g.lastSeen.remove(username);
            if (username.equals(g.creator)) {
                games.remove(g.lobbyToken);
            }
        }
    }
}