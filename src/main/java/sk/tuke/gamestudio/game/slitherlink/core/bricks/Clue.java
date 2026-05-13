package sk.tuke.gamestudio.game.slitherlink.core.bricks;

public class Clue extends Tile {
    private int value; //0/1/2/3/4 nemoze uz byt

    public Clue(int r, int c, int value) {
        super(r, c);
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public boolean isSatisfied() {
        return activeEdgeCount() == value;
    }
}
