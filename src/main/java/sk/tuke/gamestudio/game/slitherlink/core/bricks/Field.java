package sk.tuke.gamestudio.game.slitherlink.core.bricks;

import sk.tuke.gamestudio.game.slitherlink.core.logic.Validator;

import java.util.ArrayList;
import java.util.List;

public class Field {
    private int rows;
    private int cols;
    private FieldState state;
    private Tile[][] tiles;
    private Point[][] points;
    private List<Edge> edges;
    private Validator validator;

    public Field(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.state = FieldState.PLAYING;
        this.tiles = new Tile[rows][cols];
        this.points = new Point[rows + 1][cols + 1];  // bodov je vzdy o 1 viac
        this.edges = new ArrayList<>();
        this.validator = new Validator();
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public FieldState getState() {
        return state;
    }

    public void setState(FieldState s) {
        this.state = s;
    }

    public Tile getTile(int r, int c) {
        return tiles[r][c];
    }

    public void setTile(int r, int c, Tile t) {
        tiles[r][c] = t;
    }

    public Point getPoint(int r, int c) {
        return points[r][c];
    }

    public void setPoint(int r, int c, Point p) {
        points[r][c] = p;
    }

    public List<Edge> getAllEdges() {
        return edges;
    }

    public void addEdge(Edge e) {
        edges.add(e);
    }

    public Edge getEdgeAt(Point p1, Point p2) {
        for (final Edge e : edges) {
            final Point[] pts = e.getPoints();
            if ((pts[0].equals(p1) && pts[1].equals(p2)) ||
                    (pts[0].equals(p2) && pts[1].equals(p1))) {
                return e;
            }
        }
        return null;
    }

    public void checkWin() {
        if (validator.validate(this)) {
            this.state = FieldState.SOLVED;
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (tiles[r][c] instanceof Clue) {
                    sb.append(((Clue) tiles[r][c]).getValue());
                } else {
                    sb.append(".");
                }
                sb.append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public void play() {
        System.out.println(this);
    }

    public void printDebug(int r, int c) {
        final Tile t = getTile(r, c);
        System.out.println("=== TILE [" + r + "," + c + "] ===");
        System.out.println("Typ: " + (t instanceof Clue ? "Clue(" + ((Clue) t).getValue() + ")" : "Empty"));

        final Edge[] edges = t.getEdges();
        final String[] names = {"TOP", "RIGHT", "BOTTOM", "LEFT"};
        for (int i = 0; i < 4; i++) {
            System.out.println(names[i] + ": " + (edges[i] != null ? edges[i].getState() : "NULL!"));
        }

        System.out.println("Aktivne hrany: " + t.activeEdgeCount());

        // susedia
        System.out.println("--- SUSEDIA ---");
        if (r > 0) System.out.println("HORE:  " + tileInfo(getTile(r - 1, c)));
        if (r < rows - 1) System.out.println("DOLE:  " + tileInfo(getTile(r + 1, c)));
        if (c > 0) System.out.println("LAVO:  " + tileInfo(getTile(r, c - 1)));
        if (c < cols - 1) System.out.println("PRAVO: " + tileInfo(getTile(r, c + 1)));
    }

    private String tileInfo(Tile t) {
        return t instanceof Clue ? "Clue(" + ((Clue) t).getValue() + ")" : "Empty";
    }
}
