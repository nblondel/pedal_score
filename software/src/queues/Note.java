package queues;

public class Note {
  // Hz
  private double frequency = 0.0f;
  // Seconds
  private double t0 = 0.0f;
  // Milliseconds
  private double duration = 0.0f;
  // [1-9]
  private int octave = 1;
  // [CDEFGAB]
  private String name = "";
  // True / False
  private boolean hidden = false;
  
  public Note(String name, double frequency, int octave) {
    this.name = name;
    this.frequency = frequency;
    this.octave = octave;
  }
  
  public Note(double frequency, double time) {
    this.frequency = frequency;
    this.t0 = time;
  }
  
  public double getFrequency() {
    return frequency;
  }
  
  public void setFrequency(double frequency) {
    this.frequency = frequency;
  }
  
  public double getTime() {
    return t0;
  }
  
  public void setTime(double time) {
    this.t0 = time;
  }

  public double getDuration() {
    return duration;
  }

  public void setDuration(double duration) {
    this.duration = duration;
  }

  public int getOctave() {
    return octave;
  }

  public void setOctave(int octave) {
    this.octave = octave;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
  
  public boolean isHidden() {
    return hidden;
  }
  
  public void setHidden(boolean hidden) {
    this.hidden = hidden;
  }
  
  public boolean equals(Note note) {
    if(this.hidden || this.frequency == 0.0 || note.isHidden() || note.getFrequency() == 0.0) return false;
    if(Math.abs(Math.max(this.frequency, note.getFrequency())) - Math.abs(Math.min(this.frequency, note.getFrequency())) <= 1.0) return true;
    return false;
  }
}
