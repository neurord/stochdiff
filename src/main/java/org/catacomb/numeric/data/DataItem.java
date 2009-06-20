package org.catacomb.numeric.data;

import org.catacomb.interlish.structure.Named;

public abstract class DataItem implements Named {

    String name;

    boolean marked;



    public DataItem(String nm) {
        name = nm;
        marked = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String s) {
        name = s;
    }

    public void mark() {
        marked = true;
    }


    public boolean isMarked() {
        return marked;
    }



    public abstract DataItem getMarked();




}
