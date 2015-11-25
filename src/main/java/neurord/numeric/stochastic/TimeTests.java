package neurord.numeric.stochastic;

import neurord.numeric.math.MersenneTwister;
import neurord.numeric.math.NRRandom;
import neurord.numeric.BaseCalc.distribution_t;

import java.util.Random;


// TODO - should accumulate the individual tests in here;


public class TimeTests {






    public static void main(String[] argv) {
        randomNumberTimes();
        // interpTest();
    }



    public static void interpTest() {
        InterpolatingStepGenerator stepper = new InterpolatingStepGenerator(distribution_t.BINOMIAL,
                                                                            new MersenneTwister());
        stepper.timeTest();
    }


    private static void randomNumberTimes() {
        int nrep = 3;
        double rnrn = 1.e7;

        Random random = new Random();

        for (int i = 0; i < nrep; i++) {
            long t0 = System.currentTimeMillis();
            double c = 0.;
            for (int j = 0; j < rnrn; j++) {
                c += random.nextFloat();
            }
            long t1 = System.currentTimeMillis();
            System.out.println("util.random t = " + (t1 - t0));
        }



        for (int i = 0; i < nrep; i++) {
            long t0 = System.currentTimeMillis();
            double c = 0.;
            for (int j = 0; j < rnrn; j++) {
                c += NRRandom.random();
            }
            long t1 = System.currentTimeMillis();
            System.out.println("NRRandom t = " + (t1 - t0));
        }

        MersenneTwister mtw = new MersenneTwister();
        for (int i = 0; i < nrep; i++) {
            long t0 = System.currentTimeMillis();
            double c = 0.;
            for (int j = 0; j < rnrn; j++) {
                c += mtw.random();
            }
            long t1 = System.currentTimeMillis();
            System.out.println("MT t = " + (t1 - t0));
        }
    }


}
