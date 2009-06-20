package org.catacomb.datalish;

import org.catacomb.be.Position;
import org.catacomb.datalish.SColor;

import java.util.ArrayList;

public class SpriteData {

    String name;

    ArrayList<SpritePart> parts;
    ArrayList<SpriteMarker> markers;



    public SpriteData(String s) {
        name = s;
        parts = new ArrayList<SpritePart>();
        markers = new ArrayList<SpriteMarker>();
    }

    public String getName() {
        return name;
    }


    public void addMarker(String id, Position position) {
        markers.add(new SpriteMarker(id, position));

    }

    public void addSpritePart(double[] xpts, double[] ypts, double lineWidth,
                              SColor lineColor, SColor fillColor, int ocf) {
        parts.add(new SpritePart(xpts, ypts, lineWidth, lineColor, fillColor, ocf));

    }

    public void pushBox(Box b) {
        for (SpritePart sp : parts) {
            sp.pushBox(b);
        }
    }

    public ArrayList<SpritePart> getParts() {
        return parts;
    }

    public ArrayList<SpriteMarker> getMarkers() {
        return markers;
    }


}
