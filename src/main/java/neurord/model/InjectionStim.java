//6 22 2007: WK added 'interTrainInterval' and 'numTrains'.
//written by Robert Cannon
package neurord.model;

import javax.xml.bind.annotation.*;

public class InjectionStim {

    @XmlAttribute public String specieID;

    // this should be the ID of a point in the morphology file
    @XmlAttribute private String injectionSite;

    private double rate;

    private double onset;
    private double duration;
    private Double period;
    private Double end;

    private Double interTrainInterval; //the time interval between the end and
    //the start(onset) of two consecutive input trains
    private Integer numTrains;

    public String getInjectionSite() {
        assert this.injectionSite != null; /* required in the schema */
        return this.injectionSite;
    }

    public String getSpecies() {
        assert this.specieID != null;      /* required in the schema */
        return this.specieID;
    }

    public double getRate() {
        return this.rate;
    }

    public double getOnset() {
        return this.onset;
    }

    public double getDuration() {
        return this.duration;
    }

    public double getPeriod() {
        return this.period != null ? this.period : Double.NaN;
    }

    public double getEnd() {
        return this.end != null ? this.end : Double.POSITIVE_INFINITY;
    }

    public int getNumTrains() {
        return this.numTrains != null ? this.numTrains : 1;
    }

    public double getInterTrainInterval() {
        return this.interTrainInterval != null ? this.interTrainInterval : 0;
    }
}
