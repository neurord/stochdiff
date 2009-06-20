package org.catacomb.interlish.structure;


public interface ProcessWatcher {

    void progressed(RunProcess src);

    void stateChanged(RunProcess src);

}
