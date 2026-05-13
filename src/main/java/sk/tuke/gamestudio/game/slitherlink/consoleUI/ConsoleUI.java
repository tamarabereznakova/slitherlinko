package sk.tuke.gamestudio.game.slitherlink.consoleUI;

import sk.tuke.gamestudio.game.slitherlink.core.bricks.*;
import sk.tuke.gamestudio.game.slitherlink.core.logic.Play;
import sk.tuke.gamestudio.service.entity.CommentService;
import sk.tuke.gamestudio.service.entity.RatingService;
import sk.tuke.gamestudio.service.entity.ScoreService;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConsoleUI {
    private Field field;
    private String playerName;
    private Play controller;
    private Scanner scanner;
    private CommentService commentService;
    private RatingService ratingService;
    private ScoreService scoreService;
    private static final Pattern INPUT_PATTERN = Pattern.compile("(\\d+)\\s+(\\d+)\\s+([TBLRtblr])");

    public ConsoleUI(Field field, String playerName, Scanner scanner, ScoreService scoreService, CommentService commentService, RatingService ratingService) {
        this.field = field;
        this.playerName = playerName;
        this.controller = new Play(field, playerName, scoreService, 1,0);
        this.scanner = scanner;
        this.scoreService = scoreService;
        this.commentService = commentService;
        this.ratingService = ratingService;
    }

    public void run() {
        //autoSolveTest();
        while (field.getState() == FieldState.PLAYING) {
            show();
            handleInput();
        }
        if (field.getState() == FieldState.SOLVED) {
            show();
            System.out.println("\u001B[32mGRATULUJEM VYHRALA SI!\u001B[0m");
            ScoreDisplay.showTopScores(scoreService);
            new Feedback(playerName, scanner, ratingService, commentService).ask();
        }
    }

    private void show() {
        int rows = field.getRows();
        int cols = field.getCols();

        System.out.print("    ");
        for (int c = 0; c < cols; c++) {
            System.out.print(c + 1 + " ");
        }
        System.out.println();

        System.out.print("  +");
        for (int c = 0; c < cols * 2 + 1; c++) System.out.print("-");
        System.out.println("+");

        for (int r = 0; r <= rows; r++) {
            System.out.print("  |");
            for (int c = 0; c <= cols; c++) {
                System.out.print(".");
                if (c < cols) {
                    Edge e = field.getEdgeAt(field.getPoint(r, c), field.getPoint(r, c + 1));
                    if (e != null && e.isActive()) {
                        System.out.print("\u001B[32m-\u001B[0m");
                    } else if (e != null && e.getState() == EdgeState.MARKED_X) {
                        System.out.print("x");
                    } else {
                        System.out.print(" ");
                    }
                }
            }
            System.out.println("|");

            if (r < rows) {
                System.out.print(r + 1 + " |");
                for (int c = 0; c <= cols; c++) {
                    Edge e = field.getEdgeAt(field.getPoint(r, c), field.getPoint(r + 1, c));
                    if (e != null && e.isActive()) {
                        System.out.print("\u001B[32m|\u001B[0m");
                    } else if (e != null && e.getState() == EdgeState.MARKED_X) {
                        System.out.print("x");
                    } else {
                        System.out.print(" ");
                    }
                    if (c < cols) {
                        Tile t = field.getTile(r, c);
                        if (t instanceof Clue) {
                            System.out.print("\u001B[36m" + ((Clue) t).getValue() + "\u001B[0m");
                        } else {
                            System.out.print(" ");
                        }
                    }
                }
                System.out.println("|");
            }
        }

        System.out.print("  +");
        for (int c = 0; c < cols * 2 + 1; c++) System.out.print("-");
        System.out.println("+");

        System.out.println();
        System.out.println("Pocet tahov: " + controller.getMoves());
        System.out.println();
        int elapsed = (int)((System.currentTimeMillis() - controller.getStartTime()) / 1000);
        System.out.println("Cas: " + elapsed + "s");
        System.out.println();
    }

    private void handleInput() {
        System.out.print("Zadaj tah (row col T/B/L/R): ");
        String input = scanner.nextLine();
        Matcher matcher = INPUT_PATTERN.matcher(input);

        if (input.equalsIgnoreCase("q")) {
            field.setState(FieldState.FAILED);
            return;
        }

        if (!matcher.matches()) {
            System.out.println("\u001B[31mNeplatny vstup!\u001B[0m");
            return;
        }

        int row = Integer.parseInt(matcher.group(1)) -1;
        int col = Integer.parseInt(matcher.group(2)) -1;
        char side = matcher.group(3).toUpperCase().charAt(0);

        if (row < 0 || row >= field.getRows() || col < 0 || col >= field.getCols()) {
            System.out.println("\u001B[31mSuradnice su mimo rozsahu pola! Skus znova.\u001B[0m");
            return;
        }

        controller.handleClick(row, col, side);
    }

    private void autoSolveTest() {
        controller.handleClick(0, 0, 'T');
        controller.handleClick(0, 1, 'T');
        controller.handleClick(0, 0, 'L');
        controller.handleClick(0, 1, 'R');
        controller.handleClick(1, 0, 'B');
        controller.handleClick(1, 1, 'B');
        controller.handleClick(1, 1, 'R');
        controller.handleClick(1, 0, 'L');
    }
}