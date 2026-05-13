package sk.tuke.gamestudio.server.webservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sk.tuke.gamestudio.entity.Comment;
import sk.tuke.gamestudio.service.entity.CommentService;

import java.util.List;

@RestController
@RequestMapping("/api/comment")
public class CommentServiceRest {

    @Autowired
    private CommentService commentService;

    @GetMapping("/{game}")
    public List<Comment> getComments(@PathVariable String game) {
        return commentService.getComments(game);
    }

    @PostMapping
    public void addComment(@RequestBody Comment comment) {
        commentService.addComment(comment);
    }

    @GetMapping("/{game}/player/{player}")
    public List<Comment> getCommentsByPlayer(@PathVariable String game, @PathVariable String player) {
        return commentService.getCommentsByPlayer(game, player);
    }

    @DeleteMapping("/{ident}/{player}")
    public void deleteComment(@PathVariable int ident, @PathVariable String player) {
        commentService.deleteComment(ident, player);
    }

    @DeleteMapping("/{game}/player/{player}")
    public void deleteCommentsByPlayer(@PathVariable String game, @PathVariable String player) {
        commentService.deleteCommentsByPlayer(game, player);
    }
}