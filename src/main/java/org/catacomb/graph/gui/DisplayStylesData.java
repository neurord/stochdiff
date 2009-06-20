package org.catacomb.graph.gui;


import org.catacomb.interlish.content.ColorTable;
import org.catacomb.interlish.content.KeyedList;

import java.util.ArrayList;


public class DisplayStylesData {

    static DisplayStylesData instance;

    KeyedList<ColorTable> colorTables;

    KeyedList<DisplayStyleSet> styleSets;



    public static DisplayStylesData getData() {
        if (instance == null) {
            instance = new DisplayStylesData();
        }
        return instance;
    }



    public DisplayStylesData() {
        colorTables = new KeyedList<ColorTable>(ColorTable.class);
        styleSets = new KeyedList<DisplayStyleSet>(DisplayStyleSet.class);

        addColorTable(new ColorTable("default"));
    }


    public DisplayStyleSet getStyleSet(String s) {
        return styleSets.getOrMake(s);
    }




    public ArrayList<DisplayStyleSet> getStyleSets() {
        return styleSets.getItems();
    }


    public ArrayList<ColorTable> getColorTables() {
        return colorTables.getItems();

    }

    public ColorTable getFirstColorTable() {
        return colorTables.getFirst();
    }



    public ColorTable getSetColorTable(String s) {
        DisplayStyleSet dss = getStyleSet(s);
        ColorTable ct = dss.getColorTable();
        if (ct == null) {
            ct = getColorTable(dss.getColorTableName());
            dss.setColorTable(ct);
        }
        return ct;
    }


    public ColorTable getColorTable(String s) {
        return colorTables.getOrMake(s);
    }


    private void addColorTable(ColorTable ct) {
        colorTables.addItem(ct);
    }








}
