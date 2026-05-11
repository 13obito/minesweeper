package minesweeper;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PFont;

public class Tile {
    private int x, y, adjacentMines;
    private boolean isMine, isRevealed, isFlagged, isPending;
    public PImage tileImage, pendingImage, revealImage, flagImage, mineImage, currentImage;
    private PApplet p;
    public static PFont font;
    private PImage[] mineImages;
    private int animationFrame = 0;

    private static final int[][] mineCountColour = {
        {0, 0, 0},
        {0, 0, 255},
        {0, 128, 0},
        {255, 0, 0},
        {0, 0, 128},
        {128, 0, 0},
        {0, 128, 128},
        {128, 0, 128}, 
        {32, 32, 32}
    };  

    public Tile(int x, int y, PApplet p) {
        this.x = x;
        this.y = y;
        this.adjacentMines = 0;
        this.isMine = false;
        this.isRevealed = false;
        this.isFlagged = false;
        this.isPending = false;
        this.p = p;
        this.mineImages = new PImage[10];
        this.tileImage = p.loadImage("src/main/resources/minesweeper/tile1.png");
        this.pendingImage = p.loadImage("src/main/resources/minesweeper/tile2.png");
        this.revealImage = p.loadImage("src/main/resources/minesweeper/wall0.png");
        this.flagImage = p.loadImage("src/main/resources/minesweeper/flag.png");

        for (int i = 0; i < 10; i++) {
            mineImages[i] = p.loadImage("src/main/resources/minesweeper/mine" + i + ".png");
        }

        this.currentImage = tileImage;

        if (font == null) {
            font = p.createFont("Arial-Bold", 20);
        }
    }

    public void draw(PApplet app, int cellSize) {
        int screenX = x * cellSize;
        int screenY = y * cellSize;
    
        // If the tile is a revealed mine, update the animation
        if (isRevealed && isMine) {
            // Update the image every 3 frames
            if (animationFrame / 3 < mineImages.length) {
                currentImage = mineImages[animationFrame / 3];
                animationFrame++;  // Increase the frame counter
            } else {
                // If the animation is finished, keep showing the last frame
                currentImage = mineImages[mineImages.length - 1];
            }
        }
    
        app.image(currentImage, screenX, screenY, cellSize, cellSize);
        
        // Only draw the number if the tile is revealed, has adjacent mines, and is not a mine
        if (isRevealed && adjacentMines > 0 && !isMine) {
            drawNumber(app, cellSize);
        }
    }
    

    private void drawNumber(PApplet app, int cellSize) {
        app.pushStyle();
        int colorIndex = Math.min(adjacentMines, mineCountColour.length - 1);
        app.fill(mineCountColour[colorIndex][0], mineCountColour[colorIndex][1], mineCountColour[colorIndex][2]);
        app.textFont(font);
        app.textAlign(PApplet.CENTER, PApplet.CENTER);
        app.text(adjacentMines, x * cellSize + cellSize / 2, y * cellSize + cellSize / 2);
        app.popStyle();
    }

    

public void reveal() {
    if (!isFlagged && !isRevealed) {
        isRevealed = true;  
        if (!isMine) {
            setRevealImage();
        }
        else {
            // Start the animation at frame 0 when the mine is revealed
            animationFrame = 0;
        }
    }
}


    private void setRevealImage() {
        currentImage = revealImage;
        if (adjacentMines > 0) {
            p.pushStyle();
            int colorIndex = Math.min(adjacentMines, mineCountColour.length - 1);
            p.fill(mineCountColour[colorIndex][0], mineCountColour[colorIndex][1], mineCountColour[colorIndex][2]);
            p.textFont(font);
            p.textAlign(PApplet.CENTER, PApplet.CENTER);
            p.text(adjacentMines, (x + 0.5f) * 32, (y + 0.5f) * 32);
            p.popStyle();
        }
    }

    public void setHovered(boolean h) {
        this.isPending = h;
        if (h && !isRevealed && !isFlagged) {
            currentImage = pendingImage;
        } else if (!h && !isRevealed && !isFlagged) {
            currentImage = tileImage;
        }
    }
    

    public void reset() {
        isMine = false;
        isRevealed = false;
        isFlagged = false;
        adjacentMines = 0;
        currentImage = tileImage;
    }

    public boolean isMine() {
        return isMine;
    }

    public boolean isRevealed() {
        return isRevealed;
    }

    public boolean isFlagged() {
        return isFlagged;
    }

    public boolean isPending() {
        return isPending;
    }

    public int getAdjacentMines() {
        return adjacentMines;
    }

    public void setMine() {
        isMine = true;
    }

    public void setAdjacentMines(int adjacentMines) {
        this.adjacentMines = adjacentMines;
    }

    public void toggleFlag() {
        isFlagged = !isFlagged;
        if (isFlagged) {
            currentImage = flagImage;
        } else {
            currentImage = tileImage;
        }
    }

}
