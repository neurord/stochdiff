package org.catacomb.graph.gui;

import org.catacomb.interlish.content.KeyedList;


public class DataViewStore {

    static DataViewStore instance;


    public static DataViewStore getStore() {
        if (instance == null) {
            instance = new DataViewStore();
        }
        return instance;
    }


    KeyedList<ViewSet> viewSets;



    public DataViewStore() {
        viewSets =new KeyedList<ViewSet>(ViewSet.class);
    }


    public ViewSet getViewSet(String id) {
        return viewSets.getOrMake(id);
    }




}
