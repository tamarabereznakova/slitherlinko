package sk.tuke.gamestudio.service.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import sk.tuke.gamestudio.entity.Comment;
import sk.tuke.gamestudio.service.entity.CommentService;
import java.util.Arrays;
import java.util.List;

@Component //nech nekrici na mna autowired, lebo toto nem spring
public class CommentServiceRestClient implements CommentService {
    private final String url = "http://localhost:8080/api/comment";

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public void addComment(Comment comment) {
        restTemplate.postForEntity(url, comment, Comment.class);
    }

    @Override
    public List<Comment> getComments(String game) {
        return Arrays.asList(restTemplate.getForEntity(url + "/" + game, Comment[].class).getBody());
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException("Not supported via web service");
    }

    @Override
    public List<Comment> getCommentsByPlayer(String game, String player) {
        return Arrays.asList(restTemplate.getForEntity(
                url + "/" + game + "/player/" + player, Comment[].class).getBody());
    }

    @Override
    public void deleteComment(int ident, String player) {
        restTemplate.delete(url + "/" + ident + "/" + player);
    }

    @Override
    public void deleteCommentsByPlayer(String game, String player) {
        restTemplate.delete(url + "/" + game + "/player/" + player);
    }
}