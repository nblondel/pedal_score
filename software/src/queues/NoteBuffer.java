package queues;

import java.util.ArrayList;
import java.util.List;

public class NoteBuffer {
  private List<Note> notes = new ArrayList<Note>();
  private List<Note> referenceNotes = null;
  
  public NoteBuffer(List<Note> referenceNotes) {
    this.referenceNotes = referenceNotes;
  }

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

  public boolean[] getHiddenArray() {
    boolean[] hiddens = new boolean[notes.size()];
    for(int i = 0; i < notes.size(); i++) {
      hiddens[i] = notes.get(i).isHidden();
    }
    
    return hiddens;
  }
  
  public void clear() {
    notes.clear();
  }

  public void computeRealNotes() {
    for(Note note : notes) {
      
      /* Find the note that looks like the pitch */
      double freqDifference = note.getFrequency(); // Start with a difference to keep resizing to smaller and smaller until we find the smallest
      
      // Keep pitch as note to start
      double currentFrequency = note.getFrequency(); 
      String currentNoteName = "Unknown";
      int currentNoteOctave = 0;
      
      for(int index = 0; index < referenceNotes.size(); index++) {
        double newFreqDifference = Math.abs(note.getFrequency() - referenceNotes.get(index).getFrequency());
        if(newFreqDifference > freqDifference)
          break;
        
        freqDifference = newFreqDifference;
        currentFrequency = referenceNotes.get(index).getFrequency();
        currentNoteName = referenceNotes.get(index).getName();
        currentNoteOctave = referenceNotes.get(index).getOctave();
      }
      
      /* Create note from the reference notes */
      note.setName(currentNoteName);
      note.setFrequency(currentFrequency);
      note.setOctave(currentNoteOctave);
    }
  }
  
  /**
   * Create note from pitches
   * @param pitchBufferCopy
   */
  public void setRawNotesFromPitchBuffer(PitchBuffer pitchBufferCopy) {
    for(Pitch pitch : pitchBufferCopy.values()) {      
      /* Create note from the pitch */
      double frequency = ((double)((int)pitch.getFrequency()));
      if(pitch.isInhibited()) {
        frequency = 0.0;
      }
      Note newNote = new Note(frequency, pitch.getTime());
      /* Save this note */
      notes.add(newNote);
    }
  }

  /**
   * Set the attribute 'Hidden' of the Note true if the duration of the note (a set of pitches) duration is smaller than the minimum duration
   * @param minimumDuration The minimum duration (milliseconds)
   */
  public void hideSmallDurations(double minimumDuration) {
    int index = 0;
    
    while(index < notes.size()) {
      if(notes.get(index).isHidden()) {
        index++;
        continue;
      }
      
      int same_frequency_index = index;
      
      /* Find same note frequencies */
      boolean found = false;
      int ignored = 0;
      do {
        /* Check for two consecutive notes with the same frequency */
        if((same_frequency_index + 1) >= notes.size()) {
          found = true;
        } else if(notes.get(index).getFrequency() != notes.get(same_frequency_index + 1).getFrequency() || notes.get(index).isHidden()) {
          found = true;
          
          /* There was not two consecutive, looking for the 'ignored' frequencies (same frequencies with max 10 notes between them) */
//          ignored++;
//          if(ignored >= 10) {
//            found = true;
//          }
        } else {
          same_frequency_index++;
        }
      } while(!found);
      
      if(same_frequency_index < notes.size() && same_frequency_index > index) {
        // System.out.println("Same notes from " + index + " to " + same_frequency_index);
        
        /* Compute the duration of this same frequency */
        double duration_milliseconds = (notes.get(same_frequency_index).getTime() * 1000.0) - (notes.get(index).getTime() * 1000.0);
        // System.out.println("Duration: " + duration_milliseconds);
        if(duration_milliseconds <= minimumDuration) {
          for(int notes_index = index; notes_index < same_frequency_index; notes_index++) {
            /* Duration too small, hide all these notes */
            //System.out.println("note " + notes_index + " hidden for small duration ("+notes.get(notes_index).getTime()+"/"+notes.get(notes_index).getFrequency()+")");
            notes.get(notes_index).setHidden(true);
          }
        }
        index = same_frequency_index;
      } else {
        /* There is not even 2 notes with the same frequency */
        notes.get(index).setHidden(true);
        //System.out.println("note " + index + " hidden not similar to its neighbour ("+notes.get(index).getTime()+"/"+notes.get(index).getFrequency()+")");
        index++;
      }
    }
  }
}
