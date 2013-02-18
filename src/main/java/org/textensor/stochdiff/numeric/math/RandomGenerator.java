package org.textensor.stochdiff.numeric.math;

public interface RandomGenerator {
    float random();
    double gaussian();
    int poisson(double mean);

    void close();
}
