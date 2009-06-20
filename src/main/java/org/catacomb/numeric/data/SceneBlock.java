package org.catacomb.numeric.data;

import org.catacomb.be.Placement;
import org.catacomb.datalish.RunDataBlock;
import org.catacomb.datalish.SceneConfig;
import org.catacomb.datalish.SpriteEvents;



public class SceneBlock implements RunDataBlock, Timestampable {


    //   @Quantity(title="time", unit="ms")
    //   public double time;

    @MultiSprites(title="positions")
    public SceneConfig scene;



    public SceneBlock(int nspr) {
        scene = new SceneConfig(nspr);
    }

    public void stampTime(double d) {
        scene.setTime(d);
    }



    public void addPlacement(String sid, Placement pmt) {
        scene.addPlacement(sid, pmt);
    }

    public void setEvents(SpriteEvents sevs) {
        scene.setEvents(sevs);
    }


}
