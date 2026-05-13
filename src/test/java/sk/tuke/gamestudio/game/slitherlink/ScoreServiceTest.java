package sk.tuke.gamestudio.game.slitherlink;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import sk.tuke.gamestudio.entity.Score;
import sk.tuke.gamestudio.service.entity.ScoreService;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

@SpringBootTest(classes = sk.tuke.gamestudio.server.GameStudioServer.class)
public class ScoreServiceTest {

    @Autowired
    private ScoreService scoreService;

    @Test
    public void testAddAndGetScore() {
        scoreService.reset();
        scoreService.addScore(new Score("slitherlink", "testPlayer", 10, new java.util.Date(), 30, 0, 0));
        List<Score> scores = scoreService.getTopScores("slitherlink");
        assertEquals(1, scores.size());
        assertEquals("testPlayer", scores.get(0).getPlayer());
    }

    @Test
    public void testReset() {
        scoreService.addScore(new Score("slitherlink", "testPlayer", 10, new java.util.Date(), 30, 0, 0));
        scoreService.reset();
        List<Score> scores = scoreService.getTopScores("slitherlink");
        assertEquals(0, scores.size());
    }

    @Test
    public void testTopScoresOrdering() {
        scoreService.reset();
        scoreService.addScore(new Score("slitherlink", "player1", 20, new java.util.Date(), 60, 0, 0));
        scoreService.addScore(new Score("slitherlink", "player2", 5, new java.util.Date(), 10, 0, 0));
        List<Score> scores = scoreService.getTopScores("slitherlink");
        assertEquals("player2", scores.get(0).getPlayer());
    }
}