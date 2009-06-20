package org.catacomb.druid.swing.split;

import javax.swing.*;
import java.awt.*;

public class DSplitterSpace extends JComponent {

    private static final long serialVersionUID = 1L;
    public synchronized Dimension getMinimumSize() {
        return new Dimension(10, 10);
    }
    public synchronized Dimension getPreferredSize() {
        return new Dimension(10, 10);
    }
}