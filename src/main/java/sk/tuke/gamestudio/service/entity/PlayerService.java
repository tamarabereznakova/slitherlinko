package sk.tuke.gamestudio.service.entity;

import java.util.Date;

public interface PlayerService {
    boolean register(String username, String password);
    boolean login(String username, String password);
    boolean exists(String username);
    void changePassword(String username, String newPassword);
    Date getCreatedOn(String username);
    void deletePlayer(String username);
    void touchLastSeen(String username);
    boolean isOnline(String username);
}