package common;

import java.io.File;

public class Constants {
  public static String resourcesDirectory = "res";
  public static String outputDirectory = "output";
  public static String tmpOutputDirectory = outputDirectory + File.separator + "output";
  public static String aubio_path = resourcesDirectory + File.separator + "aubio-0.4.1";
  public static String pitch_executable = "aubiopitch.exe";

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
    SampleRate_8000(8000), SampleRate_11025(11025), SampleRate_16000(16000), SampleRate_22050(22050), SampleRate_44100(44100);
    private int value;

    private SampleRate(int value) {
      this.value = value;
    }
    public int value() {
      return this.value;
    }
  }
}
