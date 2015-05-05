package common;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.concurrent.BlockingQueue;

import queues.PitchBuffer;

public class Utils {
  public static class StreamGobbler extends Thread {
    InputStream is;
    BlockingQueue<PitchBuffer> pitchBufferQueue;

    public StreamGobbler(InputStream is, BlockingQueue<PitchBuffer> pitchBufferQueue) {
      this.is = is;
      this.pitchBufferQueue = pitchBufferQueue;
    }

    public void run() {
      PrintWriter pw = null;
      try {
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line=null;
        
        /* Write the result in the queue */
        PitchBuffer pitchBuffer = new PitchBuffer();
        while ( (line = br.readLine()) != null) {
          String[] words = line.split("\\s+");          
          if(words.length == 2) {
            try {
              double frequency = Double.parseDouble(words[0]);
              double time = Double.parseDouble(words[1]);

              pitchBuffer.addPitch(frequency, time);
            } catch(Exception e) {
              System.err.println("Exception while parsing double in this pitch line: " + line);
            }
          } else {
            System.err.println("Weird pitch result: " + line);
          }
        }
        pitchBufferQueue.add(pitchBuffer);
        System.out.println("New buffer of pitches inserted.");
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        if (pw != null) {
          pw.close();
        }
      }
    }
  }
}