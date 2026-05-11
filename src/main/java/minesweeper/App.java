package minesweeper;

import org.checkerframework.checker.units.qual.A;
import processing.core.PApplet;
import processing.core.PImage;
import processing.data.JSONArray;
import processing.data.JSONObject;
import processing.event.KeyEvent;
import processing.event.MouseEvent;


import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import java.io.*;
import java.util.*;

public class App extends PApplet {

    public static final int CELLSIZE = 32; //8;
    public static final int CELLHEIGHT = 32;

    public static final int CELLAVG = 32;
    public static final int TOPBAR = 64;
    public static int WIDTH = 864; //CELLSIZE*BOARD_WIDTH;
    public static int HEIGHT = 640; //BOARD_HEIGHT*CELLSIZE+TOPBAR;
    public static final int BOARD_WIDTH = WIDTH/CELLSIZE;
    public static final int BOARD_HEIGHT = 20;

    public Tile[][] board = new Tile[BOARD_WIDTH][BOARD_HEIGHT];
    public int mineCount = 100;
    public boolean gameover = false;
    public boolean gamewon = false;

    public boolean timerRunning = false;
    public float startTime;
    public float elaspsedTime = 0;

    private int mineExplosionIndex = 0;
    private int mineExplosionFrameCounter = 0;

    public static final int FPS = 30;

    public String configPath;

    public static Random random = new Random();
	
	public static int[][] mineCountColour = new int[][] {
            {0,0,0}, // 0 is not shown
            {0,0,255},
            {0,133,0},
            {255,0,0},
            {0,0,132},
            {132,0,0},
            {0,132,132},
            {132,0,132},
            {32,32,32}
    };
	
	// Feel free to add any additional methods or attributes you want. Please put classes in different files.

    public App() {
        this.configPath = "config.json";
    }

    /**
     * Initialise the setting of the window size.
     */
	@Override
    public void settings() {
        size(WIDTH, HEIGHT + TOPBAR);
    }

    /**
     * Load all resources such as images. Initialise the elements such as the player and map elements.
     */
	@Override
    public void setup() {

        String[] passedArgs = super.args;
        if (passedArgs != null && passedArgs.length > 0) {
            try {
                mineCount = Integer.parseInt(passedArgs[0]);
            } catch (NumberFormatException e) {
                mineCount = 100;
            }
        }

        frameRate(FPS);
        Tile.font = createFont("Arial-Bold", 16);
        setMineNumber(mineCount);
        Initialise();   
		//See PApplet javadoc:
		//loadJSONObject(configPath)
		//loadImage(this.getClass().getResource(filename).getPath().toLowerCase(Locale.ROOT).replace("%20", " "));

        //create attributes for data storage, eg board
    }

    public void Initialise() {

        for (int i = 0; i < BOARD_WIDTH; i++) {
            for (int j = 0; j < BOARD_HEIGHT; j++) {
                board[i][j] = new Tile(i, j, this);
            }
        }

        placeMine();
    
        for (int x = 0; x < BOARD_WIDTH; x++) {
            for (int y = 0; y < BOARD_HEIGHT; y++) {
                board[x][y].setAdjacentMines(calculateAdjacentMines(x, y));
            }
        }
    }


    public void startGame() {
        startTime = millis();  // Capture the start time in milliseconds
        timerRunning = true;
        setup();
    }


