package main;

import org.csstudio.swt.xygraph.dataprovider.CircularBufferDataProvider;
import org.csstudio.swt.xygraph.figures.ToolbarArmedXYGraph;
import org.csstudio.swt.xygraph.figures.Trace;
import org.csstudio.swt.xygraph.figures.Trace.PointStyle;
import org.csstudio.swt.xygraph.figures.XYGraph;
import org.csstudio.swt.xygraph.figures.Trace.TraceType;
import org.csstudio.swt.xygraph.linearscale.Range;
import org.csstudio.swt.xygraph.util.XYGraphMediaFactory;
import org.eclipse.draw2d.Figure;
import org.eclipse.swt.widgets.Display;

class XYGraphPitch extends Figure {
  public Trace trace;
  public XYGraph xyGraph;
  public Runnable updater;
  private final CircularBufferDataProvider traceProvider;
  private final ToolbarArmedXYGraph toolbarArmedXYGraph;
  
  public XYGraphPitch() {
    xyGraph = new XYGraph();
    xyGraph.setTitle("XY Graph Test");
    xyGraph.setFont(XYGraphMediaFactory.getInstance().getFont(XYGraphMediaFactory.FONT_TAHOMA));
    
    xyGraph.primaryXAxis.setTitle("Time");
    xyGraph.primaryXAxis.setRange(new Range(0, 1000));
    xyGraph.primaryXAxis.setAutoScale(true);
    xyGraph.primaryXAxis.setShowMajorGrid(true);
    xyGraph.primaryXAxis.setShowMinorGrid(true);
    xyGraph.primaryXAxis.setAutoScaleThreshold(0);
    
    xyGraph.primaryYAxis.setTitle("Amplitude");
    xyGraph.primaryYAxis.setAutoScale(true);
    xyGraph.primaryYAxis.setShowMajorGrid(true);
    xyGraph.primaryYAxis.setShowMinorGrid(true);

    traceProvider = new CircularBufferDataProvider(true);
    traceProvider.setBufferSize(1000);
    traceProvider.setUpdateDelay(100);
    xyGraph.setFocusTraversable(true);
    xyGraph.setRequestFocusEnabled(true);
    
    initTrace();
    toolbarArmedXYGraph = new ToolbarArmedXYGraph(xyGraph);
    add(toolbarArmedXYGraph);
  }
  
  public void initTrace() {
    if(trace != null)
      xyGraph.removeTrace(trace);
    trace = new Trace("Pitch record", xyGraph.primaryXAxis, xyGraph.primaryYAxis, traceProvider);
    trace.setTraceType(TraceType.SOLID_LINE);
    trace.setLineWidth(1);
    trace.setAreaAlpha(100);
    trace.setPointStyle(PointStyle.POINT);
    trace.setPointSize(4);
    trace.setAntiAliasing(false);
    trace.setErrorBarEnabled(false);
    trace.setTraceColor(XYGraphMediaFactory.getInstance().getColor(XYGraphMediaFactory.COLOR_BLUE));
    xyGraph.addTrace(trace);
  }
  
  public void setPoints(int amount, double[] x, double[] y) {
    updater = new Runnable(){
      public void run() {
        initTrace();
        xyGraph.primaryXAxis.setRange(new Range(0, amount));
        traceProvider.setBufferSize(amount);
        for(int i = 0; i < amount; i++) {

          traceProvider.setCurrentXData(x[i]);
          traceProvider.setCurrentYData(y[i], (long)(x[i] * 1000));
          //traceProvider.setCurrentYDataTimestamp((long)(x[i] * 1000));
        }
        xyGraph.repaint();
      }
    };

    Display.getCurrent().timerExec(0, updater);
  }
  
  public void addPoints(int amount, double[] x, double[] y) {
    updater = new Runnable(){
      public void run() {
        xyGraph.primaryXAxis.setRange(new Range(0, amount));
        traceProvider.setBufferSize(amount);
        traceProvider.setCurrentXDataArray(x);
        traceProvider.setCurrentYDataArray(y);
        xyGraph.repaint();
      }
    };

    Display.getCurrent().timerExec(0, updater);
  }

  @Override
  protected void layout() {
    toolbarArmedXYGraph.setBounds(bounds.getCopy().shrink(5, 5));
    super.layout();
  }
}