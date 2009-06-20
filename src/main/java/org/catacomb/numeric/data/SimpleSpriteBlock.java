package org.catacomb.numeric.data;

import org.catacomb.be.Placement;
import org.catacomb.datalish.RunDataBlock;
import org.catacomb.numeric.data.Quantity;
import org.catacomb.numeric.data.SpriteState;



public class SimpleSpriteBlock implements RunDataBlock {


    public XYVectorScene wall;

    @Quantity(title="time", unit="ms")
    public double time;


    @SpriteState(title="position", sceneName="wall", spriteName="")
    public Placement position;



    public SimpleSpriteBlock(double t, double[] pxy, double hxy[],
                             double[] wx, double[] wy) {
        time = t;
        position = new Placement(pxy, hxy, t);

        if (wx != null) {
            wall = new XYVectorScene(wx, wy, 0);
        }
    }

}
