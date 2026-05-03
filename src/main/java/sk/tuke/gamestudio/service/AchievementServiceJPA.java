package sk.tuke.gamestudio.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;
import sk.tuke.gamestudio.entity.Achievement;
import sk.tuke.gamestudio.entity.AchievementType;

import java.util.Date;
import java.util.List;

@Transactional
public class AchievementServiceJPA implements AchievementService {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void award(String player, AchievementType type) throws AchievementException {
        try {
            if (hasAchievement(player, type)) return;
            entityManager.persist(new Achievement(player, type.name(), new Date()));
        } catch (Exception e) {
            throw new AchievementException("Failed to award achievement", e);
        }
    }

    @Override
    public List<Achievement> getAchievements(String player) throws AchievementException {
        try {
            return entityManager.createNamedQuery("Achievement.getByPlayer", Achievement.class)
                    .setParameter("player", player)
                    .getResultList();
        } catch (Exception e) {
            throw new AchievementException("Failed to get achievements", e);
        }
    }

    @Override
    public boolean hasAchievement(String player, AchievementType type) throws AchievementException {
        try {
            List<Achievement> result = entityManager.createNamedQuery("Achievement.findByPlayerAndType", Achievement.class)
                    .setParameter("player", player)
                    .setParameter("type", type.name())
                    .getResultList();
            return !result.isEmpty();
        } catch (Exception e) {
            throw new AchievementException("Failed to check achievement", e);
        }
    }

    @Override
    public void reset() throws AchievementException {
        try {
            entityManager.createNamedQuery("Achievement.reset").executeUpdate();
        } catch (Exception e) {
            throw new AchievementException("Failed to reset achievements", e);
        }
    }
}
