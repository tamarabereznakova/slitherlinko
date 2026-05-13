package sk.tuke.gamestudio.service.entity;

import sk.tuke.gamestudio.entity.Score;

import java.util.List;

public interface ScoreService {
    void addScore(Score score) throws ScoreException;
    List<Score> getTopScores(String game) throws ScoreException;
    List<Score> getTopScoresByLevel(String game, int level) throws ScoreException;
    List<Score> getScoresByPlayer(String game, String player, int level) throws ScoreException;
    void reset() throws ScoreException;
    void deleteScore(String game, String player, int ident) throws ScoreException;
}
