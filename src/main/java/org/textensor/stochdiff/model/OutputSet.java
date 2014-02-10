//5 16 2007: written by RO; modified by WK

package org.textensor.stochdiff.model;

import java.util.ArrayList;

import javax.xml.bind.annotation.*;

public class OutputSet {
    @XmlAttribute public String filename;
    @XmlAttribute public String region;
    @XmlAttribute public Double dt;

    @XmlElement(name="OutputSpecie")
    public ArrayList<OutputSpecie> outputSpec;

    public int getNumberOfOutputSpecies() {
        return outputSpec.size();
    }

    public String[] getNamesOfOutputSpecies() {
        int ns = outputSpec.size();
        String[] ret = new String[ns];
        for (int i = 0; i < ns; i++)
            ret[i] = outputSpec.get(i).name;

        return ret;
    }

    public boolean hasRegion()  {
        return region != null;
    }

    public String getRegion() {
        return region;
    }

    public boolean hasFname() {
        return filename != null;
    }

    public String getFname() {
        return filename;
    }

    public boolean hasdt() {
        return dt != null;
    }

    public double getdt() {
        //        return dt != null ? dt : 0;
        return dt;
    }
}
