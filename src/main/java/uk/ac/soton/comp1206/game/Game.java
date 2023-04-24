package uk.ac.soton.comp1206.game;

import java.util.HashSet;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.event.GameLoopListener;
import uk.ac.soton.comp1206.event.LineClearedListener;
import uk.ac.soton.comp1206.event.NextPieceListener;
import uk.ac.soton.comp1206.ui.MultiMedia;

/**
 * The Game class handles the main logic, state and properties of the TetrECS game. Methods to
 * manipulate the game state and to handle actions made by the player should take place inside this
 * class.
 */
public class Game {

  protected static final Logger logger = LogManager.getLogger(Game.class);

  /**
   * Number of rows
   */
  protected final int rows;
  public boolean shutdown = false;

  /**
   * Number of columns
   */
  protected final int cols;

  /**
   * The grid model linked to the game
   */
  protected final Grid grid;

  /**
   * The current piece to be played in the game
   */
  protected GamePiece currentPiece;
  protected GamePiece followingPiece;

  protected IntegerProperty score = new SimpleIntegerProperty(0);
  protected IntegerProperty level = new SimpleIntegerProperty(0);
  protected IntegerProperty lives = new SimpleIntegerProperty(3);
  protected IntegerProperty multiplier = new SimpleIntegerProperty(1);

  /**
   * The multimedia class that will play any audio or music files.
   */
  private MultiMedia multiMedia = new MultiMedia();

  private NextPieceListener nextPieceListener;
  private LineClearedListener lineClearedListener;
  private GameLoopListener gameLoopListener;

  protected Timer timer = new Timer();

  /**
   * Create a new game with the specified rows and columns. Creates a corresponding grid model.
   *
   * @param cols number of columns
   * @param rows number of rows
   */
  public Game(int cols, int rows) {
    this.cols = cols;
    this.rows = rows;

    //Create a new grid model to represent the game state
    this.grid = new Grid(cols, rows);
  }

  /**
   * Start the game
   */
  public void start() {
    logger.info("Starting game");
    initialiseGame();
  }

  /**
   * Initialise a new game and set up anything that needs to be done at the start
   */
  public void initialiseGame() {
    logger.info("Initialising game");
    //Play music file
    //multiMedia.playBackgroundMusic("music/game.wav");
    currentPiece = spawnPiece();
    followingPiece = spawnPiece();
    logger.info("Piece to be played is " + currentPiece.toString());
    setNextPiece(currentPiece, followingPiece);
    gameLoop();

  }

