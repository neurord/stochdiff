package org.catacomb.interlish.content;

import java.util.ArrayList;
import java.util.HashMap;


public class QuantityList<V> {

    ArrayList<V> items;
    HashMap<V, Double> quantityHM;



    public QuantityList() {
        items = new ArrayList<V>();
        quantityHM = new HashMap<V, Double>();
    }


    public void add(V v, double d) {
        if (quantityHM.containsKey(v)) {
            items.remove(v); // POSERR - flag as err?
        }

        items.add(v);
        quantityHM.put(v, new Double(d));
    }


    public ArrayList<V> getItems() {
        return items;
    }

    public double getQuantity(V v) {
        return quantityHM.get(v).doubleValue();
    }

    public double getQuantityNonGeneric(Object obj) {
        return quantityHM.get(obj).doubleValue();
    }

    public Object[] getObjectItemArray() {
        return items.toArray();
    }


    public double[] getValueArray() {
        double[] ret = new double[items.size()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = getQuantity(items.get(i));
        }
        return ret;
    }


    public int size() {
        return items.size();
    }

}
