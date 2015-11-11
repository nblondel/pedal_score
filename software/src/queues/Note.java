package queues;

public class Note {
  private double frequency;
  private double time;
  
  public Note(double frequency, double time) {
    this.frequency = frequency;
    this.time = time;
  }
  
  public Note(Pitch pitch) {
    this.frequency = pitch.getFrequency(); // TODO find the note (if note is 442 it is set as 440)
    this.time = pitch.getTime();
  }
  
  public double getFrequency() {
    return frequency;
  }
  
  public void setFrequency(double frequency) {
    this.frequency = frequency;
  }
  
  public double getTime() {
    return time;
  }
  
  public void setTime(double time) {
    this.time = time;
  }
}
