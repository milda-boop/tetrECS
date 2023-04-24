package uk.ac.soton.comp1206.scene;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import javafx.animation.Animation;
import javafx.animation.FillTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.SequentialTransition;
import javafx.animation.StrokeTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.StringProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Grid;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The Single Player challenge scene. Holds the UI for the single player challenge mode in the
 * game.
 */
public class ChallengeScene extends BaseScene {

  protected static final Logger logger = LogManager.getLogger(MenuScene.class);
  protected Game game;
  protected PieceBoard pieceBoard;
  protected GameBoard board;
  protected PieceBoard followingPieceBoard;

  protected Rectangle timer;
  protected Timeline timeline = new Timeline();


  /**
   * Create a new Single Player challenge scene
   *
   * @param gameWindow the Game Window
   */
  public ChallengeScene(GameWindow gameWindow) {
    super(gameWindow);
    logger.info("Creating Challenge Scene");
  }

  /**
   * Build the Challenge window
   */
  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());

    setupGame();

    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    var challengePane = new StackPane();
    challengePane.setMaxWidth(gameWindow.getWidth());
    challengePane.setMaxHeight(gameWindow.getHeight());
    challengePane.getStyleClass().add("menu-background");
    root.getChildren().add(challengePane);

    var mainPane = new BorderPane();
    challengePane.getChildren().add(mainPane);

    var rightPane = new BorderPane();
    mainPane.setRight(rightPane);

    BorderPane.setMargin(rightPane, new Insets(20, 20, 20, 20));

    var grids = new VBox();
    grids.setSpacing(15.0);
    grids.setAlignment(Pos.CENTER);
    rightPane.setCenter(grids);

    var highScore = new Label("Highscore: " + getHighScore());
    rightPane.setTop(highScore);

    board = new GameBoard(game.getGrid(), gameWindow.getWidth() / 2, gameWindow.getWidth() / 2);
    mainPane.setCenter(board);

    pieceBoard = new PieceBoard(new Grid(3, 3), gameWindow.getWidth() / 5,
        gameWindow.getWidth() / 5);
    Circle circle = new Circle(15, Color.rgb(255, 255, 255, 0.8));
    pieceBoard.add(circle, 1, 1);
    circle.setManaged(false);
    circle.setCenterX(80);
    circle.setCenterY(80);
    grids.getChildren().add(pieceBoard);

    followingPieceBoard = new PieceBoard(new Grid(3, 3), gameWindow.getWidth() / 10,
        gameWindow.getWidth() / 10);
    grids.getChildren().add(followingPieceBoard);

    var info = new HBox();
    var score = new Label("Score: 0");
    var level = new Label(" Level: 0");
    var lives = new Label(" Lives: 3");
    var multiplier = new Label(" Multiplier: 1");

    timer = new Rectangle(gameWindow.getWidth(), 30);
    timer.setFill(Color.GREEN);
    mainPane.setBottom(timer);

    info.getChildren().add(score);
    info.getChildren().add(level);
    info.getChildren().add(lives);
    info.getChildren().add(multiplier);
    info.getStyleClass().add("info");

    HBox.setHgrow(info, Priority.ALWAYS);
    mainPane.setTop(info);

    game.getScore().addListener((observableValue, oldValue, newValue) ->
    {
      score.setText("Score: " + newValue);
      if (game.getScore().get() > Integer.parseInt(getHighScore())) {
        highScore.setText("Highscore: " + game.getScore());
      }
    });
    game.getLevel().addListener((observableValue, oldValue, newValue) ->
    {
      level.setText(" Level: " + newValue);
    });
    game.getLives().addListener((observableValue, oldValue, newValue) ->
    {
      if (game.getLives().get() < 0) {
        Platform.runLater(() -> {
          gameWindow.getScores(game);
        });
        shutdown();
      } else {
        lives.setText(" Lives: " + newValue);
      }

    });
    game.getMultiplier().addListener((observableValue, oldValue, newValue) ->
    {
      multiplier.setText(" Multiplier: " + newValue);
    });

    //Handle block on gameboard grid being clicked
    board.setOnBlockClick(this::blockClicked);

    //Handle mouse right-clicking on gameboard
    board.setOnRightClicked(this::rightClicked);
    game.setNextPieceListener(this::nextPiece);
    game.setLineClearedListener(this::lineCleared);
    game.setOnGameLoop(this::gameLoop);

  }

  protected void gameLoop(int delay) {
    timeline.stop();
    timeline.getKeyFrames().clear();
    timer.setTranslateX(0);
    timeline.getKeyFrames().add(new KeyFrame(Duration.millis(delay),
        new KeyValue(timer.translateXProperty(), -gameWindow.getWidth())));
    FillTransition ft = new FillTransition(Duration.millis(delay / 2), timer, Color.GREEN,
        Color.ORANGE);
    FillTransition ft1 = new FillTransition(Duration.millis(delay / 2), timer, Color.ORANGE,
        Color.RED);
    SequentialTransition st = new SequentialTransition(ft, ft1);
    timeline.play();
    st.play();
  }

  /**
   * Method that manages the key events of the scene.
   *
   * @param keyEvent The key event.
   */
  @Override
  public void handleEvents(KeyEvent keyEvent) {
    if (keyEvent.getCode() == KeyCode.SPACE) {
      game.swapCurrentPiece();
    } else if (keyEvent.getCode() == KeyCode.ESCAPE) {
      shutdown();
      gameWindow.loadScene(new MenuScene(gameWindow));
    } else if (keyEvent.getCode() == KeyCode.SHIFT) {
      game.rotateCurrentPiece();
    }
  }


  protected void lineCleared(HashSet<Pair<Integer, Integer>> blocks) {
    board.fadeOut(blocks);
  }

  protected void nextPiece(GamePiece gamePiece, GamePiece followingPiece) {
    pieceBoard.setPiece(gamePiece);
    followingPieceBoard.setPiece(followingPiece);
  }

  /**
   * Handle when a block is clicked
   *
   * @param gameBlock the Game Block that was clicked
   */
  protected void blockClicked(GameBlock gameBlock) {
    game.blockClicked(gameBlock);
  }

  /**
   * Handle when the mouse has right-clicked
   */
  protected void rightClicked() {
    game.rotateCurrentPiece();
  }


  /**
   * Setup the game object and model
   */
  public void setupGame() {
    logger.info("Starting a new challenge");

    //Start new game
    game = new Game(5, 5);
  }

  /**
   * Initialise the scene and start the game
   */
  @Override
  public void initialise() {
    logger.info("Initialising Challenge");
    game.start();
  }

  protected void shutdown() {
    game.shutdown = true;
    game.equals(null);
  }

  protected String getHighScore() {
    try {
      FileInputStream file = new FileInputStream("scores.txt");
      BufferedReader reader = new BufferedReader(new InputStreamReader(file));
      String line;
      if ((line = reader.readLine()) != null) {
        return line.split(":")[1];
      }
      reader.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return "0";
  }

}
