package org.catacomb.druid.blocks;


import org.catacomb.interlish.structure.Element;
import org.catacomb.interlish.structure.ElementReader;


public class HTMLContent implements ElementReader {

    String content;

    public void populateFrom(Element elt) {
        content = elt.serialize(); // ElementSerializer.serializeContent(elt);
    }

    public String getText() {
        return content;
    }

    public String getContent() {
        return content;
    }
}
