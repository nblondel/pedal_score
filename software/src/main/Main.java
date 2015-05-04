package main;

import graphs.XYGraphPitch;

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
import sound.PitcherThread;
import sound.Recorder;
import sound.RecorderThread;

public class Main { 
 /* Components */
 private static Group actionsGroup;
 private static Button startRecordButton;
 private static Button stopRecordButton;
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
  
  private static void pitch(String rawResultFile) {
    
  }
  
  private static void addActions(Shell shell, int horizontalSpan) {
    actionsGroup = new Group(shell, SWT.SHADOW_IN);
    actionsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, horizontalSpan, 1));
    actionsGroup.setLayout(new GridLayout(2, true));
    actionsGroup.setText("Actions");
    
    startRecordButton = new Button(actionsGroup, SWT.PUSH);
    startRecordButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
    startRecordButton.setText("Start record");
    startRecordButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        recordResult.setText("Recording...");
        startRecordButton.setEnabled(false);
        pitchResult.setText("");
        
        RecorderThread.startRecorder();
      }
    });
    
    stopRecordButton = new Button(actionsGroup, SWT.PUSH);
    stopRecordButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
    stopRecordButton.setText("Stop record");
    stopRecordButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        stopRecordButton.setEnabled(false);
        
        Thread recordThread = new Thread(new Runnable() {
          @Override
          public void run() {
            try {
              RecorderThread.stopRecorder();
              Display.getDefault().asyncExec(new Runnable() {
                @Override public void run() {
                  startRecordButton.setEnabled(true);
                  stopRecordButton.setEnabled(true);
                  recordResult.setText("Record OK.");
                }
              });

              PitcherThread.startPitcher();
              
              Display.getDefault().asyncExec(new Runnable() {
                @Override public void run() {
                  pitchResult.setText("Pitch done!");
                }
              });
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
            } catch (Exception ex) {
              ex.printStackTrace();
            }
          }
        });
        recordThread.start();
      }
    });
    
    recordResult = new Label(actionsGroup, SWT.NONE);
    recordResult.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
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
            graphicResultGraph.addPoints(coord_counter, x, y);
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



