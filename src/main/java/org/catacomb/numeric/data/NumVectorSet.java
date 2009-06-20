package org.catacomb.numeric.data;

import java.util.ArrayList;


public interface NumVectorSet extends NumDataItem {


    public ArrayList<NumVector> getVectors();


    public ArrayList<NumVector> getByIndex(int[] ia);



}
