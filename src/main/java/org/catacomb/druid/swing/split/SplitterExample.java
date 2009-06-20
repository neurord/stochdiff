package org.catacomb.druid.swing.split;

import javax.swing.*;
import java.awt.*;

public class SplitterExample extends JFrame {

    private static final long serialVersionUID = 1L;

    public SplitterExample() {
        super("SplitterLayout - santhosh@in.fiorano.com");
        JPanel contents = (JPanel)getContentPane();
        contents.setLayout(new SplitterLayout(SplitterLayout.VERTICAL));
        contents.add("1", new JButton("A (1)"));

        DSplitterBar b1 = new DSplitterBar();
        b1.setLayout(new GridLayout(1, 0));
        b1.add(new DSplitterSpace());
        b1.add(new JLabel("Status"));
        b1.add(new JTextField("Enter your name"));
        b1.add(new DSplitterSpace());
        contents.add(b1);
        contents.add("2", new JButton("B (2)"));

        DSplitterBar b2 = new DSplitterBar();
        b2.setLayout(new SplitterLayout(SplitterLayout.HORIZONTAL));
        b2.add("5", new DSplitterSpace());
        b2.add(new DSplitterBar());
        b2.add("10", new JLabel("Status"));
        b2.add(new DSplitterBar());
        b2.add("40", new JTextField("Enter your name"));
        contents.add(b2);
        contents.add("4", new JButton("C (4)"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        JFrame frame = new SplitterExample();
        frame.setSize(500, 300);
        frame.setVisible(true);
    }
}
