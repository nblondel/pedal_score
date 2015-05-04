package sound;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.apache.commons.io.FileUtils;

import common.Constants;

public class RecordFileWriter {
  private static RecordFileWriter singleton = null;
  private RecordFileWriter() {}
  
  public static RecordFileWriter getRecordWriter() {
    if(singleton == null)
      singleton = new RecordFileWriter();
    return singleton;
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
        String newWavFile = System.getProperty("user.dir") + File.separator + Constants.tmpOutputDirectory + File.separator + datetime + ".wav";
        
        try {
          System.out.println("Save new file '" + newWavFile + "'");
          if(!new File(System.getProperty("user.dir") + File.separator + Constants.tmpOutputDirectory).exists())
            FileUtils.forceMkdir(new File(System.getProperty("user.dir") + File.separator + Constants.tmpOutputDirectory));
            
          AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, new File(newWavFile));
          audioInputStream.close();
          recordBytes.close();
          
          // TODO Writer in sound file queue
        } catch (IOException e) {
          e.printStackTrace();
          new File(newWavFile).delete();
        }
      }
    };
    savingThread.start();
  }
}
