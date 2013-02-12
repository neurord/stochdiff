package org.catacomb.druid.market;

import org.catacomb.interlish.structure.*;
import org.catacomb.report.E;



public class LogMessageBoard extends ProducerConsumerBoard {



    public void connect(Producer p, Consumer c) {

        if (p instanceof LogMessageGenerator && c instanceof InfoLog) {
            ((LogMessageGenerator)p).setLog((InfoLog)c);

        } else {
            E.error("log board cannot connect " + p + " and " + c);
        }
    }



}
