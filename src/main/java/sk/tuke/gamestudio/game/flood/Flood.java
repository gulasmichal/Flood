package sk.tuke.gamestudio.game.flood;

import sk.tuke.gamestudio.game.flood.consoleui.ConsoleUI;
import sk.tuke.gamestudio.service.CommentServiceJDBC;
import sk.tuke.gamestudio.service.RatingServiceJDBC;
import sk.tuke.gamestudio.service.ScoreServiceJDBC;

public class Flood {

    public static void main(String[] args) {
        ConsoleUI ui = new ConsoleUI(
                new ScoreServiceJDBC(),
                new CommentServiceJDBC(),
                new RatingServiceJDBC()
        );
        ui.play();
    }
}
