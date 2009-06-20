package org.catacomb.serial.om;

import org.catacomb.interlish.structure.Attribute;
import org.catacomb.interlish.structure.Element;
import org.catacomb.serial.ElementSerializer;


import java.util.ArrayList;
import java.util.List;


public class OmElement implements Element {

    String name;

    String body;

    List<Attribute> attributes;
    List<Element> elements;


    private Attribute p_bufAtt;

    public OmElement() {

    }

    public OmElement(String s) {
        name = s;
    }


    public String toString() {
        String sret = "OmElemnt:" + name;

        if (attributes != null) {
            for (Attribute oma : attributes) {
                sret += " " + oma.getName() + "=" + oma.getValue();
            }
        }

        int nel = (elements != null ? elements.size() : 0);
        sret += " [" + nel + " elements]";
        sret += " body=" + body;
        return sret;
    }

    public void addToBody(String s) {
        if (body == null) {
            body = s;
        } else {
            body += " " + s;
        }
    }


    public OmElement(ArrayList<Attribute> ala, ArrayList<Element> ale) {
        name = null;
        attributes = ala;
        elements = ale;
        body = null;
    }


    public void setName(String s) {
        name = s;
    }


    public String getName() {
        return name;
    }


    public void setBody(String s) {
        body = s;
    }

    public String getBody() {
        return body;
    }

    public boolean hasBody() {
        return (body != null);
    }

    public boolean hasText() {
        return (body != null);
    }

    public String getText() {
        return body;
    }


    public void copyAttributes(Attribute[] atta) {
        if (atta != null) {
            for (int i = 0; i < atta.length; i++) {
                Attribute att = atta[i];
                addAttribute(att.getName(), att.getValue());
            }
        }
    }



    public Attribute[] getAttributeArray() {
        Attribute[] ret = null;
        if (attributes != null) {
            ret = new Attribute[attributes.size()];
            int na = 0;

            for (Attribute att : attributes) {
                ret[na++] = att;
            }
        }
        return ret;
    }



    public boolean hasAttribute(String nm) {
        return (getAttribute(nm) != null);
    }


    public void setAttribute(String nm, String val) {
        OmAttribute oma = getOmAttribute(nm);
        oma.setValue(val);
    }


    public OmAttribute getOmAttribute(String nm) {
        OmAttribute oma = null;
        if (attributes != null) {
            for (Attribute att : attributes) {
                if (att.getName().equals(nm)) {
                    oma = (OmAttribute)att;
                    break;
                }
            }
        }
        return oma;
    }


    // EFF inefficient;
    public String getAttribute(String nm) {
        String ret = null;


        if (p_bufAtt != null && p_bufAtt.getName().equals(nm)) {
            ret = p_bufAtt.getValue();

        } else {
            p_bufAtt = getOmAttribute(nm);
            if (p_bufAtt != null) {
                ret = p_bufAtt.getValue();
            }

        }

        return ret;
    }


    public boolean hasAttributes() {
        return (attributes != null && attributes.size() > 0);
    }

    public boolean hasElements() {
        return (elements != null && elements.size() > 0);
    }



    public Element[] getElementArray() {
        Element[] ret = null;
        if (elements != null) {
            ret = new Element[elements.size()];
            int na = 0;

            for (Element elt : elements) {
                ret[na++] = elt;
            }
        }
        return ret;
    }


    public Element getElement(String s) {
        Element ret = null;
        if (elements != null) {

            for (Element elt : elements) {
                if (elt.getName().equals(s)) {
                    ret = elt;
                    break;
                }
            }
        }
        return ret;
    }



    public void addElement(Element elt) {
        if (elements == null) {
            elements = new ArrayList<Element>();
        }
        elements.add(elt);
    }


    public void addAttribute(String n, String v) {
        Attribute att = new OmAttribute(n, v);
        addAttribute(att);
    }


    public void addAttribute(Attribute att) {
        if (attributes == null) {
            attributes = new ArrayList<Attribute>();
        }
        attributes.add(att);
    }




    public void setAttributes(ArrayList<Attribute> ala) {
        if (attributes != null && attributes.size() > 0) {
            System.out.println("sand.state.element error - overwriting " +
                               "non empty att array ");
            (new Exception()).printStackTrace();
        }
        attributes = ala;
    }





    public void setElements(ArrayList<Element> ale) {
        if (elements != null && elements.size() > 0) {
            System.out.println("sand.state.element error - overwriting " +
                               "non empty elt array ");
            (new Exception()).printStackTrace();
        }

        elements = ale;
    }




    public List<Attribute> getAttributes() {
        return attributes;
    }

    public List<Element> getElements() {
        return elements;
    }

    public String serialize() {
        return ElementSerializer.serializeContent(this);
    }



}


