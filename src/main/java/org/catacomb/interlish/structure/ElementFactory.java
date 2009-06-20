package org.catacomb.interlish.structure;

public interface ElementFactory {

    // for elements that should ocntain all their context as attibutes (ie, the package)
    Element makeStandaloneElementFor(Object obj);


    // for elements within a known context - just get the class name and use it for the name
    Element makeElementFor(Object obj);


    Element makeElement(String name);

    Element makeElement(String name, String body);

    // in following the objects are whatever class is returned by the above two;
    void addAttribute(Element elt, String name, String value);


    void addAttribute(Element elt, String name, double value);


    void addElement(Element parent, Object child);



}
