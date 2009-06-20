package org.catacomb.druid.market;

import org.catacomb.interlish.structure.Dependent;
import org.catacomb.interlish.structure.Supplier;

import java.util.ArrayList;
import java.util.HashMap;




public class SupplyClerk {


    String modality;

    HashMap<String, ArrayList<Dependent>> itemDependents;

    HashMap<String, Object> lastSupplied;


    public SupplyClerk(String s) {
        modality = s;

        itemDependents = new HashMap<String, ArrayList<Dependent>>();
        lastSupplied = new HashMap<String, Object>();

    }



    public void notifyAllIfChanged(Supplier supplier) {

        for (String item : itemDependents.keySet()) {

            Object value = supplier.get(modality, item);

            if (sameAsLast(item, value)) {

            } else {
                lastSupplied.put(item, value);
                sendTo(itemDependents.get(item), value);
            }

        }

    }


    private boolean sameAsLast(String s, Object val) {
        boolean ret = false;
        if (lastSupplied.containsKey(s)) {
            Object oval = lastSupplied.get(s);
            if (oval.equals(val)) {
                ret = true;
            }
        }

        return ret;
    }



    private void sendTo(ArrayList<Dependent> arl, Object value) {
        for (Dependent dep : arl) {
            dep.newValue(value);
        }
    }




    public void addDependent(Dependent dep) {
        String s = dep.getInterestedIn();

        if (itemDependents.containsKey(s)) {
            itemDependents.get(s).add(dep);

        } else {
            ArrayList<Dependent> arl = new ArrayList<Dependent>();
            arl.add(dep);
            itemDependents.put(s, arl);
        }
    }




}
