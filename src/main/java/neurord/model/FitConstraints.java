package neurord.model;

import java.util.ArrayList;

import javax.xml.bind.annotation.*;

public class FitConstraints {

    @XmlElement(name="PreserveTotal")
    public ArrayList<PreserveTotal> preserveTotals;

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
