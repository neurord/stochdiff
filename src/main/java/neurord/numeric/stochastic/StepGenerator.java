package neurord.numeric.stochastic;

import java.io.PrintStream;

import neurord.numeric.math.MersenneTwister;
import neurord.numeric.math.RandomGenerator;
import neurord.numeric.math.Binomial;
import neurord.numeric.math.BinomialLike;
import neurord.numeric.math.Poisson;
import static neurord.numeric.BaseCalc.distribution_t;
import neurord.util.Logging;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Level;

public class StepGenerator {
    static final Logger log = LogManager.getLogger();

    protected final Binomial binomial;
    protected final Poisson poisson;

    public StepGenerator(RandomGenerator random) {
        this.binomial = new Binomial(random);
        this.poisson = new Poisson(random);
    }

    /*
     * Return the number of successes in n trials, with probability of success
     * in a single trial p.
     *
     * @param n: number of trials
     * @param p: probabilify of a success in one trial
     * @returns: number of successes
     */
    public int nGo(int n, double lnp) {
        return this.versatile_ngo(1, Math.exp(lnp));
    }

    /**
     * Return a random variate from one of the distributions appropriate for
     * first-order reactions (binomial or poisson) depending on the
     * distribution_t mode specified when this object was created.
     */
    public int versatile_ngo(int n, double p) {
        assert n >= 0;

        return this.binomial.nextInt(n, p);
        /* FIXME: support higher orders better */
    }

    public int poisson(double mean) {
        return this.poisson.nextInt(mean);
    }

    public static void main(String... args)
        throws Exception
    {
        Logging.setLogLevel(null, LogManager.ROOT_LOGGER_NAME, Level.DEBUG);
        Logging.configureConsoleLogging();

        boolean print = true;

        distribution_t distrib = distribution_t.valueOf(args[0]);
        int N = Integer.valueOf(args[1]);
        int n = Integer.valueOf(args[2]);
        double p = Double.valueOf(args[3]);

        PrintStream out;
        if (args.length <= 4)
            out = System.out;
        else
            out = new PrintStream(args[4]);

        if (args.length >= 6 && args[5].equals("-n"))
            print = false;

        StepGenerator stepper = new StepGenerator(new MersenneTwister());

        for (int i = 0; i < N; i++) {
            int x;
            if (distrib == distribution_t.POISSON)
                x = stepper.poisson(n * p);
            else
                x = stepper.versatile_ngo(n, p);
            if (print)
                out.println("" + x);
        }
    }
}
