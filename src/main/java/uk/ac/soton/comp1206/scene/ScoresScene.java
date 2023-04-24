package uk.ac.soton.comp1206.scene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.ScoresList;
import uk.ac.soton.comp1206.event.CommunicationsListener;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.ui.MultiMedia;

/**
 * The scores scene that opens up after a game over. For single player, holds the local and online
 * scores. For multiplayer, holds the leaderboard and online scores.
 */
public class ScoresScene extends BaseScene {

  private static final Logger logger = LogManager.getLogger(GameWindow.class);
  private MultiMedia multiMedia = new MultiMedia();
  private ArrayList<Pair<String, Integer>> list = new ArrayList<Pair<String, Integer>>();
  private ObservableList<Pair<String, Integer>> observableList = FXCollections.observableArrayList(
      list);
  private ObservableList<Pair<String, Integer>> observableRemoteList = FXCollections.observableArrayList(
      list);
  private SimpleListProperty localScores = new SimpleListProperty<Pair<String, Integer>>(
      observableList);
  private SimpleListProperty remoteScores = new SimpleListProperty<Pair<String, Integer>>(
      observableRemoteList);
  private ScoresList uiList;
  private ScoresList uiOnlineList;
  private Integer latestScore = 0;
  private BorderPane mainPane;
  private Game game;

  /**
   * Create a new scene, passing in the GameWindow the scene will be displayed in
   *
   * @param gameWindow the game window
   */
  public ScoresScene(GameWindow gameWindow) {
    super(gameWindow);
  }

  @Override
  public void initialise() {
    logger.info("Initialising ScoresScene");
    multiMedia.playBackgroundMusic("music/end.wav");
    if (game instanceof MultiplayerGame) {
      multiPlayer();
      loadOnlineScores();
    } else {
      loadScores();
      loadOnlineScores();
      setLatestScore();
    }

  }

  @Override
  public void build() {
    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    var scoresPane = new StackPane();
    scoresPane.setMaxWidth(gameWindow.getWidth());
    scoresPane.setMaxHeight(gameWindow.getHeight());
    scoresPane.getStyleClass().add("menu-background");
    root.getChildren().add(scoresPane);

    mainPane = new BorderPane();
    scoresPane.getChildren().add(mainPane);

    uiList = new ScoresList();
    uiList.scoresProperty().bind(localScores);

    uiOnlineList = new ScoresList();
    uiOnlineList.scoresProperty().bind(remoteScores);

    var scores = new HBox();
    var onlineScores = new VBox();
    var otherScores = new VBox();
    onlineScores.getChildren().add(new Label("Online Scores:"));
    onlineScores.getChildren().add(uiOnlineList);
    if(game instanceof MultiplayerGame)
    {
      otherScores.getChildren().add(new Label("LeaderBoard:"));
    }
    else
    {
      otherScores.getChildren().add(new Label("Local Scores:"));
    }
    otherScores.getChildren().add(uiList);
    onlineScores.setAlignment(Pos.CENTER);
    onlineScores.setSpacing(50);
    otherScores.setAlignment(Pos.CENTER);
    otherScores.setSpacing(50);
    scores.getChildren().add(otherScores);
    scores.getChildren().add(onlineScores);
    scores.setSpacing(50);
    scores.setAlignment(Pos.CENTER);

    mainPane.setCenter(scores);

    //Listeners are added to the communicator.
    gameWindow.getCommunicator().addListener(communication -> {
      logger.info(communication);
      String[] arr = communication.split(" ", 2);
      logger.info(arr[0]);
      switch (arr[0]) {
        case "SCORES": {
          logger.info("prof4ssing scores");
          localScores.clear();
          String[] scoresArray = arr[1].split(System.lineSeparator());
          //sorting
          List<Pair<String,Integer>> scoresList = new ArrayList<>();
          for (String score : scoresArray) {
            String player = score.split(":", 3)[0];
            Integer playerScore = Integer.parseInt(score.split(":", 3)[1]);
            scoresList.add(new Pair<>(player,playerScore));
          }
          scoresList.sort(Comparator.<Pair<String,Integer>>comparingInt(Pair::getValue));
          Collections.reverse(scoresList);
          //updating
          for (Pair<String,Integer> score : scoresList) {
            Platform.runLater(() -> localScores.add(score));
          }
          break;
        }
        case "HISCORES": {
          String arr1 = arr[1];
          arr = arr1.split(System.lineSeparator());
          Platform.runLater(() -> {
            remoteScores.get().clear();
            int i = 0;
            for (String score : communication.split(" ")[1].split(System.lineSeparator())) {
              remoteScores.get().add(new Pair(score.split(":")[0], score.split(":")[1]));
              i = i + 1;
              if (i == 10) {
                break;
              }
            }
          });
          break;
        }
        default: {
          break;
        }
      }
    });
  }

