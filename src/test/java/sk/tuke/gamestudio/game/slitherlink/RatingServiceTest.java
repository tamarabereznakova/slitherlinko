package sk.tuke.gamestudio.game.slitherlink;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import sk.tuke.gamestudio.entity.Rating;
import sk.tuke.gamestudio.service.entity.RatingService;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = sk.tuke.gamestudio.server.GameStudioServer.class)
public class RatingServiceTest {

    @Autowired
    private RatingService ratingService;

    @Test
    public void testSetAndGetRating() {
        ratingService.reset();
        ratingService.addRating(new Rating("slitherlink", "testPlayer", 5, new java.util.Date()));
        assertEquals(5, ratingService.getRating("slitherlink", "testPlayer"));
    }

    @Test
    public void testReset() {
        ratingService.addRating(new Rating("slitherlink", "testPlayer", 5, new java.util.Date()));
        ratingService.reset();
        assertEquals(0, ratingService.getRating("slitherlink", "testPlayer"));
    }

    @Test
    public void testAverageRating() {
        ratingService.reset();
        ratingService.addRating(new Rating("slitherlink", "player1", 4, new java.util.Date()));
        ratingService.addRating(new Rating("slitherlink", "player2", 2, new java.util.Date()));
        assertEquals(3, ratingService.getAverageRating("slitherlink"));
    }
}