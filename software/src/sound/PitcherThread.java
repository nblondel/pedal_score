package sound;

import java.util.concurrent.BlockingQueue;

import queues.NoteBuffer;
import queues.PitchBuffer;
import queues.SoundFile;

public class PitcherThread {
  public static void setQueues(BlockingQueue<SoundFile> soundFileQueue, BlockingQueue<PitchBuffer> pitchBufferQueue, BlockingQueue<NoteBuffer> noteBufferQueue) {
    Pitcher.getPitcher().setQueues(soundFileQueue, pitchBufferQueue, noteBufferQueue);
  }

  public static void startPitcher() {
    final Pitcher pitcher = Pitcher.getPitcher();
    
    Thread pitchThread = new Thread() {
      public void run() {
        pitcher.start();
      }
    };
    pitchThread.start();
  }
  
  public static void stopPitcher() {
    final Pitcher pitcher = Pitcher.getPitcher();
    
    Thread pitchThread = new Thread() {
      public void run() {
        pitcher.stop();
      }
    };
    pitchThread.start();
  }
}
