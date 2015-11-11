package dialogs;

import graphs.XYGraphPitch;

import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
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

  /* Components */
  private static MenuItem startRecordItem;
  private static MenuItem stopRecordItem;
  private static MenuItem settingsRecordItem;
  private static MenuItem openWavFileItem;
  private static Label recordResult;

  private static Label rawResultLabel;
  private static Text rawResultText;
  private static Button rawResultClear;

  private static Group rawGraphicResultGroup;
  private static Button rawGraphicResultClearButton;
  private static Canvas rawGraphicResultCanvas;
  private static XYGraphPitch rawGraphicResultGraph;
  private static LightweightSystem rawGraphicLws;
  private static Group filteredGraphicResultGroup;
  private static Canvas filteredGraphicResultCanvas;
  private static XYGraphPitch filteredGraphicResultGraph;
  private static LightweightSystem filteredGraphicLws;

  public MainWindow() {
    display = new Display();
    shell = new Shell(display);
    shell.setLayout(new GridLayout(6, true));
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
  }

  private static void addRawResults(Shell shell) {
    Composite parentComposite = new Composite(shell, SWT.NONE);
    parentComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    parentComposite.setLayout(new GridLayout(1, true));

    rawResultLabel = new Label(parentComposite, SWT.NONE);
    rawResultLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
    rawResultLabel.setText("List of the RAW pitch points:");

    rawResultText = new Text(parentComposite, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
    rawResultText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    rawResultText.setText("");

    rawResultClear = new Button(parentComposite, SWT.PUSH);
    rawResultClear.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
    rawResultClear.setText("Clear");
    rawResultClear.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        rawResultText.setText("");
      }
    });
  }

  private static void addGraphicResults(Shell shell) {
    Composite parentComposite = new Composite(shell, SWT.NONE);
    parentComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 5, 1));
    parentComposite.setLayout(new GridLayout(1, true));

    rawGraphicResultGroup = new Group(parentComposite, SWT.SHADOW_IN);
    rawGraphicResultGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    rawGraphicResultGroup.setLayout(new GridLayout(7, true));
    rawGraphicResultGroup.setText("Raw Graphic results");

    rawGraphicResultClearButton = new Button(rawGraphicResultGroup, SWT.PUSH);
    rawGraphicResultClearButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
    rawGraphicResultClearButton.setText("Clear");
    rawGraphicResultClearButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        rawGraphicResultGraph.clear();
      }
    });

    // Create the canvas for drawing on
    rawGraphicResultCanvas = new Canvas(rawGraphicResultGroup, SWT.NONE);
    rawGraphicResultCanvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 7, 1));

    // Create the graph
    rawGraphicLws = new LightweightSystem(rawGraphicResultCanvas);
    rawGraphicResultGraph = new XYGraphPitch("Raw graph", "Time", "Amplitude");
    rawGraphicLws.setContents(rawGraphicResultGraph);

    filteredGraphicResultGroup = new Group(parentComposite, SWT.SHADOW_IN);
    filteredGraphicResultGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
    filteredGraphicResultGroup.setLayout(new GridLayout(2, true));
    filteredGraphicResultGroup.setText("Filtered Graphic results");

    // Create the canvas for drawing on
    filteredGraphicResultCanvas = new Canvas(filteredGraphicResultGroup, SWT.NONE);
    filteredGraphicResultCanvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

    // Create the graph
    filteredGraphicLws = new LightweightSystem(filteredGraphicResultCanvas);
    filteredGraphicResultGraph = new XYGraphPitch("Notes graph", "Time", "Amplitude");
    filteredGraphicLws.setContents(filteredGraphicResultGraph);
  }

  private static void addStatusBar(Shell shell) {
    Composite parentComposite = new Composite(shell, SWT.BORDER);
    parentComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 6, 1));
    parentComposite.setLayout(new GridLayout(1, true));

    recordResult = new Label(parentComposite, SWT.NONE);
    recordResult.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    recordResult.setText("");
  }

  @Override
  public void addGraphicPoints(int counter, double[] x, double[] y) {
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
        rawResultText.append(displayRawLines);
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
