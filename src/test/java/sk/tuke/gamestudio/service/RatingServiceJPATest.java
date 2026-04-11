package sk.tuke.gamestudio.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import sk.tuke.gamestudio.entity.Rating;
import sk.tuke.gamestudio.server.GameStudioServer;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = GameStudioServer.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class RatingServiceJPATest {

    @Autowired
    private RatingService service;

    @BeforeEach
    void setUp() {
        service.reset();
    }

    @Test
    void averageRatingIsZeroAfterReset() {
        assertEquals(0, service.getAverageRating("flood"));
    }

    @Test
    void getRatingReturnsZeroForUnknownPlayer() {
        assertEquals(0, service.getRating("flood", "Alice"));
    }

    @Test
    void setRatingAndRetrieve() {
        service.setRating(new Rating("flood", "Alice", 4, new Date()));
        assertEquals(4, service.getRating("flood", "Alice"));
    }

    @Test
    void averageRatingCalculatedCorrectly() {
        service.setRating(new Rating("flood", "Alice", 4, new Date()));
        service.setRating(new Rating("flood", "Bob", 2, new Date()));
        int avg = service.getAverageRating("flood");
        assertEquals(3, avg);
    }

    @Test
    void setRatingUpdatesExistingRating() {
        service.setRating(new Rating("flood", "Alice", 2, new Date()));
        service.setRating(new Rating("flood", "Alice", 5, new Date()));
        assertEquals(5, service.getRating("flood", "Alice"));
        assertEquals(5, service.getAverageRating("flood"));
    }

    @Test
    void ratingsFilteredByGame() {
        service.setRating(new Rating("flood", "Alice", 5, new Date()));
        service.setRating(new Rating("minesweeper", "Alice", 1, new Date()));
        assertEquals(5, service.getAverageRating("flood"));
        assertEquals(1, service.getAverageRating("minesweeper"));
    }

    @Test
    void resetClearsAllRatings() {
        service.setRating(new Rating("flood", "Alice", 5, new Date()));
        service.reset();
        assertEquals(0, service.getAverageRating("flood"));
        assertEquals(0, service.getRating("flood", "Alice"));
    }

    @Test
    void multiplePlayersAverageRoundedCorrectly() {
        service.setRating(new Rating("flood", "Alice", 5, new Date()));
        service.setRating(new Rating("flood", "Bob", 4, new Date()));
        service.setRating(new Rating("flood", "Carol", 4, new Date()));
        int avg = service.getAverageRating("flood");
        // Average = (5+4+4)/3 = 4.33... rounds to 4
        assertEquals(4, avg);
    }
}
