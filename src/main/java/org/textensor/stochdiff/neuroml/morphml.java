package org.textensor.stochdiff.neuroml;

import java.util.ArrayList;

import org.textensor.report.E;
import org.textensor.stochdiff.inter.Transitional;

public class morphml implements Transitional {

    public String id;

    // TODO read this all into an xmlns hash map;
    public String xmlns;
    public String xmlns_mml;
    public String xmlns_meta;
    public String xmlns_cml;
    public String xmlns_xsi;
    public String xsi_schemaLocation;

    public String name;
    public String lengthUnits;
    public String length_units;

    public ArrayList<cell> cells;

    public cell getMorphMLCell() {
        if (cells != null && cells.size() > 0) {
            Object obj = cells.get(0);
            try {
                if (obj instanceof Transitional) {
                    obj = ((Transitional)obj).getFinal();
                }
            } catch (Exception ex) {
                E.error("cannot convert from " + obj);
            }
            return (cell)obj;
        }
        return null;
    }

    public Object getFinal() {
        Object ret = null;
        if (cells.size() > 0) {
            ret = cells.get(0);

//			E.info("first item in cells is a " + obj);

            // obj not necessarily a MorphMLCell in fact, because of population by reflection
            if (ret instanceof Transitional) {

                ret = ((Transitional)ret).getFinal();

            }
        }
        return ret;
    }
}
