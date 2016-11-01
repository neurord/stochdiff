package neurord.model;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import javax.xml.bind.annotation.*;

import neurord.util.Settings;

public class Specie {
    static public final Logger log = LogManager.getLogger();

    @XmlAttribute private String name;
    @XmlAttribute private String id;
    @XmlAttribute private Double kdiff;
    @XmlAttribute private String kdiffunit;

    transient private int index;
    static final boolean diffusion = Settings.getProperty("neurord.diffusion",
                                                          "Allow diffusion to happen",
                                                          true);

    public String getID() {
        return id != null ? id : generateID(name);
    }

    public static String generateID(String name) {
        return name.replaceAll(" /\\\\", "_");
    }

    public String getName() {
        return name;
    }

    public void setIndex(int ict) {
        index = ict;
    }

    public int getIndex() {
        return index;
    }

    public double getDiffusionConstant() {
        if (!diffusion)
            return 0;
        if (this.kdiff == null)
            return 0;
        return this.kdiff * getFactor(this.kdiffunit);
    }

    private static double getFactor(String su) {
        // output units are microns^2/ms

        if (su == null || su.equals("µm²/s") || su.equals("mu2/s"))
            return 0.001;

        if (su.equals("m2/s"))
            return 1.e9;

        if (su.equals("cm²/s") || su.equals("cm2/s"))
            return 1.e5;

        if (su.equals("µm²/ms") || su.equals("mu2/ms"))
            return 1.;

        log.error("Unknown units '{}'", su);
        throw new RuntimeException("Unknown units '" + su + "'");
    }
}
