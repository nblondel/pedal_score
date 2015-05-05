package queues;

import java.util.ArrayList;
import java.util.List;

public class PitchBuffer {
  List<Pitch> pitches = new ArrayList<Pitch>();
  
  public void addPitch(double frequency, double time) {
    Pitch newPitch = new Pitch(frequency, time);
    pitches.add(newPitch);
  }
  
  /**
   * Get a pitch in the buffer
   * @return The next Pitch or null if empty
   */
  public Pitch getNextPitch() {
    if(pitches.size() <= 0) return null;
    return pitches.remove(0);
  }
}
