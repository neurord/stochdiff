package neurord.numeric.math;

public interface RandomGenerator {
    float random();
    double gaussian();
    double gammln(double xx);
    int poisson(double mean);

    /**
     * Randomly round up or down, so the average is @mean.
     */
    int round(double mean);

    /**
     * A number from the probability distribution
     *    [ t ≥ 0] a exp(-at)
     */
    double exponential(double tau);

    long used();
}
