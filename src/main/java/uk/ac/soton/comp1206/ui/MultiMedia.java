package uk.ac.soton.comp1206.ui;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.scene.MenuScene;

/**
 * Controls and manages the music and sounds played throughout the game.
 */
public class MultiMedia{

  private static MediaPlayer audioPlayer;
  private static MediaPlayer musicPlayer;
  private static boolean musicPlaying = false;
  private static final Logger logger = LogManager.getLogger(MenuScene.class);

  public static void playAudioFile(String file) {
    String toPlay = MultiMedia.class.getResource("/" + file).toExternalForm();
    logger.info("Playing audio: " + toPlay);
    try {
      Media play = new Media(toPlay);
      audioPlayer = new MediaPlayer(play);
      audioPlayer.play();
    } catch (Exception e) {
      e.printStackTrace();
      logger.error("Unable to play audio file, disabling audio");
    }
  }

  public static void playBackgroundMusic(String file) {
    String toPlay = MultiMedia.class.getResource("/" + file).toExternalForm();
    if(musicPlaying)
    {
      musicPlayer.stop();
    }
    try {
      Media play = new Media(toPlay);
      musicPlayer = new MediaPlayer(play);
      musicPlayer.setVolume(0.5);
      //plays music in a loop
      musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
      musicPlayer.play();
      musicPlaying = true;
    } catch (Exception e) {
      e.printStackTrace();
      logger.error("Unable to play audio file, disabling audio");
    }

  }

}
