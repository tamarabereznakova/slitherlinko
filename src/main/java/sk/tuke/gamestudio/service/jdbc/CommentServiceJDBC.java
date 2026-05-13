package sk.tuke.gamestudio.service.jdbc;

import sk.tuke.gamestudio.entity.Comment;
import sk.tuke.gamestudio.service.entity.CommentException;
import sk.tuke.gamestudio.service.entity.CommentService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentServiceJDBC implements CommentService {
    public static final String URL = "jdbc:postgresql://localhost/gamestudio";
    public static final String USER = "postgres";
    public static final String PASSWORD = "chlpaty.tamara";
    public static final String SELECT = "SELECT game, player, text, commented_on FROM comment WHERE game = ? ORDER BY commented_on DESC";
    public static final String DELETE = "DELETE FROM comment";
    public static final String INSERT = "INSERT INTO comment (game, player, text, commented_on) VALUES (?, ?, ?, ?)";

    @Override
    public void addComment(Comment comment) throws CommentException {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(INSERT)
        ) {
            statement.setString(1, comment.getGame());
            statement.setString(2, comment.getPlayer());
            statement.setString(3, comment.getText());
            statement.setTimestamp(4, new Timestamp(comment.getCommentedOn().getTime()));
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new CommentException("Problem inserting comment", e);
        }
    }

    @Override
    public List<Comment> getComments(String game) throws CommentException {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(SELECT);
        ) {
            statement.setString(1, game);
            try (ResultSet rs = statement.executeQuery()) {
                final List<Comment> comment = new ArrayList<>();
                while (rs.next()) {
                    comment.add(new Comment(rs.getString(1), rs.getString(2), rs.getString(3), rs.getTimestamp(4)));
                }
                return comment;

            }
        } catch (SQLException e) {
            throw new CommentException("Problem selecting score", e);
        }
    }

    @Override
    public void reset() throws CommentException {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(DELETE);
        } catch (SQLException e) {
            throw new CommentException("Problem deleting comments", e);
        }
    }

    @Override
    public List<Comment> getCommentsByPlayer(String game, String player) throws CommentException {
        return List.of();
    }

    @Override
    public void deleteComment(int ident, String player) throws CommentException {

    }

    @Override
    public void deleteCommentsByPlayer(String game, String player) throws CommentException {

    }
}
