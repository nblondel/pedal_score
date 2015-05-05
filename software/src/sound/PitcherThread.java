package sound;

import java.util.concurrent.BlockingQueue;

import queues.PitchBuffer;
import queues.SoundFile;

public class PitcherThread {
  private static PitcherThread singleton = null;
  private PitcherThread() {}

  public static PitcherThread getPitcherThread() {
    if(singleton == null)
      singleton = new PitcherThread();
    return singleton;
  }

  public void setQueues(BlockingQueue<SoundFile> soundFileQueue, BlockingQueue<PitchBuffer> pitchBufferQueue) {
    Pitcher.getPitcher().setQueues(soundFileQueue, pitchBufferQueue);
  }

  public void startPitcher() {
    Pitcher pitcher = Pitcher.getPitcher();
    
    Thread pitchThread = new Thread() {
      public void run() {
        pitcher.start();
      }
    };
    pitchThread.start();
  }
  
  public void stopPitcher() {
    Pitcher pitcher = Pitcher.getPitcher();
    
    Thread pitchThread = new Thread() {
      public void run() {
        pitcher.stop();
      }
    };
    pitchThread.start();
  }
}
