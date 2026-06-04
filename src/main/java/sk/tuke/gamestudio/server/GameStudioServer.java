package sk.tuke.gamestudio.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sk.tuke.gamestudio.service.entity.CommentService;
import sk.tuke.gamestudio.service.entity.PlayerService;
import sk.tuke.gamestudio.service.entity.RatingService;
import sk.tuke.gamestudio.service.entity.ScoreService;
import sk.tuke.gamestudio.service.entity.history.ReplayService;
import sk.tuke.gamestudio.service.entity.history.SavedGameService;
import sk.tuke.gamestudio.service.entity.playerADDons.*;
import sk.tuke.gamestudio.service.jpa.*;

@SpringBootApplication
@Configuration
@EntityScan({"sk.tuke.gamestudio.entity", "sk.tuke.gamestudio.entity.playerADDons", "sk.tuke.gamestudio.entity.history"})
public class GameStudioServer {
    public static void main(String[] args) {
        SpringApplication.run(GameStudioServer.class, args);
    }

    @Bean
    public ScoreService scoreService() {
        return new ScoreServiceJPA();
    }

    @Bean
    public CommentService commentService() {
        return new CommentServiceJPA();
    }

    @Bean
    public RatingService ratingService() {
        return new RatingServiceJPA();
    }

    @Bean
    public PlayerService playerService() {
        return new PlayerServiceJPA();
    }

    @Bean
    public SavedGameService savedGameService() {
        return new SavedGameServiceJPA();
    }

    @Bean
    public ReplayService replayService() {
        return new ReplayServiceJPA();
    }

    @Bean
    public PlayerSettingsService playerSettingsService() {
        return new PlayerSettingsServiceJPA();
    }

    @Bean
    public SpectateService spectateService() {
        return new SpectateServiceJPA();
    }

    @Bean
    public GameInviteService gameInviteService() {
        return new GameInviteServiceJPA();
    }

    @Bean
    public FollowService followService() {
        return new FollowServiceJPA();
    }

    @Bean
    public NotificationService notificationService() {
        return new NotificationServiceJPA();
    }

    @Bean
    public LobbyService lobbyService() {
        return new LobbyServiceJPA();
    }

    @Bean
    public MultiplayerScoreService multiplayerScoreService() {
        return new MultiplayerScoreServiceJPA();
    }

    @Bean
CommandLineRunner seedData(
        PlayerService playerService,
        ScoreService scoreService,
        CommentService commentService,
        RatingService ratingService
) {
    return args -> {

        // 1. ADMIN USER
        if (!playerService.exists("admin")) {
            playerService.register("admin", "admin");
        }

        // 2. DEMO PLAYER
        if (!playerService.exists("macicka")) {
            playerService.register("macicka", "admin");

            // SCORE
            scoreService.addScore(new Score(
                    "slitherlink",
                    "macicka",
                    58,
                    new java.util.Date(),
                    47,
                    1,
                    1
            ));

            // COMMENT
            commentService.addComment(new Comment(
                    "slitherlink",
                    "macicka",
                    "super hraaaaaaaa 🤍🤍",
                    new java.util.Date()
            ));

            // RATING
            ratingService.addRating(new Rating(
                    "slitherlink",
                    "macicka",
                    5,
                    new java.util.Date()
            ));
        }
    };
}
}
