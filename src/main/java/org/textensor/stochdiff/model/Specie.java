package org.textensor.stochdiff.model;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import javax.xml.bind.annotation.*;

public class Specie {
    static final Logger log = LogManager.getLogger(Specie.class);

    @XmlAttribute private String name;
    @XmlAttribute private String id;
    @XmlAttribute private Double kdiff;
    @XmlAttribute private String kdiffunit;

    transient private int index;

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
        if (kdiff == null)
            return 0;
        return kdiff * getFactor(kdiffunit);
    }

    private double getFactor(String su) {
        // output units are microns^2/ms

        if (su == null || su.equals("mu2/s"))
            return 0.001;

        if (su.equals("m2/s"))
            return 1.e9;

        if (su.equals("cm2/s"))
            return 1.e5;

        if (su.equals("mu2/ms"))
            return 1.;

        log.error("Unknown units '{}'", su);
        throw new RuntimeException("Unknown units '" + su + "'");
    }
}
