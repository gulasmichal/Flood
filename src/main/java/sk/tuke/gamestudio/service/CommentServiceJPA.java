package sk.tuke.gamestudio.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;
import sk.tuke.gamestudio.entity.Comment;

import java.util.List;

@Transactional
public class CommentServiceJPA implements CommentService {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void addComment(Comment comment) {
        entityManager.persist(comment);
    }

    @Override
    public List<Comment> getComments(String game) {
        return entityManager.createNamedQuery("Comment.getComments", Comment.class)
                .setParameter("game", game)
                .getResultList();
    }

    @Override
    public void reset() {
        entityManager.createNamedQuery("Comment.resetComments").executeUpdate();
    }
}
