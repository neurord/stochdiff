package org.catacomb.interlish.structure;



// not sure if specifiers should have to be declared, but it is easy -
// when the thing they specify needs to get its own spec

public interface Specifier extends IDd { //  extends Declared {


    Object make(String type);

    String getSpecifiedTypeName(Specified spcd);

}
