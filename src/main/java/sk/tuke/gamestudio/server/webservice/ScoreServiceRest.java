package sk.tuke.gamestudio.server.webservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sk.tuke.gamestudio.entity.Score;
import sk.tuke.gamestudio.service.entity.ScoreService;

import java.util.List;

@RestController
@RequestMapping("/api/score")
public class ScoreServiceRest {

    @Autowired
    private ScoreService scoreService;

    @GetMapping("/{game}")
    public List<Score> getTopScores(@PathVariable String game) {
        return scoreService.getTopScores(game);
    }

    @GetMapping("/{game}/level/{level}")
    public List<Score> getTopScoresByLevel(@PathVariable String game, @PathVariable int level) {
        return scoreService.getTopScoresByLevel(game, level);
    }

    @GetMapping("/{game}/player/{player}/level/{level}")
    public List<Score> getScoresByPlayer(@PathVariable String game,
                                         @PathVariable String player,
                                         @PathVariable int level) {
        return scoreService.getScoresByPlayer(game, player, level);
    }

    @PostMapping
    public void addScore(@RequestBody Score score) {
        scoreService.addScore(score);
    }

    @DeleteMapping("/{ident}/{player}")
    public void deleteScore(@PathVariable int ident, @PathVariable String player) {
        scoreService.deleteScore("slitherlink", player, ident);
    }
}