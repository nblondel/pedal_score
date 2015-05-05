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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import sound.RecorderThread;

public class MainWindow {
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

  private static Group graphicResultGroup;
  private static Label graphicResultLabel;
  private static Canvas graphicResultCanvas;
  private static XYGraphPitch graphicResultGraph;
  private static LightweightSystem lws;

  public MainWindow() {
    display = new Display();
    shell = new Shell(display);
    shell.setLayout(new GridLayout(7, true));
    shell.setText("Sound2Tab");
    //shell.setMaximized(true);

    addActions(shell, 1);
    addRawResults(shell, 1);
    addGraphicResults(shell, 5);
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

    recordResult = new Label(actionsGroup, SWT.BORDER);
    recordResult.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
    recordResult.setText("");
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

    // Create the canvas for drawing on
    graphicResultCanvas = new Canvas(graphicResultGroup, SWT.NONE);
    graphicResultCanvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
    graphicResultCanvas.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_GRAY));

    // Create the graph
    lws = new LightweightSystem(graphicResultCanvas);
    graphicResultGraph = new XYGraphPitch();
    lws.setContents(graphicResultGraph);
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
