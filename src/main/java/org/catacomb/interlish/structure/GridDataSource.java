
package org.catacomb.interlish.structure;


public interface GridDataSource {

    String getName();

    String[] getLineNames();

    int getNPoint();

    int getNLine();

    double[][] getLines();

    int getHighlightIndex();
}

