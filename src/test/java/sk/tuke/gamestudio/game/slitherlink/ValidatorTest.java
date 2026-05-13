package sk.tuke.gamestudio.game.slitherlink;

import org.junit.jupiter.api.Test;
import sk.tuke.gamestudio.game.slitherlink.core.logic.Play;
import sk.tuke.gamestudio.game.slitherlink.core.logic.PredefinedMaps;
import sk.tuke.gamestudio.game.slitherlink.core.bricks.Field;
import sk.tuke.gamestudio.game.slitherlink.core.logic.Generator;
import sk.tuke.gamestudio.game.slitherlink.core.logic.Validator;

import static org.junit.jupiter.api.Assertions.*;

public class ValidatorTest {

    @Test
    public void testEmptyFieldNotValid() {
        Field field = new Generator(0, 0).generateFromMap(PredefinedMaps.testMap());
        Validator validator = new Validator();
        assertFalse(validator.validate(field));
    }

    @Test
    public void testCheckCluesEmpty() {
        Field field = new Generator(0, 0).generateFromMap(PredefinedMaps.testMap());
        Validator validator = new Validator();
        assertTrue(validator.checkClues(field)); // 2x2 nema clues
    }

    @Test
    public void testCheckLoopFalseOnEmpty() {
        Field field = new Generator(0, 0).generateFromMap(PredefinedMaps.testMap());
        Validator validator = new Validator();
        assertFalse(validator.checkLoop(field));
    }

    @Test
    public void testValidAfterSolve() {
        Field field = new Generator(0, 0).generateFromMap(PredefinedMaps.testMap());
        Play play = new Play(field, "test", null, 0,11);
        play.handleClick(0, 0, 'T');
        play.handleClick(0, 1, 'T');
        play.handleClick(0, 0, 'L');
        play.handleClick(1, 0, 'L');
        play.handleClick(0, 1, 'R');
        play.handleClick(1, 1, 'R');
        play.handleClick(1, 0, 'B');
        play.handleClick(1, 1, 'B');
        assertTrue(new Validator().validate(field));
    }
}