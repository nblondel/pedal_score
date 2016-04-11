package main;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import dialogs.DisplayWindow;
import queues.NoteBuffer;
import queues.PitchBuffer;

public class Displayer {
  private static Displayer singleton = null;
  private BlockingQueue<PitchBuffer> pitchBufferQueue;
  private BlockingQueue<NoteBuffer> noteBufferQueue;
  
  private DisplayWindow displayWindow;
  private boolean isRunning = false;
  private int queueReadTimeoutMs = 1000; // Timeout in milliseconds
  
  private Displayer() { }
  public static Displayer getDisplayer() {
    if(singleton == null)
      singleton = new Displayer();
    return singleton;
  }
  
  public void setQueues(BlockingQueue<PitchBuffer> pitchBufferQueue, BlockingQueue<NoteBuffer> noteBufferQueue) {
    this.pitchBufferQueue = pitchBufferQueue;
    this.noteBufferQueue = noteBufferQueue;
  }
  
  public void setDisplayWindow(DisplayWindow displayWindow) {
    this.displayWindow = displayWindow;
  }
  
  public void startPitchDisplay() {
    if(pitchBufferQueue != null && isRunning == false) {
      try {
        System.out.println("Start pitch displayer.");
        isRunning = true;
        while(isRunning) {
          /* Blocks until new sound file comes */
          PitchBuffer pitchBuffer = pitchBufferQueue.poll(queueReadTimeoutMs, TimeUnit.MILLISECONDS);
          if(pitchBuffer != null) {
            /* This thread need to be non blocking for being stopped. */

            displayWindow.addPitches(pitchBuffer.size(), pitchBuffer.getTimeArray(), pitchBuffer.getFrequencyArray());
            pitchBuffer.clear();
          }
          
          /* Blocks until new sound file comes */
          NoteBuffer noteBuffer = noteBufferQueue.poll(queueReadTimeoutMs, TimeUnit.MILLISECONDS);
          if(noteBuffer != null) {
            /* This thread need to be non blocking for being stopped. */

            displayWindow.addNotes(noteBuffer.size(), noteBuffer.getNamesArray(), noteBuffer.getOctavesArray(), noteBuffer.getTimeArray(), noteBuffer.getFrequencyArray(), noteBuffer.getHiddenArray());
            noteBuffer.clear();
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    } else {
      System.err.println("The pitches queue has not been initialized!");
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
