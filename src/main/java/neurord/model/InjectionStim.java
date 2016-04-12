//6 22 2007: WK added 'interTrainInterval' and 'numTrains'.
//written by Robert Cannon
package neurord.model;

import java.util.ArrayList;
import java.util.List;

import neurord.xml.DoubleListAdapter;
import neurord.xml.DoubleMatrixAdapter;
import neurord.numeric.BaseCalc.distribution_t;

import org.jblas.DoubleMatrix;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.*;

public class InjectionStim {

    @XmlAttribute public String specieID;

    // this should be the ID of a point in the morphology file
    @XmlAttribute private String injectionSite;

    @XmlAttribute private distribution_t distribution;

    private Double rate;

    private Double onset;
    private Double duration;
    private Double period;
    private Double end;

    private Double interTrainInterval; //the time interval between the end and
    //the start(onset) of two consecutive input trains
    private Integer numTrains;

    @XmlJavaTypeAdapter(DoubleMatrixAdapter.class)
    private DoubleMatrix rates;

    private InjectionStim() {};

    /* For testing only */
    public InjectionStim(String species,
                         String site,
                         Double rate, Double onset, Double duration, Double period, Double end) {
        this.specieID = species;
        this.injectionSite = site;
        this.rate = rate;
        this.onset = onset;
        this.duration = duration;
        this.period = period;
        this.end = end;
    }

    public String getInjectionSite() {
        assert this.injectionSite != null; /* required in the schema */
        return this.injectionSite;
    }

    public String getSpecies() {
        assert this.specieID != null;      /* required in the schema */
        return this.specieID;
    }

    public distribution_t getDistribution() {
        if (this.distribution != null)
            return this.distribution;
        else
            return distribution_t.POISSON;
    }

    public double getRate() {
        return this.rate != null ? this.rate : Double.NaN;
    }

    public double getOnset() {
        return this.onset != null ? this.onset : Double.NaN;
    }

    public double getDuration() {
        return this.duration != null ? this.duration : Double.NaN;
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

    public double[][] getRates() {
        if (this.rates != null) {
            assert this.rate == null;
            assert this.onset == null;
            assert this.duration == null;
            assert this.period == null;
            assert this.end == null;

            return this.rates.toArray2();
        } else
            return null;
    }
}
