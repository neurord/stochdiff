package org.catacomb.druid.xtext.data;



import org.catacomb.druid.xtext.base.DMItem;
import org.catacomb.druid.xtext.base.Guise;
import org.catacomb.report.E;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;


public class XTypeStore extends DMItem {


    static XTypeStore p_instance;

    ArrayList<XType> types;

    HashMap<String, XType> typeHM;


    ArrayList<XRelationType> relations;
    HashMap<String, XRelationType> relationHM;


    XType typeType;
    XType propertyType;
    XType relType;
    XType thingType;


    public XTypeStore() {
        types = new ArrayList<XType>();
        typeHM = new HashMap<String, XType>();

        relations = new ArrayList<XRelationType>();
        relationHM = new HashMap<String, XRelationType>();
    }


    public static XTypeStore instance() {
        if (p_instance == null) {
            p_instance = new XTypeStore();
        }
        return p_instance;
    }


    public void addType(String s) {
        if (typeHM.containsKey(s)) {
            E.error("already have a type " + s);
        } else {
            XType xt = new XType(s);
            addType(xt);
        }
    }

    private void addType(XType xt) {
        types.add(xt);
        typeHM.put(xt.getID(), xt);
        xt.setParent(this);

        notifyAppearanceChange();
    }

    public ArrayList<XType> getTypes() {
        return types;
    }


    public XType getType(String ntn) {
        return typeHM.get(ntn);
    }

    public Guise getGuise(String ntn) {
        XType typ = getType(ntn);
        return typ.getGuise();
    }


    public void remove(XType typ) {
        types.remove(typ);
        typeHM.remove(typ.getID());

        notifyAppearanceChange();
    }


    public void remove(XRelationType typ) {
        relations.remove(typ);
        relationHM.remove(typ.getID());

        notifyAppearanceChange();
    }



    public void addRelationType(XRelationType xrt) {
        if (relationHM.containsKey(xrt.getID())) {
            E.error("already have a relation " + xrt.getID());
        } else {
            relations.add(xrt);
            relationHM.put(xrt.getID(), xrt);
        }
        notifyAppearanceChange();
    }









    public XType getTypeType() {
        if (typeType == null) {
            typeType = new XType("type");
            typeType.getGuise().setFontStyle("italic");
            typeType.getGuise().setColorBlack();
            typeType.getGuise().setUnderline(true);
        }
        return typeType;
    }


    public XType getPropertyType() {
        if (propertyType == null) {
            propertyType = new XType("property");
            propertyType.getGuise().setFontStyle("bold");
            propertyType.getGuise().setColorDarkGreen();
        }
        return propertyType;
    }


    public XType getRelType() {
        if (relType == null) {
            relType = new XType("rel");
            relType.getGuise().setFontStyle("bold");
            relType.getGuise().setFontColor(new Color(120, 20, 20));
            relType.getGuise().setUnderline(true);
        }
        return relType;
    }


    public XType getThingType() {
        if (thingType == null) {
            thingType = new XType("rel");
            thingType.getGuise().setFontStyle("bold");
            thingType.getGuise().setFontColor(new Color(80, 80, 80));
        }
        return thingType;

    }


    public ArrayList<XRelationType> getRelationTypes() {
        return relations;
    }


    public void addIfNew(XType xt) {
        if (typeHM.containsKey(xt.getID())) {
            // ignore;
        } else {
            addType(xt);
        }
    }


    public void addIfNew(XRelationType xrt) {
        if (relationHM.containsKey(xrt.getID())) {
            // ignore;
        } else {
            addRelationType(xrt);
        }

    }



}
