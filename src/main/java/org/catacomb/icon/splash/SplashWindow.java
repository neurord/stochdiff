
package org.catacomb.icon.splash;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Window;
import java.awt.image.BufferedImage;


public class SplashWindow extends Window {
    static final long serialVersionUID = 1001;

    BufferedImage image;

    Dimension dim;

    public SplashWindow(BufferedImage img, Frame f) {
        super(f);

        image = img;

        dim = new Dimension(image.getWidth(), image.getHeight());
    }


    public void update(Graphics g) {
        paint(g);
    }


    public void paint(Graphics g) {
        g.drawImage(image, 0, 0, this);
    }


    public Dimension getPreferredSize() {
        return dim;

    }


    public void packShow() {
        pack();
        setVisible(true);
    }

}
