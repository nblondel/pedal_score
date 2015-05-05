package queues;

import java.util.ArrayList;
import java.util.List;

public class PitchBuffer {
  List<Pitch> pitches = new ArrayList<Pitch>();
  
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
  
  public void clear() {
    pitches.clear();
  }
}
