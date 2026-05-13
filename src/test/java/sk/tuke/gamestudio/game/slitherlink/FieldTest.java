package sk.tuke.gamestudio.game.slitherlink;

import org.junit.jupiter.api.Test;
import sk.tuke.gamestudio.game.slitherlink.core.logic.Play;
import sk.tuke.gamestudio.game.slitherlink.core.logic.PredefinedMaps;
import sk.tuke.gamestudio.game.slitherlink.core.bricks.Field;
import sk.tuke.gamestudio.game.slitherlink.core.bricks.FieldState;
import sk.tuke.gamestudio.game.slitherlink.core.logic.Generator;

import static org.junit.jupiter.api.Assertions.*;

public class FieldTest {

    @Test
    public void testFieldInitialization() {
        Field field = new Generator(0, 0).generateFromMap(PredefinedMaps.testMap());
        assertEquals(FieldState.PLAYING, field.getState());
        assertNotNull(field.getTile(0, 0));
    }

    @Test
    public void testPointsInitialized() {
        Field field = new Generator(0, 0).generateFromMap(PredefinedMaps.testMap());
        for (int r = 0; r <= field.getRows(); r++) {
            for (int c = 0; c <= field.getCols(); c++) {
                assertNotNull(field.getPoint(r, c));
            }
        }
    }

    @Test
    public void testEdgesInitialized() {
        Field field = new Generator(0, 0).generateFromMap(PredefinedMaps.testMap());
        assertFalse(field.getAllEdges().isEmpty());
    }

    @Test
    public void testNotSolvedOnStart() {
        Field field = new Generator(0, 0).generateFromMap(PredefinedMaps.testMap());
        field.checkWin();
        assertNotEquals(FieldState.SOLVED, field.getState());
    }

    @Test
    public void testSolvedAfterCorrectMoves() {
        Field field = new Generator(0, 0).generateFromMap(PredefinedMaps.testMap());
        Play play = new Play(field, "test", null, 0,0);
        play.handleClick(0, 0, 'T'); // horná hrana riadok 0
        play.handleClick(0, 1, 'T'); // horná hrana riadok 0
        play.handleClick(0, 0, 'L'); // ľavá hrana
        play.handleClick(1, 0, 'L'); // ľavá hrana
        play.handleClick(0, 1, 'R'); // pravá hrana
        play.handleClick(1, 1, 'R'); // pravá hrana
        play.handleClick(1, 0, 'B'); // dolná hrana
        play.handleClick(1, 1, 'B'); // dolná hrana
        assertEquals(FieldState.SOLVED, field.getState());
    }
}