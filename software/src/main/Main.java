package main;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import javax.sound.sampled.LineUnavailableException;

import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import common.Constants;
import common.Utils;
import sound.Recorder;

public class Main {
//Record duration, in milliseconds
 private static final long RECORD_TIME = 5000;  // 5 seconds
 private static File wavFile = new File(System.getProperty("user.dir") + File.separator + Constants.outputDirectory + File.separator +  Constants.wav_file);
 
 /* Components */
 private static Group actionsGroup;
 private static Button recordButton;
 private static Label recordResult;
 private static Button pitchButton;
 private static Label pitchResult;
 
 private static Group rawResultGroup;
 private static Label rawResultLabel;
 private static Text rawResultText;
 private static Thread rawResultThread;
 private static Button rawResultClear;
 private static String resultsFilePath = "";
 
 private static Group graphicResultGroup;
 private static Label graphicResultLabel;
 private static Thread graphicResultThread;
 private static Canvas graphicResultCanvas;
 private static XYGraphPitch graphicResultGraph;
 private static LightweightSystem lws;
  
  private static boolean record() {
    final Recorder recorder = new Recorder();

    // Create a separate thread for recording
    Thread recordThread = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          System.out.println("Start recording.");
          recorder.start();
        } catch (LineUnavailableException ex) {
          ex.printStackTrace();
        }
      }
    });

    recordThread.start();

    try {
      for(int i = 0; i < (int)(RECORD_TIME/1000); i++) {
        Thread.sleep(1000);
        System.out.print(".");
      }
      System.out.println();
    } catch (InterruptedException ex) {
      ex.printStackTrace();
    }

    try {
      System.out.println("Stopping recording...");
      recorder.stop();
      System.out.println("Saving file...");
      recorder.save(wavFile);
      System.out.println("Stopped.");
    } catch (IOException ex) {
      ex.printStackTrace();
      return false;
    }

    System.out.println("Done.");
    return true;
  }
  
  private static void pitch(String rawResultFile) {
    String[] command = new String[]{ "cmd", "/c", Constants.pitch_executable, "-i", wavFile.getAbsolutePath() };
    
    // Execute the batch file to create extra cfg directory
    ProcessBuilder process = new ProcessBuilder(command);
    process.directory(new File(System.getProperty("user.dir"), Constants.aubio_path));
    process.redirectErrorStream(true);
    
    try {
      Process commandShell = process.start();           
      Utils.StreamGobbler outputGobbler = new Utils.StreamGobbler(commandShell.getInputStream(), rawResultFile);
      outputGobbler.start();
      int shellExitCode = commandShell.waitFor();
      outputGobbler.join(500);
      
      System.out.println("Shell Exit Code: " + shellExitCode);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  private static void addActions(Shell shell, int horizontalSpan) {
    actionsGroup = new Group(shell, SWT.SHADOW_IN);
    actionsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, horizontalSpan, 1));
    actionsGroup.setLayout(new GridLayout(2, true));
    actionsGroup.setText("Actions");
    
    recordButton = new Button(actionsGroup, SWT.PUSH);
    recordButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
    recordButton.setText("Record");
    recordButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        recordResult.setText("Recording...");
        recordButton.setEnabled(false);
        
        Thread recordThread = new Thread(new Runnable() {
          @Override
          public void run() {
            try {
              if(record()) {
                Display.getDefault().syncExec(new Runnable() {
                  @Override public void run() {
                    recordButton.setEnabled(true);
                    recordResult.setText("Record OK.");
                  }
                });
              }
            } catch (Exception ex) {
              ex.printStackTrace();
            }
          }
        });
        recordThread.start();
      }
    });
    
    recordResult = new Label(actionsGroup, SWT.NONE);
    recordResult.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
    recordResult.setText("");
    
    pitchButton = new Button(actionsGroup, SWT.PUSH);
    pitchButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
    pitchButton.setText("Pitch");
    pitchButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        pitch(System.getProperty("user.dir") + File.separator + Constants.outputDirectory + File.separator + "raw.txt");
        pitchResult.setText("Pitch done!");
        resultsFilePath = System.getProperty("user.dir") + File.separator + Constants.outputDirectory + File.separator + "raw.txt";
        
        rawResultThread = new Thread(new Runnable() {
          @Override
          public void run() {
            rawResultThreadFunction();
          }
        });
        rawResultThread.start();
        
        graphicResultThread = new Thread(new Runnable() {
          @Override
          public void run() {
            graphicResultThreadFunction();
          }
        });
        graphicResultThread.start();
      }
    });
    
    pitchResult = new Label(actionsGroup, SWT.NONE);
    pitchResult.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
    pitchResult.setText("");
  }
  
  private static void rawResultThreadFunction() {
    try {
      System.out.println("Start displaying results.");
      File resultsFile = new File(resultsFilePath);
      if(resultsFile.exists()) {
        Scanner scanner = new Scanner(resultsFile);
        String rawLines = "";
        while (scanner.hasNextLine())
          rawLines = rawLines + scanner.nextLine() + "\n";
        scanner.close();

        final String displayRawLines = rawLines;
        Display.getDefault().syncExec(new Runnable() {
          @Override public void run() {
            rawResultText.setText(displayRawLines);
          }
        });
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
  
  private static void graphicResultThreadFunction() {
    try {
      System.out.println("Start displaying graphical results.");
      File resultsFile = new File(resultsFilePath);
      if(resultsFile.exists()) {
        Scanner scanner = new Scanner(resultsFile);
        
        /* Count the lines of the file */
        int line_counter = 0;
        while (scanner.hasNextLine()) {
          line_counter++;
          scanner.nextLine();
        }
        scanner.close();
        
        System.out.println(line_counter + " coordinates to display.");
        
        double[] x = new double[line_counter];
        double[] y = new double[line_counter];
        
        int i = 0;
        scanner = new Scanner(resultsFile);
        while (scanner.hasNextLine()) {
          String line = scanner.nextLine();
          String[] words = line.split("\\s+");          
          if(words.length == 2) {
            x[i] = Double.parseDouble(words[0]);
            y[i] = Double.parseDouble(words[1]);
          }
          i++;
        }
        scanner.close();

        final int coord_counter = line_counter;
        Display.getDefault().syncExec(new Runnable() {
          @Override public void run() {
            graphicResultGraph.setPoints(coord_counter, x, y);
          }
        });
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
  
  private static void addRawResults(Shell shell, int horizontalSpan) {
    rawResultGroup = new Group(shell, SWT.SHADOW_IN);
    rawResultGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, horizontalSpan, 1));
    rawResultGroup.setLayout(new GridLayout(1, true));
    rawResultGroup.setText("Raw results");

    rawResultLabel = new Label(rawResultGroup, SWT.NONE);
    rawResultLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
    rawResultLabel.setText("List of the RAW pitch points:");

    rawResultText = new Text(rawResultGroup, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
    rawResultText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
    rawResultText.setText("");
    rawResultThread = null;

    rawResultClear = new Button(rawResultGroup, SWT.PUSH);
    rawResultClear.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
    rawResultClear.setText("Clear");
    rawResultClear.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        rawResultText.setText("");
      }
    });
  }
  
  private static void addGraphicResults(Shell shell, int horizontalSpan) {
    graphicResultGroup = new Group(shell, SWT.SHADOW_IN);
    graphicResultGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, horizontalSpan, 1));
    graphicResultGroup.setLayout(new GridLayout(2, true));
    graphicResultGroup.setText("Graphic results");

    graphicResultLabel = new Label(graphicResultGroup, SWT.NONE);
    graphicResultLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
    graphicResultLabel.setText("Curve:");
    
    graphicResultThread = null;
    
    // Create the canvas for drawing on
    graphicResultCanvas = new Canvas(graphicResultGroup, SWT.NONE);
    graphicResultCanvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
    graphicResultCanvas.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_GRAY));
    
    // Create the graph
    lws = new LightweightSystem(graphicResultCanvas);
    graphicResultGraph = new XYGraphPitch();
    lws.setContents(graphicResultGraph);
    
    /*final LightweightSystem lws = new LightweightSystem(graphicResultCanvas);
    final XYGraphTest testFigure = new XYGraphTest();
    lws.setContents(testFigure);
    */

  }

  public static void main(String[] args) {
    Display display = new Display();
    Shell shell = new Shell(display);
    shell.setLayout(new GridLayout(7, true));
    shell.setText("Sound2Tab");
    shell.setMaximized(true);
    
    addActions(shell, 1);
    addRawResults(shell, 1);
    addGraphicResults(shell, 5);
    // TestMusicXML();
    
    shell.open();
    while(!shell.isDisposed()) {
      if(!display.readAndDispatch())
        display.sleep();
    }
    display.dispose();
  }
}



