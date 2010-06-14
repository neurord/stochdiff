package org.textensor.stochdiff.model;

import java.util.ArrayList;

import org.textensor.report.E;
import org.textensor.stochdiff.inter.AddableTo;

public class FitConstraints implements AddableTo {

    public ArrayList<PreserveTotal> preserveTotals;


    public void add(Object obj) {
        if (obj instanceof PreserveTotal) {
            if (preserveTotals == null) {
                preserveTotals = new ArrayList<PreserveTotal>();
            }
            preserveTotals.add((PreserveTotal)obj);
        } else {
            E.error("cant add: " + obj);
        }
    }


    public String[] getTotalPreserved() {
        String[] ret = new String[0];
        if (preserveTotals != null) {
            ret = new String[preserveTotals.size()];
            for (int i = 0; i < preserveTotals.size(); i++) {
                ret[i] = preserveTotals.get(i).specieID;
            }
        }
        return ret;
    }

}
