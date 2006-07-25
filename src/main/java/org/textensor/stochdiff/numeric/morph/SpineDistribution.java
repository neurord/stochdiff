package org.textensor.stochdiff.numeric.morph;


public class SpineDistribution {

    public SpinePopulation[] populations;


    public SpineDistribution(SpinePopulation[] pa) {
        populations = pa;
    }


    public SpinePopulation[] getPopulations() {
        return populations;
    }


}