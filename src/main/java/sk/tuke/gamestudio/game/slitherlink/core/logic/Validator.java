package sk.tuke.gamestudio.game.slitherlink.core.logic;

import sk.tuke.gamestudio.game.slitherlink.core.bricks.*;

import java.util.*;

public class Validator {
    public boolean validate(Field f) {
        return checkPointRules(f) && checkClues(f) && checkLoop(f);
    }

    public boolean checkPointRules(Field f) {
        for (int r = 0; r <= f.getRows(); r++) {
            for (int c = 0; c <= f.getCols(); c++) {
                final Point p = f.getPoint(r, c);
                int count = 0;
                for (final Edge e : f.getAllEdges()) {
                    if (e.isActive()) {
                        final Point[] pts = e.getPoints();
                        if (pts[0].equals(p) || pts[1].equals(p)) {
                            count++;
                        }
                    }
                }
                if (count != 0 && count != 2) {
                    return false;
                }
            }
        }
        return true;
    }

    //toto bude BFS ?
    public boolean checkLoop(Field f) {
        final List<Edge> active = new ArrayList<>();
        for (final Edge e : f.getAllEdges()) {
            if (e.isActive()) active.add(e);
        }
        if (active.isEmpty()) return false;

        // BFS od prvého bodu
        final Set<Point> visited = new HashSet<>();
        final Queue<Point> queue = new LinkedList<>();
        final Point start = active.get(0).getPoints()[0];
        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            final Point current = queue.poll();
            for (final Edge e : active) {
                final Point[] pts = e.getPoints();
                if (pts[0].equals(current) && !visited.contains(pts[1])) {
                    visited.add(pts[1]);
                    queue.add(pts[1]);
                } else if (pts[1].equals(current) && !visited.contains(pts[0])) {
                    visited.add(pts[0]);
                    queue.add(pts[0]);
                }
            }
        }

        // vsetky body aktivnych hran musia byt navstivene
        for (final Edge e : active) {
            if (!visited.contains(e.getPoints()[0])) return false;
            if (!visited.contains(e.getPoints()[1])) return false;
        }
        return true;
    }

    public boolean checkClues(Field f) {
        for (int r = 0; r < f.getRows(); r++) {
            for (int c = 0; c < f.getCols(); c++) {
                final Tile t = f.getTile(r, c);
                if (t instanceof Clue) {
                    if (!((Clue) t).isSatisfied()) return false;
                }
            }
        }
        return true;
    }
}