package org.catacomb.interlish.content;

import java.util.ArrayList;

import org.catacomb.interlish.structure.StructureWatcher;
import org.catacomb.report.E;

public class QuantifiedListValue extends PrimitiveValue {


    private NVPair[] items;


    private ArrayList<StructureWatcher> p_structureWatchers;


    public QuantifiedListValue() {
        super();
        items = new NVPair[0];
    }


    public QuantifiedListValue(NVPair[] aa) {
        super();
        items = aa;
    }


    public String toString() {
        return "QuantifiedListValue: " + items.length + " items ";
        // return String.format("%.3g", new Double(dble));
    }


    public void silentSetQuantity(String s, double d) {
        if (s == null) {
            return;
        }

        boolean done = false;
        for (NVPair nvp : items) {
            if (nvp.isCalled(s)) {
                nvp.setValue(d, sDouble(d));
                done = true;
                break;
            }
        }

        if (done) {
            // OK;
        } else {
            NVPair[] newitems = new NVPair[items.length + 1];
            for (int i = 0; i < items.length; i++) {
                newitems[i] = items[i];
            }
            items = newitems;
            items[items.length-1] = new NVPair(s, d, sDouble(d));
            reportStructureChange("added");
        }
        logChange();
    }


    public int indexOf(String s) {
        int ipos = -1;
        for (int iit = 0; iit < items.length; iit++) {
            if (items[iit].isCalled(s)) {
                ipos = iit;
                break;
            }

        }
        return ipos;
    }

    public void removeItem(String s) {
        int ipos = indexOf(s);
        if (ipos >= 0) {
            removeItem(ipos);
        }
    }


    private void removeItem(int itg) {
        NVPair[] newitems = new NVPair[items.length-1];
        for (int i = 0; i < itg; i++) {
            newitems[i] = items[i];
        }
        for (int i = itg; i < items.length-1; i++) {
            newitems[i] = items[i+1];
        }
        items = newitems;
        reportStructureChange("removed");
    }


    public void reportableSetQuantity(String s, double d, Object src) {
        silentSetQuantity(s, d);
        //  E.info("ql val reportin vc ");
        reportValueChange(src);
    }


    public int size() {
        return items.length;
    }


    private String sDouble(double d) {
        return String.format("%.3g", new Double(d));
    }

    public String[] getItemNames() {

        String[] sa = new String[items.length];
        for (int i = 0; i < items.length; i++) {
            sa[i] = items[i].getName();
        }
        return sa;
    }


    public NVPair[] getItems() {
        return items;
    }

    public NVPair[] getNameValueItems() {
        return items;
    }



    public double getIthDouble(int idx) {
        return items[idx].getValue();
    }


    public void setIthDouble(int idx, double dv) {
        items[idx].setValue(dv, sDouble(dv));
        E.info("qlv reporting set val " + dv);
        reportValueChange(this);
    }



    public void addStructureWatcher(StructureWatcher sw) {
        if (p_structureWatchers == null) {
            p_structureWatchers = new ArrayList<StructureWatcher>();
        }
        p_structureWatchers.add(sw);
    }

    public void removeStructureWatcher(StructureWatcher sw) {
        p_structureWatchers.remove(sw);
    }

    public void reportStructureChange(String typ) {
        if (p_structureWatchers != null) {
            for (StructureWatcher sw : p_structureWatchers) {
                sw.structureChanged(this, typ);
            }
        }
    }


}
