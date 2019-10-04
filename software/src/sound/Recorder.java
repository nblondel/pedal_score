package sound;

import javax.sound.sampled.*;

import common.Constants;
import common.Constants.Channel;
import common.Constants.SampleBitsSize;
import common.Constants.SampleRate;

import java.io.*;

/* This class is supposed to be used via the RecorderThread class only */
public class Recorder {
	/* Singleton */
	private static Recorder singleton = null;
	private Recorder() {}

	/* Constants */
	private final int BUFFER_SIZE = 10_000;
	/* Sound */
	private TargetDataLine audioLine;
	/* Thread */
	private volatile boolean isRunning = false;
	private int writingIntervalMs = Constants.DEFAULT_RECORDER_REFRESH_PERIOD; // Interval to write .wav file in milliseconds

	public static Recorder getRecorder() {
		if (singleton == null)
			singleton = new Recorder();
		return singleton;
	}

	/**
	 * Defines an audio format used to record
	 */
	public AudioFormat getAudioFormat() {
		float sampleRate = SampleRate.SampleRate_44100.value();
		int sampleSizeInBits = SampleBitsSize.SIZE_16.value();
		int channels = Channel.MONO.value();
		boolean signed = true;
		boolean bigEndian = true;
		return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
	}

	public boolean setWritingInterval(int interval) {
		if (interval >= 10 && interval < 10000) {
			this.writingIntervalMs = interval;
			return true;
		} else {
			System.err.println("The interval should be between 10 and 10000 milliseconds.");
			return false;
		}
	}

	public boolean checkWritingInterval(int interval) {
		if (interval >= 10 && interval < 10000) {
			return true;
		} else {
			return false;
		}
	}

	public int getWritingInterval() {
		return this.writingIntervalMs;
	}

	/**
	 * Start recording sound.
	 * 
	 * @throws LineUnavailableException if the system does not support the specified
	 *                                  audio format nor open the audio data line.
	 */
	public void start() throws LineUnavailableException {
		if (isRunning == false) {
			long currentMillis = 0;
			ByteArrayOutputStream recordBytes = new ByteArrayOutputStream();
			byte[] buffer = new byte[BUFFER_SIZE];
			int bytesRead = 0;
			AudioFormat format = getAudioFormat();
			System.out.println("Start recording.");

			// Checks if system supports the data line
			DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
			if (!AudioSystem.isLineSupported(info)) {
				throw new LineUnavailableException("The system does not support the specified format.");
			}

			audioLine = AudioSystem.getTargetDataLine(format);
			audioLine.open(format);
			audioLine.start();
			isRunning = true;

			currentMillis = System.currentTimeMillis();
			while (isRunning) {
				bytesRead = audioLine.read(buffer, 0, buffer.length);
				recordBytes.write(buffer, 0, bytesRead);

				/* Save bytes to WAV file every interval of time */
				if (System.currentTimeMillis() - currentMillis > this.writingIntervalMs) {
					currentMillis = System.currentTimeMillis();
					try {
						/* Make a copy of the stream to save */
						ByteArrayOutputStream recordBytesTmp = new ByteArrayOutputStream();
						recordBytes.writeTo(recordBytesTmp);
						/* Save this temporary buffer stream */
						RecordFileWriter.getRecordWriter().save(recordBytesTmp, format);
						recordBytes.flush();
						recordBytes.reset();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * Stop recording sound.
	 * 
	 * @throws IOException if any I/O error occurs.
	 */
	public void stop() throws IOException {
		if (isRunning == true) {
			System.out.println("Stopping recording...");
			isRunning = false;

			if (audioLine != null) {
				audioLine.stop();
				audioLine.flush();
				audioLine.close();
			}

			System.out.println("Recording stopped.");
		}
	}
}