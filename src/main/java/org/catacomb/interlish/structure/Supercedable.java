package org.catacomb.interlish.structure;


public interface Supercedable<V> {

    V getSupercessor();

    void setSupercessor(Object obj);

    boolean superceded();

}
