package dialogs;

import java.awt.Component;

import graphs.XYGraphPitch;

import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
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

  private static Group graphicResultGroup;
  private static Label graphicResultLabel;
  private static Canvas graphicResultCanvas;
  private static XYGraphPitch graphicResultGraph;
  private static LightweightSystem lws;

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
    graphicResultGroup = new Group(shell, SWT.SHADOW_IN);
    graphicResultGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 5, 1));
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

  @Override
  public void addGraphicPoints(int counter, double[] x, double[] y) {
    Display.getDefault().syncExec(new Runnable() {
      @Override public void run() {
        graphicResultGraph.addPoints(counter, x, y);
      }
    });

    String rawLines = "";
    for(int i = 0; i < counter; i++)
      rawLines = rawLines + x[i] + " " + y[i] + "\n";
    final String displayRawLines = rawLines;
    Display.getDefault().syncExec(new Runnable() {
      @Override public void run() {
        rawResultText.setText(displayRawLines);
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
