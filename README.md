# Minesweeper

A Minesweeper game built with Java and [Processing](https://processing.org/) (`PApplet`).

## Requirements

- **JDK 8** or newer (Java 8 is fine for this project)

## Run

From the project root:

```bash
# Windows
.\gradlew.bat run
```

Or double-click `run.bat` on Windows.

On macOS / Linux:

```bash
chmod +x gradlew   # once
./gradlew run
```

If you use a system-wide Gradle install instead of the wrapper:

```bash
gradle run
```

Optional CLI arguments (comma-separated):

```bash
gradle run -Pargs=arg1,arg2
```

## Controls

| Input | Action |
|--------|--------|
| **Left click** | Reveal a cell |
| **Right click** | Flag / unflag a covered cell |
| **R** | Reset the board |

## Build a runnable JAR

```bash
.\gradlew.bat jar
```

The fat JAR is `build/libs/minesweeper-1.0.jar`. Run with:

```bash
java -jar build/libs/minesweeper-1.0.jar
```

## Project layout

- `src/main/java/minesweeper/` — game source (`App.java`, `Tile.java`, …)
- `build.gradle` — Gradle build and `application` main class (`minesweeper.App`)
