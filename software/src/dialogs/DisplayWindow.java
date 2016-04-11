package dialogs;

public abstract class DisplayWindow {
  public abstract void addPitches(int counter, double[] x, double[] y);
  public abstract void addNotes(int counter, String[] names, int[] octaves, double[] x, double[] y, boolean[] hidden);
}
