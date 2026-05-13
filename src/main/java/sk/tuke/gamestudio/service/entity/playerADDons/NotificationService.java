package sk.tuke.gamestudio.service.entity.playerADDons;

import sk.tuke.gamestudio.entity.playerADDons.Notification;
import java.util.List;

public interface NotificationService {
    void create(Notification notification);
    List<Notification> getUnseen(String recipient);
    List<Notification> getAll(String recipient);
    void markAllSeen(String recipient);
    void deleteByPlayer(String player);
}