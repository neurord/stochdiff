package org.catacomb.icon.splash;

import org.catacomb.report.E;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.InputStream;

import javax.imageio.ImageIO;


public class Splash implements Runnable {

    String imgsrc;

    SplashWindow splashWindow;


    public Splash(String s, String fnm) {
        imgsrc = s.replaceAll("\\.", "/") + "/" + fnm;
    }


    public void show() {

        InputStream fis = ClassLoader.getSystemResourceAsStream(imgsrc);

        try {
            BufferedImage bim = ImageIO.read(fis);

            // NB creating this frame takes up half the startup time!
            Frame frame = new Frame();

            splashWindow = new SplashWindow(bim, frame);


            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Dimension windowSize = splashWindow.getPreferredSize();


            splashWindow.setLocation(screenSize.width / 2 - (windowSize.width / 2), screenSize.height
                                     / 2 - (windowSize.height / 2));

            splashWindow.packShow();

            Thread th = new Thread(this);
            th.start();
        } catch (Exception ex) {
            E.error("cannot read splash " + imgsrc + " " + ex);
        }
    }



    public void run() {
        try {
            Thread.sleep(4000);
        } catch (Exception ex) {

        }

        splashWindow.setVisible(false);

    }


    public void hide() {
        splashWindow.setVisible(false);
    }


}
