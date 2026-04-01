package sk.tuke.gamestudio.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;
import sk.tuke.gamestudio.entity.Rating;

import java.util.List;

@Transactional
public class RatingServiceJPA implements RatingService {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void setRating(Rating rating) {
        entityManager.createNamedQuery("Rating.deleteByGameAndPlayer")
                .setParameter("game", rating.getGame())
                .setParameter("player", rating.getPlayer())
                .executeUpdate();
        entityManager.persist(rating);
    }

    @Override
    public int getAverageRating(String game) {
        Double avg = (Double) entityManager.createNamedQuery("Rating.getAverageRating")
                .setParameter("game", game)
                .getSingleResult();
        return avg == null ? 0 : (int) Math.round(avg);
    }

    @Override
    public int getRating(String game, String player) {
        List<Rating> results = entityManager.createNamedQuery("Rating.getRating", Rating.class)
                .setParameter("game", game)
                .setParameter("player", player)
                .getResultList();
        return results.isEmpty() ? 0 : results.get(0).getStars();
    }

    @Override
    public void reset() {
        entityManager.createNamedQuery("Rating.resetRatings").executeUpdate();
    }
}
