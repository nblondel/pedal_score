package sound;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;

import queues.Note;
import queues.NoteBuffer;
import queues.PitchBuffer;
import queues.SoundFile;
import common.Constants;

public class Pitcher {
  /* Singleton */
  private static Pitcher singleton = null;
  
  /* Thread */
  private boolean isRunning = false;
  private int queueReadTimeoutMs = 1000; // Timeout in milliseconds
  
  /* Queue */
  private BlockingQueue<SoundFile> soundFileQueue = null;
  private BlockingQueue<PitchBuffer> pitchBufferQueue = null;
  private BlockingQueue<NoteBuffer> noteBufferQueue = null;
  
  /* Filter attributes */
  private List<Note> referenceNotes = new ArrayList<Note>();
  
  public static Pitcher getPitcher() {
    if(singleton == null)
      singleton = new Pitcher();
    return singleton;
  }
  
  
  private Pitcher() { 
    isRunning = false;
    queueReadTimeoutMs = 1000;
    soundFileQueue = null;
    pitchBufferQueue = null;
    noteBufferQueue = null;

    /* Read the existing notes CSV file */
    List<String> linesList = new ArrayList<String>();

    try {
      File readFile = new File(System.getProperty("user.dir"), Constants.notes_file);
      if(readFile.exists() && readFile.canRead()) {
        List<String> lines = FileUtils.readLines(readFile);
        for(int i = 0; i < lines.size(); i++) {
          String line = lines.get(i);
          if(line.startsWith("#")) // Skip comments
            continue;

          linesList.add(line);
        }
      } else {
        System.err.println("The file " + System.getProperty("user.dir") + File.separator + Constants.notes_file + " cannot be read.");
      }
      
      /* Parse the notes */
      referenceNotes.clear();
      
      String[] names = linesList.get(0).split(",");
      for(int i = 1; i < linesList.size(); i++) {
        String[] notes = linesList.get(i).split(",");
        if(notes.length <= 0) // Skip empty lines
          continue;

        for(int j = 0; j < notes.length; j++) {
          try {
            /* Get name */
            String noteName = names[j];
            /* Get the frequency  */
            double frequency = Double.parseDouble(notes[j]);
            
            //System.out.println("New note: " + noteName + "("+j+") - " + frequency);
            referenceNotes.add(new Note(noteName, frequency, i));
          } catch(Exception e) {
            e.printStackTrace();
          }
        }
      }
    } catch (Exception e) {
      System.err.println("Exception while reading " + System.getProperty("user.dir") + File.separator + Constants.notes_file + ": " + e.getMessage());
      e.printStackTrace();
    }
  }

  
  /**
   * Set queues to interact with.
   * @param soundFileQueue The queue where to extract the sound file to pitch
   * @param pitchBufferQueue The queue where to insert the pitch buffer computed from sound files.
   * @param noteBufferQueue The queue where to insert the note buffer filtered from pitches
   */
  public void setQueues(BlockingQueue<SoundFile> soundFileQueue, BlockingQueue<PitchBuffer> pitchBufferQueue, BlockingQueue<NoteBuffer> noteBufferQueue) {
    this.soundFileQueue = soundFileQueue;
    this.pitchBufferQueue = pitchBufferQueue;
    this.noteBufferQueue = noteBufferQueue;
  }
  
  public void start() {
    /* Read the sound file queue */
    if(soundFileQueue != null && isRunning == false) {
      try {
        System.out.println("Start pitching.");
        isRunning = true;
        while(isRunning) {
          /* Blocks until new sound file comes */
          SoundFile soundFile = soundFileQueue.poll(queueReadTimeoutMs, TimeUnit.MILLISECONDS);
          if(soundFile != null) {
            /* This thread need to be non blocking for being stopped. */
            
            if(soundFile.exists()) {
              System.out.println("Extracted file from queue '"+ soundFile.getFileAbsolutePath() +"'");

              /* Extract the pitch from the sound file */
              String[] command = new String[]{ "cmd", "/c", Constants.pitch_executable, "-i", soundFile.getFileAbsolutePath() };

              // Execute the exe file to create pitch raw points
              ProcessBuilder process = new ProcessBuilder(command);
              process.directory(new File(System.getProperty("user.dir"), Constants.aubio_path));
              //process.redirectErrorStream(true);

              /* Extract the result to the pitch buffer queue */
              try {
                Process commandShell = process.start();
                PitchResultThread resultThread = new PitchResultThread(commandShell.getInputStream(), pitchBufferQueue, noteBufferQueue);
                resultThread.setReferencesNotes(referenceNotes);
                resultThread.start();
                commandShell.waitFor();
                resultThread.join(500);
              } catch (Exception e) {
                e.printStackTrace();
              }
              
              try {
                soundFile.delete();
              } catch (Exception e) {
                e.printStackTrace();
              }
            }
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    } else {
      System.err.println("The sound file queue has not been initialized!");
    }
  }
  
  public void stop() {
    if(isRunning == true) {
      try {
        System.out.println("Stopping pitching...");
        isRunning = false;
        Thread.sleep(queueReadTimeoutMs);
        System.out.println("Pitching stopped.");
      } catch(Exception e) {
        e.printStackTrace();
      }
    }
  }
  
  private static class PitchResultThread extends Thread {
    private InputStream is;
    private BlockingQueue<PitchBuffer> pitchBufferQueue;
    private BlockingQueue<NoteBuffer> noteBufferQueue;
    private List<Note> referenceNotes;

    public PitchResultThread(InputStream is, BlockingQueue<PitchBuffer> pitchBufferQueue, BlockingQueue<NoteBuffer> noteBufferQueue) {
      this.is = is;
      this.pitchBufferQueue = pitchBufferQueue;
      this.noteBufferQueue = noteBufferQueue;
    }
    
    public void setReferencesNotes(List<Note> referenceNotes) {
      this.referenceNotes = referenceNotes;
    }

    public void run() {
      PrintWriter pw = null;
      try {
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line=null;
        
        /* Write the pitch result in the pitch queue */
        PitchBuffer pitchBuffer = new PitchBuffer();
        while ( (line = br.readLine()) != null) {
          String[] words = line.split("\\s+");          
          if(words.length == 2) {
            try {
              double time = Double.parseDouble(words[0]);
              double frequency = Double.parseDouble(words[1]);

              pitchBuffer.addPitch(frequency, time);
            } catch(Exception e) {
              System.err.println("Exception while parsing double in this pitch line: " + line);
            }
          } else {
            System.err.println("Weird pitch result: " + line);
          }
        }
        pitchBufferQueue.add(pitchBuffer);
        
        /* Make a copy of the pitch result buffer to work with it */
        PitchBuffer pitchBufferCopy = new PitchBuffer(pitchBuffer);
        /* Filter to take only one pitch per X milliseconds */
        pitchBufferCopy.compressTime(25);
        /* FIXME Try to remove noise */
        // ...
        /* FIXME Try other things */
        // ...
        
        /* Create notes from pitch (set the real notes frequencies) */
        NoteBuffer noteBuffer = new NoteBuffer();
        noteBuffer.setNotesFromPitchBuffer(pitchBufferCopy, referenceNotes);
        /* Compute the notes durations and remove notes that last less than X milliseconds */
        noteBuffer.computeDurations(50);
        
        noteBufferQueue.add(noteBuffer);
        
        
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