    public int calculateAdjacentMines(int x, int y) {
        int count = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) {
                    continue;
                }
                int adjacentX = x + i;
                int adjacentY = y + j;
                if (adjacentX >= 0 && adjacentX < BOARD_WIDTH && adjacentY >= 0 && adjacentY < BOARD_HEIGHT && board[adjacentX][adjacentY].isMine()) {
                    count++;
                }
            }
        }
        return count;
    }

    public void setMineNumber(int mineNumber) {
        if(mineNumber > 0 && mineNumber < BOARD_WIDTH * BOARD_HEIGHT) {
            mineCount = mineNumber;
        }
        else {
            mineCount = 100; // Default value if the input is invalid
        }
    }


    private void placeMine() {
        int placedMines = 0;
        while (placedMines < mineCount) {
            int x = (int) random(BOARD_WIDTH);
            int y = (int) random(BOARD_HEIGHT);
            if (!board[x][y].isMine()) {
                board[x][y].setMine();
                placedMines++;
            }
        }
    }

    public boolean isValidPosition(int x, int y) {
        return x >= 0 && x < BOARD_WIDTH && y >= 0 && y < BOARD_HEIGHT;
    }


    private void expandZeroes(int x, int y) {
        if (!isValidPosition(x, y)) {
            return;  // Check bounds first
        }
    
        Tile tile = board[x][y];
        if (tile.isRevealed() || tile.isFlagged() || tile.isMine()) {
            return;  // Do not reveal if already revealed, flagged, or is a mine
        }
    
        tile.reveal();  // Reveal the current tile
    
        // Only continue recursion if there are no adjacent mines
        if (tile.getAdjacentMines() == 0) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx != 0 || dy != 0) {  // Skip the current tile
                        expandZeroes(x + dx, y + dy);
                    }
                }
            }
        }
    }
    
    public void gameOver() {
        gameover = true;
        timerRunning = false;
        mineExplosionIndex = 0;
        mineExplosionFrameCounter = 0;
    }


    private void handleSequentialMineExplosions() {
        if (mineExplosionFrameCounter % 3 == 0 && mineExplosionIndex < BOARD_WIDTH * BOARD_HEIGHT) {
            for (int i = mineExplosionIndex; i < BOARD_WIDTH * BOARD_HEIGHT; i++) {
                int row = i / BOARD_WIDTH;
                int col = i % BOARD_WIDTH;
                if (board[col][row].isMine() && !board[col][row].isRevealed()) {
                    board[col][row].reveal();
                    mineExplosionIndex = i + 1;
                    break;
                }
            }
        }
        mineExplosionFrameCounter++;
    }
    

    public boolean checkWinCondition() {
        for (int i = 0; i < BOARD_WIDTH; i++) {
            for (int j = 0; j < BOARD_HEIGHT; j++) {
                Tile tile = board[i][j];
                // If the tile not a mine and hasn't been revealed, return false
                if (!tile.isMine() && !tile.isRevealed()) {
                    return false;
                }
            }
        }
        return true;
    }


    public void gameWon() {
        gameover = true;
        gamewon = true;
        timerRunning = false;
    }


    public void resetBoard() {
        // Reset all game state variables
        elaspsedTime = 0;
        timerRunning = true;
        startTime = millis();
        gameover = false;
        gamewon = false;

    
        // Clear and reinitialize the board
        for (int i = 0; i < BOARD_WIDTH; i++) {
            for (int j = 0; j < BOARD_HEIGHT; j++) {
                board[i][j].reset();
            }
        }
    
        placeMine();  // Redistribute mines

        // Recalculate adjacent mines for all tiles
        for (int x = 0; x < BOARD_WIDTH; x++) {
            for (int y = 0; y < BOARD_HEIGHT; y++) {
                board[x][y].setAdjacentMines(calculateAdjacentMines(x, y));
            }
        }
    }


    /**
     * Receive key pressed signal from the keyboard.
     */
    @Override
    public void keyPressed() {
        if (key == 'r' || key == 'R') {
            resetBoard();
        }
    }

    /**
     * Receive key released signal from the keyboard.
     */
	@Override
    public void keyReleased(){
        
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if(gameover) {
            return;
        }
        
        int col = e.getX() / CELLSIZE;
        int row = (e.getY() - 64) / CELLSIZE;
    
        if (e.getButton() == LEFT) {
            if (isValidPosition(col, row)) {
                Tile tile = board[col][row];
                if (!tile.isRevealed() && !tile.isFlagged()) {
                    if (tile.isMine()) {
                        gameOver();
                    } else {
                        if (tile.getAdjacentMines() == 0) {
                            expandZeroes(col, row);
                        } else {
                            tile.reveal();
                        }
                        
                        if (checkWinCondition()) {
                            gamewon = true;
                            gameWon();
                        }
                    }
                }
            }
        } else if (e.getButton() == RIGHT) {
            if (isValidPosition(col, row) && !board[col][row].isRevealed()) {
                board[col][row].toggleFlag();
            }
        }
        redraw();
    }


    @Override
    public void mouseReleased(MouseEvent e) {

    }
    

    /**
     * Draw all elements in the game by current frame.
     */

     @Override
     public void draw() {
         background(200);
     
         if (!gameover) {
             elaspsedTime = (millis() - startTime) / 1000;
         }
     
         fill(0);
         textSize(20);
         textAlign(RIGHT, TOP);
         text("Time: " + (int)elaspsedTime, WIDTH - 20, 20);
     
         if (gameover && !gamewon) {
             textAlign(CENTER, CENTER);
             textSize(32);
             fill(0, 0, 0);
             text("You lost!", WIDTH / 2, TOPBAR / 2);
             handleSequentialMineExplosions();
         }

         if(gameover && gamewon) {
             textAlign(CENTER, CENTER);
             textSize(32);
             fill(0, 0, 0);
             text("You won!", WIDTH / 2, TOPBAR / 2);
         }

         
         translate(0, TOPBAR);
     
         int mouseCol = mouseX / CELLSIZE;
         int mouseRow = (mouseY - TOPBAR) / CELLSIZE;
         for (int i = 0; i < BOARD_WIDTH; i++) {
             for (int j = 0; j < BOARD_HEIGHT; j++) {
                 if (i == mouseCol && j == mouseRow && !gameover) {
                     board[i][j].setHovered(true);
                 } else {
                     board[i][j].setHovered(false);
                 }
                 board[i][j].draw(this, CELLSIZE);
             }
         }
     
         translate(0, -TOPBAR); 
     }

        public static void main(String[] args) {
        PApplet.main("minesweeper.App", args);
    }
}
