package org.catacomb.druid.market;

import org.catacomb.interlish.structure.*;


public class StatusBoard extends ProducerConsumerBoard {


    public void connect(Producer p, Consumer c) {
        ((StatusSource)p).setStatusDisplay((StatusDisplay)c);
    }



}
