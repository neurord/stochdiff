package neurord.disc;

import java.util.HashMap;

import neurord.numeric.morph.TreePoint;

public class Resolution {

    final double defaultDelta;
    final HashMap<String, Double> deltaHM;

    public Resolution(double defaultDelta, HashMap<String, Double> deltaHM) {
        this.defaultDelta = defaultDelta;
        this.deltaHM = deltaHM;
    }

    public double getLocalDelta(TreePoint cpa, TreePoint cpb) {
        String id = cpa.segmentIDWith(cpb);
        if (id != null && this.deltaHM != null && this.deltaHM.containsKey(id))
            return this.deltaHM.get(id);

        String region = cpa.regionClassWith(cpb);
        if (region != null && this.deltaHM != null && this.deltaHM.containsKey(region))
            return this.deltaHM.get(region);

        return this.defaultDelta;
    }
}
