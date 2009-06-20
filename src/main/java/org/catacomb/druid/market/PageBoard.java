package org.catacomb.druid.market;

import org.catacomb.interlish.structure.Consumer;
import org.catacomb.interlish.structure.PageDisplay;
import org.catacomb.interlish.structure.PageSupplier;
import org.catacomb.interlish.structure.Producer;


public class PageBoard extends ProducerConsumerBoard {



    public void connect(Producer p, Consumer c) {
        ((PageDisplay)c).setPageSupplier((PageSupplier)p);
    }



}
