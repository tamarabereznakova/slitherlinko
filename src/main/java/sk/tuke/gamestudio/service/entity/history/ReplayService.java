package sk.tuke.gamestudio.service.entity.history;

import sk.tuke.gamestudio.entity.history.Replay;
import java.util.List;

public interface ReplayService {
    void saveReplay(List<Replay> moves);
    List<Replay> getReplay(int scoreIdent);
    void deleteReplay(int scoreIdent);
}