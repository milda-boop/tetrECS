package uk.ac.soton.comp1206.scene;

import javafx.animation.Animation;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.ui.MultiMedia;

/**
 * The main menu of the game. Provides a gateway to the rest of the game.
 */
public class MenuScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);
    private MultiMedia multiMedia = new MultiMedia();

    /**
     * Create a new menu scene
     * @param gameWindow the Game Window this will be displayed in
     */
    public MenuScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Menu Scene");
    }

    /**
     * Build the menu layout
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var menuPane = new StackPane();
        menuPane.setMaxWidth(gameWindow.getWidth());
        menuPane.setMaxHeight(gameWindow.getHeight());
        menuPane.getStyleClass().add("menu-background");
        root.getChildren().add(menuPane);

        var mainPane = new BorderPane();
        menuPane.getChildren().add(mainPane);

        var title = new Text("TetrECS");
        //title.setWrappingWidth(gameWindow.getWidth());
        //title.setTextAlignment(TextAlignment.CENTER);
        title.getStyleClass().add("bigtitle");
        mainPane.setTop(title);
        ScaleTransition st = new ScaleTransition(Duration.millis(4000), title);
        st.setByX(0.8f);
        st.setByY(0.8f);
        st.setCycleCount(Animation.INDEFINITE);
        st.setAutoReverse(true);
        st.play();

        BorderPane.setMargin(title, new Insets(100, 0, 0, 0));
        BorderPane.setAlignment(title,Pos.CENTER);

        //For now, let us just add a button that starts the game. I'm sure you'll do something way better.
        var list = new VBox();
        var button = new Button("Play");
        var button1 = new Button("Instructions");
        var button2 = new Button("Multiplayer");
        var button3 = new Button("Quit");

        list.getChildren().add(button);
        list.getChildren().add(button1);
        list.getChildren().add(button2);
        list.getChildren().add(button3);
        list.setAlignment(Pos.CENTER);
        list.setSpacing(20);
        mainPane.setCenter(list);

        button.setOnMouseEntered((e)->{
            multiMedia.playAudioFile("sounds/mouse_hover.mp3");
        });
        button1.setOnMouseEntered((e)->{
            multiMedia.playAudioFile("sounds/mouse_hover.mp3");
        });
        button.setOnMouseClicked((e)->{
            multiMedia.playAudioFile("sounds/explode.wav");
        });
        button1.setOnMouseClicked((e)->{
            multiMedia.playAudioFile("sounds/explode.wav");
        });

        button2.setOnMouseEntered((e)->{
            multiMedia.playAudioFile("sounds/mouse_hover.mp3");
        });
        button2.setOnMouseClicked((e)->{
            multiMedia.playAudioFile("sounds/explode.wav");
        });

        button3.setOnMouseEntered((e)->{
            multiMedia.playAudioFile("sounds/mouse_hover.mp3");
        });
        button3.setOnMouseClicked((e)->{
            multiMedia.playAudioFile("sounds/explode.wav");
        });

        //Bind the button action to the startGame method in the menu
        button.setOnAction(this::startGame);
        button1.setOnAction(this::getInstructions);
        button2.setOnAction(this::startLobby);
        button3.setOnAction(this::endApplication);
    }

    @Override
    public void handleEvents(KeyEvent e) {

    }

    /**
     * Initialise the menu
     */
    @Override
    public void initialise() {
        multiMedia.playBackgroundMusic("music/menu.mp3");
    }

    /**
     * Handle when the Start Game button is pressed
     * @param event event
     */
    private void startGame(ActionEvent event) {
        gameWindow.startChallenge();
        multiMedia.playBackgroundMusic("music/game.wav");
    }
    private void getInstructions(ActionEvent event)
    {
        gameWindow.getInstructions();
    }
    private void startLobby(ActionEvent event){gameWindow.startLobby();}

    private void endApplication(ActionEvent event)
    {
        System.exit(0);
    }
}
