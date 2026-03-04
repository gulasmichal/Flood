package sk.tuke.gamestudio.game.flood.core;

import java.util.LinkedList;
import java.util.Queue;

public class Field {

    public static final int DEFAULT_SIZE = 8;
    public static final int DEFAULT_MAX_MOVES = 25;

    private final int rows;
    private final int cols;
    private final int maxMoves;
    private int moveCount;
    private Tile[][] tiles;
    private GameState gameState;

    public Field(int rows, int cols, int maxMoves) {
        this.rows = rows;
        this.cols = cols;
        this.maxMoves = maxMoves;
        generate();
    }

    public Field() {
        this(DEFAULT_SIZE, DEFAULT_SIZE, DEFAULT_MAX_MOVES);
    }

    public void generate() {
        moveCount = 0;
        gameState = GameState.PLAYING;
        tiles = new Tile[rows][cols];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                tiles[r][c] = new Tile(TileColor.random());
            }
        }

        // flood initial connected region from (0,0)
        initialFlood(tiles[0][0].getColor());
    }

    /**
     * Flood the board with the chosen color.
     * All tiles in the current flooded region change to color,
     * then adjacent tiles of that color are absorbed into the region.
     */
    public void flood(TileColor color) {
        if (gameState != GameState.PLAYING) return;

        TileColor currentColor = tiles[0][0].getColor();
        if (color == currentColor) return;

        moveCount++;

        // recolor the existing flooded region
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (tiles[r][c].getState() == TileState.FLOODED) {
                    tiles[r][c].setColor(color);
                }
            }
        }

        // BFS - expand flooded region to neighbors with the same color
        Queue<int[]> queue = new LinkedList<>();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (tiles[r][c].getState() == TileState.FLOODED) {
                    queue.add(new int[]{r, c});
                }
            }
        }

        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        while (!queue.isEmpty()) {
            int[] pos = queue.poll();
            for (int[] dir : directions) {
                int nr = pos[0] + dir[0];
                int nc = pos[1] + dir[1];
                if (isValid(nr, nc)
                        && tiles[nr][nc].getState() == TileState.NOT_FLOODED
                        && tiles[nr][nc].getColor() == color) {
                    tiles[nr][nc].setState(TileState.FLOODED);
                    queue.add(new int[]{nr, nc});
                }
            }
        }

        updateGameState();
    }

    /**
     * BFS flood fill from (0,0) to mark the initial connected region.
     */
    private void initialFlood(TileColor color) {
        Queue<int[]> queue = new LinkedList<>();
        tiles[0][0].setState(TileState.FLOODED);
        queue.add(new int[]{0, 0});

        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        while (!queue.isEmpty()) {
            int[] pos = queue.poll();
            for (int[] dir : directions) {
                int nr = pos[0] + dir[0];
                int nc = pos[1] + dir[1];
                if (isValid(nr, nc)
                        && tiles[nr][nc].getState() == TileState.NOT_FLOODED
                        && tiles[nr][nc].getColor() == color) {
                    tiles[nr][nc].setState(TileState.FLOODED);
                    queue.add(new int[]{nr, nc});
                }
            }
        }
    }

    private boolean isValid(int r, int c) {
        return r >= 0 && r < rows && c >= 0 && c < cols;
    }

    private void updateGameState() {
        if (isSolved()) {
            gameState = GameState.SOLVED;
        } else if (moveCount >= maxMoves) {
            gameState = GameState.FAILED;
        }
    }

    public boolean isSolved() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (tiles[r][c].getState() == TileState.NOT_FLOODED) {
                    return false;
                }
            }
        }
        return true;
    }

    public Tile getTile(int row, int col) {
        return tiles[row][col];
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public int getMoveCount() {
        return moveCount;
    }

    public int getMaxMoves() {
        return maxMoves;
    }

    public GameState getGameState() {
        return gameState;
    }
}
