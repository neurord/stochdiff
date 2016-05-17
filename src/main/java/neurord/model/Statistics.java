package neurord.model;

import javax.xml.bind.annotation.*;

public class Statistics {
    @XmlAttribute public Double interval;

    @XmlValue public String value;
}
