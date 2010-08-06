package org.textensor.vis;


import javax.swing.JPanel;

public interface Visualizer {


    public final static int LOW = 0;
    public final static int MEDIUM = 1;
    public final static int HIGH = 2;

    public JPanel getPanel();

    public void setScaleFactor(double f);

    public void buildViewable(Object obj);

    public void refreshDecoration(Object obj);

    public void deltaLights(double d);

    public void setLightsPercent(int p);

    public void setAA(boolean b);

    public void setResolution(int res);

    public void setFourMatrix(double[] fourMatrixOrientation);

    public double[] getFourMatrix();



}
