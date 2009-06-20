package org.catacomb.serial.state;

import java.util.ArrayList;


public class TypeArchive {


    public ArrayList<TypeClass> typeClasses;



    public TypeArchive() {
        typeClasses = new ArrayList<TypeClass>();
    }


    public void addTypeClass(TypeClass tc) {
        typeClasses.add(tc);
    }

}
