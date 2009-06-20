package org.catacomb.druid.market;

import org.catacomb.interlish.structure.Consumer;
import org.catacomb.interlish.structure.InfoExporter;
import org.catacomb.interlish.structure.InfoReceiver;
import org.catacomb.interlish.structure.Producer;


public class InfoBoard extends ProducerConsumerBoard {


    public void connect(Producer p, Consumer c) {
        ((InfoExporter)p).setInfoReceiver((InfoReceiver)c);
    }



}
