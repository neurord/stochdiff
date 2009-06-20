package org.catacomb.interlish.structure;

import org.catacomb.interlish.content.BooleanValue;
import org.catacomb.interlish.content.DoubleValue;
import org.catacomb.interlish.report.RunException;



public interface RunProcess extends IDd, Tagged, Infod, Progressed, StateProcess, Labelable {


    void resume() throws RunException;

    void start() throws RunException;

    void pause() throws RunException;

    void reset() throws RunException;

    void reportProgress();

    void setProcessWatcher(ProcessWatcher pw);

    Object getResultData();

    DoubleValue getRuntime();

    DoubleValue getTimestep();

    BooleanValue getAutoRerun();



}
