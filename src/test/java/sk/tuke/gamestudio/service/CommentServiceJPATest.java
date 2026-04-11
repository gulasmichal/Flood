package sk.tuke.gamestudio.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import sk.tuke.gamestudio.entity.Comment;
import sk.tuke.gamestudio.server.GameStudioServer;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = GameStudioServer.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class CommentServiceJPATest {

    @Autowired
    private CommentService service;

    @BeforeEach
    void setUp() {
        service.reset();
    }

    @Test
    void commentsAreEmptyAfterReset() {
        List<Comment> comments = service.getComments("flood");
        assertTrue(comments.isEmpty());
    }

    @Test
    void addedCommentAppearsInList() {
        service.addComment(new Comment("flood", "Alice", "Super hra!", new Date()));
        List<Comment> comments = service.getComments("flood");
        assertEquals(1, comments.size());
        assertEquals("Alice", comments.get(0).getPlayer());
        assertEquals("Super hra!", comments.get(0).getContent());
    }

    @Test
    void commentsOrderedByDateDescending() throws InterruptedException {
        service.addComment(new Comment("flood", "Alice", "Prvy komentar", new Date()));
        Thread.sleep(10);
        service.addComment(new Comment("flood", "Bob", "Druhy komentar", new Date()));
        List<Comment> comments = service.getComments("flood");
        assertEquals("Bob", comments.get(0).getPlayer());
        assertEquals("Alice", comments.get(1).getPlayer());
    }

    @Test
    void commentsFilteredByGame() {
        service.addComment(new Comment("flood", "Alice", "Flood komentar", new Date()));
        service.addComment(new Comment("minesweeper", "Bob", "Minesweeper komentar", new Date()));
        List<Comment> comments = service.getComments("flood");
        assertEquals(1, comments.size());
        assertEquals("Alice", comments.get(0).getPlayer());
    }

    @Test
    void resetClearsAllComments() {
        service.addComment(new Comment("flood", "Alice", "Komentar", new Date()));
        service.reset();
        assertTrue(service.getComments("flood").isEmpty());
    }

    @Test
    void commentHasCorrectGame() {
        service.addComment(new Comment("flood", "Alice", "Test", new Date()));
        Comment c = service.getComments("flood").get(0);
        assertEquals("flood", c.getGame());
    }

    @Test
    void commentHasCommentedOnDate() {
        service.addComment(new Comment("flood", "Alice", "Test", new Date()));
        Comment c = service.getComments("flood").get(0);
        assertNotNull(c.getCommentedOn());
    }

    @Test
    void multiplePlayersCanComment() {
        service.addComment(new Comment("flood", "Alice", "Komentar Alice", new Date()));
        service.addComment(new Comment("flood", "Bob", "Komentar Bob", new Date()));
        List<Comment> comments = service.getComments("flood");
        assertEquals(2, comments.size());
    }
}
