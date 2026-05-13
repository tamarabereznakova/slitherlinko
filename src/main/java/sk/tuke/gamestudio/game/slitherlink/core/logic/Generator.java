package sk.tuke.gamestudio.game.slitherlink.core.logic;

import sk.tuke.gamestudio.game.slitherlink.core.bricks.*;

import java.util.Random;

public class Generator {
    private int rows;
    private int cols;

    public Generator(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
    }

    public Field generate() {
        final Field field = new Field(rows, cols);
        buildGrid(field);
        linkEdgesAndPoints(field);
        placeClues(field);
        return field;
    }

    private void buildGrid(Field field) {
        for (int r = 0; r <= rows; r++) {
            for (int c = 0; c <= cols; c++) {
                field.setPoint(r, c, new Point(r, c));
            }
        }
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                field.setTile(r, c, new Empty(r, c));
            }
        }
    }

    private void placeClues(Field field) {
        final Random random = new Random();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (random.nextInt(3) != 0) {
                    final int value = random.nextInt(4); // 0..3
                    final Clue clue = new Clue(r, c, value);
                    final Edge[] oldEdges = field.getTile(r, c).getEdges();
                    for (int i = 0; i < 4; i++) {
                        clue.setEdge(i, oldEdges[i]);
                    }
                    field.setTile(r, c, clue);
                }
            }
        }
    }

    private void linkEdgesAndPoints(Field field) {
        linkHorizontalEdges(field);
        linkVerticalEdges(field);
    }

    private void linkHorizontalEdges(Field field) {
        for (int r = 0; r <= rows; r++) {
            for (int c = 0; c < cols; c++) {
                final Point p1 = field.getPoint(r, c);
                final Point p2 = field.getPoint(r, c + 1);
                final Edge edge = new Edge(p1, p2);
                field.addEdge(edge);
                if (r < rows) field.getTile(r, c).setEdge(0, edge);
                if (r > 0) field.getTile(r - 1, c).setEdge(2, edge);
            }
        }
    }

    private void linkVerticalEdges(Field field) {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c <= cols; c++) {
                final Point p1 = field.getPoint(r, c);
                final Point p2 = field.getPoint(r + 1, c);
                final Edge edge = new Edge(p1, p2);
                field.addEdge(edge);
                if (c < cols) field.getTile(r, c).setEdge(3, edge);
                if (c > 0) field.getTile(r, c - 1).setEdge(1, edge);
            }
        }
    }

    //NA MOJE MAPKY - TIE ASPIN NEVYSKAKUJU MIMO POLA TERMINALU
    public Field generateFromMap(int[][] map) {
        this.rows = map.length;
        this.cols = map[0].length;
        final Field field = new Field(rows, cols);
        buildGrid(field);
        linkEdgesAndPoints(field);
        placeCluesFromMap(field, map);
        return field;
    }

    private void placeCluesFromMap(Field field, int[][] map) {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (map[r][c] != -1) {
                    final Clue clue = new Clue(r, c, map[r][c]);
                    final Edge[] oldEdges = field.getTile(r, c).getEdges();
                    for (int i = 0; i < 4; i++) {
                        clue.setEdge(i, oldEdges[i]);
                    }
                    field.setTile(r, c, clue);
                }
            }
        }
    }
}
