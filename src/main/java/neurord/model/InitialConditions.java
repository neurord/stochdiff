package neurord.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.HashMap;

import javax.xml.bind.annotation.*;

import neurord.inter.FloatValued;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class InitialConditions {
    static final Logger log = LogManager.getLogger();

    @XmlElement(name="ConcentrationSet")
    public ArrayList<ConcentrationSet> concentrationSets;

    transient private HashMap<String, ConcentrationSet> concSetHM;

    @XmlElement(name="SurfaceDensitySet")
    public ArrayList<SurfaceDensitySet> sdSets;

    transient private HashMap<String, SurfaceDensitySet> sdSetHM;

    public FitConstraints fitConstraints;

    public synchronized HashMap<String, ConcentrationSet> getConcentrationSets() {
        if (this.concSetHM == null)
            this.concSetHM = listToRegionMap(this.concentrationSets);
        return this.concSetHM;
    }

    transient private ConcentrationSet defaultConcs;
    private synchronized ConcentrationSet getDefaultConcentrations() {
        if (this.defaultConcs == null) {
            if (this.concentrationSets != null)
                for (ConcentrationSet set: this.concentrationSets)
                    if (!set.hasRegion()) {
                        this.defaultConcs = set;
                        break;
                    }
            if (this.defaultConcs == null)
                this.defaultConcs = new ConcentrationSet();
        }

        return this.defaultConcs;
    }

    public void verify(String[] regions, String[] species) {
        this.getConcentrationSets();
        this.getSurfaceDensitySets();

        if (this.concentrationSets != null)
            for (ConcentrationSet set: this.concentrationSets)
                set.verify(regions, species);

        if (this.sdSets != null)
            for (SurfaceDensitySet set: this.sdSets)
                set.verify(regions, species);
    }

    public double[][] makeRegionConcentrations(String[] regions, String[] species) {
        double[][] ret = new double[regions.length][];
        for (int i = 0; i < regions.length; i++)
            ret[i] = getRegionConcentration(regions[i], species);

        return ret;
    }

    public double[] getRegionConcentration(String region, String[] species) {
        final double[] ans = new double[species.length];

        final ConcentrationSet set = this.getConcentrationSets().get(region);
        final ConcentrationSet def = this.getDefaultConcentrations();

        for (int i = 0; i < species.length; i++) {
            Double c = null;

            if (set != null)
                c = set.getNanoMolarConcentration(species[i]);
            if (c == null)
                c = def.getNanoMolarConcentration(species[i]);
            if (c != null)
                ans[i] = c;
        }

        return ans;
    }

    private SurfaceDensitySet getDefaultSurfaceDensities() {
        if (this.sdSets != null)
            for (SurfaceDensitySet set: this.sdSets)
                if (!set.hasRegion())
                    return set;
        return null;
    }

    public double[][] makeRegionSurfaceDensities(String[] regions, String[] species) {
        double[][] ret = new double[regions.length][];
        for (int i = 0; i < regions.length; i++)
            ret[i] = this.getRegionSurfaceDensity(regions[i], species);
        return ret;
    }

    public double[] getRegionSurfaceDensity(String region, String[] species) {
        double[] ret = new double[species.length];

        SurfaceDensitySet set = this.getSurfaceDensitySets().get(region);
        if (set == null)
            set = this.getDefaultSurfaceDensities();

        for (int j = 0; j < species.length; j++) {
            Double val = null;
            if (set != null)
                val = set.getSurfaceDensity(species[j]);
            /* NaN means "no value" */
            ret[j] = val != null ? val : Double.NaN;
        }
        return ret;
    }

    private synchronized HashMap<String, SurfaceDensitySet> getSurfaceDensitySets() {
        if (this.sdSetHM == null)
            this.sdSetHM = listToRegionMap(this.sdSets);
        return this.sdSetHM;
    }

    public ArrayList<FloatValued> getFloatValuedElements() {
        ArrayList<FloatValued> afv = new ArrayList<>();

        if (concentrationSets != null)
            for (ConcentrationSet cs : concentrationSets)
                cs.addFloatValued(afv);

        if (sdSets != null)
            for (SurfaceDensitySet sdSet : sdSets)
                sdSet.addFloatValued(afv);

        return afv;
    }

    public String[] getTotalPreserved() {
        if (this.fitConstraints != null)
            return this.fitConstraints.getTotalPreserved();
        else
            return new String[0];
    }

    private static <T extends Regional> HashMap<String, T> listToRegionMap(List<T> list) {
        final HashMap<String, T> hm = new HashMap<>();

        if (list != null)
            for (T item: list)
                if (item.hasRegion()) {
                    T old = hm.put(item.getRegion(), item);
                    if (old != null) {
                        log.error("Duplicate {} for region '{}'",
                                  item.getClass().getSimpleName(), item.getRegion());
                        throw new RuntimeException("Duplicate " + item.getClass());
                    }
                }

        return hm;
    }
}
