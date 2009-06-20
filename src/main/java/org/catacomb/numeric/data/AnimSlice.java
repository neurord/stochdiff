package org.catacomb.numeric.data;

import java.lang.reflect.Field;

import org.catacomb.datalish.*;
import org.catacomb.report.E;


// REFAC - if there is a specific block type for animations, don't
// need the reflection stuff


public class AnimSlice extends StackSingleSlice implements SpriteAnim {


    SceneConfig[] data;
    double[] times;

    int npcache;

    SpriteStore spriteStore;

    public AnimSlice(BlockStack bs, String fnm, Field f, String t,
                     SpriteStore ss) {
        super(bs, fnm, f, null, t);
        spriteStore = ss;
        npcache = 0;
        data = new SceneConfig[10];
        times = new double[10];
    }

    public SpriteStore getSpriteStore() {
        return spriteStore;
    }


    public SceneConfig getSceneConfig(int ip) {
        int np = blockStack.getSize();
        if (np > data.length) {
            int nn = np + np/2 + 10;
            SceneConfig[] dn = new SceneConfig[nn];
            for (int i = 0; i < npcache; i++) {
                dn[i] = data[i];
            }
            data = dn;
        }

        SceneConfig ret = null;
        if (ip < np) {
            ret = data[ip];
            if (ret == null) {
                try {
                    ret = (SceneConfig)(field.get(blockStack.getBlock(ip)));
                    data[ip] = ret;
                    if (npcache < ip) {
                        npcache = ip;
                    }
                } catch (Exception ex) {
                    E.error("exception reading slice from block stack " + this + " " + ex);
                }

            }
        }
        return ret;
    }




    void clearCache() {
        npcache = 0;
    }

    public Box getBox() {
        // REFAC - cold know sprite sizes (currently painter does)
        int np = blockStack.getSize();
        Box box = new Box();
        int ival = 1 + np / 20;
        for (int i = 0; i < np; i += ival) {
            for (SpritePlacement sp : getSceneConfig(i).getPlacements()) {
                box.push(sp.getPosition());
            }
        }
        box.enlarge(0.2);
        return box;
    }




    public String getFrameDescription(int ifr) {
        SceneConfig sc = getSceneConfig(ifr);
        return String.format("%8.3g", new Double(sc.getTime()));
    }

}
