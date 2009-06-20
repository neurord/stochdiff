package org.catacomb.interlish.structure;


public interface DataWatcher {

    void dataStructureChanged(Object src, Object item);

    void dataValueChanged(Object src, Object item);

    void dataComplete(Object src);


}
