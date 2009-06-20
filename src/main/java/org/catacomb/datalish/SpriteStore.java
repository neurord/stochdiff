package org.catacomb.datalish;

import java.util.HashMap;

import org.catacomb.report.E;


public class SpriteStore {

    HashMap<String, SpriteData> sprites;



    public SpriteStore() {
        sprites = new HashMap<String, SpriteData>();
    }


    public void add(SpriteData sd) {
        sprites.put(sd.getName(), sd);

    }


    public SpriteData getSprite(String sid) {
        SpriteData ret = null;
        if (sprites.containsKey(sid)) {
            ret = sprites.get(sid);
        } else {
            E.error("no such sprite " + sid);
        }
        return ret;
    }



}
