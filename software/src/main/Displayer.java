package main;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import dialogs.DisplayWindow;
import queues.PitchBuffer;

public class Displayer {
  private static Displayer singleton = null;
  private BlockingQueue<PitchBuffer> pitchBufferQueue;
  private DisplayWindow displayWindow;
  private boolean isRunning = false;
  private int queueReadTimeoutMs = 1000; // Timeout in milliseconds
  
  private Displayer() { }
  public static Displayer getDisplayer() {
    if(singleton == null)
      singleton = new Displayer();
    return singleton;
  }
  
  public void setQueue(BlockingQueue<PitchBuffer> pitchBufferQueue) {
    this.pitchBufferQueue = pitchBufferQueue;
  }
  
  public void setDisplayWindow(DisplayWindow displayWindow) {
    this.displayWindow = displayWindow;
  }
  
  public void start() {
    if(pitchBufferQueue != null && isRunning == false) {
      try {
        System.out.println("Start pitching.");
        isRunning = true;
        while(isRunning) {
          /* Blocks until new sound file comes */
          PitchBuffer pitchBuffer = pitchBufferQueue.poll(queueReadTimeoutMs, TimeUnit.MILLISECONDS);
          if(pitchBuffer != null) {
            /* This thread need to be non blocking for being stopped. */

            displayWindow.addGraphicPoints(pitchBuffer.size(), pitchBuffer.getFrequencyArray(), pitchBuffer.getTimeArray());
            pitchBuffer.clear();
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
        System.out.println("Stopping displayer...");
        isRunning = false;
        Thread.sleep(queueReadTimeoutMs);
        System.out.println("Displayer stopped.");
      } catch(Exception e) {
        e.printStackTrace();
      }
    }
  }
}
