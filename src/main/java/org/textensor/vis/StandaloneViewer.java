package org.textensor.vis;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.JFrame;


public class StandaloneViewer {

    JFrame frame;

    SceneGraphViewer viewer;


    public StandaloneViewer() {
        frame = new JFrame();
        frame.setPreferredSize(new Dimension(400, 400));
        Container ctr = frame.getContentPane();

        viewer = new SceneGraphViewer();
        ctr.setLayout(new BorderLayout());
        ctr.add(viewer.getPanel(), BorderLayout.CENTER);

        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);


    }



    public void show() {
        frame.pack();
        frame.setVisible(true);
    }



    public static void main(String[] argv) {
        StandaloneViewer sv = new StandaloneViewer();
        sv.show();
    }



}

