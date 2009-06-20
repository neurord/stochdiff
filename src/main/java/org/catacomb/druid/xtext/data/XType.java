package org.catacomb.druid.xtext.data;


import org.catacomb.druid.xtext.base.DMItem;
import org.catacomb.druid.xtext.base.Guise;
import org.catacomb.interlish.structure.TextDisplayed;


import java.util.ArrayList;


public class XType extends DMItem implements TextDisplayed {

    String id;

    public ArrayList<XType> fields;

    boolean expanded;

    Guise guise;

    String[] fieldNames;


    public XType(String s) {
        super();
        id = s;
        fields = new ArrayList<XType>();
        guise = new Guise();
        guise.setBoldFont();
        guise.setNextPaletteColor();

    }

    public String getID() {
        return id;
    }



    public void addCopyOfType(XType xt) {
        fields.add(xt.makeCopy());
    }

    private XType makeCopy() {
        return new XType(id);
    }

    public void addType(String s) {
        fields.add(new XType(s));
        fieldNames = null;
    }

    public void expand() {
        expanded = true;
    }

    public void collapse() {
        expanded = false;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public boolean hasFields() {
        return (fields.size() > 0);
    }

    public ArrayList<XType> getSubtypes() {
        return fields;
    }

    public Guise getGuise() {
        return guise;
    }

    public String[] getFieldNames() {
        if (fieldNames == null) {
            fieldNames = new String[fields.size()];
            for (int i = 0; i < fieldNames.length; i++) {
                fieldNames[i] = fields.get(i).getID();
            }
        }
        return fieldNames;
    }

    public ArrayList<XType> getFields() {
        return fields;
    }

    public void addProperty(String sid) {
        addType(sid);
    }

    public String getDisplayText() {
        return id;
    }

}
