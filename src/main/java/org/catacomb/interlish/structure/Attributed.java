
package org.catacomb.interlish.structure;


import java.util.List;


public interface Attributed {


    List<Attribute> getAttributes();

    Attribute[] getAttributeArray();

    String getAttribute(String name);

    boolean hasAttributes();

    boolean hasAttribute(String name);

}
