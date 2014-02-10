package org.textensor.stochdiff.model;

import javax.xml.bind.annotation.*;

public class Section {

    @XmlAttribute public Double width;
    @XmlAttribute public Double at;

    @XmlAttribute public String regionClass;

    @XmlAttribute public String label;

}
