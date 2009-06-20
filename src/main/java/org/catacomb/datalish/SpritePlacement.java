package org.catacomb.datalish;

import org.catacomb.be.Placement;
import org.catacomb.be.Position;



public class SpritePlacement {

    String spriteID;
    Placement placement;


    public SpritePlacement(String sid, Placement pmt) {
        spriteID = sid;
        placement = pmt;
    }

    public Position getPosition() {
        return placement.getPosition();
    }

    public String getID() {
        return spriteID;
    }

    public Placement getPlacement() {
        return placement;
    }


}
