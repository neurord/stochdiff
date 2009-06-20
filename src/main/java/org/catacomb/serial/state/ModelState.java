package org.catacomb.serial.state;


public class ModelState {


    public TypeArchive typeArchive;

    public MainType mainType;

    public MainState mainState;




    public void setTypeArchive(TypeArchive ta) {
        typeArchive = ta;
    }


    public void setMainType(MainType mt) {
        mainType = mt;
    }


    public void setMainState(MainState ms) {
        mainState = ms;
    }

}
