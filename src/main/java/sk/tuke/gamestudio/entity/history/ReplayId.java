package sk.tuke.gamestudio.entity.history;

import java.io.Serializable;
import java.util.Objects;

public class ReplayId implements Serializable {
    private int scoreIdent;
    private int moveOrder;

    public ReplayId() {
    }

    public ReplayId(int scoreIdent, int moveOrder) {
        this.scoreIdent = scoreIdent;
        this.moveOrder = moveOrder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReplayId)) return false;
        final ReplayId that = (ReplayId) o;
        return scoreIdent == that.scoreIdent && moveOrder == that.moveOrder;
    }

    @Override
    public int hashCode() {
        return Objects.hash(scoreIdent, moveOrder);
    }
}