package common;

import java.io.File;

public class Constants {
	public static String resourcesDirectory = "res";
	public static String notes_file = resourcesDirectory + File.separator + "notes.csv";
	public static String aubio_path_32 = resourcesDirectory + File.separator + "aubio-0.4.2" + File.separator + "32";
	public static String aubio_path_64 = resourcesDirectory + File.separator + "aubio-0.4.2" + File.separator + "64";

	public static String outputDirectory = "output";
	public static String tmpOutputDirectory = outputDirectory + File.separator + "tmp";
	public static String pitch_executable = "aubiopitch.exe";

	public static int soundFilesQueueCapacity = 50;
	public static int pitchBufferQueueCapacity = 50;
	public static int noteBufferQueueCapacity = 50;
	public static int DEFAULT_RECORDER_REFRESH_PERIOD = 250; // ms

	public enum SampleBitsSize {
		SIZE_8(8), SIZE_16(16);

		private int value;

		private SampleBitsSize(int value) {
			this.value = value;
		}

		public int value() {
			return this.value;
		}
	}

	public enum Channel {
		MONO(1), STEREO(2);

		private int value;

		private Channel(int value) {
			this.value = value;
		}

		public int value() {
			return this.value;
		}
	}

	public enum SampleRate {
		SampleRate_8000(8000), SampleRate_11025(11025), SampleRate_16000(16000), SampleRate_22050(22050),
		SampleRate_44100(44100);

		private int value;

		private SampleRate(int value) {
			this.value = value;
		}

		public int value() {
			return this.value;
		}
	}
}
