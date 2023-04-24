package uk.ac.soton.comp1206.scene;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
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
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.ui.MultiMedia;

public class LobbyScene extends BaseScene {

  /**
   * Create a new scene, passing in the GameWindow the scene will be displayed in
   *
   * @param gameWindow the game window
   */
  private static final Logger logger = LogManager.getLogger(LobbyScene.class);
  private ListProperty<String> channelnames = new SimpleListProperty<>();
  private MultiMedia multiMedia = new MultiMedia();
  private Timer timer = new Timer();
  private Timer channelTimer = new Timer();

  private Communicator communicator;
  private boolean exited = false;
  private boolean channelExited = true;

  public LobbyScene(GameWindow gameWindow) {
    super(gameWindow);
    communicator = gameWindow.getCommunicator();
  }

  @Override
  public void initialise() {
    loop();
  }

  private void loop() {
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        gameWindow.getCommunicator().send("LIST");
        if (exited == false) {
          loop();
        }
      }
    }, 2000);
  }


  @Override
  public void build() {
    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());
    var lobbyPane = new StackPane();
    lobbyPane.setMaxWidth(gameWindow.getWidth());
    lobbyPane.setMaxHeight(gameWindow.getHeight());
    lobbyPane.getStyleClass().add("menu-background");
    root.getChildren().add(lobbyPane);

    var mainPane = new BorderPane();
    mainPane.setPadding(new Insets(20));
    lobbyPane.getChildren().add(mainPane);

    var channels = new VBox();
    channelnames.addListener((observableValue, oldValue, newValue) ->
    {
      channels.getChildren().clear();
      for (String channel : newValue) {
        channels.getChildren().add(new Label(channel));
      }
      for (Node channel : channels.getChildren()) {
        if (channel instanceof Label) {
          channel.setOnMouseClicked((e) -> {
            communicator.send("JOIN " + ((Label) channel).getText());
          });
        }

      }
    });
    StringProperty usernames = new SimpleStringProperty();
    var users = new Label();
    users.getStyleClass().add("labelsmall");

    var label = new Label("Channels:");
    var right = new VBox();
    right.setSpacing(20);

    //Creating the nodes
    var messages = new TextFlow();
    var scroller = new ScrollPane();
    scroller.setContent(messages);
    messages.setPrefSize(300, 150);
    TextField messageToSend = new TextField();
    HBox.setHgrow(messageToSend, Priority.ALWAYS);
    HBox sendMessageBar = new HBox();
    Button sendMessage = new Button("Send");
    sendMessageBar.getChildren().add(messageToSend);
    sendMessageBar.getChildren().add(sendMessage);

    VBox buttons = new VBox();
    Button leave = new Button("Leave");
    Button changeNick = new Button("Change name");
    Button start = new Button("Start");
    Button create = new Button("Create");

    right.getChildren().add(create);
    mainPane.setTop(label);
    mainPane.setCenter(channels);
    mainPane.setRight(right);

    // Adding the textbox
    var textBoxNick = new TextField();
    // Adding the label
    var label1Nick = new Label("New name:");

    create.setOnMouseEntered((e) -> {
      multiMedia.playAudioFile("sounds/mouse_hover.mp3");
    });
    create.setOnMouseClicked((e) -> {
      right.getChildren().clear();
      mainPane.getChildren().remove(create);

      // Adding the textbox
      var textBox = new TextField();
      // Adding the label
      var label1 = new Label("Channel name:");
      label1.getStyleClass().add("labelsmall");

      right.getChildren().add(label1);
      right.getChildren().add(textBox);
      textBox.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
        if (keyEvent.getCode() == KeyCode.ENTER) {
          gameWindow.getCommunicator().send("CREATE " + textBox.getText());
          textBox.clear();
        }
      });
    });

    //Listener for the communicator
    gameWindow.getCommunicator().addListener(communication -> {
      String[] arr = communication.split(" ", 2);
      switch (arr[0]) {
        case "CHANNELS": {
          if (arr.length == 1) {
            Platform.runLater(() -> {
              channelnames.clear();
            });
            break;
          }
          String arr1 = arr[1];
          arr = arr1.split("\n");
          ObservableList<String> temp = FXCollections.observableArrayList(new ArrayList<>());
          for (String channel : arr) {
            Platform.runLater(() -> temp.add(channel));
          }
          channelnames.set(temp);
          break;
        }

        case "JOIN": {
          String channel = arr[1];
          Platform.runLater(() -> {

            channelExited = false;
            channelLoop();

            usernames.addListener((observableValue, oldValue, newValue) -> {
              users.setText("Users:" + System.lineSeparator() + newValue);
            });

            right.getChildren().clear();
            buttons.getChildren().clear();

            right.getChildren().add(new Label(channel));
            right.getChildren().add(scroller);
            right.getChildren().add(sendMessageBar);
            buttons.getChildren().addAll(leave, changeNick);
            right.getChildren().add(users);
            right.getChildren().add(buttons);

            messageToSend.setOnKeyPressed(keyEvent -> {
              if (keyEvent.getCode() != KeyCode.ENTER) {
                return;
              }
              communicator.send("MSG " + messageToSend.getText());
              messageToSend.clear();
            });

            sendMessage.setOnAction(actionEvent -> {
              communicator.send("MSG " + messageToSend.getText());
              messageToSend.clear();
            });

            leave.setOnAction(actionEvent -> {
              communicator.send("PART");
            });

            changeNick.setOnAction(actionEvent -> {

              right.getChildren().removeAll(textBoxNick, label1Nick);
              label1Nick.getStyleClass().add("labelsmall");

              right.getChildren().add(label1Nick);
              right.getChildren().add(textBoxNick);

              textBoxNick.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
                if (keyEvent.getCode() == KeyCode.ENTER) {
                  communicator.send("NICK " + textBoxNick.getText());
                  textBoxNick.clear();
                  right.getChildren().removeAll(label1Nick, textBoxNick);
                }
              });

            });

          });
          break;
        }

        case "PARTED": {
          Platform.runLater(() -> {
            buttons.getChildren().clear();
            messages.getChildren().clear();
            right.getChildren().clear();
            mainPane.getChildren().removeAll(scroller, sendMessageBar, buttons);
            channelExited = true;
            right.getChildren().add(create);
          });
          break;
        }

        case "HOST": {
          Platform.runLater(() -> {
            buttons.getChildren().add(start);
            start.setOnAction(actionEvent -> {
              communicator.send("START");
            });
          });
          break;
        }

        case "MSG": {
          String message = arr[1];
          Platform.runLater(() -> {
            Text receivedMessage = new Text(message + "\n");
            messages.getChildren().add(receivedMessage);
            multiMedia.playAudioFile("sounds/message.wav");
          });
          break;
        }
        case "START": {
          Platform.runLater(() -> {
            exited = true;
            channelExited = true;
            gameWindow.loadScene(new MultiplayerScene(gameWindow, communicator));
            multiMedia.playBackgroundMusic("music/game.wav");
          });

          break;
        }
        case "ERROR": {
          String errorMessage = arr[1];
          Label errorLabel = new Label("Error: " + errorMessage);
          Platform.runLater(() -> {
            mainPane.setBottom(errorLabel);
          });
          break;
        }
        case "USERS": {
          String arr1 = arr[1];
          Platform.runLater(() -> {
            usernames.set(arr1);
          });
          break;
        }
        default:
          break;
      }


    });
  }

  private void channelLoop() {
    channelTimer.schedule(new TimerTask() {
      @Override
      public void run() {
        if (channelExited == false) {
          gameWindow.getCommunicator().send("USERS");
          if (channelExited == false) {
            channelLoop();
          }
        }
      }
    }, 3000);
  }


  @Override
  public void handleEvents(KeyEvent e) {
    if (e.getCode() == KeyCode.ESCAPE) {
      exited = true;
      communicator.send("PART");
      gameWindow.loadScene(new MenuScene(gameWindow));
    }
  }
}
