package sound;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jtransforms.fft.DoubleFFT_1D;

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
	private static int currentTimeSeconds = 0;

	/* Queue */
	private BlockingQueue<SoundFile> soundFileQueue = null;
	private BlockingQueue<PitchBuffer> pitchBufferQueue = null;
	private BlockingQueue<NoteBuffer> noteBufferQueue = null;

	/* Filter attributes */
	private List<Note> referenceNotes = new ArrayList<Note>();

	public static Pitcher getPitcher() {
		if (singleton == null)
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
			if (readFile.exists() && readFile.canRead()) {
				List<String> lines = FileUtils.readLines(readFile);
				for (int i = 0; i < lines.size(); i++) {
					String line = lines.get(i);
					if (line.startsWith("#")) // Skip comments
						continue;

					linesList.add(line);
				}
			} else {
				System.err.println("The file " + System.getProperty("user.dir") + File.separator + Constants.notes_file
						+ " cannot be read.");
			}

			/* Parse the notes */
			referenceNotes.clear();

			String[] names = linesList.get(0).split(",");
			for (int i = 1; i < linesList.size(); i++) {
				String[] notes = linesList.get(i).split(",");
				if (notes.length <= 0) // Skip empty lines
					continue;

				for (int j = 0; j < notes.length; j++) {
					try {
						/* Get name */
						String noteName = names[j];
						/* Get the frequency */
						double frequency = Double.parseDouble(notes[j]);

						System.out.println("New note: " + noteName + "(" + j + ") - " + frequency);
						referenceNotes.add(new Note(noteName, frequency, i));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		} catch (Exception e) {
			System.err.println("Exception while reading " + System.getProperty("user.dir") + File.separator
					+ Constants.notes_file + ": " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Set queues to interact with.
	 * 
	 * @param soundFileQueue   The queue where to extract the sound file to pitch
	 * @param pitchBufferQueue The queue where to insert the pitch buffer computed
	 *                         from sound files.
	 * @param noteBufferQueue  The queue where to insert the note buffer filtered
	 *                         from pitches
	 */
	public void setQueues(BlockingQueue<SoundFile> soundFileQueue, BlockingQueue<PitchBuffer> pitchBufferQueue,
			BlockingQueue<NoteBuffer> noteBufferQueue) {
		this.soundFileQueue = soundFileQueue;
		this.pitchBufferQueue = pitchBufferQueue;
		this.noteBufferQueue = noteBufferQueue;
	}

	public void start() {
		/* Read the sound file queue */
		if (soundFileQueue != null && isRunning == false) {
			try {
				System.out.println("Start pitching.");
				isRunning = true;
				while (isRunning) {
					/* Blocks until new sound file comes */
					SoundFile soundFile = soundFileQueue.poll(queueReadTimeoutMs, TimeUnit.MILLISECONDS);
					if (soundFile != null) {
						/* This thread need to be non blocking for being stopped. */

						if (soundFile.exists()) {
							System.out.println("Extracted file from queue '" + soundFile.getFileAbsolutePath() + "'");
							/* Extract the result to the pitch buffer queue */
							try {
								PitchResultThread resultThread = new PitchResultThread(soundFile.toFile(), pitchBufferQueue, noteBufferQueue);
								resultThread.setReferencesNotes(referenceNotes);
								resultThread.start();
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
		if (isRunning == true) {
			try {
				System.out.println("Stopping pitching...");
				isRunning = false;
				Thread.sleep(queueReadTimeoutMs);
				System.out.println("Pitching stopped.");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static class PitchResultThread extends Thread {
		private static double[] window = null;
		private File file;
		private BlockingQueue<PitchBuffer> pitchBufferQueue;
		private BlockingQueue<NoteBuffer> noteBufferQueue;
		private List<Note> referenceNotes;

		public PitchResultThread(File file, BlockingQueue<PitchBuffer> pitchBufferQueue,
				BlockingQueue<NoteBuffer> noteBufferQueue) {
			this.file = file;
			this.pitchBufferQueue = pitchBufferQueue;
			this.noteBufferQueue = noteBufferQueue;
		}

		public void setReferencesNotes(List<Note> referenceNotes) {
			this.referenceNotes = referenceNotes;
		}

		public void run() {
			try {
				/* Write the pitch result in the pitch queue */
				PitchBuffer pitchBuffer = new PitchBuffer();
				FileInputStream fis = new FileInputStream(file);
				byte[] pitchBufferArray =  IOUtils.toByteArray(fis);
				
				fis.close();
				file.delete();
				
				System.out.println("Bytes buffer length: " + pitchBufferArray.length);
				short[] frequencies = new short[pitchBufferArray.length];
				for (int i = 0; i < pitchBufferArray.length; i++) {
					frequencies[i] = (short) pitchBufferArray[i];
				}

				double frequency = extractFrequency(frequencies, (int)Recorder.getRecorder().getAudioFormat().getSampleRate());
				pitchBuffer.addPitch(frequency, currentTimeSeconds);
				pitchBufferQueue.add(pitchBuffer);
				currentTimeSeconds++; 

				/* Make a copy of the pitch result buffer to work with it */
				PitchBuffer pitchBufferCopy = new PitchBuffer(pitchBuffer);
				/* Filter to take only one pitch per X milliseconds */
				//pitchBufferCopy.compressTime(20);
				/* Apply a low pass filter */
				//pitchBufferCopy.filterNoise(3, 30);
				/*
				 * Remove noise http://phrogz.net/js/framerate-independent-low-pass-filter.html
				 */
				pitchBufferCopy.applyLowPassFilter(2);
				/* Remove the pitches with frequency below X Hz */
				pitchBufferCopy.removeFrequenciesBelow(20);

				NoteBuffer noteBuffer = new NoteBuffer(referenceNotes);
				/* Create notes from pitch (set the real notes frequencies) */
				noteBuffer.setRawNotesFromPitchBuffer(pitchBufferCopy);
				/*
				 * Compute the notes durations and remove notes that last less than X
				 * milliseconds
				 */
				//noteBuffer.hideSmallDurations(50, 8);
				/*
				 * Set the real musical frequencies on notes (find the nearest frequency for
				 * each)
				 */
				noteBuffer.computeRealNotes();
				//noteBuffer.hideSmallDurations(50, 8);
				noteBufferQueue.add(noteBuffer);

				System.out.println("New buffer of pitches inserted.");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		/**
		 * build a Hamming window filter for samples of a given size See
		 * http://www.labbookpages.co.uk/audio/firWindowing.html#windows
		 * 
		 * @param size the sample size for which the filter will be created
		 */
		private static void buildHammWindow(int size) {
			if (window != null && window.length == size) {
				return;
			}
			window = new double[size];
			for (int i = 0; i < size; ++i) {
				window[i] = .54 - .46 * Math.cos(2 * Math.PI * i / (size - 1.0));
			}
		}

		/**
		 * apply a Hamming window filter to raw input data
		 * 
		 * @param input an array containing unfiltered input data
		 * @return a double array containing the filtered data
		 */
		private static double[] applyWindow(short[] input) {
			double[] res = new double[input.length];

			buildHammWindow(input.length);
			for (int i = 0; i < input.length; ++i) {
				res[i] = (double) input[i] * window[i];
			}
			return res;
		}

		/**
		 * extract the dominant frequency from 16bit PCM data.
		 * 
		 * @param sampleData an array containing the raw 16bit PCM data.
		 * @param sampleRate the sample rate (in HZ) of sampleData
		 * @return an approximation of the dominant frequency in sampleData
		 */
		private static double extractFrequency(short[] sampleData, int sampleRate) {
			/* sampleData + zero padding */
			DoubleFFT_1D fft = new DoubleFFT_1D(sampleData.length + 24 * sampleData.length);
			double[] a = new double[(sampleData.length + 24 * sampleData.length) * 2];

			System.arraycopy(applyWindow(sampleData), 0, a, 0, sampleData.length);
			fft.realForward(a);

			/* find the peak magnitude and it's index */
			double maxMag = Double.NEGATIVE_INFINITY;
			int maxInd = -1;

			for (int i = 0; i < a.length / 2; ++i) {
				double re = a[2 * i];
				double im = a[2 * i + 1];
				double mag = Math.sqrt(re * re + im * im);

				if (mag > maxMag) {
					maxMag = mag;
					maxInd = i;
				}
			}

			/* calculate the frequency */
			return (double) sampleRate * maxInd / (a.length / 2);
		}
	}
}
