package org.catacomb.interlish.structure;


public interface Constructor {

    Object newInstance(String cnm);

    Object getChildObject(Object parent, String name, Attribute[] atta);

    void applyAttributes(Object obj, Attribute[] atta);

    boolean setAttributeField(Object parent, String fieldName, String child);

    boolean setField(Object parent, String fieldName, Object child);

    Object getField(Object parent, String fieldName);

    void appendContent(Object child, String content);

    void setIntFromStatic(Object ret, String id, String sv);

}
