package sk.tuke.gamestudio.game.slitherlink.core.logic;

import sk.tuke.gamestudio.game.slitherlink.core.bricks.Edge;
import sk.tuke.gamestudio.game.slitherlink.core.bricks.EdgeState;
import sk.tuke.gamestudio.game.slitherlink.core.bricks.Field;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

public class UndoRedo {

    private static final int MAX_UNDO = 30;
    private final Deque<EdgeState> undoBuffer = new ArrayDeque<>();
    private final Deque<int[]> undoMoves = new ArrayDeque<>();
    private final Deque<EdgeState> redoBuffer = new ArrayDeque<>();
    private final Deque<int[]> redoMoves = new ArrayDeque<>();

    public void saveToBuffer(int row, int col, char side, EdgeState stateBefore) {
        if (undoBuffer.size() >= MAX_UNDO) {
            undoBuffer.pollFirst();
            undoMoves.pollFirst();
        }
        undoBuffer.addLast(stateBefore);
        undoMoves.addLast(new int[]{row, col, (int) side});
    }

    public void clearRedo() {
        redoBuffer.clear();
        redoMoves.clear();
    }

    public boolean undo(Field field, Play play) {
        if (undoBuffer.isEmpty()) return false;
        final EdgeState prevState = undoBuffer.pollLast();
        final int[] move = undoMoves.pollLast();
        final char side = (char) move[2];
        final Edge edge = play.findEdge(move[0], move[1], side);
        if (edge != null) {
            if (redoBuffer.size() >= MAX_UNDO) {
                redoBuffer.pollFirst();
                redoMoves.pollFirst();
            }
            redoBuffer.addLast(edge.getState());
            redoMoves.addLast(new int[]{move[0], move[1], (int) side});
            edge.setState(prevState);
            field.checkWin();
        }
        return true;
    }

    public boolean redo(Field field, Play play) {
        if (redoBuffer.isEmpty()) return false;
        final EdgeState nextState = redoBuffer.pollLast();
        final int[] move = redoMoves.pollLast();
        final char side = (char) move[2];
        final Edge edge = play.findEdge(move[0], move[1], side);
        if (edge != null) {
            saveToBuffer(move[0], move[1], side, edge.getState());
            edge.setState(nextState);
            field.checkWin();
        }
        return true;
    }

    public String serializeUndo() {
        return serialize(undoBuffer, undoMoves);
    }

    public String serializeRedo() {
        return serialize(redoBuffer, redoMoves);
    }

    private String serialize(Deque<EdgeState> states, Deque<int[]> moves) {
        final StringBuilder sb = new StringBuilder("[");
        final Iterator<EdgeState> stateIt = states.iterator();
        final Iterator<int[]> moveIt = moves.iterator();
        while (stateIt.hasNext()) {
            final int[] m = moveIt.next();
            final EdgeState s = stateIt.next();
            sb.append("{\"r\":").append(m[0])
                    .append(",\"c\":").append(m[1])
                    .append(",\"s\":").append(m[2])
                    .append(",\"state\":\"").append(s.name()).append("\"},");
        }
        if (sb.charAt(sb.length() - 1) == ',') sb.deleteCharAt(sb.length() - 1);
        sb.append("]");
        return sb.toString();
    }

    public void deserializeUndo(String json) {
        deserialize(json, undoBuffer, undoMoves);
    }

    public void deserializeRedo(String json) {
        deserialize(json, redoBuffer, redoMoves);
    }

    private void deserialize(String json, Deque<EdgeState> states, Deque<int[]> moves) {
        states.clear();
        moves.clear();
        if (json == null || json.equals("[]")) return;
        json = json.substring(1, json.length() - 1);
        for (String entry : json.split("\\},\\{")) {
            entry = entry.replace("{", "").replace("}", "");
            int r = 0, c = 0, s = 0;
            EdgeState state = EdgeState.INACTIVE;
            for (final String part : entry.split(",")) {
                final String[] kv = part.split(":");
                final String key = kv[0].replace("\"", "");
                final String val = kv[1].replace("\"", "");
                switch (key) {
                    case "r":
                        r = Integer.parseInt(val);
                        break;
                    case "c":
                        c = Integer.parseInt(val);
                        break;
                    case "s":
                        s = Integer.parseInt(val);
                        break;
                    case "state":
                        state = EdgeState.valueOf(val);
                        break;
                }
            }
            moves.addLast(new int[]{r, c, s});
            states.addLast(state);
        }
    }
}