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
  public static void main(String[] args) {
    MainWindow mainWindow = new MainWindow();
    
    /* Create the queues */
    BlockingQueue<SoundFile> soundFileQueue = new ArrayBlockingQueue<>(Constants.soundFilesQueueCapacity);
    BlockingQueue<PitchBuffer> pitchBufferQueue = new ArrayBlockingQueue<>(Constants.pitchBufferQueueCapacity);
    
    /* Start threads */
    RecordFileWriter.getRecordWriter().setQueue(soundFileQueue);
    PitcherThread.setQueues(soundFileQueue, pitchBufferQueue);
    PitcherThread.startPitcher();
    DisplayerThread.setQueue(pitchBufferQueue);
    DisplayerThread.setDisplayWindow(mainWindow);
    DisplayerThread.startDisplayer();
    
    /* Display the main window */
    mainWindow.live();
    
    /* Stop threads */
    RecorderThread.stopRecorder();
    PitcherThread.stopPitcher();
    DisplayerThread.stopDisplayer();
  }
}

