package queues;

import java.io.File;

public class SoundFile {
  private String FileAbsolutePath = null;

  public SoundFile(String FileAbsolutePath) {
    this.FileAbsolutePath = FileAbsolutePath;
  }
  
  public String getFileAbsolutePath() {
    return this.FileAbsolutePath;
  }
  
  public boolean exists() {
    return new File(this.FileAbsolutePath).exists();
  }
  
  public void delete() {
    if(new File(this.FileAbsolutePath).exists()) {
      try {
        new File(this.FileAbsolutePath).delete();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
