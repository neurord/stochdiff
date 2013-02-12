package org.catacomb.numeric.data;

import org.catacomb.report.E;

import java.util.ArrayList;



public class DataSetArray extends DataItem {

    NumDataSet first;

    ArrayList<NumDataSet> items;

    NumDataSet[] bufdsa;



    public DataSetArray() {
        this("error");
    }


    public DataSetArray(String s) {
        super(s);
        items = new ArrayList<NumDataSet>();
    }



    public DataSetArray(String s, NumDataSet[] dsa) {
        super(s);
        items = new ArrayList<NumDataSet>();
        name = s;
        for (int i = 0; i < dsa.length; i++) {
            items.add(dsa[i]);
        }
    }



    public void add(NumDataSet ds) {
        if (first == null) {
            first = ds;

        } else {
            if (first.matches(ds)) {
                // OK;
            } else {
                E.error("mismatched sets in data set array  " + first + " " + ds);
            }
        }
        bufdsa = null;
        items.add(ds);
    }


    public int length() {
        return items.size();
    }


    public NumDataSet[] getDataSets() {
        if (bufdsa == null) {
            bufdsa = (items.toArray(new NumDataSet[0]));
        }
        return bufdsa;
    }

    public NumDataSet firstElement() {
        NumDataSet[] dsa = getDataSets();
        return dsa[0];
    }



    public DataSetArray slice(String cond) {
        DataSetArray ret = null;
        if (cond.equals("*")) {
            ret = this;

        } else {
            NumDataSet[] dsa = getDataSets();

            int index = 0;
            try {
                index = Integer.parseInt(cond);

            } catch (Exception ex) {
                E.error(" cannot slice dat aset array with " + cond + " " + ex);
            }
            ret = new DataSetArray();
            ret.add(dsa[index]);

        }
        return ret;
    }


    public DataItem getMarked() {
        DataSetArray ret = new DataSetArray(getName());
        NumDataSet[] sds = getDataSets();
        for (int i = 0; i < sds.length; i++) {
            if (sds[i].isMarked()) {
                ret.add(sds[i].copyMarked());
            }
        }
        return ret;
    }


}
