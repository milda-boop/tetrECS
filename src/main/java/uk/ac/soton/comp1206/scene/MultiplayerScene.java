package uk.ac.soton.comp1206.scene;

import java.util.ArrayList;
import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Pair;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.LeaderBoard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.Grid;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The Multiplayer challenge scene. Holds the UI for the multiplayer challenge mode in the game.
 */
public class MultiplayerScene extends ChallengeScene {

  private Communicator communicator;

  /**
   * Create a new Single Player challenge scene
   *
   * @param gameWindow the Game Window
   */
  public MultiplayerScene(GameWindow gameWindow, Communicator communicator) {
    super(gameWindow);
    this.communicator = communicator;
  }

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

    var messages = new TextFlow();
    var scroller = new ScrollPane();
    messages.setPrefSize(300, 80);
    scroller.setContent(messages);
    TextField messageToSend = new TextField();
    messageToSend.setFocusTraversable(false);
    HBox.setHgrow(messageToSend, Priority.ALWAYS);
    HBox sendMessageBar = new HBox();
    Button sendMessage = new Button("Send");
    sendMessage.setFocusTraversable(false);
    sendMessageBar.getChildren().add(messageToSend);
    sendMessageBar.getChildren().add(sendMessage);

    messageToSend.setOnKeyPressed(keyEvent -> {
      if (keyEvent.getCode() != KeyCode.ENTER) {
        return;
      }
      communicator.send("MSG " + messageToSend.getText());
      messageToSend.clear();
      root.requestFocus();
    });

    sendMessage.setOnMouseClicked(actionEvent -> {
      communicator.send("MSG " + messageToSend.getText());
      messageToSend.clear();
      root.requestFocus();
    });
    communicator.addListener(communication -> {
      String[] arr = communication.split(" ", 2);
      if (arr[0].equals("MSG")) {
        String message = arr[1];
        Platform.runLater(() -> {
          Text receivedMessage = new Text(message + "\n");
          messages.getChildren().add(receivedMessage);
        });
      }
    });
    VBox mpMessages = new VBox(scroller, sendMessageBar);
    rightPane.setBottom(mpMessages);

    var grids = new VBox();
    grids.setSpacing(15.0);
    grids.setAlignment(Pos.CENTER);
    rightPane.setCenter(grids);

    LeaderBoard leaderBoard = new LeaderBoard();
    leaderBoard.scoresProperty().bind(game.getUserScores());
    rightPane.setTop(leaderBoard);

    board = new GameBoard(game.getGrid(), gameWindow.getWidth() / 2, gameWindow.getWidth() / 2);
    mainPane.setCenter(board);

    pieceBoard = new PieceBoard(new Grid(3, 3), gameWindow.getWidth() / 7,
        gameWindow.getWidth() / 7);
    Circle circle = new Circle(10, Color.rgb(255, 255, 255, 0.8));
    pieceBoard.add(circle, 1, 1);
    circle.setManaged(false);
    circle.setCenterX(56);
    circle.setCenterY(56);
    grids.getChildren().add(pieceBoard);

    followingPieceBoard = new PieceBoard(new Grid(3, 3), gameWindow.getWidth() / 14,
        gameWindow.getWidth() / 14);
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
  @Override
  protected void shutdown() {
    game.shutdown = true;
    communicator.send("DIE");
    game.equals(null);
  }

  @Override
  public void setupGame() {

    //Start new game
    game = new MultiplayerGame(5, 5, communicator);
  }
}
