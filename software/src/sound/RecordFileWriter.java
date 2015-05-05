package sound;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.BlockingQueue;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import queues.SoundFile;
import common.Constants;

public class RecordFileWriter {
  private BlockingQueue<SoundFile> soundFileQueue = null;
  private static RecordFileWriter singleton = null;
  private RecordFileWriter() {}
  
  public static RecordFileWriter getRecordWriter() {
    if(singleton == null)
      singleton = new RecordFileWriter();
    return singleton;
  }
  
  public void setQueue(BlockingQueue<SoundFile> soundFileQueue) {
    this.soundFileQueue = soundFileQueue;
  }
  
  /**
   * Save recorded sound data into a .wav file format.
   * Save the new .wav file in the sound file queue.
   * @param recordBytes The bytes stream to save.
   * @throws IOException if any I/O error occurs.
   */
  public void save(ByteArrayOutputStream recordBytes, AudioFormat format) {
    Thread savingThread = new Thread() {
      public void run() {
        /* Create audio input stream to write */
        byte[] audioData = recordBytes.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
        AudioInputStream audioInputStream = new AudioInputStream(bais, format, audioData.length / format.getFrameSize());

        /* Generate a temporary wav file name */
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String datetime = dateFormat.format(Calendar.getInstance().getTime()) + "_" + Calendar.getInstance().get(Calendar.MILLISECOND);
        SoundFile newWavFile = new SoundFile(System.getProperty("user.dir") + File.separator + Constants.tmpOutputDirectory + File.separator + datetime + ".wav");
        
        try {
          System.out.println("Save new file '" + newWavFile.getFileAbsolutePath() + "'");
          AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, new File(newWavFile.getFileAbsolutePath()));
          audioInputStream.close();
          recordBytes.close();
          
          /* Write the sound in the queue for PitcherThread */
          if(soundFileQueue != null) {
            // TODO use a non blocking method an buffer all the file name temporary (see http://tutorials.jenkov.com/java-util-concurrent/blockingqueue.html)
            soundFileQueue.put(newWavFile);
          } else {
            System.err.println("The sound file queue has not been initialized!");
          }
          
        } catch (Exception e) {
          e.printStackTrace();
          newWavFile.delete();
        }
      }
    };
    savingThread.start();
  }
}
