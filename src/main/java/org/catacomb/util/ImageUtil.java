package org.catacomb.util;

import org.catacomb.report.E;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageUtil {


    public static void writePNG(RenderedImage img, File f) {
        try {
            ImageIO.write(img, "png", f);
        } catch (IOException ex) {
            E.error("write failed for " + f + " " + ex);
        }
    }

}
