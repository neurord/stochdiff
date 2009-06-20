
package org.catacomb.util;

import org.catacomb.report.E;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;


public class AWTUtil {



    public static BufferedImage getScaledBufferedImage(Image srcim, int wscl, int hscl) {

        BufferedImage imret = new BufferedImage(wscl, hscl, BufferedImage.TYPE_3BYTE_BGR);

        Image sim = srcim.getScaledInstance(wscl, hscl, Image.SCALE_AREA_AVERAGING);

        Graphics gg = imret.getGraphics();

        gg.drawImage(sim, 0, 0, null);

        return imret;
    }






    public static BufferedImage getBufferedImage(Object ob) {
        Component fr = (Component) ob;
        Dimension d = fr.getSize();
        int imw = d.width;
        int imh = d.height;
        BufferedImage im = new BufferedImage(imw, imh, BufferedImage.TYPE_3BYTE_BGR);
        Graphics gg = im.getGraphics();
        fr.paint(gg);

        /*
        // J ?
        if (fr instanceof JPanel) {
        ((JPanel)fr).paintComponent(gg);
        } else {
        S.p("image saving error - cant handl component type " + fr);
        }
        //	 fr.printAll (gg);
        */

        /*
        int imw = im.getWidth(null);
        int imh = im.getHeight(null);

        if (Math.abs(f-1.) > 0.001) {
        imw = (int)(f*imw);
         imh = (int)(f*imh);
         im = im.getScaledInstance(imw, imh, im.SCALE_AREA_AVERAGING);
            }
            */
        return im;
    }









    public static Image getImage(Object ob) {
        return (getImage(ob, 1.));
    }


    public static Image getImage(Object ob, double f) {
        if (!(ob instanceof Image || ob instanceof Component)) {
            E.error("cant get image of " + ob);
            return null;
        }

        Image im = null;
        if (ob instanceof Image) {
            im = (Image) ob;

        } else {
            Component fr = (Component) ob;
            Dimension d = fr.getSize();
            int imw = d.width;
            int imh = d.height;
            im = fr.createImage(imw, imh);
            Graphics gg = im.getGraphics();

            fr.paint(gg);
            /*
            // J ?
            if (fr instanceof JPanel) {
               ((JPanel)fr).paintComponent(gg);
            } else {
               S.p("image saving error - cant handl component type " + fr);
            }
            //	 fr.printAll (gg);
            */

        }

        int imw = im.getWidth(null);
        int imh = im.getHeight(null);

        if (Math.abs(f-1.) > 0.001) {
            imw = (int)(f*imw);
            imh = (int)(f*imh);
            im = im.getScaledInstance(imw, imh, Image.SCALE_AREA_AVERAGING);
        }
        return im;
    }



    public static int[][] getIntegerImage(Object ob) {
        return getIntegerImage(ob, 1.);
    }

    public static int[][] getIntegerImage(Object ob, double f) {

        Image img = getImage(ob, f);

        int imw = img.getWidth(null);
        int imh = img.getHeight(null);

        int[] pix = new int[imw * imh];
        PixelGrabber grabber = new PixelGrabber(img, 0, 0, imw, imh, pix, 0, imw);

        try {
            grabber.grabPixels();
        } catch (Exception e) {

            E.error("pixel grabbing interrupted");
        }


        int[][] ret = new int[imh][imw];

        for (int i = 0; i < imh; i++) {
            for (int j = 0; j < imw; j++) {
                int pi = pix[i*imw + j];
                int ir = ((pi  >> 16) & 0xFF);
                int ig = ((pi >> 8) & 0xFF);
                int ib = (pi & 0xFF);
                ret[i][j] = (ir << 16) + (ig << 8) + ib;
                if (ret[i][j] < 0) {
                    E.error("neg pix value??  " + ret[i][j]);
                }
            }
        }


        return ret;
    }

}
