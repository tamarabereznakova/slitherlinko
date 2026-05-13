package sk.tuke.gamestudio.service.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import sk.tuke.gamestudio.entity.Score;
import sk.tuke.gamestudio.service.entity.ScoreService;
import java.util.Arrays;
import java.util.List;

@Component
public class ScoreServiceRestClient implements ScoreService {
    private final String url = "http://localhost:8080/api/score";

    @Autowired
    private RestTemplate restTemplate;
    //private RestTemplate restTemplate = new RestTemplate();

    @Override
    public void addScore(Score score) {
        restTemplate.postForEntity(url, score, Score.class);
    }

    @Override
    public List<Score> getTopScores(String gameName) {
        return Arrays.asList(restTemplate.getForEntity(url + "/" + gameName, Score[].class).getBody());
    }

    @Override
    public List<Score> getTopScoresByLevel(String game, int level) {
        return Arrays.asList(restTemplate.getForEntity(url + "/" + game + "/level/" + level, Score[].class).getBody());
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException("Not supported via web service");
    }

    @Override
    public List<Score> getScoresByPlayer(String game, String player, int level) {
        return Arrays.asList(restTemplate.getForEntity(
                url + "/" + game + "/player/" + player + "/level/" + level,
                Score[].class).getBody());
    }

    @Override
    public void deleteScore(String game, String player, int ident) {
        restTemplate.delete(url + "/" + ident + "/" + player);
    }
}