package uk.ac.soton.comp1206.scene;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Grid;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

public class InstructionsScene extends BaseScene
{

  /**
   * Create a new scene, passing in the GameWindow the scene will be displayed in
   *
   * @param gameWindow the game window
   */
  public InstructionsScene(GameWindow gameWindow) {
    super(gameWindow);
  }

  @Override
  public void initialise() {

  }

  @Override
  public void build() {
    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    var instructionsPane = new StackPane();
    instructionsPane.setMaxWidth(gameWindow.getWidth());
    instructionsPane.setMaxHeight(gameWindow.getHeight());
    instructionsPane.getStyleClass().add("menu-background");
    root.getChildren().add(instructionsPane);

    var mainPane = new BorderPane();
    mainPane.setPadding(new Insets(20));
    instructionsPane.getChildren().add(mainPane);

    var centre = new VBox();
    centre.setAlignment(Pos.CENTER);
    List<Label> labels = new ArrayList<>();
    var label1 = new Label("1: Place a piece anywhere on the board by its centre block.");
    var label2 = new Label("2: Rotate pieces by right clicking anywhere on the board.");
    var label3 = new Label("3: On the right, find the next two incoming pieces. Press the spacebar to swap them around.");
    var label4 = new Label("4: Clear lines by filling them in either horizontally or vertically.");
    var label5 = new Label("5: For booster points, try clear multiple lines at once or keep a streak of clearing lines to increase"
        + System.lineSeparator() + " the multiplier.");
    var label6 = new Label("6: The timer will increase with each level, place the pieces before the timer is up.");
    var label7 = new Label("7: You have three lives, try beat the highscore... Good Luck!");
    Collections.addAll(labels,label1,label2,label3,label4,label5,label6,label7);
    for(Label label : labels)
    {
      label.getStyleClass().add("labelsmall");
      label.setAlignment(Pos.CENTER);
    }
    centre.getChildren().addAll(label1,label2,label3,label4,label5,label6,label7);
    centre.setSpacing(10);
    mainPane.setCenter(centre);

    var bottom = new VBox();
    bottom.setAlignment(Pos.CENTER);
    bottom.setSpacing(30);
    var piecesLabel = new Label("THE GAME PIECES");
    var grid = new GridPane();
    grid.setAlignment(Pos.CENTER);
    grid.setHgap(20);
    grid.setVgap(20);
    int count = 0;
    for(int i = 0; i< 5; i++)
    {
      for(int j = 0; j < 3; j++)
      {
        var piece = new PieceBoard(new Grid(3,3),gameWindow.getWidth() / 15,
            gameWindow.getWidth() / 15);
        piece.setPiece(GamePiece.createPiece(count));
        count++;
        GridPane.setConstraints(piece,i,j);
        grid.getChildren().add(piece);
      }

    }
    bottom.getChildren().add(piecesLabel);
    bottom.getChildren().add(grid);
    mainPane.setBottom(bottom);



  }

  @Override
  public void handleEvents(KeyEvent e) {
    if(e.getCode()== KeyCode.ESCAPE)
    {
      gameWindow.loadScene(new MenuScene(gameWindow));
    }
  }
}
