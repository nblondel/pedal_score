package main;

import java.util.concurrent.BlockingQueue;

import dialogs.DisplayWindow;
import queues.NoteBuffer;
import queues.PitchBuffer;

public class DisplayerThread {
  
  public static void setQueues(BlockingQueue<PitchBuffer> pitchBufferQueue, BlockingQueue<NoteBuffer> noteBufferQueue) {
    Displayer.getDisplayer().setQueues(pitchBufferQueue, noteBufferQueue);
  }
  
  public static void setDisplayWindow(DisplayWindow displayWindow) {
    Displayer.getDisplayer().setDisplayWindow(displayWindow);
  }
  
  public static void startDisplayer() {
    Displayer displayer = Displayer.getDisplayer();
    
    Thread displayThread = new Thread() {
      public void run() {
        displayer.startPitchDisplay();
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
