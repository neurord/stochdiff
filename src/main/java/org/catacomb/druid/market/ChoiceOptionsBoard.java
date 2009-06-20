package org.catacomb.druid.market;

import org.catacomb.interlish.structure.Consumer;
import org.catacomb.interlish.structure.Producer;
import org.catacomb.report.E;



public class ChoiceOptionsBoard extends ProducerConsumerBoard {



    public void connect(Producer p, Consumer c) {
        E.info("time to connect choice options " + p + " " + c);
        //      ((InfoExporter)p).setInfoReceiver((InfoReceiver)c);
    }



}
