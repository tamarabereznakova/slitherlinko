package sk.tuke.gamestudio.game.slitherlink;

import org.junit.jupiter.api.Test;
import sk.tuke.gamestudio.game.slitherlink.core.logic.PredefinedMaps;
import sk.tuke.gamestudio.game.slitherlink.core.bricks.Field;
import sk.tuke.gamestudio.game.slitherlink.core.logic.Generator;

import static org.junit.jupiter.api.Assertions.*;

public class GeneratorTest {

    @Test
    public void testGeneratedFieldSize() {
        Field field = new Generator(0, 0).generateFromMap(PredefinedMaps.testMap());
        assertEquals(2, field.getRows());
        assertEquals(2, field.getCols());
    }

    @Test
    public void testGeneratedFieldHasEdges() {
        Field field = new Generator(0, 0).generateFromMap(PredefinedMaps.testMap());
        assertFalse(field.getAllEdges().isEmpty());
    }

    @Test
    public void testGeneratedFieldHasTiles() {
        Field field = new Generator(0, 0).generateFromMap(PredefinedMaps.testMap());
        for (int r = 0; r < field.getRows(); r++)
            for (int c = 0; c < field.getCols(); c++)
                assertNotNull(field.getTile(r, c));
    }

    @Test
    public void testSmallMapSize() {
        Field field = new Generator(0, 0).generateFromMap(PredefinedMaps.randomSmallMap());
        assertEquals(5, field.getRows());
        assertEquals(5, field.getCols());
    }

    @Test
    public void testLargeMapSize() {
        Field field = new Generator(0, 0).generateFromMap(PredefinedMaps.randomLargeMap());
        assertEquals(7, field.getRows());
        assertEquals(7, field.getCols());
    }
}