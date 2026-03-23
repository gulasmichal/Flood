package sk.tuke.gamestudio.service;

import sk.tuke.gamestudio.entity.Rating;

import java.sql.*;

public class RatingServiceJDBC implements RatingService {
    public static final String URL = "jdbc:postgresql://localhost/gamestudio";
    public static final String USER = "michalgulas";
    public static final String PASSWORD = "postgres";

    private static final String DELETE_ONE = "DELETE FROM rating WHERE game = ? AND player = ?";
    private static final String INSERT = "INSERT INTO rating (game, player, stars, ratedOn) VALUES (?, ?, ?, ?)";
    private static final String SELECT_AVG = "SELECT COALESCE(ROUND(AVG(stars)), 0) FROM rating WHERE game = ?";
    private static final String SELECT_ONE = "SELECT stars FROM rating WHERE game = ? AND player = ?";
    private static final String DELETE_ALL = "DELETE FROM rating";

    @Override
    public void setRating(Rating rating) {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            try (PreparedStatement del = connection.prepareStatement(DELETE_ONE)) {
                del.setString(1, rating.getGame());
                del.setString(2, rating.getPlayer());
                del.executeUpdate();
            }
            try (PreparedStatement ins = connection.prepareStatement(INSERT)) {
                ins.setString(1, rating.getGame());
                ins.setString(2, rating.getPlayer());
                ins.setInt(3, rating.getStars());
                ins.setTimestamp(4, new Timestamp(rating.getRatedOn().getTime()));
                ins.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RatingException("Problem setting rating", e);
        }
    }

    @Override
    public int getAverageRating(String game) {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(SELECT_AVG)) {
            statement.setString(1, game);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        } catch (SQLException e) {
            throw new RatingException("Problem getting average rating", e);
        }
    }

    @Override
    public int getRating(String game, String player) {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(SELECT_ONE)) {
            statement.setString(1, game);
            statement.setString(2, player);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        } catch (SQLException e) {
            throw new RatingException("Problem getting rating", e);
        }
    }

    @Override
    public void reset() {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(DELETE_ALL);
        } catch (SQLException e) {
            throw new RatingException("Problem deleting ratings", e);
        }
    }
}
