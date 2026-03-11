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

    // ANSI background colors + reset
    private static final String RESET  = "\033[0m";
    private static final String BOLD   = "\033[1m";
    private static final String BG_RED    = "\033[41m";
    private static final String BG_BLUE   = "\033[44m";
    private static final String BG_GREEN  = "\033[42m";
    private static final String BG_YELLOW = "\033[43m";
    private static final String BG_PURPLE = "\033[45m";
    private static final String BG_ORANGE = "\033[48;5;208m";

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
            System.out.printf("%3d", c + 1);
        }
        System.out.println();
        for (int r = 0; r < field.getRows(); r++) {
            System.out.printf("%2d ", r + 1);
            for (int c = 0; c < field.getCols(); c++) {
                Tile tile = field.getTile(r, c);
                String bg = colorToBg(tile.getColor());
                char ch = colorToChar(tile.getColor());
                System.out.print(bg + BOLD + " " + ch + " " + RESET);
            }
            System.out.println();
        }
        System.out.println();
    }

    private String colorToBg(TileColor color) {
        return switch (color) {
            case RED    -> BG_RED;
            case BLUE   -> BG_BLUE;
            case GREEN  -> BG_GREEN;
            case YELLOW -> BG_YELLOW;
            case PURPLE -> BG_PURPLE;
            case ORANGE -> BG_ORANGE;
        };
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

    private void printLegend() {
        System.out.print("Farby: ");
        TileColor[] colors = TileColor.values();
        for (TileColor c : colors) {
            System.out.print(colorToBg(c) + BOLD + " " + colorToChar(c) + " " + RESET + " ");
        }
        System.out.println();
    }

    public void handleInput() {
        String input;
        Matcher matcher;
        do {
            printLegend();
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
