package sk.tuke.gamestudio.game.slitherlink.core.bricks;

public class Edge {
    private EdgeState state;
    private Point[] points; //[2]
    private Tile[] tiles; //[1..2]

    public Edge(Point p1, Point p2) {
        this.points = new Point[]{p1, p2};
        this.tiles = new Tile[2];
        this.state = EdgeState.INACTIVE;
    }

    public EdgeState getState() {
        return state;
    }

    public void setState(EdgeState state) {
        this.state = state;
    }

    public Point[] getPoints() {
        return points;
    }

    public Tile[] getTiles() {
        return tiles;
    }

    public void cycleState() {
        switch (state) {
            case INACTIVE:
                state = EdgeState.ACTIVE;
                break;
            case ACTIVE:
                state = EdgeState.MARKED_X;
                break;
            case MARKED_X:
                state = EdgeState.INACTIVE;
                break;
        }
    }

    public boolean isActive() {
        return state == EdgeState.ACTIVE;
    }

    public Point getOtherPoint(Point p) {
        if (points[0].equals(p)) {
            return points[1];
        }
        if (points[1].equals(p)) {
            return points[0];
        }
        return null;
    }
}
