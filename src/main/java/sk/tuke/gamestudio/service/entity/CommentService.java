package sk.tuke.gamestudio.service.entity;

import sk.tuke.gamestudio.entity.Comment;

import java.util.List;

public interface CommentService {
    void addComment(Comment comment) throws CommentException;
    List<Comment> getComments(String game) throws CommentException;
    void reset() throws CommentException;
    List<Comment> getCommentsByPlayer(String game, String player) throws CommentException;
    void deleteComment(int ident, String player) throws CommentException;
    void deleteCommentsByPlayer(String game, String player) throws CommentException;
}
