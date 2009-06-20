package org.catacomb.druid.market;

import org.catacomb.interlish.structure.*;



public class FocusBoard extends ProducerConsumerBoard {


    public void connectVisibleViewer(Visible vbl, Viewer vwr) {

        ((FocusSource)vbl).addFocusListener((FocusListener)vwr);


    }



}
