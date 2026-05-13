package sk.tuke.gamestudio.game.slitherlink.core.bricks;

public abstract class Tile {
    private int row;
    private int col;
    private Point[] corners; //Point[4]
    private Edge[] edges; //Edge[4]

    public Tile(int row, int col) {
        this.row = row;
        this.col = col;
        this.edges = new Edge[4];
        this.corners = new Point[4];
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public Edge[] getEdges() {
        return edges;
    }

    public Point[] getCorners() {
        return corners;
    }

    public int activeEdgeCount() {
        int count = 0;
        for (final Edge e : edges) {
            if (e != null && e.isActive()) {
                count++;
            }
        }
        return count;
    }

    public void setEdge(int i, Edge edge) {
        this.edges[i] = edge;
    }

    public void setCorner(int i, Point p) {
        this.corners[i] = p;
    }
}
