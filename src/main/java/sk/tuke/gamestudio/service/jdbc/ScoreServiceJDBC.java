package sk.tuke.gamestudio.service.jdbc;

import sk.tuke.gamestudio.entity.Score;
import sk.tuke.gamestudio.service.entity.ScoreException;
import sk.tuke.gamestudio.service.entity.ScoreService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class ScoreServiceJDBC implements ScoreService {
    public static final String URL = "jdbc:postgresql://localhost/gamestudio";
    public static final String USER = "postgres";
    public static final String PASSWORD = "chlpaty.tamara";
    //public static final String SELECT = "SELECT game, player, points, playedOn FROM score WHERE game = ? ORDER BY points DESC LIMIT 10";
    public static final String SELECT = "SELECT game, player, moves, played_on, duration FROM score WHERE game = ? ORDER BY duration ASC, moves ASC, played_on DESC LIMIT 10";
    public static final String DELETE = "DELETE FROM score";
    public static final String DELETE_ONE = "DELETE FROM score WHERE game = ? AND player = ?";
    public static final String INSERT = "INSERT INTO score (game, player, moves, played_on, duration) VALUES (?, ?, ?, ?, ?)";

    @Override
    public void addScore(Score score) {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(INSERT)
        ) {
            statement.setString(1, score.getGame());
            statement.setString(2, score.getPlayer());
            statement.setInt(3, score.getMoves());
            statement.setTimestamp(4, new Timestamp(score.getPlayedOn().getTime()));
            statement.setInt(5, score.getDuration());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new ScoreException("Problem inserting score", e);
        }
    }

    @Override
    public List<Score> getTopScores(String game) {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(SELECT);
        ) {
            statement.setString(1, game);
            try (ResultSet rs = statement.executeQuery()) {
                final List<Score> scores = new ArrayList<>();
                while (rs.next()) {
                    scores.add(new Score(rs.getString(1), rs.getString(2), rs.getInt(3), rs.getTimestamp(4), rs.getInt(5), 0, 0));
                }
                return scores;
            }
        } catch (SQLException e) {
            throw new ScoreException("Problem selecting score", e);
        }
    }

    @Override
    public List<Score> getTopScoresByLevel(String game, int level) {
        throw new UnsupportedOperationException("Not supported via JDBC");
    }

    @Override
    public List<Score> getScoresByPlayer(String game, String player, int level) throws ScoreException {
        return List.of();
    }

    @Override
    public void reset() {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement statement = connection.createStatement();
        ) {
            statement.executeUpdate(DELETE);
        } catch (SQLException e) {
            throw new ScoreException("Problem deleting score", e);
        }
    }

    @Override
    public void deleteScore(String game, String player, int ident) throws ScoreException {

    }

    public void deleteScore(String game, String player) {
        //final String sql = "DELETE FROM score WHERE game = ? AND player = ?";
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(DELETE_ONE)) {
            statement.setString(1, game);
            statement.setString(2, player);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new ScoreException("Problem deleting score", e);
        }
    }

    public boolean playerExists(String game, String player) {
        final String sql = "SELECT COUNT(*) FROM score WHERE game = ? AND player = ?";
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, game);
            statement.setString(2, player);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new ScoreException("Problem checking player", e);
        }
        return false;
    }//JE TO TU, lebo mi to v lobby neslo normalne implementovat tak co uz
    //aj tak to uz nepouzivam *tami z buducnosti*
}
