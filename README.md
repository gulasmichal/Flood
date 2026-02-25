# GameStudio Readme (SK)

Toto je šablóna projektu GameStudio. Naklonujte si tento projekt a otvorte ho v IntelliJ Idea podľa nižšie uvedeného návodu.
Všetky súbory ukladajte do toho projektu podľa pokynov.

## Klonovanie

Naklonujte si svoj projekt pomocou príkazu:

```git clone git@git.kpi.fei.tuke.sk:...```

V príkaze zameňte URL za SSH URL vášho projektu. Získate ho v GitLab-e.


## Otvorenie projektu v IntelliJ Idea

V menu Idey vyberte **File -> Open...**.
V dialógu pre výber projektov nájdite **pom.xml** v hlavnom adresári vášho naklonovaného projektu a potvrďte.
Potom vyberte **Open as Project**.


## Nastavenie projektu

V adresári `src/main/java/sk.tuke.gamestudio.game` si vytvorte nový balík pomenovaný podľa názvu vašej hry.
Všetky súbory a adresáre vašej hry umiestnite do tohto balíka (napr. `Main.java`, `core`, `consoleui`, atď.).
Keď budete vytvárať Unit testy, umiestnite ich do balíka `src/test/java/sk.tuke.gamestudio.game.[yourGame]`.

Používajte [štandardnú štruktúru Maven projektov](https://maven.apache.org/guides/introduction/introduction-to-the-standard-directory-layout.html) a odporúčanú adresárovú štruktúru projektu GameStudio.


# GameStudio Readme (EN)

Template for your GameStudio project. Clone the project and open in IntelliJ Idea. Place all your files into this project.

## Cloning

Clone your project using:

```git clone git@git.kpi.fei.tuke.sk:...```

Replace the url with your project's SSH url. You can get it in GitLab.


## Opening in IntelliJ Idea

In Idea, select **File -> Open...** from the menu.
In the file selection dialog, find and open your project's **pom.xml**.
Then select **Open as Project**.


## Project Setup

Create a new package inside `src/main/java/sk.tuke.gamestudio.game`. Name it after your game.
Place all files and packages related to your game (e.g. `Main.java`, `core`, `consoleui`, etc.) into this package.
When creating new unit tests, place them into the `src/test/java/sk.tuke.gamestudio.game.[yourGame]` package.

Use the [standard Maven directory layout](https://maven.apache.org/guides/introduction/introduction-to-the-standard-directory-layout.html) and GameStudio recommended directory structure when adding more classes.