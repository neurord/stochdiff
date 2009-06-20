package org.catacomb.numeric.difnet;



public interface Stimulus {


    int VALUE = 1;
    int FLUX = 2;


    double getValue(double t);

    int getType();


}
