package sk.tuke.gamestudio.server.controller.helpers;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;
import sk.tuke.gamestudio.entity.Comment;
import sk.tuke.gamestudio.entity.playerADDons.PlayerSettings;
import sk.tuke.gamestudio.entity.Score;
import sk.tuke.gamestudio.game.slitherlink.core.bricks.Field;
import sk.tuke.gamestudio.game.slitherlink.core.logic.Play;
import java.util.List;

@Component
@SessionScope
public class SlitherlinkSession {

    private boolean spectatorMode = false;
    private String playerName = null;
    private boolean returning = false;
    private boolean hasPlayed = false;
    private int gamesPlayed = 0;
    private Field field;
    private Play play;
    private int currentLevel = 1;
    private int currentMapIndex = 0;
    private int lastMoves;
    private int lastDuration;
    private boolean inTop10;
    private List<Score> cachedScores0 = null;
    private List<Score> cachedScores1 = null;
    private List<Score> cachedScores2 = null;
    private List<Comment> cachedComments = null;
    private long cacheTime = 0;
    private String failReason = null;
    private PlayerSettings settings = null;

    public boolean isSpectatorMode() {
        return spectatorMode;
    }
    public void setSpectatorMode(boolean spectatorMode) {
        this.spectatorMode = spectatorMode;
    }

    public String getPlayerName() {
        return playerName;
    }
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public boolean isReturning() {
        return returning;
    }
    public void setReturning(boolean returning) {
        this.returning = returning;
    }

    public boolean isHasPlayed() {
        return hasPlayed;
    }
    public void setHasPlayed(boolean hasPlayed) {
        this.hasPlayed = hasPlayed;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }
    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public Field getField() {
        return field;
    }
    public void setField(Field field) {
        this.field = field;
    }

    public Play getPlay() {
        return play;
    }
    public void setPlay(Play play) {
        this.play = play;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }
    public void setCurrentLevel(int currentLevel) {
        this.currentLevel = currentLevel;
    }

    public int getCurrentMapIndex() {
        return currentMapIndex;
    }
    public void setCurrentMapIndex(int currentMapIndex) {
        this.currentMapIndex = currentMapIndex;
    }

    public int getLastMoves() {
        return lastMoves;
    }
    public void setLastMoves(int lastMoves) {
        this.lastMoves = lastMoves;
    }

    public int getLastDuration() {
        return lastDuration;
    }
    public void setLastDuration(int lastDuration) {
        this.lastDuration = lastDuration;
    }

    public boolean isInTop10() {
        return inTop10;
    }
    public void setInTop10(boolean inTop10) {
        this.inTop10 = inTop10;
    }

    public String getFailReason() {
        return failReason;
    }
    public void setFailReason(String failReason) {
        this.failReason = failReason;
    }

    public PlayerSettings getSettings() {
        return settings;
    }
    public void setSettings(PlayerSettings settings) {
        this.settings = settings;
    }
}