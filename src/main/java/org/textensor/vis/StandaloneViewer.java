package org.textensor.vis;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JFrame;


public class StandaloneViewer {

    JFrame frame;

    SceneGraphViewer viewer;


    public StandaloneViewer() {
        frame = new JFrame();
        frame.setPreferredSize(new Dimension(800, 600));
        Container ctr = frame.getContentPane();

        viewer = new SceneGraphViewer();
        ctr.setLayout(new BorderLayout());
        ctr.add(viewer.getPanel(), BorderLayout.CENTER);

        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        loadElements(new File("test/sample-mesh.tri"));
    }



    public void show() {
        frame.pack();
        frame.setVisible(true);
    }



    public static void main(String[] argv) {
        StandaloneViewer sv = new StandaloneViewer();
        sv.show();
    }



    public void loadElements(File ftri) {
        ElementReader er = new ElementReader(ftri);
        er.read();
        SceneGraphBuilder sgb = new SceneGraphBuilder();

        /*
        int ii = 0;
        ArrayList<VolElt> samp = new ArrayList<VolElt>();
        for (VolElt ve : er.getElements()) {
        	if (ii < 5 || ii % 10 == 0) {
        		samp.add(ve);
        	}
        	ii += 1;
        }
        */
        sgb.loadElements(er.getElements());
        viewer.setSceneGraph(sgb.getSceneGraph(), sgb.getShapes());
    }



}

