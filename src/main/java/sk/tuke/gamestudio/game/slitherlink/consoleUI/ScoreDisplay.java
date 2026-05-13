package sk.tuke.gamestudio.game.slitherlink.consoleUI;

import sk.tuke.gamestudio.entity.Comment;
import sk.tuke.gamestudio.entity.Score;
import sk.tuke.gamestudio.service.entity.CommentService;
import sk.tuke.gamestudio.service.entity.ScoreService;

import java.text.SimpleDateFormat;
import java.util.List;

public class ScoreDisplay {
    public static void showTopScores(ScoreService scoreService) {
        SimpleDateFormat sdf = new SimpleDateFormat("d. MMMM yyyy", new java.util.Locale("sk"));
        List<Score> scores = scoreService.getTopScores("slitherlink");
        System.out.println("\u001B[33m=== TOP 10 HRACOV ===\u001B[0m");
        int i = 1;
        for (Score s : scores) {
            System.out.println(i++ + ". " + s.getPlayer() + " - " + s.getMoves() + " tahov - " + s.getDuration() + "s (" + sdf.format(s.getPlayedOn()) + ")");
        }
    }

    public static void showComments(CommentService commentService) {
        List<Comment> comments = commentService.getComments("slitherlink");
        if (comments.isEmpty()) {
            System.out.println("\u001B[33mNESU KOMENTARE.\u001B[0m");
            return;
        }
        System.out.println("\u001B[33m=== KOMENTARE ===\u001B[0m");
        for (Comment c : comments) {
            System.out.println("  " + c.getPlayer() + ": " + c.getText());
        }
    }
}