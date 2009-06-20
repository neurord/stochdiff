

package org.catacomb.graph.arbor;

import javax.swing.JFrame;

import org.catacomb.graph.gui.DataDisplay;

public class SegmentDemo {


    public static void main(String[] argv) {

        JFrame f = new JFrame();
        DataDisplay wc = new DataDisplay(600, 500);


        SegmentGraph sg = new SegmentGraph();

        SegmentGraphVE sgve = new SegmentGraphVE();
        sgve.setSegmentGraph(sg);


        wc.setBuildPaintInstructor(sgve);

        wc.setPickListener(sgve);

        f.getContentPane().add(wc);
        f.pack();
        f.setVisible(true);
    }


}
