package org.catacomb.druid.xtext.data;

import org.catacomb.druid.xtext.base.DocStore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

public class PageDataStore {


    HashMap<XType, ArrayList<XItem>> itemHM;

    ArrayList<XRelation> relations;
    HashMap<XRelationType, ArrayList<XRelation>> relationHM;

    DocStore docStore;

    public PageDataStore(DocStore ds) {
        docStore = ds;
        itemHM = new HashMap<XType, ArrayList<XItem>>();
        relationHM = new HashMap<XRelationType, ArrayList<XRelation>>();
        relations = new ArrayList<XRelation>();
    }



    public void addItem(XType xt, String text) {
        XItem xit = new XItem(xt, text);
        addItem(xit);
    }



    public void addItem(XItem xit) {
        if (itemHM.containsKey(xit.getType())) {
            (itemHM.get(xit.getType())).add(xit);
        } else {
            ArrayList<XItem> al = new ArrayList<XItem>();
            al.add(xit);
            itemHM.put(xit.getType(), al);
        }

        reportChange();

    }


    public void reportChange() {
        docStore.pageDataChanged();
    }



    public Set<XType> getTypes() {
        return itemHM.keySet();
    }

    public ArrayList<XItem> getList(XType xt) {
        return itemHM.get(xt);
    }

    public Collection<ArrayList<XItem>> getLists() {
        return itemHM.values();
    }


    public void addRelation(XRelation xr) {
        relations.add(xr);
        if (relationHM.containsKey(xr.getRelationType())) {
            (relationHM.get(xr.getRelationType())).add(xr);
        } else {
            ArrayList<XRelation> al = new ArrayList<XRelation>();
            al.add(xr);
            relationHM.put(xr.getRelationType(), al);
        }
        reportChange();

    }


    public ArrayList<XRelation> getRelations() {
        return relations;
    }


    public boolean hasRelations() {
        return (relations.size() > 0);
    }

}
