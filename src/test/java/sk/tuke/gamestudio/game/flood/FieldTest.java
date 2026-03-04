package sk.tuke.gamestudio.game.flood;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sk.tuke.gamestudio.game.flood.core.Field;
import sk.tuke.gamestudio.game.flood.core.GameState;
import sk.tuke.gamestudio.game.flood.core.TileColor;
import sk.tuke.gamestudio.game.flood.core.TileState;

import static org.junit.jupiter.api.Assertions.*;

public class FieldTest {

    private Field field;

    @BeforeEach
    public void setUp() {
        field = new Field(8, 8, 25);
    }

    @Test
    public void fieldIsInitialized() {
        assertNotNull(field);
        assertEquals(GameState.PLAYING, field.getGameState());
        assertEquals(0, field.getMoveCount());
    }

    @Test
    public void allTilesAreNotNull() {
        for (int r = 0; r < field.getRows(); r++) {
            for (int c = 0; c < field.getCols(); c++) {
                assertNotNull(field.getTile(r, c));
                assertNotNull(field.getTile(r, c).getColor());
            }
        }
    }

    @Test
    public void topLeftTileIsFlooded() {
        assertEquals(TileState.FLOODED, field.getTile(0, 0).getState());
    }

    @Test
    public void adjacentSameColorTilesAreFloodedAtStart() {
        TileColor startColor = field.getTile(0, 0).getColor();
        if (field.getTile(0, 1).getColor() == startColor) {
            assertEquals(TileState.FLOODED, field.getTile(0, 1).getState());
        }
        if (field.getTile(1, 0).getColor() == startColor) {
            assertEquals(TileState.FLOODED, field.getTile(1, 0).getState());
        }
    }

    @Test
    public void moveCountIncreasesAfterFlood() {
        TileColor different = getDifferentColor(field.getTile(0, 0).getColor());
        field.flood(different);
        assertEquals(1, field.getMoveCount());
    }

    @Test
    public void sameColorFloodDoesNotIncreaseMoveCount() {
        TileColor current = field.getTile(0, 0).getColor();
        field.flood(current);
        assertEquals(0, field.getMoveCount());
    }

    @Test
    public void floodedTileHasNewColor() {
        TileColor different = getDifferentColor(field.getTile(0, 0).getColor());
        field.flood(different);
        assertEquals(different, field.getTile(0, 0).getColor());
    }

    @Test
    public void gameFailsAfterMaxMoves() {
        Field tightField = new Field(8, 8, 1);
        TileColor different = getDifferentColor(tightField.getTile(0, 0).getColor());
        tightField.flood(different);
        if (!tightField.isSolved()) {
            assertEquals(GameState.FAILED, tightField.getGameState());
        }
    }

    @Test
    public void generateResetsField() {
        field.flood(getDifferentColor(field.getTile(0, 0).getColor()));
        field.generate();
        assertEquals(0, field.getMoveCount());
        assertEquals(GameState.PLAYING, field.getGameState());
        assertEquals(TileState.FLOODED, field.getTile(0, 0).getState());
    }

    @Test
    public void floodDoesNothingWhenGameNotPlaying() {
        Field tightField = new Field(8, 8, 1);
        TileColor different = getDifferentColor(tightField.getTile(0, 0).getColor());
        tightField.flood(different);
        // game is now either SOLVED or FAILED
        int movesBefore = tightField.getMoveCount();
        tightField.flood(getDifferentColor(tightField.getTile(0, 0).getColor()));
        assertEquals(movesBefore, tightField.getMoveCount());
    }

    private TileColor getDifferentColor(TileColor color) {
        for (TileColor c : TileColor.values()) {
            if (c != color) return c;
        }
        return color;
    }
}
