package org.catacomb.interlish.structure;



public interface Supplier {


    boolean canSupply(String modality, String item);

    Object get(String modality, String item);

    void addDependent(Dependent dep);



}
