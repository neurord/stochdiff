package org.catacomb.numeric.data;

import org.catacomb.interlish.structure.*;
import org.catacomb.report.E;

import java.util.Collection;
import java.util.HashMap;




public class NumDataSet extends DataItem implements ElementWriter {


    HashMap<String, DataItem> itemHM;


    public NumDataSet() {
        this("error");

    }


    public NumDataSet(String s) {
        super(s);
        itemHM = new HashMap<String, DataItem>();
    }



    public Collection<String> getNames() {
        return itemHM.keySet();
    }

    public Collection<DataItem> getValues() {
        return itemHM.values();
    }


    public int size() {
        return itemHM.keySet().size();
    }

    public String toString() {
        return "NumDataSet:" + name;
    }



    public boolean matches(NumDataSet ds) {
        return name.equals(ds.getName());
    }






    public DataItem get(String sit) {
        DataItem ret = null;
        if (itemHM.containsKey(sit)) {
            ret = itemHM.get(sit);
        } else {
            E.error("Data Set no such item " + sit);
        }
        return ret;
    }





    public void add(Object obj) {
        if (obj instanceof Named && obj instanceof DataItem) {
            itemHM.put(((Named)obj).getName(), (DataItem)obj);
        } else {
            E.error("NumDataSet cannot add " + obj);
        }
    }


    public void addScalar(String lname, String value) {
        addScalar(new FloatScalar(lname, value));
    }

    public void addScalar(FloatScalar fs) {
        itemHM.put(fs.getName(), fs);
    }


    public void addVector(FloatVector fv) {
        itemHM.put(fv.getName(), fv);
    }


    public void addVectorSet(VectorSet vs) {
        FloatVector[] fva = vs.getVectors();
        for (int i = 0; i < fva.length; i++) {
            addVector(fva[i]);
        }
    }


    public void addVectorOrScalar(String nm, String stxt) {
        FloatVector fv = new FloatVector(nm, stxt);
        if (fv.length() > 1) {
            addVector(fv);
        } else {
            addScalar(new FloatScalar(fv));
        }
    }



    // if only one, just have it on its own, if more, have an array of them;
    public void addDataSet(NumDataSet ds) {
        String nm = ds.getName();
        if (itemHM.containsKey(nm)) {
            Object obj = itemHM.get(nm);

            if (obj instanceof DataSetArray) {
                ((DataSetArray)obj).add(ds);

            } else if (obj instanceof NumDataSet) {
                DataSetArray arl = new DataSetArray();
                arl.setName(nm);
                arl.add((NumDataSet)obj);
                arl.add(ds);
                itemHM.remove(nm);
                itemHM.put(nm, arl);
            } else {
                E.error("cannot add data set " + ds + " to " + this + " : mismatched types");
            }


        } else {
            itemHM.put(nm, ds);
        }
    }




    public Element makeElement(ElementFactory efac, Elementizer eltz) {
        Element elt = efac.makeElement(name != null ? name : "NumDataSet");

        for (String sk : itemHM.keySet()) {
            Object obj = itemHM.get(sk);

            if (obj instanceof NumDataSet) {
                efac.addElement(elt, ((NumDataSet)obj).makeElement(efac, eltz));

            } else if (obj instanceof FloatScalar) {
                FloatScalar fs = (FloatScalar)obj;
                efac.addAttribute(elt, fs.getName(), fs.getValue());

            } else if (obj instanceof FloatVector) {
                FloatVector fv = (FloatVector)obj;
                efac.addElement(elt, efac.makeElement(fv.getName(), fv.getStringValue()));

            } else if (obj instanceof DataSetArray) {
                DataSetArray dsa = (DataSetArray)obj;
                NumDataSet[] dsv = dsa.getDataSets();
                for (int i = 0; i < dsv.length; i++) {
                    efac.addElement(elt, dsv[i].makeElement(efac, eltz));
                }

            } else {
                E.error("Data set element making cannot handle " + obj);
            }
        }

        return elt;
    }



    public DataItem getMarked() {
        return copyMarked();
    }


    public NumDataSet copyMarked() {
        NumDataSet ret = new NumDataSet(getName());

        for (DataItem dit : itemHM.values()) {
            if (dit.isMarked()) {
                ret.add(dit.getMarked());
                //	    E.info("keeping " + dit.getName());

            } else {
                //	    E.info("not keeping " + dit.getName());
            }
        }
        return ret;
    }


}