  protected void gameLoop() {
    timer = new Timer();
    loop();
    logger.info("timer reset");
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        if (lives.get() == 0) {
          logger.info("Game over");
          lives.set(-1);
        } else if (shutdown == true) {
          logger.info("Game exited");
        } else {
          Platform.runLater(() -> {
            lives.set(lives.get() - 1);
            logger.info("lives set to " + lives.get());
            multiplier.set(1);
            currentPiece = followingPiece;
            followingPiece = spawnPiece();
            setNextPiece(currentPiece, followingPiece);
          });
          gameLoop();
        }
      }
    }, getTimerDelay());

  }

  public void setOnGameLoop(GameLoopListener listener) {
    gameLoopListener = listener;
  }

  protected void loop() {
    gameLoopListener.gameLooped(getTimerDelay());
  }

  public void setNextPieceListener(NextPieceListener listener) {
    this.nextPieceListener = listener;
  }

  protected void setNextPiece(GamePiece currentPiece, GamePiece followingPiece) {
    nextPieceListener.nextPiece(currentPiece, followingPiece);
  }

  public void setLineClearedListener(LineClearedListener listener) {
    this.lineClearedListener = listener;
  }

  private void clearLine(HashSet<Pair<Integer, Integer>> blocks) {
    lineClearedListener.lineCleared(blocks);
  }


  /**
   * Handle what should happen when a particular block is clicked
   *
   * @param gameBlock the block that was clicked
   */
  public void blockClicked(GameBlock gameBlock) {
    //Get the position of this block
    int x = gameBlock.getX();
    int y = gameBlock.getY();

    //If the piece can be played, update grid with new values
    if (grid.canPlayPiece(currentPiece, x, y) == true) {
      grid.playPiece(currentPiece, x, y);
      multiMedia.playAudioFile("sounds/place.wav");
      afterPiece();
      nextPiece();
      timer.cancel();
      gameLoop();
    } else {
      multiMedia.playAudioFile("sounds/fail.wav");
    }
  }

  /**
   * Get the grid model inside this game representing the game state of the board
   *
   * @return game grid model
   */
  public Grid getGrid() {
    return grid;
  }

  /**
   * Get the number of columns in this game
   *
   * @return number of columns
   */
  public int getCols() {
    return cols;
  }

  /**
   * Get the number of rows in this game
   *
   * @return number of rows
   */
  public int getRows() {
    return rows;
  }

  /**
   * @return the randomly generated new game piece
   */
  public GamePiece spawnPiece() {
    Random random = new Random();
    int num = random.nextInt(0, GamePiece.PIECES);
    return GamePiece.createPiece(num, 0);
  }

  /**
   * Replaces the current piece with a new piece
   */
  public void nextPiece() {
    currentPiece = followingPiece;
    followingPiece = spawnPiece();
    logger.info("Piece to be played is " + currentPiece.toString());
    setNextPiece(currentPiece, followingPiece);
  }

  /**
   * Handles the clearing of lines after a piece has been played
   */
  public void afterPiece() {
    int linesToClear = 0;
    //Blocks stored as pairs (coordinates) in a hashset so that when cleared, blocks aren't
    //processed more than once.
    HashSet<Pair<Integer, Integer>> blocksToClear = new HashSet<Pair<Integer, Integer>>();
    //Searching for horizontal lines to clear.
    for (int i = 0; i < 5; i++) {
      boolean clearLine = true;
      for (int j = 0; j < 5; j++) {
        if (grid.get(i, j) == 0) {
          clearLine = false;
          break;
        }
      }
      if (clearLine) {
        linesToClear++;
        for (int j = 0; j < 5; j++) {
          blocksToClear.add(new Pair<>(i, j));
        }
      }
    }
    //Searching for vertical lines to clear.
    for (int i = 0; i < 5; i++) {
      boolean clearLine = true;
      for (int j = 0; j < 5; j++) {
        if (grid.get(j, i) == 0) {
          clearLine = false;
          break;
        }
      }
      if (clearLine) {
        linesToClear++;
        for (int j = 0; j < 5; j++) {
          blocksToClear.add(new Pair<>(j, i));
        }
      }
    }
    for (Pair<Integer, Integer> block : blocksToClear) {
      grid.set(block.getKey(), block.getValue(), 0);
    }
    if (linesToClear > 0) {
      multiMedia.playAudioFile("sounds/clear.wav");
      clearLine(blocksToClear);
    }
    score(linesToClear, blocksToClear.size());
  }

  public IntegerProperty getScore() {
    return score;
  }

  public IntegerProperty getLevel() {
    return level;
  }

  public IntegerProperty getLives() {
    return lives;
  }

  public IntegerProperty getMultiplier() {
    return multiplier;
  }

  /**
   * Calculates the new score each time a piece is played
   *
   * @param lines  the number of lines cleared
   * @param blocks the number of blocks cleared
   */
  public void score(int lines, int blocks) {
    int linesScore = lines * blocks * multiplier.get() * 10;
    score.set(score.get() + linesScore);
    logger.info("Score changed to: " + score.get());
    //Multiplier either resets to 1, or increases.
    if (linesScore == 0) {
      multiplier.set(1);
    } else {
      multiplier.set(multiplier.get() + 1);
    }
    //Level is recalculated
    level.set(score.get() / 1000);

  }

  /**
   * Rotates the current piece once
   */
  public void rotateCurrentPiece() {
    logger.info("Piece rotated");
    currentPiece.rotate();
    multiMedia.playAudioFile("sounds/rotate.wav");
    setNextPiece(currentPiece, followingPiece);
  }

  public void swapCurrentPiece() {
    logger.info("Pieces swapped");
    GamePiece piece = currentPiece;
    currentPiece = followingPiece;
    followingPiece = piece;
    setNextPiece(currentPiece, followingPiece);
  }

  public int getTimerDelay() {
    return 12000 - (500 * level.get());
    //return 1000;
  }

  public SimpleListProperty getUserScores() {
    return null;
  }


}
