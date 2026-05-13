package sk.tuke.gamestudio.entity.history;

import java.io.Serializable;
import java.util.Objects;

public class SavedGameId implements Serializable {
    private String player;
    private int level;

    public SavedGameId() {
    }

    public SavedGameId(String player, int level) {
        this.player = player;
        this.level = level;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SavedGameId)) return false;
        final SavedGameId that = (SavedGameId) o;
        return level == that.level && Objects.equals(player, that.player);
    }

    @Override
    public int hashCode() {
        return Objects.hash(player, level);
    }
}