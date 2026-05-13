package sk.tuke.gamestudio.server.controller.helpers.notification;

import sk.tuke.gamestudio.entity.Comment;
import sk.tuke.gamestudio.entity.Score;
import sk.tuke.gamestudio.entity.playerADDons.Follow;
import sk.tuke.gamestudio.entity.playerADDons.multiplayer.MultiplayerScore;
import java.util.ArrayList;
import java.util.List;

public class AchievementCalculator {

    public static class Achievement {
        public final String code;
        public final String emoji;
        public final String name;
        public final String description;
        public final boolean unlocked;

        public Achievement(String code, String emoji, String name, String description, boolean unlocked) {
            this.code = code;
            this.emoji = emoji;
            this.name = name;
            this.description = description;
            this.unlocked = unlocked;
        }

        public String getCode() { return code; }
        public String getEmoji() { return emoji; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public boolean isUnlocked() { return unlocked; }
    }

    public static List<Achievement> calculate(String username, List<Score> soloScores, List<MultiplayerScore> multiplayerScores, List<Follow> following, List<Follow> followers, List<Comment> commentsByUser) {
        final List<Achievement> result = new ArrayList<>();

        final int soloWins = soloScores.size();

        boolean speedDemon = false;
        boolean marathon = false;
        boolean tutorial = false;
        boolean brainiac = false;
        boolean god = false;
        for (final Score s : soloScores) {
            if (s.getLevel() == 1 && s.getDuration() < 30) speedDemon = true;
            if (s.getDuration() > 300) marathon = true;
            if (s.getLevel() == 0) tutorial = true;
            if (s.getLevel() == 3) brainiac = true;
            if (s.getLevel() == 3 && s.getDuration() < 600) god = true;
        }

        int coopGames = 0;
        int coopWins = 0;
        int raceWins = 0;
        for (final MultiplayerScore m : multiplayerScores) {
            if ("COOP".equals(m.getMode())) {
                coopGames++;
                coopWins++;
            } else if ("RACE".equals(m.getMode()) && username.equals(m.getWinner())) {
                raceWins++;
            }
        }

        result.add(new Achievement("first_loop", "🌱", "First Loop", "Win 1 solo game", soloWins >= 1));
        result.add(new Achievement("on_fire", "🔥", "On Fire", "Win 10 solo games", soloWins >= 10));
        result.add(new Achievement("veteran", "💎", "Veteran", "Win 50 solo games", soloWins >= 50));
        result.add(new Achievement("master", "🏆", "Master", "Win 100 solo games", soloWins >= 100));

        result.add(new Achievement("speed_demon", "⚡", "Speed Demon", "Win 5×5 under 30 seconds", speedDemon));
        result.add(new Achievement("marathon", "🐌", "Marathon", "Play a game longer than 5 minutes", marathon));
        result.add(new Achievement("tutorial", "🎓", "Tutorial Complete", "Win on 2×2", tutorial));
        result.add(new Achievement("brainiac", "🧠", "Brainiac", "Win on 10×10", brainiac));
        result.add(new Achievement("god", "🌌", "God", "Win 10×10 under 10 minutes", god));

        result.add(new Achievement("team_player", "👥", "Team Player", "Play 1 co-op game", coopGames >= 1));
        result.add(new Achievement("coop_champion", "🤝", "Co-op Champion", "Win 10 co-op games", coopWins >= 10));
        result.add(new Achievement("first_blood", "🩸", "First Blood", "Win 1 race", raceWins >= 1));
        result.add(new Achievement("race_master", "👑", "Race Master", "Win 10 races", raceWins >= 10));

        result.add(new Achievement("social_butterfly", "🌟", "Social Butterfly", "Follow 5+ players", following.size() >= 5));
        result.add(new Achievement("popular", "💜", "Popular", "Have 5+ followers", followers.size() >= 5));
        result.add(new Achievement("wall_writer", "📝", "Wall Writer", "Write 10+ comments", commentsByUser.size() >= 10));

        return result;
    }

    public static int countUnlocked(List<Achievement> achievements) {
        int c = 0;
        for (final Achievement a : achievements) {
            if (a.unlocked) c++;
        }
        return c;
    }

    public static List<Achievement> findNew(List<Achievement> current, java.util.Set<String> alreadyNotifiedCodes) {
        final List<Achievement> result = new ArrayList<>();
        for (final Achievement a : current) {
            if (a.unlocked && !alreadyNotifiedCodes.contains(a.code)) {
                result.add(a);
            }
        }
        return result;
    }
}