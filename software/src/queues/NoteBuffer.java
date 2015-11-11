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
      times[i] = notes.get(i).getTime();
    }
    
    return times;
  }
  
  public String[] getNamesArray() {
    String[] names = new String[notes.size()];
    for(int i = 0; i < notes.size(); i++) {
      names[i] = notes.get(i).getName();
    }
    
    return names;
  }

  public int[] getOctavesArray() {
    int[] octaves = new int[notes.size()];
    for(int i = 0; i < notes.size(); i++) {
      octaves[i] = notes.get(i).getOctave();
    }
    
    return octaves;
  }
  
  public void clear() {
    notes.clear();
  }

  public void setNotesFromPitchBuffer(PitchBuffer pitchBufferCopy, List<Note> referenceNotes) {
    for(Pitch pitch : pitchBufferCopy.values()) {
      
      /* Find the note that looks like the pitch */
      double freqDifference = pitch.getFrequency(); // Start with a difference to keep resizing to smaller and smaller until we find the smallest
      
      // Keep pitch as note to start
      double currentFrequency = pitch.getFrequency(); 
      String currentNoteName = "Unknown";
      int currentNoteOctave = 0;
      
      for(int index = 0; index < referenceNotes.size(); index++) {
        double newFreqDifference = Math.abs(pitch.getFrequency() - referenceNotes.get(index).getFrequency());
        if(newFreqDifference > freqDifference)
          break;
        
        freqDifference = newFreqDifference;
        currentFrequency = referenceNotes.get(index).getFrequency();
        currentNoteName = referenceNotes.get(index).getName();
        currentNoteOctave = referenceNotes.get(index).getOctave();
      }
      
      /* Create note from the reference notes */
      //System.out.println("set Pitch " + pitch.getFrequency() + " -> " + currentFrequency);
      Note newNote = new Note(currentNoteName, currentFrequency, currentNoteOctave);
      /* Add the pitch attributes */
      newNote.setTime(pitch.getTime());
      /* Save this note */
      notes.add(newNote);
    }
  }

  public void computeDurations(int minimumDuration) {
    // TODO 
  }
}
