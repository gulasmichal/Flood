package sk.tuke.gamestudio.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;
import sk.tuke.gamestudio.entity.GameUser;

import java.util.List;

@Transactional
public class UserServiceJPA implements UserService {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void register(String username, String password) throws UserException {
        List<GameUser> existing = entityManager
                .createNamedQuery("GameUser.findByUsername", GameUser.class)
                .setParameter("username", username)
                .getResultList();
        if (!existing.isEmpty()) {
            throw new UserException("Používateľ s týmto menom už existuje.");
        }
        entityManager.persist(new GameUser(username, password));
    }

    @Override
    public boolean login(String username, String password) throws UserException {
        List<GameUser> users = entityManager
                .createNamedQuery("GameUser.findByCredentials", GameUser.class)
                .setParameter("username", username)
                .setParameter("password", password)
                .getResultList();
        return !users.isEmpty();
    }
}
