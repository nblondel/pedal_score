package dialogs;

import graphs.XYNoteGraph;
import graphs.XYPitchGraph;

import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import sound.RecordFileWriter;
import sound.RecorderThread;

public class MainWindow extends DisplayWindow {
  private Shell shell;
  private Display display;

  /* Menu */
  private static MenuItem startRecordItem;
  private static MenuItem stopRecordItem;
  private static MenuItem settingsRecordItem;
  private static MenuItem openWavFileItem;
  /* Status bar (bottom) */
  private static Label recordResult;
  /* Raw text results */
  private static Text rawTextResultText;
  /* Filtered text results */
  private static Text filteredTextResultText;
  /* Raw graphic results */
  private static Group rawResultGroup;
  private static Canvas rawGraphicResultCanvas;
  private static XYPitchGraph rawGraphicResultGraph;
  private static LightweightSystem rawGraphicLws;
  /* Filtered */
  private static Group filteredResultGroup;
  private static Canvas filteredGraphicResultCanvas;
  private static XYNoteGraph filteredGraphicResultGraph;
  private static LightweightSystem filteredGraphicLws;

  public MainWindow() {
    display = new Display();
    shell = new Shell(display);
    shell.setLayout(new GridLayout());
    shell.setText("Sound2Tab");
    shell.setMaximized(true);

    addMenu(shell);
    addRawResults(shell);
    addGraphicResults(shell);
    addStatusBar(shell);
  }

