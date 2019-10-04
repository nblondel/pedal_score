package main;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import queues.NoteBuffer;
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
		BlockingQueue<NoteBuffer> noteBufferQueue = new ArrayBlockingQueue<>(Constants.noteBufferQueueCapacity);

		/* Start threads */
		RecordFileWriter.getRecordWriter().setQueue(soundFileQueue);
		PitcherThread.setQueues(soundFileQueue, pitchBufferQueue, noteBufferQueue);
		PitcherThread.startPitcher();
		DisplayerThread.setQueues(pitchBufferQueue, noteBufferQueue);
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
