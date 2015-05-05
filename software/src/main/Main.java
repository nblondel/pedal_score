package main;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import queues.PitchBuffer;
import queues.SoundFile;
import common.Constants;
import dialogs.MainWindow;
import sound.PitcherThread;
import sound.RecordFileWriter;
import sound.RecorderThread;

public class Main { 
  //        final int coord_counter = line_counter;
  //        Display.getDefault().syncExec(new Runnable() {
  //          @Override public void run() {
  //            graphicResultGraph.addPoints(coord_counter, x, y);
  //          }
  //        });
 
  public static void main(String[] args) {
    MainWindow mainWindow = new MainWindow();
    
    /* Create the queue manager */
    BlockingQueue<SoundFile> soundFileQueue = new ArrayBlockingQueue<>(Constants.soundFilesQueueCapacity);
    BlockingQueue<PitchBuffer> pitchBufferQueue = new ArrayBlockingQueue<>(Constants.pitchBufferQueueCapacity);
    RecordFileWriter.getRecordWriter().setQueue(soundFileQueue);
    PitcherThread.getPitcherThread().setQueues(soundFileQueue, pitchBufferQueue);
    PitcherThread.getPitcherThread().startPitcher();
    
    /* Display the main window */
    mainWindow.live();    
    
    RecorderThread.stopRecorder();
    PitcherThread.getPitcherThread().stopPitcher();
  }
}

