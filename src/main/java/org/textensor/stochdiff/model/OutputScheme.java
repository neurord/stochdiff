//5 16 2007: written by RO; modified by WK

package org.textensor.stochdiff.model;

import java.util.ArrayList;
import java.util.HashMap;

import org.textensor.report.E;
import org.textensor.stochdiff.inter.AddableTo;

import org.textensor.util.inst;

public class OutputScheme implements AddableTo
{
    public final ArrayList<OutputSet> outputSets = inst.newArrayList();
    public HashMap<String, OutputSet> outputSetHM;

    public void add(Object obj)
    {
        if (obj instanceof OutputSet)
        {
            OutputSet outset = (OutputSet)obj;
            outputSets.add(outset);
        }
        else
        {
            E.error("cannot add " + obj);
        }
    }

    //<--WK
    public void print()
    {
        System.out.println("<OutputScheme>");
        for (int i = 0; i < outputSets.size(); i++)
        {
            outputSets.get(i).print();
        }
    }
    //WK-->
}