  private static void addMenu(Shell shell) {
    // Create the bar menu
    Menu menuBar = new Menu(shell, SWT.BAR);
    shell.setMenuBar(menuBar);

    // Create the File item's menu
    Menu fileMenu = new Menu(menuBar);
    MenuItem fileItem = new MenuItem(menuBar, SWT.CASCADE);
    fileItem.setText("File");
    fileItem.setMenu(fileMenu);
    openWavFileItem = new MenuItem(fileMenu, SWT.NONE);
    openWavFileItem.setText("Open WAV file...");
    openWavFileItem.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        FileDialog fileDialog = new FileDialog(shell, SWT.OPEN);
        fileDialog.setFilterExtensions(new String[] { "*.wav" });
        fileDialog.setText("Open WAV file");
        fileDialog.setFilterPath("C:/");

        String wavFilePath = fileDialog.open();
        if(wavFilePath != null && !wavFilePath.isEmpty()) {
          recordResult.setText("Loading external WAV file '" + wavFilePath + "'.");
          RecordFileWriter.getRecordWriter().saveFromExternal(wavFilePath);
        }
      }
    });

    // Create the Record item's menu
    Menu recordMenu = new Menu(menuBar);
    MenuItem recordItem = new MenuItem(menuBar, SWT.CASCADE);
    recordItem.setText("Recorder");
    recordItem.setMenu(recordMenu);

    startRecordItem = new MenuItem(recordMenu, SWT.NONE);
    startRecordItem.setText("Start");
    startRecordItem.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        recordResult.setText("Recording...");
        startRecordItem.setEnabled(false);
        openWavFileItem.setEnabled(false);

        RecorderThread.startRecorder();
      }
    });

    stopRecordItem = new MenuItem(recordMenu, SWT.NONE);
    stopRecordItem.setText("Stop");
    stopRecordItem.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        stopRecordItem.setEnabled(false);
        recordResult.setText("Stopping recording...");

        RecorderThread.stopRecorder();

        startRecordItem.setEnabled(true);
        stopRecordItem.setEnabled(true);
        openWavFileItem.setEnabled(true);
        recordResult.setText("Record stopped.");
      }
    });

    settingsRecordItem = new MenuItem(recordMenu, SWT.NONE);
    settingsRecordItem.setText("Settings...");
    settingsRecordItem.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        RecorderSettingsDialog dialog = new RecorderSettingsDialog(shell);
        dialog.create();
        if(dialog.open() == RecorderSettingsDialog.OK) {
          int newPeriod = dialog.getNewPeriod();
          if(newPeriod > -1)
            RecorderThread.setWritingInterval(newPeriod);
        }
      }
    });
    
    // Create the Results item's menu
    Menu resultsMenu = new Menu(menuBar);
    MenuItem resultsItem = new MenuItem(menuBar, SWT.CASCADE);
    resultsItem.setText("Results");
    resultsItem.setMenu(resultsMenu);
    MenuItem clearResultsFileItem = new MenuItem(resultsMenu, SWT.NONE);
    clearResultsFileItem.setText("Clear all");
    clearResultsFileItem.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        rawGraphicResultGraph.clear();
        filteredGraphicResultGraph.clear();
        rawTextResultText.setText("");
        filteredTextResultText.setText("");
      }
    });
  }

  private static void addRawResults(Shell shell) {
    rawResultGroup = new Group(shell, SWT.SHADOW_IN);
    rawResultGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    rawResultGroup.setLayout(new GridLayout(10, true));
    rawResultGroup.setText("Raw results (pitches)");

    rawTextResultText = new Text(rawResultGroup, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
    rawTextResultText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    rawTextResultText.setText("");
    
    // Create the canvas for drawing on
    rawGraphicResultCanvas = new Canvas(rawResultGroup, SWT.NONE);
    rawGraphicResultCanvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 9, 1));

    // Create the graph
    rawGraphicLws = new LightweightSystem(rawGraphicResultCanvas);
    rawGraphicResultGraph = new XYPitchGraph("Raw graph", "Time", "Amplitude");
    rawGraphicLws.setContents(rawGraphicResultGraph);
  }

  private static void addGraphicResults(Shell shell) {
    filteredResultGroup = new Group(shell, SWT.SHADOW_IN);
    filteredResultGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    filteredResultGroup.setLayout(new GridLayout(10, true));
    filteredResultGroup.setText("Filtered results (notes)");
    
    filteredTextResultText = new Text(filteredResultGroup, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
    filteredTextResultText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    filteredTextResultText.setText("");

    // Create the canvas for drawing on
    filteredGraphicResultCanvas = new Canvas(filteredResultGroup, SWT.NONE);
    filteredGraphicResultCanvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 9, 1));

    // Create the graph
    filteredGraphicLws = new LightweightSystem(filteredGraphicResultCanvas);
    filteredGraphicResultGraph = new XYNoteGraph("Filtered graph", "Time", "Amplitude");
    filteredGraphicLws.setContents(filteredGraphicResultGraph);
  }

  private static void addStatusBar(Shell shell) {
    recordResult = new Label(shell, SWT.NONE);
    recordResult.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    recordResult.setText("");
  }

  @Override
  public void addPitches(int counter, double[] x, double[] y) {
    Display.getDefault().syncExec(new Runnable() {
      @Override public void run() {
        rawGraphicResultGraph.addPoints(counter, x, y);
      }
    });

    String rawLines = "";
    for(int i = 0; i < counter; i++)
      rawLines = rawLines + x[i] + " " + y[i] + "\n";
    final String displayRawLines = rawLines;
    Display.getDefault().syncExec(new Runnable() {
      @Override public void run() {
        rawTextResultText.append(displayRawLines);
      }
    });
  }
  
  @Override
  public void addNotes(int counter, String[] names, int[] octaves, double[] x, double[] y) {
    Display.getDefault().syncExec(new Runnable() {
      @Override public void run() {
        filteredGraphicResultGraph.addNotes(counter, names, octaves, x, y);
      }
    });
    
    String rawLines = "";
    for(int i = 0; i < counter; i++)
      rawLines = rawLines + x[i] + " " + y[i] + "\n";
    final String displayRawLines = rawLines;
    Display.getDefault().syncExec(new Runnable() {
      @Override public void run() {
        filteredTextResultText.append(displayRawLines);
      }
    });
  }

  public void live() {
    shell.open();
    while(!shell.isDisposed()) {
      if(!display.readAndDispatch())
        display.sleep();
    }
    display.dispose();    
  }
}
