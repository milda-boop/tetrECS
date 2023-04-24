package uk.ac.soton.comp1206.game;

import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Pair;
import uk.ac.soton.comp1206.event.CommunicationsListener;
import uk.ac.soton.comp1206.network.Communicator;

public class MultiplayerGame extends Game {

  private GamePiece[] pieceList = new GamePiece[3];
  private ArrayList<Pair<String, Integer>> list = new ArrayList<Pair<String, Integer>>();
  private ObservableList<Pair<String, Integer>> observableList = FXCollections.observableArrayList(
      list);
  private SimpleListProperty userScores = new SimpleListProperty<Pair<String, Integer>>(
      observableList);
  private Communicator communicator;

  /**
   * Create a new game with the specified rows and columns. Creates a corresponding grid model.
   *
   * @param cols number of columns
   * @param rows number of rows
   */
  public MultiplayerGame(int cols, int rows, Communicator communicator) {
    super(cols, rows);
    this.communicator = communicator;
    addListeners();
    communicator.send("SCORES");
    communicator.send("PIECE");
    communicator.send("PIECE");
  }

  @Override
  public void initialiseGame() {
    boolean received = false;

    do {
      if (pieceList[1] != null) {
        try {
          Thread.sleep(50);
        } catch (Exception e) {
          e.printStackTrace();
        }
        logger.info("Received is true");
        received = true;
      }
    } while (received == false);
    currentPiece = spawnPiece();
    followingPiece = spawnPiece();
    logger.info(pieceList);
    //logger.info("Piece to be played is " + currentPiece.toString());
    setNextPiece(currentPiece, followingPiece);
    gameLoop();

  }

  private void addListeners() {
    communicator.addListener(communication -> {
      String[] arr = communication.split(" ", 2);
      switch (arr[0]) {
        case "PIECE": {
          if (pieceList[0] == null) {
            pieceList[0] = GamePiece.createPiece(Integer.parseInt(arr[1]));
          } else if (pieceList[1] == null) {
            pieceList[1] = GamePiece.createPiece(Integer.parseInt(arr[1]));
          }
          break;
        }
        case "SCORE": {
          communicator.send("SCORES");
          break;
        }
        case "SCORES": {
          Platform.runLater(() -> {
            userScores.clear();
            String[] scores = arr[1].split("\n");
            //sorting
            List<Pair<String,Integer>> scoresList = new ArrayList<>();
            for (String score : scores) {
              String player = score.split(":", 3)[0];
              Integer playerScore = Integer.parseInt(score.split(":", 3)[1]);
              String playerLife = score.split(":",3)[2];
              if(!playerLife.equals("DEAD"))
              {
                scoresList.add(new Pair<>(player,playerScore));
              }
            }
            scoresList.sort(Comparator.<Pair<String,Integer>>comparingInt(Pair::getValue));
            Collections.reverse(scoresList);
            //updating
            for (Pair<String,Integer> score : scoresList) {
                userScores.add(score);
            }

            });

          }
        }
    });
  }

  @Override
  public GamePiece spawnPiece() {
    GamePiece temp = pieceList[0];
    pieceList[0] = pieceList[1];
    pieceList[1] = null;
    communicator.send("PIECE");
    return temp;
  }

  @Override
  public void score(int lines, int blocks) {
    int linesScore = lines * blocks * multiplier.get() * 10;
    score.set(score.get() + linesScore);
    communicator.send("SCORE " + score.get());
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

  @Override
  public SimpleListProperty getUserScores() {
    return userScores;
  }
  @Override
  protected void gameLoop()
  {
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
            communicator.send("LIVES " + lives.get());
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


}
