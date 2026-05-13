package sk.tuke.gamestudio.server.controller.helpers.multiplayer;

import org.springframework.stereotype.Component;
import sk.tuke.gamestudio.game.slitherlink.core.bricks.Clue;
import sk.tuke.gamestudio.game.slitherlink.core.bricks.Field;
import sk.tuke.gamestudio.game.slitherlink.core.bricks.FieldState;
import sk.tuke.gamestudio.game.slitherlink.core.bricks.Tile;
import sk.tuke.gamestudio.game.slitherlink.core.logic.Play;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RaceSessionRegistry {

    public static class PlayerRaceState {
        public Field field;
        public Play play;
        public long lastSeen;

        public PlayerRaceState(Field field, Play play) {
            this.field = field;
            this.play = play;
            this.lastSeen = System.currentTimeMillis();
        }
    }

    public static class RaceGame {
        public final String lobbyToken;
        public final String playerA;
        public final String playerB;
        public final int level;
        public final int mapIndex;
        public final long startTime;
        public final Map<String, PlayerRaceState> states = new ConcurrentHashMap<>();
        public boolean finished = false;
        public long finishedAt = 0;
        public String winner = null;

        public RaceGame(String lobbyToken, String playerA, String playerB, int level, int mapIndex, Field fieldA, Play playA, Field fieldB, Play playB) {
            this.lobbyToken = lobbyToken;
            this.playerA = playerA;
            this.playerB = playerB;
            this.level = level;
            this.mapIndex = mapIndex;
            this.startTime = System.currentTimeMillis();
            this.states.put(playerA, new PlayerRaceState(fieldA, playA));
            this.states.put(playerB, new PlayerRaceState(fieldB, playB));
        }

        public int getDuration() {
            return (int)((System.currentTimeMillis() - startTime) / 1000);
        }

        public boolean isPlayerOnline(String username) {
            final PlayerRaceState s = states.get(username);
            if (s == null) return false;
            return (System.currentTimeMillis() - s.lastSeen) < 3_000;
        }

        public int getCorrectProgress(String username) {
            final PlayerRaceState s = states.get(username);
            if (s == null) return 0;
            final Field f = s.field;
            if (f.getState() == FieldState.SOLVED) return 100;
            int totalClues = 0;
            int satisfiedClues = 0;
            for (int r = 0; r < f.getRows(); r++) {
                for (int c = 0; c < f.getCols(); c++) {
                    final Tile t = f.getTile(r, c);
                    if (t instanceof Clue) {
                        totalClues++;
                        if (((Clue) t).isSatisfied()) satisfiedClues++;
                    }
                }
            }
            if (totalClues == 0) return 0;
            final int pct = (int)((satisfiedClues * 100.0) / totalClues);
            return Math.min(pct, 99);
        }
        public String opponentOf(String username) {
            return username.equals(playerA) ? playerB : playerA;
        }
    }

    private final Map<String, RaceGame> games = new ConcurrentHashMap<>();

    public void start(RaceGame game) {
        games.put(game.lobbyToken, game);
    }

    public RaceGame get(String lobbyToken) {
        return games.get(lobbyToken);
    }

    public void remove(String lobbyToken) {
        games.remove(lobbyToken);
    }

    public void touch(String lobbyToken, String username) {
        final RaceGame g = games.get(lobbyToken);
        if (g != null) {
            final PlayerRaceState s = g.states.get(username);
            if (s != null) {
                s.lastSeen = System.currentTimeMillis();
            }
        }
    }

    public void removePlayerFromAll(String username) {
        for (final RaceGame g : new HashMap<>(games).values()) {
            if (username.equals(g.playerA) || username.equals(g.playerB)) {
                games.remove(g.lobbyToken);
            }
        }
    }
}