package uk.ac.soton.comp1206.component;

import java.util.Comparator;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ScoresList extends VBox
{
  private static final Logger logger = LogManager.getLogger(ScoresList.class);
  protected ListProperty<Pair<String, Integer>> scores = new SimpleListProperty<>();

  public ScoresList()
  {
    scores.addListener((observableValue, oldValue, newValue) ->
    {
      getChildren().clear();
      for(Pair<String,Integer> score : newValue)
      {
        getChildren().add(new Label(score.getKey() + ": " + score.getValue()));
      }
    });
  }
  public void reveal()
  {

  }
  public ListProperty scoresProperty()
  {
    return scores;
  }
  public ListProperty<Pair<String,Integer>> getOrderedScores()
  {
    return scores;
  }

}
