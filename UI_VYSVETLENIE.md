# Vysvetlenie UI — Flood Game

Tento dokument vysvetľuje, ako funguje UI aplikácie pre niekoho, kto pozná React/Node.js, ale nie klasický server-rendered HTML so Spring MVC.

---

## 1. Základný rozdiel oproti Reactu

V Reacte žije stav (`useState`) v prehliadači. Každý klik zmení stav a React prerenduje komponent na strane klienta.

Tu funguje to inak — stav hry (rozohraná partia, počet ťahov, čas…) žije **na serveri v HTTP session**. Thymeleaf (obdoba JSX) vygeneruje kompletné HTML na serveri a pošle ho prehliadaču. Prehliadač ho len zobrazí — žiadna hydratácia, žiadny virtual DOM.

```
React:  klik → useState() → re-render v prehliadači
Tu:     klik → HTTP POST → server zmení session → server vygeneruje nové HTML → prehliadač zobrazí
```

---

## 2. Ako fungujú formuláre a tlačidlá (`th:action`)

### Klasický HTML formulár (POST + redirect)

Väčšina akcií v hre funguje cez štandardný HTML `<form>`:

```html
<form method="post" th:action="@{/new}" style="display:contents">
    <button type="submit">Nová hra</button>
</form>
```

- `method="post"` — prehliadač pošle HTTP POST požiadavku
- `th:action="@{/new}"` — Thymeleaf vygeneruje URL, napr. `/new`
- `type="submit"` — klik na tlačidlo odošle formulár

Na serveri controller spracuje POST, zmení session a vráti **redirect**:

```java
@PostMapping("/new")
public String newGame(@RequestParam String difficulty, HttpSession session) {
    session.setAttribute("field", newField(difficulty));   // nová hra do session
    // ...
    return "redirect:/";   // presmeruj prehliadač na GET /
}
```

Prehliadač dostane `302 Redirect`, sám zavolá `GET /`, server vygeneruje nové HTML s novým stavom a pošle ho. Toto sa volá **PRG (Post/Redirect/Get)** vzor — chráni pred opätovným odoslaním formulára pri obnovení stránky.

V Reacte by to bolo: `fetch('/api/new', { method: 'POST' }).then(() => setState(...))`.

### Select s automatickým odoslaním

```html
<select name="difficulty" id="diff" onchange="this.form.submit()">
```

`onchange="this.form.submit()"` — natívny JavaScript, pri zmene selectu okamžite odošle formulár. V Reactu by si na to použil `onChange` handler.

### Podmienené zobrazenie (`th:if`)

```html
<div th:if="${player == null}" class="guest-banner">
    Hráte ako hosť…
</div>
```

`th:if` je Thymeleaf ekvivalent `{player === null && <div>…</div>}` v JSX. Server element buď vloží do HTML, alebo nie. Keď ho nevloží, v DOM vôbec neexistuje.

### Iterácia (`th:each`)

```html
<div class="board-row" th:each="row, ri : ${board}">
    <div class="tile"
         th:each="color, ci : ${row}"
         th:class="${'tile ' + color}"
         th:attr="data-r=${ri.index}, data-c=${ci.index}">
    </div>
</div>
```

`th:each` je ekvivalent `.map()` v JSX. Pre každý riadok tabuľky vytvorí `<div class="board-row">` a v ňom pre každú dlaždicu `<div class="tile red">` (alebo iná farba). `data-r` a `data-c` sú HTML data atribúty — JavaScript ich neskôr číta pre BFS hover preview.

---

## 3. Farebné tlačidlá a AJAX ťah

Farebné tlačidlá sú **výnimka** — nerobí sa full-page reload. Fungujú cez `fetch` (AJAX):

```html
<form id="colorForm" method="post" th:action="@{/flood}">
    <button type="submit" name="color" th:value="${color.name().toLowerCase()}"
            th:disabled="${gameState != 'PLAYING' or color == currentColor}">
    </button>
</form>
```

JavaScript zachytí odoslanie formulára a zastaví ho (`preventDefault`):

```javascript
document.getElementById('colorForm').addEventListener('submit', function(e) {
    e.preventDefault();                         // zastav klasický POST
    const color = e.submitter.value;            // zisti, ktoré tlačidlo bolo kliknuté
    submitColor(color);                         // AJAX call
});
```

