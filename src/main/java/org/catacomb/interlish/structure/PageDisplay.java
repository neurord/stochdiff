package org.catacomb.interlish.structure;


public interface PageDisplay extends Consumer {


    void setPageSupplier(PageSupplier ps);

    void showPage(Page p);

}
