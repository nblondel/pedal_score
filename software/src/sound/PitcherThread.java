package sound;

import java.io.File;

import common.Constants;
import common.Utils;

public class PitcherThread {
  public static void startPitcher() {
    Thread pitchThread = new Thread() {
      public void run() {
        /* Read the sound file queue */
        String waveFilePath = "test.wav"; // TODO read queue
        File waveFile = new File(waveFilePath);
        
        if(waveFile.exists()) {
          /* Extract the pitch from the sound file */
          String[] command = new String[]{ "cmd", "/c", Constants.pitch_executable, "-i", waveFile.getAbsolutePath() };

          // Execute the exe file to create pitch raw file
          ProcessBuilder process = new ProcessBuilder(command);
          process.directory(new File(System.getProperty("user.dir"), Constants.aubio_path));
          //process.redirectErrorStream(true);

          /* Extract the result to the pitch buffer queue */
//          try {
//            Process commandShell = process.start();
//            Utils.StreamGobbler outputGobbler = new Utils.StreamGobbler(commandShell.getInputStream(), rawResultFile);
//            outputGobbler.start();
//            commandShell.waitFor();
//            outputGobbler.join(500);
//          } catch (Exception e) {
//            e.printStackTrace();
//          }
        }
      }
    };
    pitchThread.start();
  }
}
