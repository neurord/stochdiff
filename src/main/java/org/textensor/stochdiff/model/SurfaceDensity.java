package org.textensor.stochdiff.model;

import org.textensor.stochdiff.inter.FloatValued;


public abstract class SurfaceDensity implements FloatValued {

    public String specieID;

    public abstract double getPicoMoleSurfaceDensity();

    public abstract String makeXMLLine();


}
