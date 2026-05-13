package sk.tuke.gamestudio.service.entity;

import sk.tuke.gamestudio.entity.Rating;

public interface RatingService {
    void addRating(Rating rating) throws RatingException;
    int getAverageRating(String game) throws RatingException;
    int getRating(String game, String player) throws RatingException;
    void reset() throws RatingException;
    void deleteRating(String game, String player) throws RatingException;
}
