package org.catacomb.interlish.structure;



public interface PageSupplier extends Producer {


    Page getPage(String path);

    boolean canGet(String s);


}
