package dialogs;

import graphs.XYGraphPitch;

import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import sound.RecorderThread;

public class MainWindow extends DisplayWindow {
  private Shell shell;
  private Display display;

  /* Components */
  private static Group actionsGroup;
  private static Button startRecordButton;
  private static Button stopRecordButton;
  private static Label recordResult;

  private static Group rawResultGroup;
  private static Label rawResultLabel;
  private static Text rawResultText;
  private static Button rawResultClear;

  private static Group rawGraphicResultGroup;
  private static Button rawGraphicResultClearButton;
  private static Text refreshPeriodText;
  private static Button refreshPeriodButton;
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
    //shell.setMaximized(true);

    addActions(shell);
    addGraphicResults(shell);
  }

  private static void addActions(Shell shell) {    
    Composite parentComposite = new Composite(shell, SWT.NONE);
    parentComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));
    parentComposite.setLayout(new GridLayout(1, true));
    
    actionsGroup = new Group(parentComposite, SWT.SHADOW_IN);
    actionsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
    actionsGroup.setLayout(new GridLayout(2, true));
    actionsGroup.setText("Actions");

    startRecordButton = new Button(actionsGroup, SWT.PUSH);
    startRecordButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
    startRecordButton.setText("Start record");
    startRecordButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        recordResult.setText("Recording...");
        startRecordButton.setEnabled(false);

        RecorderThread.startRecorder();
      }
    });

    stopRecordButton = new Button(actionsGroup, SWT.PUSH);
    stopRecordButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
    stopRecordButton.setText("Stop record");
    stopRecordButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        stopRecordButton.setEnabled(false);
        recordResult.setText("Stopping recording...");

        RecorderThread.stopRecorder();

        startRecordButton.setEnabled(true);
        stopRecordButton.setEnabled(true);
        recordResult.setText("Record stopped.");
      }
    });

    recordResult = new Label(actionsGroup, SWT.NONE);
    recordResult.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
    recordResult.setText("");
    
    
    rawResultGroup = new Group(parentComposite, SWT.SHADOW_IN);
    rawResultGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
    rawResultGroup.setLayout(new GridLayout(1, true));
    rawResultGroup.setText("Raw results");

    rawResultLabel = new Label(rawResultGroup, SWT.NONE);
    rawResultLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
    rawResultLabel.setText("List of the RAW pitch points:");

    rawResultText = new Text(rawResultGroup, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
    rawResultText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
    rawResultText.setText("");

    rawResultClear = new Button(rawResultGroup, SWT.PUSH);
    rawResultClear.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
    rawResultClear.setText("Clear");
    rawResultClear.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        rawResultText.setText("");
      }
    });
  }

  private static void addGraphicResults(Shell shell) {
    Composite parentComposite = new Composite(shell, SWT.NONE);
    parentComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 5, 2));
    parentComposite.setLayout(new GridLayout(1, true));
    
    rawGraphicResultGroup = new Group(parentComposite, SWT.SHADOW_IN);
    rawGraphicResultGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
    rawGraphicResultGroup.setLayout(new GridLayout(7, true));
    rawGraphicResultGroup.setText("Raw Graphic results");
    
    rawGraphicResultClearButton = new Button(rawGraphicResultGroup, SWT.PUSH);
    rawGraphicResultClearButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
    rawGraphicResultClearButton.setText("Clear");
    rawGraphicResultClearButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        rawGraphicResultGraph.clear();
      }
    });
    
    Label RefreshPeriodLabel = new Label(rawGraphicResultGroup, SWT.NONE);
    RefreshPeriodLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
    RefreshPeriodLabel.setText("Refresh period:");
    
    refreshPeriodText = new Text(rawGraphicResultGroup, SWT.BORDER);
    refreshPeriodText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
    refreshPeriodText.setText(((Integer)RecorderThread.getWritingInterval()).toString());
    
    refreshPeriodButton = new Button(rawGraphicResultGroup, SWT.PUSH);
    refreshPeriodButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
    refreshPeriodButton.setText("Apply");
    refreshPeriodButton.addSelectionListener(new SelectionListener() {
      @Override
      public void widgetSelected(SelectionEvent arg0) {
        try {
          Integer newPeriod = Integer.parseInt(refreshPeriodText.getText());
          if(!RecorderThread.setWritingInterval(newPeriod)) {
            refreshPeriodText.setText(((Integer)RecorderThread.getWritingInterval()).toString());
          }
        } catch(NumberFormatException e) {
          refreshPeriodText.setText(((Integer)RecorderThread.getWritingInterval()).toString());
        }
      }
      
      @Override
      public void widgetDefaultSelected(SelectionEvent arg0) { }
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