  /**
   * Communicator requests the online scores.
   */
  public void loadOnlineScores() {
    gameWindow.getCommunicator().send("HISCORES UNIQUE");
  }

  /**
   * Reads scores from the local scores text file and updates the ui component.
   */
  public void loadScores() {
    try {
      FileInputStream file = new FileInputStream("scores.txt");
      BufferedReader reader = new BufferedReader(new InputStreamReader(file));
      localScores.get().clear();
      String line;
      int i = 0;
      while ((line = reader.readLine()) != null && i < 10) {
        localScores.get().add(new Pair(line.split(":")[0], line.split(":")[1]));
        i = i + 1;
      }
      reader.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    logger.info("Local scores are: " + localScores.get());
  }

  /**
   * Communicator requests the scores for the leaderboard.
   */
  public void multiPlayer() {
    gameWindow.getCommunicator().send("SCORES");
  }

  /**
   * Writes the either changed or unchanged local scores to the text file.
   */
  public void writeScores() {
    try {
      PrintWriter writer = new PrintWriter("scores.txt", "UTF-8");

      for (Pair<String, Integer> score : uiList.getOrderedScores()) {
        writer.println(score.getKey() + ":" + score.getValue());
      }
      writer.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Sends the score to the server to be processed as an online score.
   *
   * @param name username
   */
  public void writeOnlineScore(String name) {
    gameWindow.getCommunicator().send("HISCORE " + name + ":" + game.getScore().get());
    gameWindow.getCommunicator().addListener(new CommunicationsListener() {
      @Override
      public void receiveCommunication(String communication) {
        logger.info(communication);
      }
    });
  }

  /**
   * Method that manages the key events of the scene.
   *
   * @param e The key event.
   */
  @Override
  public void handleEvents(KeyEvent e) {
    if (e.getCode() == KeyCode.ESCAPE) {
      gameWindow.loadScene(new MenuScene(gameWindow));
    }
  }

  /**
   * Checks the users score and if it beats any of the top local scores, gets added. Also gets sent
   * to the server to get added to the online scores.
   */
  public void setLatestScore() {
    logger.info("getting latest score");
    latestScore = game.getScore().get();
    logger.info(game.getScore().get());
    logger.info(uiList.getOrderedScores().get(0).getValue());
    int length = uiList.getOrderedScores().getSize();
    int position = 9;
    if (uiList.getOrderedScores().getSize() < 10) {
      position = uiList.getOrderedScores().getSize() - 1;
    }

    if (position == 0) {
      logger.info("setting latest score");
      var textBox = new TextField();
      var nameLabel = new Label("Enter your name:");
      VBox scoreSetting = new VBox(nameLabel,textBox);
      mainPane.setTop(scoreSetting);
      textBox.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
        if (keyEvent.getCode() == KeyCode.ENTER) {
          localScores.get().add(new Pair(textBox.getText(), latestScore));
          //loadScores();
          String name = textBox.getText();
          scoreSetting.getChildren().removeAll(textBox,nameLabel);
          mainPane.getChildren().removeAll(textBox,nameLabel,scoreSetting);
          writeScores();
          loadScores();
          writeOnlineScore(name);
          loadOnlineScores();
        }
      });
    } else if (latestScore > Integer.parseInt(
        String.valueOf(uiList.getOrderedScores().get(position).getValue()))) {
      logger.info("setting latest score");
      var textBox = new TextField();
      var nameLabel = new Label("Enter your name:");
      VBox scoreSetting = new VBox(nameLabel,textBox);
      mainPane.setTop(scoreSetting);
      textBox.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
        if (keyEvent.getCode() == KeyCode.ENTER) {
          boolean found = false;
          int i = 1;
          while (found == false) {
            i++;
            if (latestScore < Integer.parseInt(
                String.valueOf(uiList.getOrderedScores().get(length - i).getValue()))) {
              found = true;
            }
          }
          localScores.get().add(length - i + 1, new Pair(textBox.getText(), latestScore));
          String name = textBox.getText();
          scoreSetting.getChildren().removeAll(textBox,nameLabel);
          mainPane.getChildren().removeAll(textBox,nameLabel,scoreSetting);
          //loadScores();
          writeScores();
          loadScores();
          writeOnlineScore(name);
          loadOnlineScores();

        }
      });

    }
  }

  public void setGame(Game game) {
    this.game = game;
  }
}