`e.submitter` je tlačidlo, ktoré spustilo submit — v Reactu by si mal `onClick` na každom tlačidle samostatne.

AJAX call pošle POST na `/flood-move` (nie `/flood`), server vráti JSON:

```javascript
fetch('/flood-move', {
    method: 'POST',
    headers: {'Content-Type': 'application/x-www-form-urlencoded'},
    body: 'color=' + encodeURIComponent(color)
})
.then(r => r.json())
.then(applyMoveData);   // aktualizuj DOM bez reloadu
```

`applyMoveData()` potom manuálne aktualizuje DOM — zmení CSS triedy dlaždíc, prepíše status bar, upraví tlačidlá. Ak hra skončí (SOLVED/FAILED), urobí `window.location.href = '/'` čo spustí full reload (server zaznamená skóre).

`th:disabled` na tlačidle — Thymeleaf vygeneruje `disabled` HTML atribút keď je podmienka pravdivá. Disabled tlačidlo nemôže byť kliknuté ani odoslané.

---

## 4. Denná výzva

```html
<form method="post" th:action="@{/daily}">
    <button type="submit">⚡ Denná výzva</button>
</form>
```

Klik pošle POST na `/daily`. Server:

1. Vytvorí špeciálne herné pole so **seedom = dnešný dátum** (číslo dní od 1.1.1970):
   ```java
   long seed = LocalDate.now(ZoneId.of("Europe/Bratislava")).toEpochDay();
   Field template = new Field(12, 12, 500, seed);
   ```
   Seed zabezpečí, že všetci hráči dostanú **rovnaké** pole v rovnaký deň.

2. Vypočíta minimálny počet ťahov potrebných na riešenie pomocou **2-krokového greedy algoritmu**: skúsi všetkých 5×5 = 25 kombinácií dvoch ťahov, vyberie tú, ktorá zaplavila najviac dlaždíc, a toto opakuje kým nie je pole vyriešené. Výsledok je limit ťahov pre hráča.

3. Uloží `isDailyChallenge = true` do session.

Skóre sa ukladá pod kľúčom `"flood-daily-YYYY-MM-DD"` (napr. `"flood-daily-2026-05-12"`), čo umožňuje oddeliť denné rebríčky od bežných. Server kontroluje, či hráč už dnes hral pomocou:
```java
List<Score> todayPlays = scoreService.getScoresByPlayer(todayDailyGameKey(), player);
dailyAlreadyPlayed = !todayPlays.isEmpty();
```

---

## 5. Colorblind Mode

Režim pre farboslepých je **čisto CSS + localStorage** — žiadna komunikácia so serverom.

Tlačidlo:
```html
<button class="colorblind-btn" id="colorblindBtn">👁 CB</button>
```

JavaScript:
```javascript
let colorblindOn = localStorage.getItem('colorblindMode') === 'on';

function applyColorblind() {
    document.body.classList.toggle('colorblind-mode', colorblindOn);
}

document.getElementById('colorblindBtn').addEventListener('click', () => {
    colorblindOn = !colorblindOn;
    localStorage.setItem('colorblindMode', colorblindOn ? 'on' : 'off');
    applyColorblind();
});
```

- `localStorage` — prehliadačové úložisko, pretrváva medzi reloadmi (ako `localStorage` v Node.js/React)
- `document.body.classList.toggle('colorblind-mode', bool)` — pridá/odoberie CSS triedu na `<body>`

Keď je trieda `colorblind-mode` na `<body>`, CSS pridá vzory na dlaždice namiesto spoliehania sa čisto na farbu:

```css
.colorblind-mode .tile.yellow {
    background-image: repeating-linear-gradient(
        -45deg, rgba(0,0,0,0.30) 0px, rgba(0,0,0,0.30) 4px,
        transparent 4px, transparent 10px
    );
}
.colorblind-mode .tile.orange {
    background-image: repeating-linear-gradient(45deg, ...),
                      repeating-linear-gradient(-45deg, ...);
}
```

Každá farba má unikátny šrafovací vzor (uhol, hustota čiar).

---

## 6. Zvuky (Web Audio API)

Žiadne audio súbory — zvuky sa generujú programovo cez **Web Audio API**:

