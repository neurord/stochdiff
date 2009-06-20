package org.catacomb.interlish.structure;

import java.util.ArrayList;

public interface Assembly {

    public ArrayList<? extends Object> getComponents();

    public ArrayList<Socket> getSockets();

    public String getSizeUnit();

    public double getTypicalSize();

}
