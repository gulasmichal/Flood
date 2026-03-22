# Návrh hry Flood (Flood It)

## Popis hry

**Flood** (Flood It) je logická dlaždicová hra pre jedného hráča. Hracie pole je mriežka (napr. 8×8) vyplnená farebnými dlaždicami. Hráč začína v ľavom hornom rohu a v každom ťahu si zvolí farbu. Všetky dlaždice súvisle prepojené s ľavým horným rohom (rovnakej farby) zmenia farbu na zvolenú. Cieľom je zaplaviť celé pole jednou farbou v obmedzenom počte ťahov.

Viac informácií: [Flood-It (Wikipedia)](https://en.wikipedia.org/wiki/Flood-It!)

---

## Krok 2 – Konceptuálny model (Diagram tried)

### Úloha 2.1 – Analýza objektov a akcií

#### Identifikované objekty (entity)

| Objekt          | Vlastnosti                                                  | Akcie                                                                 |
|-----------------|-------------------------------------------------------------|-----------------------------------------------------------------------|
| **Field**       | `rows`, `cols`, `tiles[][]`, `maxMoves`, `moveCount`        | `generate()`, `flood(color)`, `getFloodRegion()`, `isSolved()`, `getTile(r,c)` |
| **Tile**        | `color`, `state`                                            | `setColor(color)`, `getColor()`, `getState()`                         |
| **TileColor**   | `RED`, `BLUE`, `GREEN`, `YELLOW`, `PURPLE`, `ORANGE`        | –                                                                     |
| **TileState**   | `FLOODED`, `NOT_FLOODED`                                    | –                                                                     |
| **GameState**   | `PLAYING`, `SOLVED`, `FAILED`                               | –                                                                     |
| **Flood**       | `field`, `gameState`                                        | `main()`                                                              |
| **ConsoleUI**   | `field`, `gameState`                                        | `play()`, `printField()`, `readInput()`, `printGameResult()`          |

#### Vzťahy medzi objektami

- **Field** *obsahuje* (kompozícia) pole **Tile** objektov (1 : N).
- **Tile** *má* (asociácia) **TileColor** a **TileState**.
- **ConsoleUI** *používa* (asociácia) **Field** a **GameState**.
- **Flood** *vytvára* **ConsoleUI** a **Field**.

### Úloha 2.2 – Štruktúra balíkov a tried

```
flood/                          — základný balík
├── Flood.java                  — hlavná trieda aplikácie s metódou main()
│
├── core/                       — balík s hernou logikou (nezávislý od UI)
│   ├── Field.java              — hracie pole, obsahuje dlaždice, logiku zaplavenia
│   ├── Tile.java               — dlaždica s farbou a stavom
│   ├── TileColor.java          — enumeračný typ – farba dlaždice (RED, BLUE, GREEN, YELLOW, PURPLE, ORANGE)
│   ├── TileState.java          — enumeračný typ – stav dlaždice (FLOODED, NOT_FLOODED)
│   └── GameState.java          — enumeračný typ – stav hry (PLAYING, SOLVED, FAILED)
│
└── consoleui/                  — balík pre konzolové rozhranie
    └── ConsoleUI.java          — trieda definujúca interakciu hry s používateľom
```

### Diagram tried (textová reprezentácia UML)

```
┌──────────────────────────┐
│       <<enum>>           │
│       TileColor          │
├──────────────────────────┤
│ RED                      │
│ BLUE                     │
│ GREEN                    │
│ YELLOW                   │
│ PURPLE                   │
│ ORANGE                   │
└──────────────────────────┘

┌──────────────────────────┐
│       <<enum>>           │
│       TileState          │
├──────────────────────────┤
│ FLOODED                  │
│ NOT_FLOODED              │
└──────────────────────────┘

┌──────────────────────────┐
│       <<enum>>           │
│       GameState          │
├──────────────────────────┤
│ PLAYING                  │
│ SOLVED                   │
│ FAILED                   │
└──────────────────────────┘

┌──────────────────────────┐         ┌──────────────────────────┐
│         Tile             │         │         Field            │
├──────────────────────────┤         ├──────────────────────────┤
│ - color: TileColor       │  N   1  │ - rows: int              │
│ - state: TileState       │◄────────│ - cols: int              │
├──────────────────────────┤         │ - tiles: Tile[][]        │
│ + getColor(): TileColor  │         │ - maxMoves: int          │
│ + setColor(TileColor)    │         │ - moveCount: int         │
│ + getState(): TileState  │         │ - colorCount: int        │
│ + setState(TileState)    │         ├──────────────────────────┤
└──────────────────────────┘         │ + generate(): void       │
                                     │ + flood(TileColor): void │
                                     │ + getFloodRegion(): List │
                                     │ + isSolved(): boolean    │
                                     │ + getGameState(): GameState│
                                     │ + getTile(r,c): Tile     │
                                     │ + getRows(): int         │
                                     │ + getCols(): int         │
                                     │ + getMoveCount(): int    │
                                     │ + getMaxMoves(): int     │
                                     └──────────┬───────────────┘
                                                │ uses
                                                ▼
┌──────────────────────────┐         ┌──────────────────────────┐
│        Flood             │         │       ConsoleUI          │
├──────────────────────────┤         ├──────────────────────────┤
│                          │         │ - field: Field           │
├──────────────────────────┤────────►├──────────────────────────┤
│ + main(String[]): void   │ creates │ + play(): void           │
└──────────────────────────┘         │ + printField(): void     │
                                     │ + readInput(): TileColor │
                                     │ + printGameResult(): void│
                                     └──────────────────────────┘
```

---

## Krok 3 – Stavový diagram

### Úloha 3.1 – Stavy dlaždíc

Dlaždica v hre Flood môže byť v dvoch stavoch:

| Stav            | Popis                                                              |
|-----------------|--------------------------------------------------------------------|
| `NOT_FLOODED`   | Dlaždica ešte nie je súčasťou zaplavenej oblasti (počiatočný stav) |
| `FLOODED`       | Dlaždica je súčasťou zaplavenej oblasti (prepojená s ľavým horným rohom) |

```
Stavový diagram dlaždice (TileState):

                    ┌─────────────────┐
                    │                 │
         ┌─────────▼──────────┐      │ flood() – dlaždica susedí
         │                    │      │ so zaplavenou oblasťou
         │   NOT_FLOODED      │──────┘ a má rovnakú farbu
         │   (počiatočný)     │
         └─────────┬──────────┘
                   │
                   │ flood(color) – dlaždica susedí
                   │ so zaplavenou oblasťou
                   │ a má rovnakú farbu ako zvolená
                   ▼
         ┌────────────────────┐
         │                    │
         │     FLOODED        │
         │                    │
         └────────────────────┘
```

**Poznámka:** Prechod z `NOT_FLOODED` do `FLOODED` je jednosmerný – raz zaplavená dlaždica sa už nikdy neodplaví.

### Stavy hry

| Stav       | Popis                                                          |
|------------|----------------------------------------------------------------|
| `PLAYING`  | Hra prebieha, hráč vyberá farby.                               |
| `SOLVED`   | Celé pole je zaplavené jednou farbou – hráč vyhral.            |
| `FAILED`   | Hráč vyčerpal maximálny počet ťahov bez zaplavenia celého poľa.|

```
Stavový diagram hry (GameState):

                ┌──────────────────────┐
                │     PLAYING          │
                │   (počiatočný)       │
                └──┬───────────────┬───┘
                   │               │
    isSolved()     │               │  moveCount >= maxMoves
    == true        │               │  && !isSolved()
                   │               │
                   ▼               ▼
         ┌──────────────┐  ┌──────────────┐
         │   SOLVED     │  │   FAILED     │
         │  (vyhratá)   │  │  (prehratá)  │
         └──────────────┘  └──────────────┘
```

**Prechody:**

| Z stavu    | Do stavu   | Podmienka / Akcia                                          |
|------------|------------|-------------------------------------------------------------|
| `PLAYING`  | `PLAYING`  | `flood(color)` – pole nie je celé zaplavené a ostávajú ťahy |
| `PLAYING`  | `SOLVED`   | `flood(color)` → `isSolved() == true` (všetky dlaždice sú `FLOODED`) |
| `PLAYING`  | `FAILED`   | `flood(color)` → `moveCount >= maxMoves` a `!isSolved()`    |

---

## Krok 4 – Základná logika hry

### Úloha 4.1 – Opis hernej logiky

#### 1. Generovanie herného poľa (`Field.generate()`)

1. Vytvoriť 2D pole dlaždíc `tiles[rows][cols]`.
2. Pre každú pozíciu `(r, c)` v poli:
   - Vytvoriť novú dlaždicu `Tile` s **náhodnou farbou** z `TileColor` (napr. 6 farieb).
   - Nastaviť stav dlaždice na `NOT_FLOODED`.
3. Dlaždicu na pozícii `(0, 0)` (ľavý horný roh) nastaviť na stav `FLOODED`.
4. Všetky dlaždice susediace s `(0, 0)`, ktoré majú **rovnakú farbu**, taktiež označiť ako `FLOODED` (rekurzívne / BFS).
5. Inicializovať `moveCount = 0`.

#### 2. Ťah hráča – zaplavenie (`Field.flood(TileColor color)`)

Zaplavenie je hlavná herná akcia. Po zvolení farby hráčom:

1. Ak je zvolená farba **rovnaká** ako aktuálna farba zaplavenej oblasti → ťah sa **nepočíta**, nič sa nemení.
2. Zvýšiť `moveCount` o 1.
3. Zmeniť farbu **všetkých** dlaždíc v stave `FLOODED` na zvolenú farbu `color`.
4. Nájsť **nové dlaždice**, ktoré susedia so zaplavenou oblasťou a majú farbu `color`:
   - Použiť **BFS (prehľadávanie do šírky)** alebo **flood fill** algoritmus:
     - Začať od všetkých aktuálne `FLOODED` dlaždíc.
     - Pre každú `FLOODED` dlaždicu skontrolovať susedov (hore, dole, vľavo, vpravo).
     - Ak sused má farbu `color` a stav `NOT_FLOODED` → zmeniť stav na `FLOODED` a pridať do fronty.
5. Opakovať krok 4, kým sa nájdu nové dlaždice.

**Pseudokód:**
```
flood(color):
    if currentFloodColor == color:
        return   // žiadna zmena

    moveCount++

    // Zmeniť farbu existujúcej zaplavenej oblasti
    for each tile in tiles:
        if tile.state == FLOODED:
            tile.setColor(color)

    // BFS – rozšíriť zaplavenou oblasť
    queue = všetky dlaždice so stavom FLOODED
    while queue is not empty:
        current = queue.dequeue()
        for each neighbor of current (hore, dole, vľavo, vpravo):
            if neighbor.state == NOT_FLOODED and neighbor.color == color:
                neighbor.setState(FLOODED)
                neighbor.setColor(color)
                queue.enqueue(neighbor)
```

#### 3. Overovanie stavu hry

Po každom ťahu (`flood()`) sa kontroluje stav hry:

```
getGameState():
    if isSolved():
        return SOLVED
    if moveCount >= maxMoves:
        return FAILED
    return PLAYING
```

**`isSolved()`** – hra je vyhratá, ak **všetky** dlaždice v poli majú stav `FLOODED`:

```
isSolved():
    for each tile in tiles:
        if tile.state == NOT_FLOODED:
            return false
    return true
```

#### 4. Herná slučka (`ConsoleUI.play()`)

```
play():
    field.generate()
    while field.getGameState() == PLAYING:
        printField()
        print("Ťah " + field.getMoveCount() + "/" + field.getMaxMoves())
        color = readInput()          // hráč zvolí farbu
        field.flood(color)           // vykonanie ťahu
    printField()
    printGameResult()                // výpis výsledku (SOLVED / FAILED)
```

#### 5. Zobrazenie herného poľa (`ConsoleUI.printField()`)

- Pre každú dlaždicu sa vypíše písmeno alebo symbol reprezentujúci jej farbu.
- Príklad mapovanie farieb na znaky:

| TileColor | Symbol |
|-----------|--------|
| RED       | `R`    |
| BLUE      | `B`    |
| GREEN     | `G`    |
| YELLOW    | `Y`    |
| PURPLE    | `P`    |
| ORANGE    | `O`    |

**Príklad výstupu (8×8 pole):**
```
  1 2 3 4 5 6 7 8
1 R R B G Y P O R
2 B R G G Y O O B
3 G G R B P Y R G
4 Y B G R R B G Y
5 P R Y G B R B P
6 O G B Y G G R O
7 R B P O R B G R
8 B G R R Y P O B

Ťah: 3/25
Zvoľte farbu (R/B/G/Y/P/O):
```

---