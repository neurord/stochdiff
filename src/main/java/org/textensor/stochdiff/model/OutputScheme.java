//5 16 2007: written by RO; modified by WK

package org.textensor.stochdiff.model;

import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.bind.annotation.*;

public class OutputScheme {

    @XmlElement(name="OutputSet")
    public ArrayList<OutputSet> outputSets;
}
