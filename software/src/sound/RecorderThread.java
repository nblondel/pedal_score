package sound;

import java.io.IOException;

import javax.sound.sampled.LineUnavailableException;

public class RecorderThread {
  public static void setWritingInterval(int interval) {
    Recorder.getRecorder().setWritingInterval(interval);
  }
  
  public static void startRecorder() {
    Recorder recorder = Recorder.getRecorder();

    // Create a separate thread for recording
    Thread recordThread = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          System.out.println("Start recording.");
          recorder.start();
        } catch (LineUnavailableException ex) {
          ex.printStackTrace();
        }
      }
    });
    recordThread.start();
  }
  
  public static void stopRecorder() {
    Recorder recorder = Recorder.getRecorder();
    try {
      System.out.println("Stopping recording...");
      recorder.stop();
      System.out.println("Recording stopped.");
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }
}
