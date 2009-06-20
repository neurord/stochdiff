package org.catacomb.druid.build;

import org.catacomb.interlish.structure.IDd;


public interface GroupContingent extends IDd {


    void enable();

    void disable();

    void select();

    void deselect();

}
