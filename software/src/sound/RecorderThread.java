package sound;

import java.io.IOException;

import javax.sound.sampled.LineUnavailableException;

import org.eclipse.swt.widgets.Shell;

public class RecorderThread {
  public static boolean setWritingInterval(int interval) {
    return Recorder.getRecorder().setWritingInterval(interval);
  }
  
  public static boolean checkWritingInterval(int interval) {
    return Recorder.getRecorder().checkWritingInterval(interval);
  }
  
  public static int getWritingInterval() {
    return Recorder.getRecorder().getWritingInterval();
  }
  
  public static void startRecorder(final Shell shell) {
    final Recorder recorder = Recorder.getRecorder();

    // Create a separate thread
    Thread recordThread = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          recorder.start();
        } catch (LineUnavailableException ex) {
          ex.printStackTrace();
          stopRecorder();
        }
      }
    });
    recordThread.start();
  }
  
  public static void stopRecorder() {
    final Recorder recorder = Recorder.getRecorder();

    // Create a separate thread
    Thread recordThread = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          recorder.stop();
        } catch (IOException ex) {
          ex.printStackTrace();
        }
      }
    });
    recordThread.start();
    
    try {
      recordThread.join(2000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
