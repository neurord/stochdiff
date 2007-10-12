//5 16 2007: written by RO; modified by WK

package org.textensor.stochdiff.model;

import java.util.ArrayList;

import org.textensor.report.E;
import org.textensor.stochdiff.inter.AddableTo;

public class OutputSet implements AddableTo
{
    public String filename;
    public String region;
    public String dt;
    public ArrayList<OutputSpecie> outputSpec;

    public void add(Object obj)
    {
        if (outputSpec == null)
        {
            outputSpec = new ArrayList<OutputSpecie>();
        }
        if (obj instanceof OutputSpecie)
        {
            outputSpec.add((OutputSpecie)obj);
        }
        else
        {
            E.error("cant add " + obj);
        }
    }


    //<--WK
    public int getNumberOfOutputSpecies()
    {
        return outputSpec.size();
    }
    public String getNamesOfOutputSpecies()
    {
        String names = new String();
        for (int i = 0; i < outputSpec.size()-1; i++)
        {
            names += outputSpec.get(i).name;
            names += " ";
        }
        names += outputSpec.get(outputSpec.size()-1).name;
        return names;
    }
    //WK-->

    public boolean hasRegion()
    {
        return (region != null);
    }

    public String getRegion()
    {
        return region;
    }

    public boolean hasFname()
    {
        return (filename != null);
    }

    public String getFname()
    {
        return filename;
    }

    public boolean hasdt()
    {
        return (dt != null);
    }

    public String getdt()
    {
        return dt;
    }

    //<--WK
    public void print()
    {
        System.out.println("<OutputSet>");
        System.out.print(" filename: " + filename + " |region: ");
        if (hasRegion())
            System.out.print(region);
        else
            System.out.print("Not-Specified");
        System.out.print(" |dt: ");
        if (hasdt())
            System.out.println(dt);
        else
            System.out.println(" default");
        System.out.println("OutputSpecies: ");
        for (int i = 0; i < outputSpec.size(); i++)
        {
            System.out.print((outputSpec.get(i)).name + " ");
        }
        System.out.println();
    }
    //WK-->
}
