package sound;

import javax.sound.sampled.*;

import common.Constants.Channel;
import common.Constants.SampleBitsSize;
import common.Constants.SampleRate;

import java.io.*;
 
public class Recorder {

  private static final int BUFFER_SIZE = 4096;
  private ByteArrayOutputStream recordBytes;
  private TargetDataLine audioLine;
  private AudioFormat format;
  private boolean isRunning;

  /**
   * Defines an audio format used to record
   */
  AudioFormat getAudioFormat() {
    float sampleRate = SampleRate.SampleRate_44100.value();
    int sampleSizeInBits = SampleBitsSize.SIZE_16.value();
    int channels = Channel.MONO.value();
    boolean signed = true;
    boolean bigEndian = true;
    return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
  }

  /**
   * Start recording sound.
   * @throws LineUnavailableException if the system does not support the specified
   * audio format nor open the audio data line.
   */
  public void start() throws LineUnavailableException {
    format = getAudioFormat();
    DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

    // checks if system supports the data line
    if (!AudioSystem.isLineSupported(info)) {
      throw new LineUnavailableException(
          "The system does not support the specified format.");
    }

    audioLine = AudioSystem.getTargetDataLine(format);

    audioLine.open(format);
    audioLine.start();

    byte[] buffer = new byte[BUFFER_SIZE];
    int bytesRead = 0;

    recordBytes = new ByteArrayOutputStream();
    isRunning = true;

    while (isRunning) {
      bytesRead = audioLine.read(buffer, 0, buffer.length);
      recordBytes.write(buffer, 0, bytesRead);
    }
  }

  /**
   * Stop recording sound.
   * @throws IOException if any I/O error occurs.
   */
  public void stop() throws IOException {
    isRunning = false;

    if (audioLine != null) {
      audioLine.drain();
      audioLine.close();
    }
  }

  /**
   * Save recorded sound data into a .wav file format.
   * @param wavFile The file to be saved.
   * @throws IOException if any I/O error occurs.
   */
  public void save(File wavFile) throws IOException {
    byte[] audioData = recordBytes.toByteArray();
    ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
    AudioInputStream audioInputStream = new AudioInputStream(bais, format,
        audioData.length / format.getFrameSize());

    AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, wavFile);

    audioInputStream.close();
    recordBytes.close();
  }
}