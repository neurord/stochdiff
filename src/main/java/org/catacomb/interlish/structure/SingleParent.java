package org.catacomb.interlish.structure;


import java.util.ArrayList;


public interface SingleParent extends Related {


    ArrayList<? extends Object> getChildren();

}
