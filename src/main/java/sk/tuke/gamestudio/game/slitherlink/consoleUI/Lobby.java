package sk.tuke.gamestudio.game.slitherlink.consoleUI;

import sk.tuke.gamestudio.game.slitherlink.core.bricks.Field;
import sk.tuke.gamestudio.game.slitherlink.core.bricks.FieldState;
import sk.tuke.gamestudio.game.slitherlink.core.logic.Generator;
import sk.tuke.gamestudio.game.slitherlink.core.logic.PredefinedMaps;
import sk.tuke.gamestudio.service.entity.CommentService;
import sk.tuke.gamestudio.service.entity.RatingService;
import sk.tuke.gamestudio.service.entity.ScoreService;
import sk.tuke.gamestudio.service.jdbc.ScoreServiceJDBC;

import java.util.Scanner;

public class Lobby {
    private Scanner scanner;
    private ScoreService scoreService;
    private CommentService commentService;
    private RatingService ratingService;

    public Lobby(ScoreService scoreService, CommentService commentService, RatingService ratingService) {
        this.scanner = new Scanner(System.in);
        this.scoreService = scoreService;
        this.commentService = commentService;
        this.ratingService = ratingService;
    }

    public void start() {
        showLobby();
        waitForSpace();
        startGame();
    }

    private void showLobby() {
        System.out.println("\u001B[36m");
        System.out.println("     _       _   _               _ _       _    ");
        System.out.println(" ___| |_   _| |_| |__   ___ _ __| (_)_ __ | | __");
        System.out.println("/ __| | | | | __| '_ \\ / _ \\ '__| | | '_ \\| |/ /");
        System.out.println("\\__ \\ | |_| | |_| | | |  __/ |  | | | | | |   < ");
        System.out.println("|___/_|\\__, |\\__|_| |_|\\___|_|  |_|_|_| |_|_|\\_\\");
        System.out.println("       |___/                                    ");
        System.out.println("\u001B[0m");

        int avg = ratingService.getAverageRating("slitherlink");
        System.out.println("\u001B[33mPriemerne hodnotenie hry: " + avg + "/5 ✨\u001B[0m");

        System.out.println();
        ScoreDisplay.showComments(commentService);
        System.out.println();

        System.out.println("\u001B[36mAko hrat:");
        System.out.println("  - Zadaj tah vo formate:  riadok stlpec T/B/L/R");
        System.out.println("  - OZNACENIE pola zacina v lavom hornom rohu od 1");
        System.out.println("  - T = top, B = bottom, L = left, R = right");
        System.out.println("  - Cielom je vytvorit jeden uzavrety slucku");
        System.out.println("  - Cisla ukazuju kolko hran okolo policka musi byt aktivnych");
        System.out.println("  - Pocas hrania vies stlacenim Q odist\u001B[0m");
        System.out.println();
        ScoreDisplay.showTopScores(scoreService);
        System.out.println();
        System.out.println("\u001B[32m  Stlac ENTER pre start...\u001B[0m");
    }

    private void waitForSpace() {
        scanner.nextLine();
    }

    private void startGame() {
        while (true) {
            String level;
            do {
                System.out.print("Zvol level (0 = 2x2, 1 = 5x5, 2 = 7x7): ");
                level = scanner.nextLine().trim();
                if (!level.equals("0") && !level.equals("1") && !level.equals("2"))
                    System.out.println("\u001B[31mNeplatna volba! Zadaj 0, 1 alebo 2!\u001B[0m");
            } while (!level.equals("0") && !level.equals("1") && !level.equals("2"));

            Field field;
            if (level.equals("0")) {
                field = new Generator(0, 0).generateFromMap(PredefinedMaps.testMap());
            } else if (level.equals("2")) {
                field = new Generator(0, 0).generateFromMap(PredefinedMaps.randomLargeMap());
            } else {
                field = new Generator(0, 0).generateFromMap(PredefinedMaps.randomSmallMap());
            }

            String playerName;
            ScoreServiceJDBC scoreServiceJDBC = new ScoreServiceJDBC();
            do {
                System.out.print("Zadaj svoje meno: ");
                playerName = scanner.nextLine().trim();
                if (scoreServiceJDBC.playerExists("slitherlink", playerName))
                    System.out.println("\u001B[31mToto meno uz existuje! Zvol ine.\u001B[0m");
            } while (scoreServiceJDBC.playerExists("slitherlink", playerName));

            new ConsoleUI(field, playerName, scanner, scoreService, commentService, ratingService).run();

            if (FieldState.FAILED.equals(field.getState())) {
                System.out.println("\u001B[33mDakujem za hranie :)\u001B[0m");
                break;
            }

            System.out.println("\u001B[32mGratulujeme!\u001B[0m");
            System.out.println("\u001B[32mCHCES SVOJE SKORE ULOZIT (ENTER = ano, N = nie)?\u001B[0m");
            String answer1 = scanner.nextLine().trim();
            if (answer1.equalsIgnoreCase("n")) {
                scoreServiceJDBC.deleteScore("slitherlink", playerName);
            }
            System.out.println("\u001B[32mChces hrat znova? (ENTER = ano, N = nie)\u001B[0m");
            String answer2 = scanner.nextLine().trim();
            if (answer2.equalsIgnoreCase("n")) break;
            showLobby();
        }
    }
}