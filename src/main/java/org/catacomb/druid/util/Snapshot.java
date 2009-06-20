package org.catacomb.druid.util;

import java.awt.image.BufferedImage;
import java.io.File;

import org.catacomb.util.AWTUtil;
import org.catacomb.util.ImageUtil;



public class Snapshot {




    public void saveSnapshot(Object obj) {
        BufferedImage img = AWTUtil.getBufferedImage(obj);

        File fw = FileChooser.getChooser().getFileToWrite("snapshot");
        if (img != null) {
            if (fw != null) {
                ImageUtil.writePNG(img, fw);
            }
        }
    }





}
