package org.textensor.vis;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.textensor.report.E;



public class Icing3DViewer implements Visualizer {

    double scaleFactor = 1.;

    JFrame frame;

    SceneGraphViewer sceneGraphViewer;


    int resolution = Visualizer.MEDIUM;

    IcingPoint[] cachedPoints;


    public Icing3DViewer() {
        sceneGraphViewer = new SceneGraphViewer();


        frame = new JFrame();
        frame.setPreferredSize(new Dimension(400, 400));
        Container ctr = frame.getContentPane();


        ctr.setLayout(new BorderLayout());
        ctr.add(sceneGraphViewer.getPanel(), BorderLayout.CENTER);

        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        buildViewable(makeDummyTree());
        setLightsPercent(80);
    }



    public void show() {
        frame.pack();
        frame.setVisible(true);
    }



    public static void main(String[] argv) {
        Icing3DViewer sv = new Icing3DViewer();
        sv.show();
    }




    public IcingPoint[] makeDummyTree() {
        ArrayList<IcingPoint> aip = new ArrayList<IcingPoint>();
        IcingPoint p0 = new IcingPoint(0, 0, 0, 6);
        IcingPoint p1 = new IcingPoint(50, 0, 0, 4, p0);
        IcingPoint p2 = new IcingPoint(80, 0, 40, 2, p1);
        IcingPoint p3 = new IcingPoint(70, 10, -30, 2, p1);
        aip.add(p0);
        aip.add(p1);
        aip.add(p2);
        aip.add(p3);

        IcingPoint[] ret = aip.toArray(new IcingPoint[aip.size()]);
        return ret;
    }


    public JPanel getPanel() {
        return sceneGraphViewer.getPanel();
    }

    public void setScaleFactor(double d) {
        scaleFactor = d;
    }


    public void buildViewable(Object obj) {
        RunBuilder rb = new RunBuilder(this, obj);
        Thread thread = new Thread(rb);
        thread.start();
    }

    public void reallyBuildVewable(Object obj) {
        if (obj instanceof IcingPoint[]) {
            IcingPoint[] points = (IcingPoint[])obj;
            cachedPoints = points;
            SceneGraphBuilder sgb  = new SceneGraphBuilder();
            sgb.buildTree(points, resolution, scaleFactor);
            sceneGraphViewer.removeAllDecoration();
            sceneGraphViewer.setSceneGraph(sgb.getSceneGraph(), null);

        } else {
            E.error("cannot build viewable from " + obj);
        }
    }

    public void refreshDecoration(Object obj) {
        RunDecorator rb = new RunDecorator(this, obj);
        Thread thread = new Thread(rb);
        thread.start();
    }


    public void reallyRefreshDecoration(Object obj) {

    }



    public void deltaLights(double d) {
        sceneGraphViewer.deltaLights(d);

    }



    public void setAA(boolean b) {
        sceneGraphViewer.setAA(b);

    }



    public void setResolution(int res) {
        if (res != resolution) {
            resolution = res;
            if (cachedPoints != null) {
                SceneGraphBuilder sgb  = new SceneGraphBuilder();
                sgb.buildTree(cachedPoints, resolution, scaleFactor);
                sceneGraphViewer.setSceneGraph(sgb.getSceneGraph(), null);
            }


        }
    }

    public Color makeColor(Object obj) {
        Color ret = null;
        if (obj instanceof Color) {
            ret = (Color)obj;
        } else if (obj instanceof String) {
            String s = (String)obj;
            if (s == null) {
                s = "0xff0000";
            }
            if (!s.toLowerCase().startsWith("0x")) {
                s = "0x" + s;
            }
            try {
                ret = new Color(Integer.decode(s).intValue());
            } catch (Exception ex) {
                E.warning("dodgy color " + s);
                ret = Color.red;
            }
        } else {
            ret = Color.cyan;
        }
        return ret;
    }



    public void setLightsPercent(int p) {
        sceneGraphViewer.setLightsPercent(p);
    }



    public void setFourMatrix(double[] fmo) {
        sceneGraphViewer.setFourMatrix(fmo);
    }



    public double[] getFourMatrix() {
        return sceneGraphViewer.getFourMatrix();
    }

}
