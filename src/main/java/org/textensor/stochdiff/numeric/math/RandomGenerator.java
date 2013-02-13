package org.textensor.stochdiff.numeric.math;

public interface RandomGenerator {
    float random();
    double gaussian();
    double gammln(double xx);
    int poisson(double mean);
}
