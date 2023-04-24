package uk.ac.soton.comp1206.event;

import java.util.HashSet;
import javafx.util.Pair;

public interface LineClearedListener {
    public void lineCleared(HashSet<Pair<Integer,Integer>> blocks);
}
