package sk.tuke.gamestudio.game.slitherlink;

import org.junit.jupiter.api.Test;
import sk.tuke.gamestudio.game.slitherlink.core.logic.Play;
import sk.tuke.gamestudio.game.slitherlink.core.logic.PredefinedMaps;
import sk.tuke.gamestudio.game.slitherlink.core.bricks.Edge;
import sk.tuke.gamestudio.game.slitherlink.core.bricks.EdgeState;
import sk.tuke.gamestudio.game.slitherlink.core.bricks.Field;
import sk.tuke.gamestudio.game.slitherlink.core.bricks.FieldState;
import sk.tuke.gamestudio.game.slitherlink.core.logic.Generator;

import static org.junit.jupiter.api.Assertions.*;

public class PlayTest {

    @Test
    public void testMovesCountAfterClick() {
        Field field = new Generator(0, 0).generateFromMap(PredefinedMaps.testMap());
        Play play = new Play(field, "test", null, 0,0);
        play.handleClick(0, 0, 'T');
        assertEquals(1, play.getMoves());
    }

    @Test
    public void testInvalidClickDoesNotCountMove() {
        Field field = new Generator(0, 0).generateFromMap(PredefinedMaps.testMap());
        Play play = new Play(field, "test", null, 0,0);
        play.handleClick(99, 99, 'T'); // mimo rozsahu
        assertEquals(0, play.getMoves());
    }

    @Test
    public void testStateAfterWin() {
        Field field = new Generator(0, 0).generateFromMap(PredefinedMaps.testMap());
        Play play = new Play(field, "test", null, 0,0);
        play.handleClick(0, 0, 'T');
        play.handleClick(0, 1, 'T');
        play.handleClick(0, 0, 'L');
        play.handleClick(1, 0, 'L');
        play.handleClick(0, 1, 'R');
        play.handleClick(1, 1, 'R');
        play.handleClick(1, 0, 'B');
        play.handleClick(1, 1, 'B');
        assertEquals(FieldState.SOLVED, field.getState());
    }

    @Test
    public void testEdgeCycleState() {
        Field field = new Generator(0, 0).generateFromMap(PredefinedMaps.testMap());
        Play play = new Play(field, "test", null, 0,0);
        Edge edge = play.findEdge(0, 0, 'T');
        assertNotNull(edge);
        assertEquals(EdgeState.INACTIVE, edge.getState());
        play.handleClick(0, 0, 'T');
        assertEquals(EdgeState.ACTIVE, edge.getState());
        play.handleClick(0, 0, 'T');
        assertEquals(EdgeState.MARKED_X, edge.getState());
    }
}