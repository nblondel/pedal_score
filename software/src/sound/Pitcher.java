package sound;

import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import queues.PitchBuffer;
import queues.SoundFile;
import common.Constants;
import common.Utils;

public class Pitcher {
  /* Singleton */
  private static Pitcher singleton = null;
  
  /* Thread */
  private boolean isRunning = false;
  private int loopIntervalMs = 1000; // Timeout in milliseconds
  
  /* Queue */
  private BlockingQueue<SoundFile> soundFileQueue = null;
  private BlockingQueue<PitchBuffer> pitchBufferQueue = null;

  public static Pitcher getPitcher() {
    if(singleton == null)
      singleton = new Pitcher();
    return singleton;
  }
  
  /**
   * Set queues to interact with.
   * @param soundFileQueue The queue where to extract the sound file to pitch
   * @param pitchBufferQueue The queue where to insert the pitch buffer computed from sound files.
   */
  public void setQueues(BlockingQueue<SoundFile> soundFileQueue, BlockingQueue<PitchBuffer> pitchBufferQueue) {
    this.soundFileQueue = soundFileQueue;
    this.pitchBufferQueue = pitchBufferQueue;
  }
  
  public void start() {
    /* Read the sound file queue */
    if(soundFileQueue != null && isRunning == false) {
      try {
        System.out.println("Start pitching.");
        isRunning = true;
        while(isRunning) {
          /* Blocks until new sound file comes */
          SoundFile soundFile = soundFileQueue.poll(loopIntervalMs, TimeUnit.MILLISECONDS);
          if(soundFile != null) {
            /* This thread need to be non blocking for being stopped. */
            
            if(soundFile.exists()) {
              System.out.println("Extracted file from queue '"+ soundFile.getFileAbsolutePath() +"'");

              /* Extract the pitch from the sound file */
              String[] command = new String[]{ "cmd", "/c", Constants.pitch_executable, "-i", soundFile.getFileAbsolutePath() };

              // Execute the exe file to create pitch raw points
              ProcessBuilder process = new ProcessBuilder(command);
              process.directory(new File(System.getProperty("user.dir"), Constants.aubio_path));
              //process.redirectErrorStream(true);

              /* Extract the result to the pitch buffer queue */
              try {
                Process commandShell = process.start();
                Utils.StreamGobbler outputGobbler = new Utils.StreamGobbler(commandShell.getInputStream(), pitchBufferQueue);
                outputGobbler.start();
                commandShell.waitFor();
                outputGobbler.join(500);
              } catch (Exception e) {
                e.printStackTrace();
              }
            }
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    } else {
      System.err.println("The sound file queue has not been initialized!");
    }
  }
  
  public void stop() {
    if(isRunning == true) {
      try {
        System.out.println("Stopping pitching...");
        isRunning = false;
        Thread.sleep(loopIntervalMs);
        System.out.println("Pitching stopped.");
      } catch(Exception e) {
        e.printStackTrace();
      }
    }
  }
}
