package queues;

import java.util.ArrayList;
import java.util.List;

public class NoteBuffer {
  List<Note> notes = new ArrayList<Note>();
  
  public void addNote(double frequency, double time) {
    Note newPitch = new Note(frequency, time);
    notes.add(newPitch);
  }
  
  public int size() {
    return notes.size();
  }
  
  public double[] getFrequencyArray() {
    double[] frequencies = new double[notes.size()];
    for(int i = 0; i < notes.size(); i++) {
      frequencies[i] = notes.get(i).getFrequency();
    }
    
    return frequencies;
  }
  
  public double[] getTimeArray() {
    double[] times = new double[notes.size()];
    for(int i = 0; i < notes.size(); i++) {
      times[i] = notes.get(i).getApparitionTime();
    }
    
    return times;
  }
  
  public void clear() {
    notes.clear();
  }

  public void setNotesFromPitchBuffer(PitchBuffer pitchBufferCopy) {
    for(Pitch pitch : pitchBufferCopy.values()) {
      Note newNote = new Note(pitch);
      notes.add(newNote);
    }
  }

  public void computeDurations(int minimumDuration) {
    // TODO 
  }
}
