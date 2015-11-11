package queues;

import java.util.ArrayList;
import java.util.List;

public class PitchBuffer {
  private List<Pitch> pitches;

  /**
   * Default constructor
   */
  public PitchBuffer() {
    pitches = new ArrayList<Pitch>();
  }

  /**
   * Copy constructor
   * @param clone Copy
   */
  public PitchBuffer(PitchBuffer clone) {
    pitches = new ArrayList<Pitch>(clone.pitches);
  }

  public void addPitch(double frequency, double time) {
    Pitch newPitch = new Pitch(frequency, time);
    pitches.add(newPitch);
  }

  public int size() {
    return pitches.size();
  }

  public double[] getFrequencyArray() {
    double[] frequencies = new double[pitches.size()];
    for(int i = 0; i < pitches.size(); i++) {
      frequencies[i] = pitches.get(i).getFrequency();
    }

    return frequencies;
  }

  public double[] getTimeArray() {
    double[] times = new double[pitches.size()];
    for(int i = 0; i < pitches.size(); i++) {
      times[i] = pitches.get(i).getTime();
    }

    return times;
  }

  public List<Pitch> values() {
    return pitches;
  }

  public void clear() {
    pitches.clear();
  }

  /**
   * Keep on pitch every divisor milliseconds
   * @param time_divisor_ms
   */
  public void compressTime(int time_divisor_ms) {
    boolean done = false;
    double currentTime = 0.0f;
    int index = 0;
    List<Pitch> OldPitches = new ArrayList<Pitch>(pitches);
    List<Double> frequencies = new ArrayList<Double>();
    
    pitches.clear();

    while(!done) {
      for(; index < OldPitches.size(); index++) {
        double pitchTime = OldPitches.get(index).getTime() * 1000; // Seconds to milliseconds
        if(pitchTime >= currentTime + time_divisor_ms) {
          /* We have a sample of time_divisor_ms milliseconds of pitch, compute the frequency for this time */
          if(frequencies.size() > 0) pitches.add(new Pitch(compressFrequencies(frequencies), (double)(currentTime / 1000.0f)));
          /* Go to the next sample to do */
          frequencies.clear();
          currentTime += time_divisor_ms;
          
          break;
        } else {
          // System.out.println("Add frequency " + OldPitches.get(index).getFrequency());
          frequencies.add(OldPitches.get(index).getFrequency());
        }
      }
      
      if(!frequencies.isEmpty()) {
        /* Add the last frequencies */
        pitches.add(new Pitch(compressFrequencies(frequencies), (double)(currentTime / 1000.0f)));
        done = true;
      }
    }
  }

  /**
   * TODO
   * @param frequencies
   * @return
   */
  private int compressFrequencies(List<Double> frequencies) {
    int sum = 0, mean = 0;
    for(Double frequency : frequencies) {
      sum += frequency.intValue();
    }
    mean = (int)(sum / frequencies.size());

    return mean;
  }
}
