package org.catacomb.graph.drawing;


import org.catacomb.graph.gui.Painter;
import org.catacomb.graph.gui.WorldTransform;

import java.util.HashMap;


import java.awt.image.BufferedImage;



public class IconImageCache {

    static IconImageCache cache;


    public static IconImageCache getCache() {
        if (cache == null) {
            cache = new IconImageCache();
        }
        return cache;
    }

    HashMap<VectorIcon, IconImage> image32x32HM;
    HashMap<VectorIcon, IconImage> image48x48HM;


    public IconImageCache() {
        image32x32HM = new HashMap<VectorIcon, IconImage>();
        image48x48HM = new HashMap<VectorIcon, IconImage>();
    }



    public IconImage getImage32x32(VectorIcon vi) {
        return getImage(image32x32HM, vi, 32, 32);
    }


    public IconImage getImage48x48(VectorIcon vi) {
        return getImage(image48x48HM, vi, 48, 48);
    }


    private IconImage getImage(HashMap<VectorIcon, IconImage> hm, VectorIcon vi, int nx, int ny) {
        IconImage ret = null;
        if (hm.containsKey(vi)) {
            IconImage iim = hm.get(vi);
            if (iim.getTouchTime().isBefore(vi.getTouchTime())) {
                ret = iim;
            }
        }
        if (ret == null) {
            ret = makeImage(vi, nx, ny);

            ret.cacheAsFile(vi.hashCode());

            image32x32HM.put(vi, ret);
        }
        return ret;
    }


    public IconImage makeImage(VectorIcon vi, int nx, int ny) {

        WorldTransform wt = new WorldTransform();
        wt.setXRange(-1.2, 1.2);
        wt.setYRange(-1.2, 1.2);
        Painter painter = new Painter(wt);

        BufferedImage transim = new BufferedImage(nx, ny, BufferedImage.TYPE_INT_ARGB);

        wt.setWidth(nx);
        wt.setHeight(ny);
        wt.setXRange(-1.2, 1.2);
        wt.setYRange(-1.2, 1.2);

        DrawingPainter dp = new DrawingPainter();
        painter.setGraphics(transim.createGraphics());
        dp.instruct(painter, vi, 0., 0., 1.);


        return new IconImage(transim);
    }



}
