package org.catacomb.druid.util;


import org.catacomb.interlish.structure.RunProcess;


public interface TaskProgressDisplay {


    void setRunTasks(RunProcess[] rta);

    void updateDisplay();

}
