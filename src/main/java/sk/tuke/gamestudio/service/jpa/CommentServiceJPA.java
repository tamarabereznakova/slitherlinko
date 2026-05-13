package sk.tuke.gamestudio.service.jpa;

import org.springframework.stereotype.Component;
import sk.tuke.gamestudio.entity.Comment;
import sk.tuke.gamestudio.service.entity.CommentException;
import sk.tuke.gamestudio.service.entity.CommentService;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;

@Component
@Transactional
public class CommentServiceJPA implements CommentService {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void addComment(Comment comment) throws CommentException {
        entityManager.persist(comment);
    }

    @Override
    public List<Comment> getComments(String game) throws CommentException {
        return entityManager.createNamedQuery("Comment.getComments")
                .setParameter("game", game)
                .getResultList();
    }

    @Override
    public void reset() throws CommentException {
        entityManager.createNamedQuery("Comment.reset").executeUpdate();
    }

    @Override
    public List<Comment> getCommentsByPlayer(String game, String player) {
        return entityManager.createNamedQuery("Comment.getCommentsByPlayer", Comment.class)
                .setParameter("game", game)
                .setParameter("player", player)
                .getResultList();
    }

    @Override
    public void deleteComment(int ident, String player) {
        entityManager.createNamedQuery("Comment.deleteByIdent")
                .setParameter("ident", ident)
                .setParameter("player", player)
                .executeUpdate();
    }

    @Override
    public void deleteCommentsByPlayer(String game, String player) {
        entityManager.createQuery(
                        "DELETE FROM Comment c WHERE c.game = :game AND c.player = :player")
                .setParameter("game", game)
                .setParameter("player", player)
                .executeUpdate();
    }
}