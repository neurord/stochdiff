package neurord.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.xml.bind.annotation.*;

import neurord.inter.FloatValued;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class SurfaceDensitySet implements Regional {
    static final Logger log = LogManager.getLogger();

    @XmlAttribute public String region;

    @XmlElement(name="PicoSD")
    public ArrayList<PicoSD> sds;

    transient HashMap<String, PicoSD> sdHM;

    private synchronized HashMap<String, PicoSD> getSurfaceDensities() {
        if (this.sdHM == null) {
            this.sdHM = new HashMap<>();
            if (sds != null)
                for (PicoSD sd: sds)
                    sdHM.put(sd.specieID, sd);
        }
        return sdHM;
    }

    public Double getSurfaceDensity(String species) {
        PicoSD sd = this.getSurfaceDensities().get(species);
        if (sd == null)
            return null;
        else
            return sd.getValue();
    }

    public boolean hasRegion() {
        return region != null;
    }

    public String getRegion() {
        return region;
    }

    public void addFloatValued(ArrayList<FloatValued> afv) {
        afv.addAll(sds);
    }

    public void verify(String[] regions, String[] species) {
        if (this.hasRegion() && !Arrays.asList(regions).contains(this.region)) {
            log.error("SurfaceDensitySet has region \"{}\", not in {}",
                      this.region, regions);
            throw new RuntimeException("SurfaceDensitySet with bad region: " + this.region);
        }
        for (PicoSD conc: this.sds)
            conc.verify(regions, species);
    }
}
