package org.catacomb.datalish;

import java.util.ArrayList;

import org.catacomb.be.Event;

public class SpriteEvents {


    ArrayList<Event> events;

    public SpriteEvents(ArrayList<Event> evs) {
        events = evs;
    }


    public void perform() {
        if (events != null) {
            for (Event ev : events) {
                ev.perform();
            }
        }
    }

}
