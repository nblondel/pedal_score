package sound;

import java.util.concurrent.BlockingQueue;

import queues.PitchBuffer;
import queues.SoundFile;

public class PitcherThread {
  public static void setQueues(BlockingQueue<SoundFile> soundFileQueue, BlockingQueue<PitchBuffer> pitchBufferQueue) {
    Pitcher.getPitcher().setQueues(soundFileQueue, pitchBufferQueue);
  }

  public static void startPitcher() {
    Pitcher pitcher = Pitcher.getPitcher();
    
    Thread pitchThread = new Thread() {
      public void run() {
        pitcher.start();
      }
    };
    pitchThread.start();
  }
  
  public static void stopPitcher() {
    Pitcher pitcher = Pitcher.getPitcher();
    
    Thread pitchThread = new Thread() {
      public void run() {
        pitcher.stop();
      }
    };
    pitchThread.start();
  }
}
