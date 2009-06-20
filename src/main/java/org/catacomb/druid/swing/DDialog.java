
package org.catacomb.druid.swing;

import java.awt.Color;

import javax.swing.JDialog;


public class DDialog extends JDialog {
    static final long serialVersionUID = 1001;
    String name = "anon";

    public DDialog() {
        this(null, "anon");
    }

    public DDialog(DFrame dframe, String s) {
        super(dframe);
        name = s;
        setTitle(s);
        setBg(LAF.getBackgroundColor());
    }


    public int[] getIntArraySize() {
        int[] wh = new int[2];
        wh[0] = getWidth();
        wh[1] = getHeight();
        return wh;
    }


    public void setBg(Color c) {
        setBackground(c);
    }


    public void setName(String s) {
        name = s;
    }

    public String getName() {
        return name;
    }

    public void setTitle(String s) {
        super.setTitle(s);
    }


    public void open() {
        pack();
        setVisible(true);
    }


    public void close() {
        setVisible(false);
    }

    public void setPanel(DPanel dp) {
        getContentPane().add("Center", dp);
    }

}







