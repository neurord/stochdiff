package org.catacomb.druid.xtext.data;


public class XRelation {


    XRelationType type;

    String aText;
    String bText;

    public XRelation(XRelationType xrt) {
        type = xrt;
    }
    public void setA(String atxt, Object asrc) {
        aText = atxt;

    }
    public void setB(String btxt, Object bsrc) {
        bText = btxt;
    }

    public XRelationType getRelationType() {
        return type;
    }


    public String getAText() {
        return aText;
    }

    public String getBText() {
        return bText;
    }

    public String getTypeID() {
        return type.getID();
    }

}
