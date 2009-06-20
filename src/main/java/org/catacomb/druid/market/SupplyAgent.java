
package org.catacomb.druid.market;

import org.catacomb.interlish.structure.Dependent;
import org.catacomb.interlish.structure.Supplier;

import java.util.HashMap;




public class SupplyAgent {


    HashMap<String, SupplyClerk> clerks;


    public SupplyAgent() {
        clerks = new HashMap<String, SupplyClerk>();
    }


    public void addDependent(Dependent dep, Supplier supplier) {
        SupplyClerk sc = getSupplyClerk(dep.getModality());
        sc.addDependent(dep);

        sc.notifyAllIfChanged(supplier);
    }



    private SupplyClerk getSupplyClerk(String modality) {
        SupplyClerk ret = null;

        if (clerks.containsKey(modality)) {
            ret = clerks.get(modality);

        } else {
            ret = new SupplyClerk(modality);
            clerks.put(modality, ret);
        }
        return ret;
    }



    public void notifyAllIfChanged(Supplier supplier) {
        for (SupplyClerk sc : clerks.values()) {
            sc.notifyAllIfChanged(supplier);
        }
    }



}