```javascript
function playTone(freq, type, duration, volume) {
    const ctx = new AudioContext();
    const osc = ctx.createOscillator();   // generátor zvuku
    const gain = ctx.createGain();         // hlasitosť

    osc.connect(gain);
    gain.connect(ctx.destination);         // výstup do reproduktorov

    osc.type = type;            // tvar vlny: 'sine', 'sawtooth', ...
    osc.frequency.value = freq; // výška tónu v Hz

    gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + duration); // fade out
    osc.start();
    osc.stop(ctx.currentTime + duration);
}
```

- **Klik na farbu** — `sine` vlna, 440 Hz, 0.12 sekundy (krátky cvaknutý tón)
- **Výhra** — postupnosť 4 tónov (523→659→784→1047 Hz) s 120ms medzerami (fanfára)
- **Prehra** — `sawtooth` vlna, 180 Hz, 0.6 sekundy (dunivý tón)

Stav zvuku sa ukladá do `localStorage`:
```javascript
let soundOn = localStorage.getItem('floodSound') !== 'off';
```

---

## 7. Nápoveda (Hint)

Tlačidlo nápovedy:
```html
<form method="post" th:action="@{/toggle-hint}">
    <button type="submit">💡 Nápoveda: VYP</button>
</form>
```

Pošle POST na `/toggle-hint`, server prepne `showHint` v session a presmeruje späť.

Keď je nápoveda zapnutá, server pri každom `GET /` vypočíta najlepšiu farbu BFS algoritmom — prejde hranicu zaplavených dlaždíc a spočíta, koľko dlaždíc by každá farba pridala:

```java
private TileColor bestGainColor(Field field) {
    TileColor best = null;
    int bestGain = -1;
    for (TileColor color : TileColor.values()) {
        if (color == current) continue;
        int gain = computeFloodGain(field, color); // BFS od hranice zaplavených
        if (gain > bestGain) { bestGain = gain; best = color; }
    }
    return best;
}
```

Výsledná farba príde do šablóny ako `${hintColor}` a Thymeleaf pridá CSS triedu `hint-glow` na príslušné tlačidlo + zobrazí odznak `?`:

```html
th:class="${'color-btn ' + color + (color == hintColor ? ' hint-glow' : '')}"
```

Po AJAX ťahu JavaScript sám prepočíta `hintColor` z dát vrátených serverom a aktualizuje CSS triedy tlačidiel bez reloadu.

---

## 8. Časomiera

Časomiera **nežije na serveri** — meranie prebieha v prehliadači (JavaScript `setInterval`).

**Štart:** server zaznamená čas prvého ťahu do session (`gameStartMs = System.currentTimeMillis()`). Toto číslo (Unix timestamp v ms) príde do HTML cez Thymeleaf:

```javascript
const GAME_START_MS = /*[[${gameStartMs}]]*/ 0;
```

JavaScript spustí odpočet:
```javascript
let timerInterval = null;

// Po prvom AJAX ťahu (data.timerStarted == true):
const startSec = Math.floor((Date.now() - data.gameStartMs) / 1000);
let elapsed = startSec;
timerInterval = setInterval(() => {
    elapsed++;
    timerEl.textContent = formatTime(elapsed);
}, 1000);
```

`/*[[${gameStartMs}]]*/` je Thymeleaf syntax pre inline JavaScript — server doplní skutočnú hodnotu priamo do JS kódu. Výsledný HTML v prehliadači vyzerá napr.:
```javascript
const GAME_START_MS = 1747047234891;
```

**Zastavenie:** keď hra skončí, prehliadač robí `window.location.href = '/'`. Server na `GET /` vypočíta dĺžku:
```java
int durationSecs = (int) ((System.currentTimeMillis() - startMs) / 1000);
session.setAttribute("gameDurationSeconds", durationSecs);
```

Výsledný čas sa zobrazí v game-over overlay a uloží k skóre.

---

## Zhrnutie architektúry

| Funkcia | Kde žije stav | Komunikácia |
|---|---|---|
| Herné pole, ťahy | Server (HTTP session) | AJAX fetch → JSON |
| Nová hra, nápoveda, rating, komentár | Server (HTTP session) | HTML form POST → redirect |
| Denná výzva | Server (DB + session) | HTML form POST → redirect |
| Colorblind mode | Prehliadač (localStorage) | Žiadna |
| Zvuky | Prehliadač (Web Audio API) | Žiadna |
| Časomiera | Prehliadač (setInterval) | Štartovací timestamp zo servera |
| Skóre, komentáre, úspechy | Server (databáza) | Čítanie na `GET /`, zápis pri konci hry |
