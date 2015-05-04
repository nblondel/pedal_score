package common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class Utils {
  public static class StreamGobbler extends Thread {
    InputStream is;
    String filename;

    public StreamGobbler(InputStream is, String filename) {
      this.is = is;
      this.filename = filename;
    }

    public void run() {
      PrintWriter pw = null;
      try {
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line=null;

        File file = new File(filename);
        if(file.exists()) {
          file.delete();
          file.createNewFile();
        }
        
        /* Write the result in a file */
        FileWriter fw = new FileWriter(file, true);
        pw = new PrintWriter(fw);

        while ( (line = br.readLine()) != null) {
          pw.println(line);
        }
      } catch (Exception ioe) {
        ioe.printStackTrace();
      } finally {
        if (pw != null) {
          pw.close();
        }
      }
    }
  }
}