package sk.tuke.gamestudio.game.flood.consoleui;

import sk.tuke.gamestudio.game.flood.core.Field;
import sk.tuke.gamestudio.game.flood.core.GameState;
import sk.tuke.gamestudio.game.flood.core.Tile;
import sk.tuke.gamestudio.game.flood.core.TileColor;

import java.util.Scanner;

public class ConsoleUI {

    private final Field field;
    private final Scanner scanner;

    public ConsoleUI(Field field) {
        this.field = field;
        this.scanner = new Scanner(System.in);
    }

    public void play() {
        field.generate();
        while (field.getGameState() == GameState.PLAYING) {
            printField();
            System.out.printf("Tah: %d/%d%n", field.getMoveCount(), field.getMaxMoves());
            TileColor color = readInput();
            if (color != null) {
                field.flood(color);
            }
        }
        printField();
        printGameResult();
    }

    public void printField() {
        System.out.print("  ");
        for (int c = 0; c < field.getCols(); c++) {
            System.out.printf("%2d", c + 1);
        }
        System.out.println();
        for (int r = 0; r < field.getRows(); r++) {
            System.out.printf("%2d", r + 1);
            for (int c = 0; c < field.getCols(); c++) {
                Tile tile = field.getTile(r, c);
                System.out.print(" " + colorToChar(tile.getColor()));
            }
            System.out.println();
        }
        System.out.println();
    }

    private char colorToChar(TileColor color) {
        return switch (color) {
            case RED    -> 'R';
            case BLUE   -> 'B';
            case GREEN  -> 'G';
            case YELLOW -> 'Y';
            case PURPLE -> 'P';
            case ORANGE -> 'O';
        };
    }

    public TileColor readInput() {
        System.out.print("Zvolte farbu (R/B/G/Y/P/O): ");
        String input = scanner.nextLine().trim().toUpperCase();
        return switch (input) {
            case "R" -> TileColor.RED;
            case "B" -> TileColor.BLUE;
            case "G" -> TileColor.GREEN;
            case "Y" -> TileColor.YELLOW;
            case "P" -> TileColor.PURPLE;
            case "O" -> TileColor.ORANGE;
            default  -> {
                System.out.println("Neplatny vstup. Zadajte R, B, G, Y, P alebo O.");
                yield null;
            }
        };
    }

    public void printGameResult() {
        if (field.getGameState() == GameState.SOLVED) {
            System.out.println("Gratulujeme! Zaplavili ste cele pole za " + field.getMoveCount() + " tahov!");
        } else {
            System.out.println("Prehra! Minuli ste vsetky tahy. Skuste to znova.");
        }
    }
}
