# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

```bash
mvn clean compile        # Compile
mvn clean test           # Run all tests
mvn clean package        # Build JAR
mvn -Dtest=FieldTest test                        # Run single test class
mvn -Dtest=FieldTest#testFlood test              # Run single test method
```

## Database Setup

PostgreSQL is required. Create the database and tables before running:

```bash
psql -U michalgulas -d gamestudio -f src/main/resources/create-tables.sql
```

Connection is hardcoded in the JDBC service classes (localhost, database `gamestudio`, user `michalgulas`).

## Architecture

**Layered structure:**

1. **Core game logic** (`game/flood/core/`) — `Field`, `Tile`, and enums (`TileColor`, `TileState`, `GameState`). No UI or persistence dependencies.
2. **Console UI** (`game/flood/consoleui/ConsoleUI`) — reads input, renders ANSI-colored grid, and orchestrates game loop and post-game interactions.
3. **Entities** (`entity/`) — plain data objects: `Score`, `Comment`, `Rating`.
4. **Services** (`service/`) — interface + JDBC implementation pairs for each entity. Each service has a corresponding custom exception.

**Entry point:** `game/flood/Flood.main()` wires the three JDBC services into `ConsoleUI` and starts the game.

**Service pattern:** Each service (`ScoreService`, `CommentService`, `RatingService`) has an interface and a `*JDBC` implementation. Tests use a `reset()` method (called in `@BeforeEach`) to clear DB state between test runs.

**Flood fill algorithm:** BFS from `(0,0)`. On each move, all `FLOODED` tiles change color, then the BFS expands to adjacent `NOT_FLOODED` tiles matching the new color. Game ends when all tiles are flooded (`SOLVED`) or moves run out (`FAILED`).

## Testing Notes

- Service tests require a live PostgreSQL connection — they are integration tests, not unit tests.
- `FieldTest` tests pure game logic with no DB dependency.
- Game tests live in `src/test/java/sk/tuke/gamestudio/game/flood/`.
- Service tests live in `src/test/java/sk/tuke/gamestudio/service/`.
