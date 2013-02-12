package org.catacomb.druid.market;

import org.catacomb.interlish.structure.*;
import org.catacomb.report.E;



public class LogBoard extends ProducerConsumerBoard {



    public void connect(Producer p, Consumer c) {

        if (p instanceof InfoLog && c instanceof LogDisplay) {
            ((LogDisplay)c).addInfoLog((InfoLog)p);


        } else {
            E.error("log board cannot connect " + p + " and " + c);
        }
    }



}
