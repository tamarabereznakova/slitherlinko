package sk.tuke.gamestudio.entity.history;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "replay")
@IdClass(ReplayId.class)
public class Replay implements Serializable {

    @Id
    private int scoreIdent;
    @Id
    private int moveOrder;

    private int rowNum;
    private int colNum;
    private char side;

    public Replay() {
    }

    public Replay(int scoreIdent, int moveOrder, int rowNum, int colNum, char side) {
        this.scoreIdent = scoreIdent;
        this.moveOrder = moveOrder;
        this.rowNum = rowNum;
        this.colNum = colNum;
        this.side = side;
    }

    public int getScoreIdent() {
        return scoreIdent;
    }

    public void setScoreIdent(int scoreIdent) {
        this.scoreIdent = scoreIdent;
    }

    public int getMoveOrder() {
        return moveOrder;
    }

    public void setMoveOrder(int moveOrder) {
        this.moveOrder = moveOrder;
    }

    public int getRowNum() {
        return rowNum;
    }

    public void setRowNum(int rowNum) {
        this.rowNum = rowNum;
    }

    public int getColNum() {
        return colNum;
    }

    public void setColNum(int colNum) {
        this.colNum = colNum;
    }

    public char getSide() {
        return side;
    }

    public void setSide(char side) {
        this.side = side;
    }
}