package org.textensor.stochdiff.model;

import org.textensor.stochdiff.phys.Phys;


public class NumberDensity extends Concentration {

    public double number;      // per cubic micron;





    public double getNanoMolarConcentration() {
        // volume is in cubic microns
        // 10^15 cubic microns in a litre

        double fvol = 1.e15 * 1.e9 / Phys.AVAGADRO;
        // (about 1.6)

        double nanomolarity = fvol * number;



        return nanomolarity;

    }



    public String makeXMLLine() {
        return "<NumberDensity specieID=\"" + specieID + "\" number=\"" + number + "\"/>";

    }


    public double getValue() {
        return number;
    }


    public void setValue(double d) {
        number = d;
    }

}
