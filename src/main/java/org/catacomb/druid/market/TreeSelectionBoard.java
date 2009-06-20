package org.catacomb.druid.market;

import org.catacomb.interlish.structure.SelectionListener;
import org.catacomb.interlish.structure.SelectionSource;
import org.catacomb.interlish.structure.Viewer;
import org.catacomb.interlish.structure.Visible;



public class TreeSelectionBoard extends ProducerConsumerBoard {


    public void connectVisibleViewer(Visible vbl, Viewer vwr) {
        ((SelectionListener)vwr).setSelectionSource((SelectionSource)vbl);
    }



}
