package org.catacomb.dataview.formats;

import java.io.BufferedReader;


public interface DataReader {

    public boolean canRead(String lineType);

    public void readBlock(String line, BufferedReader br);

}
