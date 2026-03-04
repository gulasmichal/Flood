package sk.tuke.gamestudio.game.flood.core;

public enum TileColor {
    RED, BLUE, GREEN, YELLOW, PURPLE, ORANGE;

    public static TileColor random() {
        TileColor[] values = values();
        return values[(int) (Math.random() * values.length)];
    }
}
