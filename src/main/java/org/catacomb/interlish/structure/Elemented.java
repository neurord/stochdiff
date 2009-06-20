
package org.catacomb.interlish.structure;


import java.util.List;


public interface Elemented {

    List<Element> getElements();

    boolean hasElements();

    Element[] getElementArray();

    Element getElement(String s);
}
