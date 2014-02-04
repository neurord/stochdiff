package org.textensor.stochdiff.model;

import javax.xml.bind.annotation.*;

import org.textensor.stochdiff.inter.FloatValued;

public abstract class Concentration implements FloatValued {

    @XmlAttribute public String specieID;

    public abstract double getNanoMolarConcentration();

    public abstract String makeXMLLine();

}
