package neurord.sscalc;

public class EntropyMoment {
    private final int truncationLimit;
    private final int numSpecies;
    private static final int MAX_ITERATIONS = 1000;
    private static final double TOLERANCE = 1e-4;

    public EntropyMoment(int numSpecies, int truncationLimit) {
        this.truncationLimit = truncationLimit;
        this.numSpecies = numSpecies;
    }
    
	public double calculateEntropyMoment(int[] orders, double[] multipliers) {
	    double moment = 1.0;

	    for (int n = 0; n < numSpecies; n++) {
	        int order = orders[n]; // Order of moment for species n
	        double stateEnumeration = 0.0; // Marginalized sum for species n

	        // Precompute and store marginalized p_H(x) per iteration
	        double[] probability = new double[MAX_ITERATIONS];
	        for (int x = 0; x < MAX_ITERATIONS; x++) {
	            double exponent = -1.0;
	            for (int m = 0; m <= truncationLimit; m++) {
	                int index = n * (truncationLimit + 1) + m;
	                exponent -= multipliers[index] * Math.pow(x, m);
	            }
	            probability[x] = Math.exp(exponent);
	        }

	        // Calculate the summation term for <x^order>
	        for (int x = 0; x < MAX_ITERATIONS; x++) {
	            double diff = (order > 0 ? Math.pow(x, order) : 1.0) * probability[x];
	            stateEnumeration += diff;

	            if (diff < TOLERANCE && x > 0) {
	                break;
	            }
	        }

	        moment *= stateEnumeration;
	    }

	    return moment;
	}

}
