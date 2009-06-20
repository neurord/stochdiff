package org.catacomb.druid.market;

import org.catacomb.interlish.structure.*;
import org.catacomb.report.E;



public class TreeBoard extends ProducerConsumerBoard {



    public void connect(Producer p, Consumer c) {
        if (c instanceof TreeExplorer) {

            if (p instanceof TreeProvider) {
                ((TreeExplorer)c).setTreeProvider((TreeProvider)p);

            } else if (p instanceof Tree) {
                ((TreeExplorer)c).setTree((Tree)p);

            } else {
                E.error("cant set tree - not a TreeProvider " + p);
            }
        } else {
            E.error("cant set tree - not a TreeExplorer " + c);
        }

    }


}
