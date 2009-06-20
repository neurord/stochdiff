package org.catacomb.datalish;

import org.catacomb.be.Placement;


public class SceneConfig {


    double time;

    SpritePlacement[] placements;

    SpriteEvents events;

    int count;


    public SceneConfig(int nspr) {
        placements = new SpritePlacement[nspr];
        count = 0;
    }

    public void setTime(double t) {
        time = t;
    }

    public SceneConfig(double t, SpritePlacement[] spa, SpriteEvents evts) {
        time = t;
        placements = spa;
        events = evts;
    }


    public double getTime() {
        return time;
    }


    public void addPlacement(String sid, Placement pmt) {
        placements[count] = new SpritePlacement(sid, pmt);
        count += 1;
    }

    public SpritePlacement[] getPlacements() {
        return placements;
    }

    public void setEvents(SpriteEvents sevs) {
        events = sevs;

    }


    public SpriteEvents getEvents() {
        return events;
    }


}
