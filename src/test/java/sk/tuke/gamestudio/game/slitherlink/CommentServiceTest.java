package sk.tuke.gamestudio.game.slitherlink;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import sk.tuke.gamestudio.entity.Comment;
import sk.tuke.gamestudio.service.entity.CommentService;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

@SpringBootTest(classes = sk.tuke.gamestudio.server.GameStudioServer.class)
public class CommentServiceTest {

    @Autowired
    private CommentService commentService;

    @Test
    public void testAddAndGetComment() {
        commentService.reset();
        commentService.addComment(new Comment("slitherlink", "testPlayer", "super hra!", new java.util.Date()));
        List<Comment> comments = commentService.getComments("slitherlink");
        assertEquals(1, comments.size());
        assertEquals("testPlayer", comments.get(0).getPlayer());
    }

    @Test
    public void testReset() {
        commentService.addComment(new Comment("slitherlink", "testPlayer", "super hra!", new java.util.Date()));
        commentService.reset();
        List<Comment> comments = commentService.getComments("slitherlink");
        assertEquals(0, comments.size());
    }

    @Test
    public void testCommentText() {
        commentService.reset();
        commentService.addComment(new Comment("slitherlink", "testPlayer", "super hra!", new java.util.Date()));
        List<Comment> comments = commentService.getComments("slitherlink");
        assertEquals("super hra!", comments.get(0).getText());
    }
}