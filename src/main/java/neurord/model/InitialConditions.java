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

    public double[][] makeRegionConcentrations(String[] regions, String[] species) {
        ConcentrationSet defaults = this.getDefaultConcentrations();
        double[][] ret = new double[regions.length][species.length];
        for (int i = 0; i < regions.length; i++) {
            ConcentrationSet set = this.getConcentrationSets().get(regions[i]);

            for (int j = 0; j < species.length; j++) {
                Double val = null;
                if (set != null)
                    val = set.getNanoMolarConcentration(species[j]);
                if (val == null)
                    val = defaults.getNanoMolarConcentration(species[j]);
                if (val != null)
                    ret[i][j] = val;
            }
        }
        return ret;
    }

    transient private SurfaceDensitySet defaultSurfaceDensities;
    private synchronized SurfaceDensitySet getDefaultSurfaceDensities() {
        if (this.defaultSurfaceDensities == null) {
            if (this.sdSets != null)
                for (SurfaceDensitySet set: this.sdSets)
                    if (!set.hasRegion()) {
                        this.defaultSurfaceDensities = set;
                        break;
                    }
            if (this.defaultSurfaceDensities == null)
                this.defaultSurfaceDensities = new SurfaceDensitySet();
        }

        return this.defaultSurfaceDensities;
    }

    public double[][] makeRegionSurfaceDensities(String[] sra, String[] species) {
        SurfaceDensitySet defaults = this.getDefaultSurfaceDensities();
        double[][] ret = new double[sra.length][species.length];
        for (int i = 0; i < sra.length; i++) {
            SurfaceDensitySet set = this.getSurfaceDensitySets().get(sra[i]);

            for (int j = 0; j < species.length; j++) {
                Double val = null;
                if (set != null)
                    val = set.getSurfaceDensity(species[j]);
                if (val == null)
                    val = defaults.getSurfaceDensity(species[j]);

                /* NaN means "no value" */
                ret[i][j] = val != null ? val : Double.NaN;
            }
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

    public static <T extends Regional> HashMap<String, T> listToRegionMap(List<T> list) {
        HashMap<String, T> hm = new HashMap<>();

        if (list != null)
            for(T item: list)
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
