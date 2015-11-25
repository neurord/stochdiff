package neurord.model;

import javax.xml.bind.annotation.*;

import neurord.inter.FloatValued;

public abstract class Concentration implements FloatValued {

    @XmlAttribute public String specieID;

    public abstract double getNanoMolarConcentration();
}
