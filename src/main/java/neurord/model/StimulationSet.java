package neurord.model;

import java.util.List;
import java.util.ArrayList;

import javax.xml.bind.annotation.*;

public class StimulationSet {

    @XmlElement(name="InjectionStim")
    public List<InjectionStim> stimulations = new ArrayList<>();
}
