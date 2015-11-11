package queues;

public class Pitch {
  /**
   * Frequency in Hz
   */
  private double frequency;
  
  /**
   * Time in seconds
   */
  private double time;
  
  public Pitch(double frequency, double time) {
    this.frequency = frequency;
    this.time = time;
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
