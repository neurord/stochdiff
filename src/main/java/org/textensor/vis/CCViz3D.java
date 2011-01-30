package org.textensor.vis;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JFrame;


public class CCViz3D {

    JFrame frame;

    SceneGraphViewer viewer;


    public CCViz3D() {
        frame = new JFrame();
        frame.setPreferredSize(new Dimension(800, 600));
        Container ctr = frame.getContentPane();

        viewer = new SceneGraphViewer();
        ctr.setLayout(new BorderLayout());
        ctr.add(viewer.getPanel(), BorderLayout.CENTER);

        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        //	loadElements(new File("test/sample-mesh.tri"));
    }



    public void show() {
        frame.pack();
        frame.setVisible(true);
    }



    public static void main(String[] argv) {
        CCViz3D sv = new CCViz3D();
        sv.show();
        if (argv.length > 0) {
            sv.loadElements(new File(argv[0]));
        }
    }



    public void loadElements(File ftri) {
        ElementReader er = new ElementReader(ftri);
        er.read();
        SceneGraphBuilder sgb = new SceneGraphBuilder();

        sgb.loadElements(er.getElements());
        viewer.setSceneGraph(sgb.getSceneGraph(), sgb.getShapes());
    }



}

