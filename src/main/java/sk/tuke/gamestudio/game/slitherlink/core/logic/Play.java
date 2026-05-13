package sk.tuke.gamestudio.game.slitherlink.core.logic;

import sk.tuke.gamestudio.entity.Score;
import sk.tuke.gamestudio.game.slitherlink.core.bricks.*;
import sk.tuke.gamestudio.service.entity.ScoreService;
import java.util.ArrayList;
import java.util.List;

public class Play {
    private final Field field;
    private final String playerName;
    private final ScoreService scoreService;
    private final int level;
    private final int mapIndex;
    private final UndoRedo undoRedo = new UndoRedo();
    private final List<int[]> moveHistory = new ArrayList<>();

    private int moves = 0;
    private long startTime;
    private boolean started = false;

    public Play(Field field, String playerName, ScoreService scoreService, int level, int mapIndex) {
        this.field = field;
        this.playerName = playerName;
        this.scoreService = scoreService;
        this.level = level;
        this.mapIndex = mapIndex;
        this.startTime = System.currentTimeMillis();
    }

    public int getMoves() {
        return moves;
    }

    public void setMoves(int moves) {
        this.moves = moves;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public List<int[]> getMoveHistory() {
        return moveHistory;
    }

    public String serializeUndoBuffer() {
        return undoRedo.serializeUndo();
    }

    public String serializeRedoBuffer() {
        return undoRedo.serializeRedo();
    }

    public void deserializeUndoBuffer(String json) {
        undoRedo.deserializeUndo(json);
    }

    public void deserializeRedoBuffer(String json) {
        undoRedo.deserializeRedo(json);
    }

    public boolean isStarted() {
        return started;
    }

    public void handleClick(int row, int col, char side) {
        if (!started) {
            started = true;
            startTime = System.currentTimeMillis();
        }
        if (row < 0 || row >= field.getRows() || col < 0 || col >= field.getCols()) return;
        final Edge edge = findEdge(row, col, side);
        if (edge != null) {
            undoRedo.saveToBuffer(row, col, side, edge.getState());
            undoRedo.clearRedo();
            edge.cycleState();
            moves++;
            moveHistory.add(new int[]{row, col, (int) side});
            afterMove();
        }
    }

    public Edge findEdge(int row, int col, char side) {
        Point p1, p2;
        switch (side) {
            case 'T':
                p1 = field.getPoint(row, col);
                p2 = field.getPoint(row, col + 1);
                break;
            case 'B':
                p1 = field.getPoint(row + 1, col);
                p2 = field.getPoint(row + 1, col + 1);
                break;
            case 'L':
                p1 = field.getPoint(row, col);
                p2 = field.getPoint(row + 1, col);
                break;
            case 'R':
                p1 = field.getPoint(row, col + 1);
                p2 = field.getPoint(row + 1, col + 1);
                break;
            default:
                return null;
        }
        return field.getEdgeAt(p1, p2);
    }

    public void afterMove() {
        field.checkWin();
        if (field.getState() == FieldState.SOLVED && scoreService != null) {
            final int duration = (int) ((System.currentTimeMillis() - startTime) / 1000);
            scoreService.addScore(new Score("slitherlink", playerName, moves,
                    new java.util.Date(), duration, level, 0));
        }
    }

    public boolean undo() {
        return undoRedo.undo(field, this);
    }

    public boolean redo() {
        return undoRedo.redo(field, this);
    }
}