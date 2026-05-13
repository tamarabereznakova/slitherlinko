package sk.tuke.gamestudio.game.slitherlink.consoleUI;

import sk.tuke.gamestudio.entity.Comment;
import sk.tuke.gamestudio.entity.Rating;
import sk.tuke.gamestudio.service.entity.CommentService;
import sk.tuke.gamestudio.service.entity.RatingService;

import java.util.Scanner;

public class Feedback {
    private Scanner scanner;
    private String playerName;
    private RatingService ratingService;
    private CommentService commentService;

    public Feedback(String playerName, Scanner scanner, RatingService ratingService, CommentService commentService) {
        this.playerName = playerName;
        this.scanner = scanner;
        this.ratingService = ratingService;
        this.commentService = commentService;
    }

    public void ask() {
        String answer;
        do {
            System.out.print("Chces ohodnotit hru? (A/N): ");
            answer = scanner.nextLine().trim();
        } while (!answer.equalsIgnoreCase("a") && !answer.equalsIgnoreCase("n"));

        if (answer.equalsIgnoreCase("a")) {
            String ratingInput;
            do {
                System.out.print("Zadaj hodnotenie (1-5): ");
                ratingInput = scanner.nextLine().trim();
                if (!ratingInput.matches("[1-5]"))
                    System.out.println("\u001B[31mNeplatne hodnotenie! Zadaj cislo 1-5.\u001B[0m");
            } while (!ratingInput.matches("[1-5]"));
            ratingService.addRating(new Rating("slitherlink", playerName, Integer.parseInt(ratingInput), new java.util.Date()));
            System.out.println("Hodnotenie ulozene!");
        }

        do {
            System.out.print("Chces zanechat komentar? (A/N): ");
            answer = scanner.nextLine().trim();
        } while (!answer.equalsIgnoreCase("a") && !answer.equalsIgnoreCase("n"));

        if (answer.equalsIgnoreCase("a")) {
            String text;
            do {
                System.out.print("Zadaj komentar: ");
                text = scanner.nextLine().trim();
                if (text.length() > 100)
                    System.out.println("\u001B[31mKomentar musi mat do 100 znakov!\u001B[0m");
                else if (text.isEmpty())
                    System.out.println("\u001B[31mKomentar NESMIE byt prazdny!\u001B[0m");
            } while (text.isEmpty() || text.length() > 100);
            commentService.addComment(new Comment("slitherlink", playerName, text, new java.util.Date()));
            System.out.println("Komentar ulozeny!");
        }
    }
}