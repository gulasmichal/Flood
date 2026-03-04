package sk.tuke.gamestudio.game.flood;

import sk.tuke.gamestudio.game.flood.consoleui.ConsoleUI;
import sk.tuke.gamestudio.game.flood.core.Field;

public class Flood {

    public static void main(String[] args) {
        Field field = new Field();
        ConsoleUI ui = new ConsoleUI(field);
        ui.play();
    }
}
