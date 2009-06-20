package org.catacomb.interlish.structure;

import org.catacomb.interlish.content.KeyedList;


public interface List extends Ablable {

    public void setKeyedList(KeyedList<? extends IDd> kl);

    public void setSelected(Object obj);

}
