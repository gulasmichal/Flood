package sk.tuke.gamestudio.game.flood.consoleui;

import sk.tuke.gamestudio.game.flood.core.Field;
import sk.tuke.gamestudio.game.flood.core.GameState;
import sk.tuke.gamestudio.game.flood.core.Tile;
import sk.tuke.gamestudio.game.flood.core.TileColor;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConsoleUI {

    private Field field;
    private final Scanner scanner = new Scanner(System.in);

    // A.1: case-insensitive, also accepts X/x for exit
    private static final Pattern INPUT_PATTERN =
            Pattern.compile("[RBGYPOX]", Pattern.CASE_INSENSITIVE);

    public void play(Field field) {
        this.field = field;
        this.field.generate();

        do {
            show();
            System.out.printf("Tah: %d/%d%n", field.getMoveCount(), field.getMaxMoves());
            handleInput();
        } while (field.getGameState() == GameState.PLAYING);

        show();

        if (field.getGameState() == GameState.SOLVED) {
            System.out.println("Gratulujeme, vyhrali ste!");
        } else if (field.getGameState() == GameState.FAILED) {
            System.out.println("Prehra! Minuli ste vsetky tahy.");
        }

        // A.2: offer new game
        System.out.print("Prajete si zacat novu hru (A/N)? ");
        String answer = scanner.nextLine().trim().toUpperCase();
        if (answer.equals("A")) {
            play(new Field());
        }
    }

    public void show() {
        System.out.print("   ");
        for (int c = 0; c < field.getCols(); c++) {
            System.out.printf("%2d", c + 1);
        }
        System.out.println();
        for (int r = 0; r < field.getRows(); r++) {
            System.out.printf("%2d ", r + 1);
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

    public void handleInput() {
        String input;
        Matcher matcher;
        do {
            System.out.print("Zvolte farbu (R/B/G/Y/P/O) alebo X pre ukoncenie: ");
            input = scanner.nextLine().trim();
            matcher = INPUT_PATTERN.matcher(input);
            if (!matcher.matches()) {
                System.out.println("Neplatny vstup. Zadajte R, B, G, Y, P, O alebo X.");
            }
        } while (!matcher.matches());

        if (input.equalsIgnoreCase("X")) {
            System.out.println("Koniec hry.");
            System.exit(0);
        }

        TileColor color = switch (input.toUpperCase()) {
            case "R" -> TileColor.RED;
            case "B" -> TileColor.BLUE;
            case "G" -> TileColor.GREEN;
            case "Y" -> TileColor.YELLOW;
            case "P" -> TileColor.PURPLE;
            case "O" -> TileColor.ORANGE;
            default  -> null;
        };

        if (color != null) {
            field.flood(color);
        }
    }
}
