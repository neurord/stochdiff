//5 16 2007: written by RO; modified by WK

package org.textensor.stochdiff.model;

import java.util.ArrayList;

import org.textensor.report.E;
import org.textensor.stochdiff.inter.AddableTo;

public class OutputSet implements AddableTo {
    public String filename;
    public String region;
    public String dt;
    public ArrayList<OutputSpecie> outputSpec;

    public void add(Object obj) {
        if (outputSpec == null) {
            outputSpec = new ArrayList<OutputSpecie>();
        }

        if (obj instanceof OutputSpecie) {
            outputSpec.add((OutputSpecie)obj);

        } else {
            E.error("cant add " + obj);
        }
    }


    public int getNumberOfOutputSpecies() {
        return outputSpec.size();
    }


    public String[] getNamesOfOutputSpecies() {
        int ns = outputSpec.size();
        String[] ret = new String[ns];
        for (int i = 0; i < ns; i++) {
            ret[i] = outputSpec.get(i).name;

        }
        return ret;
    }

    public boolean hasRegion()  {
        return (region != null);
    }

    public String getRegion() {
        return region;
    }

    public boolean hasFname() {
        return (filename != null);
    }

    public String getFname() {
        return filename;
    }

    public boolean hasdt() {
        return (dt != null);
    }

    public double getdt() {
        return Double.parseDouble(dt);
    }

    //<--WK
    public void print()	{
        System.out.println("<OutputSet>");
        System.out.print(" filename: " + filename + " |region: ");
        if (hasRegion()) {
            System.out.print(region);
        } else {
            System.out.print("Not-Specified");
        }
        System.out.print(" |dt: ");
        if (hasdt()) {
            System.out.println(dt);
        } else {
            System.out.println(" default");
        }
        System.out.println("OutputSpecies: ");
        for (int i = 0; i < outputSpec.size(); i++) {
            System.out.print((outputSpec.get(i)).name + " ");
        }
        System.out.println();
    }
    //WK-->
}
