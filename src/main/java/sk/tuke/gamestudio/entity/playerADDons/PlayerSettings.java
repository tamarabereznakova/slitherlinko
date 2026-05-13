package sk.tuke.gamestudio.entity.playerADDons;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "player_settings")
public class PlayerSettings implements Serializable {

    @Id
    private String username;
    private String preferredTheme = "neon";
    private int preferredLevel = 1;
    private int moveLimit = 0;
    private int timeLimit = 0;
    private boolean showMultiplayerScores = true;

    public PlayerSettings() {
    }

    public PlayerSettings(String username) {
        this.username = username;
        this.showMultiplayerScores = true;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getPreferredTheme() {
        return preferredTheme;
    }
    public void setPreferredTheme(String t) {
        this.preferredTheme = t;
    }

    public int getPreferredLevel() {
        return preferredLevel;
    }
    public void setPreferredLevel(int l) {
        this.preferredLevel = l;
    }

    public int getMoveLimit() {
        return moveLimit;
    }
    public void setMoveLimit(int m) {
        this.moveLimit = m;
    }

    public int getTimeLimit() {
        return timeLimit;
    }
    public void setTimeLimit(int t) {
        this.timeLimit = t;
    }

    public boolean isShowMultiplayerScores() {
        return showMultiplayerScores;
    }
    public void setShowMultiplayerScores(boolean showMultiplayerScores) {
        this.showMultiplayerScores = showMultiplayerScores;
    }
}