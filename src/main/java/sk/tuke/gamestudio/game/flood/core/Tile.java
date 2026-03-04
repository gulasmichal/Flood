package sk.tuke.gamestudio.game.flood.core;

public class Tile {

    private TileColor color;
    private TileState state;

    public Tile(TileColor color) {
        this.color = color;
        this.state = TileState.NOT_FLOODED;
    }

    public TileColor getColor() {
        return color;
    }

    public void setColor(TileColor color) {
        this.color = color;
    }

    public TileState getState() {
        return state;
    }

    public void setState(TileState state) {
        this.state = state;
    }
}
