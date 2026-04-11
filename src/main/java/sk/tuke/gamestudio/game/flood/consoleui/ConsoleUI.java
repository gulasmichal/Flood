package sk.tuke.gamestudio.game.flood.consoleui;

import sk.tuke.gamestudio.entity.Comment;
import sk.tuke.gamestudio.entity.Rating;
import sk.tuke.gamestudio.entity.Score;
import sk.tuke.gamestudio.game.flood.core.Field;
import sk.tuke.gamestudio.game.flood.core.GameState;
import sk.tuke.gamestudio.game.flood.core.Tile;
import sk.tuke.gamestudio.game.flood.core.TileColor;
import sk.tuke.gamestudio.service.CommentService;
import sk.tuke.gamestudio.service.RatingService;
import sk.tuke.gamestudio.service.ScoreService;

import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class ConsoleUI {
    private static final String GAME_NAME = "flood";

    private Field field;
    private String playerName;
    private String difficultyName;
    private final Scanner scanner = new Scanner(System.in);

    private final ScoreService scoreService;
    private final CommentService commentService;
    private final RatingService ratingService;


    private static final String RESET  = "\033[0m";
    private static final String BOLD   = "\033[1m";
    private static final String BG_RED    = "\033[41m";
    private static final String BG_BLUE   = "\033[44m";
    private static final String BG_GREEN  = "\033[42m";
    private static final String BG_YELLOW = "\033[43m";
    private static final String BG_PURPLE = "\033[45m";
    private static final String BG_ORANGE = "\033[48;5;208m";

    public ConsoleUI(ScoreService scoreService, CommentService commentService, RatingService ratingService) {
        this.scoreService = scoreService;
        this.commentService = commentService;
        this.ratingService = ratingService;
    }

    private Field selectDifficulty() {
        System.out.println("\nZvolte narocnost:");
        System.out.println("  1. Jednoducha  ( 6x6,  20 tahov)");
        System.out.println("  2. Stredna     ( 8x8,  25 tahov)");
        System.out.println("  3. Tazka       (12x12, 30 tahov)");
        while (true) {
            System.out.print("Vasa volba (1-3): ");
            switch (scanner.nextLine().trim()) {
                case "1": difficultyName = "Jednoducha";  return new Field(6,  6,  20);
                case "2": difficultyName = "Stredna";     return new Field(8,  8,  25);
                case "3": difficultyName = "Tazka";       return new Field(12, 12, 30);
                default:  System.out.println("Neplatna volba. Zadajte 1, 2 alebo 3.");
            }
        }
    }

    public void play() {
        this.field = selectDifficulty();

        if (playerName == null) {
            System.out.print("Zadajte svoje meno: ");
            playerName = scanner.nextLine().trim();
            if (playerName.isEmpty()) {
                playerName = "Anonym";
            }
        }

        System.out.printf("%nHrac: %s  |  Narocnost: %s%n%n", playerName, difficultyName);
        this.field.generate();

        do {
            show();
            showProgress();
            System.out.printf("Tah: %d/%d%n", this.field.getMoveCount(), this.field.getMaxMoves());
            handleInput();
        } while (this.field.getGameState() == GameState.PLAYING);

        show();

        if (this.field.getGameState() == GameState.SOLVED) {
            System.out.println("Gratulujeme, vyhrali ste!");
            int points = this.field.getMaxMoves() - this.field.getMoveCount();
            saveScore(points);
        } else if (this.field.getGameState() == GameState.FAILED) {
            System.out.println("Prehra! Minuli ste vsetky tahy.");
        }

        showTopScores();
        showAverageRating();
        promptComment();
        promptRating();

        System.out.print("Prajete si zacat novu hru (A/N)? ");
        String answer = scanner.nextLine().trim().toUpperCase();
        if (answer.equals("A")) {
            play();
        }
    }

    private String scoreGameName() {
        return GAME_NAME + "-" + difficultyName.toLowerCase();
    }

    private void saveScore(int points) {
        try {
            scoreService.addScore(new Score(scoreGameName(), playerName, points, new Date()));
            System.out.println("Skore ulozene: " + points + " bodov.");
        } catch (Exception e) {
            System.out.println("Nepodarilo sa ulozit skore: " + e.getMessage());
        }
    }

    private void showTopScores() {
        try {
            List<Score> scores = scoreService.getTopScores(scoreGameName());
            if (scores.isEmpty()) {
                System.out.println("Zatial ziadne skore.");
                return;
            }
            System.out.println("\n--- TOP 10 SKORE [" + difficultyName + "] ---");
            int rank = 1;
            for (Score s : scores) {
                System.out.printf("%2d. %-20s %4d bodov  (%s)%n", rank++, s.getPlayer(), s.getPoints(), s.getPlayedOn());
            }
            System.out.println("----------------------------------");
        } catch (Exception e) {
            System.out.println("Nepodarilo sa nacitat skore: " + e.getMessage());
        }
    }

    private void showAverageRating() {
        try {
            int avg = ratingService.getAverageRating(GAME_NAME);
            if (avg == 0) {
                System.out.println("Hra este nebola hodnotena.");
            } else {
                System.out.printf("Priemerne hodnotenie hry: %d/5 hviezdicky%n", avg);
            }
        } catch (Exception e) {
            System.out.println("Nepodarilo sa nacitat hodnotenie: " + e.getMessage());
        }
    }

    private void promptComment() {
        System.out.print("Chcete pridat komentar? (A/N): ");
        String answer = scanner.nextLine().trim().toUpperCase();
        if (!answer.equals("A")) return;

        System.out.print("Vas komentar: ");
        String content = scanner.nextLine().trim();
        if (content.isEmpty()) return;

        try {
            commentService.addComment(new Comment(GAME_NAME, playerName, content, new Date()));
            System.out.println("Komentar ulozeny.");

            List<Comment> comments = commentService.getComments(GAME_NAME);
            System.out.println("\n--- KOMENTARE ---");
            for (Comment c : comments) {
                System.out.printf("[%s] %s: %s%n", c.getCommentedOn(), c.getPlayer(), c.getContent());
            }
            System.out.println("-----------------");
        } catch (Exception e) {
            System.out.println("Nepodarilo sa ulozit komentar: " + e.getMessage());
        }
    }

    private void promptRating() {
        System.out.print("Chcete ohodnotit hru (1-5 hviezdicky)? (A/N): ");
        String answer = scanner.nextLine().trim().toUpperCase();
        if (!answer.equals("A")) return;

        int stars = 0;
        while (stars < 1 || stars > 5) {
            System.out.print("Zadajte hodnotenie (1-5): ");
            String input = scanner.nextLine().trim();
            try {
                stars = Integer.parseInt(input);
                if (stars < 1 || stars > 5) {
                    System.out.println("Hodnotenie musi byt medzi 1 a 5.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Neplatny vstup.");
            }
        }

        try {
            ratingService.setRating(new Rating(GAME_NAME, playerName, stars, new Date()));
            System.out.println("Hodnotenie ulozene.");
        } catch (Exception e) {
            System.out.println("Nepodarilo sa ulozit hodnotenie: " + e.getMessage());
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
        while (true) {
            printLegend();
            System.out.print("Zadajte farbu (R/G/B/Y/P/O), ? napoveda, X ukoncenie: ");
            String input = scanner.nextLine().trim().toUpperCase();

            if (input.equals("X")) {
                System.out.println("Koniec hry.");
                System.exit(0);
            }

            if (input.equals("?")) {
                showHint();
                continue;
            }

            TileColor color = switch (input) {
                case "R" -> TileColor.RED;
                case "G" -> TileColor.GREEN;
                case "B" -> TileColor.BLUE;
                case "Y" -> TileColor.YELLOW;
                case "P" -> TileColor.PURPLE;
                case "O" -> TileColor.ORANGE;
                default -> null;
            };

            if (color == null) {
                System.out.println("Neplatna farba. Zadajte R, G, B, Y, P alebo O.");
                continue;
            }

            if (color == field.getTile(0, 0).getColor()) {
                System.out.println("Tato farba uz je aktivna. Zvolte inu farbu.");
                continue;
            }

            field.flood(color);
            break;
        }
    }

    private void showProgress() {
        int total = field.getRows() * field.getCols();
        int flooded = field.getFloodedCount();
        int barWidth = 20;
        int filled = (int) Math.round((double) flooded / total * barWidth);
        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < barWidth; i++) bar.append(i < filled ? '█' : '░');
        bar.append("]");
        System.out.printf("Zaplavene: %s %d%% (%d/%d)%n", bar, flooded * 100 / total, flooded, total);
    }

    private void showHint() {
        TileColor hint = field.getBestHintColor();
        if (hint != null) {
            System.out.printf("Napoveda: zvolte %s%s %c %s%n",
                    colorToBg(hint), BOLD, colorToChar(hint), RESET);
        }
    }
}
