package org.catacomb.datalish;

import org.catacomb.be.Position;


public class SpriteMarker {

    Position position;
    String id;

    public SpriteMarker(String sid, Position pos) {
        position = new Position(pos);
        id = sid;
    }

    public Position getPosition() {
        return position;
    }

}
