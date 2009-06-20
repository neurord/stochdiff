package org.catacomb.dataview.formats;

import java.io.File;

import org.catacomb.datalish.Box;
import org.catacomb.dataview.display.ViewConfig;
import org.catacomb.graph.gui.Painter;
import org.catacomb.util.FileUtil;

import java.util.StringTokenizer;


public class SWCDisplay implements DataHandler {

    Node[] nodes;

    static String[] viewOptions = {"lines", "boxes", "frame", "solid"};

    String viewStyle = "lines";


    public String[] getViewOptions() {
        return viewOptions;
    }

    public void setViewStyle(String s) {
        viewStyle = s;
    }


    public String getMagic() {
        return "cctswc00";
    }


    public void read(File f) {

        String s = FileUtil.readStringFromFile(f);
        StringTokenizer st = new StringTokenizer(s, "\n");
        int npx = st.countTokens();
        nodes = new Node[npx];

        while (st.hasMoreTokens()) {
            String sl = st.nextToken();
            StringTokenizer stl = new StringTokenizer(sl, " ");
            if (stl.countTokens() >= 6) {
                int ip = nextInt(stl);
                double x = nextDouble(stl);
                double y = nextDouble(stl);
                double z = nextDouble(stl);
                double r = nextDouble(stl);
                int ipar = nextInt(stl);
                Node parent = null;
                if (ipar >= 0) {
                    parent = nodes[ipar];
                }
                nodes[ip] = new Node(ip, x, y, z, r, parent);
            }

        }


    }

    private int nextInt(StringTokenizer stl) {
        return Integer.parseInt(stl.nextToken());
    }

    private double nextDouble(StringTokenizer stl) {
        return Double.parseDouble(stl.nextToken());
    }


    public boolean antialias() {
        return false;
    }


    public void instruct(Painter p) {

        if (viewStyle.equals("lines")) {
            instructLines(p);

        } else if (viewStyle.equals("boxes")) {
            instructBoxes(p);

        } else if (viewStyle.equals("frame")) {
            instructFrame(p);

        } else if (viewStyle.equals("filled")) {
            instructFilled(p);
        }
    }



    private void instructLines(Painter p) {
        for (Node node : nodes) {
            if (node != null) {
                Node pn = node.parent;
                if (pn != null) {
                    p.setColorWhite();
                    p.drawLine(node.x, node.y, pn.x, pn.y);

                }
                p.fillIntCircle(node.x, node.y, 3);
            }
        }

    }


    private void instructBoxes(Painter p) {
        for (Node node : nodes) {
            if (node != null) {
                Node pn = node.parent;
                if (pn != null) {
                    p.setColorWhite();
                    // need ends too
                    p.drawCarrotSides(node.x, node.y, node.r, pn.x, pn.y, pn.r);
                }
            }
        }
    }

    private void instructFrame(Painter p) {
        for (Node node : nodes) {
            if (node != null) {
                Node pn = node.parent;
                if (pn != null) {
                    p.setColorWhite();
                    p.drawCarrotSides(node.x, node.y, node.r, pn.x, pn.y, pn.r);

                }
                p.drawCircle(node.x, node.y, node.r);
            }
        }

    }

    private void instructFilled(Painter p) {
        for (Node node : nodes) {
            if (node != null) {
                Node pn = node.parent;
                if (pn != null) {
                    p.setColorWhite();
                    p.drawLine(node.x, node.y, pn.x, pn.y);

                }
                p.fillIntCircle(node.x, node.y, 3);
            }
        }

    }


    public Box getLimitBox() {
        return null;
    }




    class Node {
        int index;
        Node parent;
        double x;
        double y;
        double z;
        double r;


        public Node(int i, double ax, double ay, double az, double ar, Node par) {
            index = i;
            x = ax;
            y = ay;
            z = az;
            r = ar;
            parent = par;
        }
    }




    public String[] getPlotNames() {
        // TODO Auto-generated method stub
        return null;
    }

    public double getMinValue() {
        // TODO Auto-generated method stub
        return 0;
    }

    public double getMaxValue() {
        // TODO Auto-generated method stub
        return 0;
    }

    public double[] getFrameValues() {
        // TODO Auto-generated method stub
        return null;
    }

    public int getContentStyle() {
        return STATIC;
    }

    public void setFrame(int ifr) {
        // TODO Auto-generated method stub

    }

    public void setPlot(String s) {
        // TODO Auto-generated method stub

    }

    public DataHandler getCoHandler() {
        return null;
    }

    public boolean hasData() {
        return true;
    }

    public String getXAxisLabel() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getYAxisLabel() {
        // TODO Auto-generated method stub
        return null;
    }

    public ViewConfig getViewConfig(String s) {
        // TODO Auto-generated method stub
        return null;
    }

    public void setZValue(double d) {
        // TODO Auto-generated method stub

    }
}



