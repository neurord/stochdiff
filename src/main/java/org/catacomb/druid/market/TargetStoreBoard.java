package org.catacomb.druid.market;

import org.catacomb.interlish.structure.*;



public class TargetStoreBoard extends ProducerConsumerBoard {


    public void connectVisibleViewer(Visible vbl, Viewer vwr) {

        ((TargetStoreUser)vwr).setTargetStore((TargetStore)vbl);

    }



}
