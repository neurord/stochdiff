//5 16 2007: written by RO; modified by WK

package neurord.model;

import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.bind.annotation.*;

public class OutputScheme {

    @XmlAttribute public Boolean dependencies;

    @XmlElement(name="OutputSet")
    public ArrayList<OutputSet> outputSets;
}
