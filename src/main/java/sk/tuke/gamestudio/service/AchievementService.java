package sk.tuke.gamestudio.service;

import sk.tuke.gamestudio.entity.Achievement;
import sk.tuke.gamestudio.entity.AchievementType;

import java.util.List;

public interface AchievementService {
    void award(String player, AchievementType type) throws AchievementException;
    List<Achievement> getAchievements(String player) throws AchievementException;
    boolean hasAchievement(String player, AchievementType type) throws AchievementException;
    void reset() throws AchievementException;
}
