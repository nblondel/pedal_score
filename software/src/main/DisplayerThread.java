package main;

import java.util.concurrent.BlockingQueue;

import dialogs.DisplayWindow;
import queues.PitchBuffer;

public class DisplayerThread {
  
  public static void setQueue(BlockingQueue<PitchBuffer> pitchBufferQueue) {
    Displayer.getDisplayer().setQueue(pitchBufferQueue);
  }
  
  public static void setDisplayWindow(DisplayWindow displayWindow) {
    Displayer.getDisplayer().setDisplayWindow(displayWindow);
  }
  
  public static void startDisplayer() {
    Displayer displayer = Displayer.getDisplayer();
    
    Thread displayThread = new Thread() {
      public void run() {
        displayer.start();
      }
    };
    displayThread.start();
  }
  
  public static void stopDisplayer() {
    Displayer displayer = Displayer.getDisplayer();
    
    Thread displayThread = new Thread() {
      public void run() {
        displayer.stop();
      }
    };
    displayThread.start();
  }
}
