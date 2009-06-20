package org.catacomb.graph.drawing;


import org.catacomb.interlish.content.BasicTouchTime;
import org.catacomb.util.ImageUtil;

import java.awt.image.BufferedImage;

import java.io.File;


public class IconImage {

    BufferedImage image;

    BasicTouchTime touchTime;

    String filePath;


    public IconImage(BufferedImage img) {
        image = img;
        touchTime = new BasicTouchTime();
    }



    public BasicTouchTime getTouchTime() {
        return touchTime;
    }


    public void savePNG(File f) {
        ImageUtil.writePNG(image, f);
    }



    public void cacheAsFile(int i) {
        filePath = "/tmp/icon-" + i + ".png";
        File f = new File(filePath);
        savePNG(f);
    }

    public String getFilePath() {
        return filePath;
    }

}
