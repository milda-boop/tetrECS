package uk.ac.soton.comp1206.component;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Pair;

public class LeaderBoard extends ScoresList{
  //private ListProperty<String> lives = new SimpleListProperty<>();
  public LeaderBoard()
  {
      scores.addListener((observableValue, oldValue, newValue) ->
      {
        getChildren().clear();
        for(Pair<String,Integer> score : newValue)
        {
          Label label = new Label(score.getKey() + ": " + score.getValue());
          label.getStyleClass().add("labelsmall");
          getChildren().add(label);
        }
      });

  }
}
