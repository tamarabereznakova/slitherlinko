package sk.tuke.gamestudio.service.jpa;

import org.springframework.stereotype.Component;
import sk.tuke.gamestudio.entity.history.SavedGame;
import sk.tuke.gamestudio.entity.history.SavedGameId;
import sk.tuke.gamestudio.service.entity.history.SavedGameService;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

@Component
@Transactional
public class SavedGameServiceJPA implements SavedGameService {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void saveGame(SavedGame savedGame) {
        final SavedGame existing = entityManager.find(SavedGame.class,
                new SavedGameId(savedGame.getPlayer(), savedGame.getLevel()));
        if (existing != null) {
            existing.setEdges(savedGame.getEdges());
            existing.setMoves(savedGame.getMoves());
            existing.setElapsed(savedGame.getElapsed());
            existing.setSavedOn(savedGame.getSavedOn());
            existing.setUndoBuffer(savedGame.getUndoBuffer());
            existing.setRedoBuffer(savedGame.getRedoBuffer());
        } else {
            entityManager.persist(savedGame);
        }
    }

    @Override
    public SavedGame loadGame(String player, int level) {
        return entityManager.find(SavedGame.class, new SavedGameId(player, level));
    }

    @Override
    public void deleteGame(String player, int level) {
        final SavedGame existing = entityManager.find(SavedGame.class,
                new SavedGameId(player, level));
        if (existing != null) {
            entityManager.remove(existing);
        }
    }
}