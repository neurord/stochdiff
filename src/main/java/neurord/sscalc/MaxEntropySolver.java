package neurord.sscalc;

public class MaxEntropySolver {
	
	private static final double ALPHA = 0.1;       // Decay profile parameter
    private static final double EPSILON = 1e-6;   // Small value for higher-order lambdas
    private static final int M_CRITICAL = 6;      // Threshold for lambda decay
    private static final int MAX_ITERATIONS = 100;
    private static final double TOLERANCE = 1e-4;
	
	private double[] population;
	private double[] multipliers;
//	private double[][] moments;
	private int closureOrder;
	private int numSpecies;
	
	public MaxEntropySolver(double[] population, int closureOrder) {
		this.population = population;
		this.closureOrder = closureOrder;
		this.numSpecies = population.length;
		this.multipliers = new double[(closureOrder + 1) * numSpecies];	// Space allocated for multipliers
//		this.moments = new double[numSpecies][];
	}
	
//	private double[] solve() {
		// Returns an array of the multipliers, basically the answer array that we are looking for.
		// Initialize the multipliers matrix (step 1)
		// Calculate the lower-order maximum entropy moments, μ_H , using p_H(x) (step 3)
		// Calculate the difference between the known moments and maximum entropy moments (step 4)
		// Calculate the 2-norm error: $\epsilon = \Delta\mu^T * \Delta\mu$ (step 5)
		
//	}
	
	public void initializeMultipliers() {
		for (int n = 0; n < numSpecies; n++) {
			for (int m = 1; m < closureOrder + 1; m++) {
				int index = n * (closureOrder + 1) + m;
				multipliers[index] = m <= M_CRITICAL ? ALPHA / Math.pow(m, 3) : EPSILON;
			}
		}
	};

	// This method calculates the first two moments only (0th and 1st moments),
	// assuming that only the 1st moment is known.
//	private double[] calculateEntropyMoments() {
//		// Calculate entropy moments using p_H(x)
//		double normalizationConst = calcNormalizationConst();
//		double zerothMoment, firstMoment = 1.0;
//		
//		for (int n = 0; n < numSpecies; n++) {
//			zerothMoment *= calcSingleComponenetMoment(0); 
//			firstMoment *= calcSingleComponenetMoment(componentMomentOrder[n]);
//		}
//	}
	
	/*
	private double[] calculateEntropyMoments() {
		// Calculate entropy moments using p_H(x)
		// Can postpone the calculation of normalization constants to when 
		// the needed moments are combined.
		for (int n = 0; n < numSpecies; n++) {
			for (int m = 0; m < orderSet[n].length; m++) {
				int order = orderSet[m];
//				double normalizationConst = calculateNormalizationConst(n, order);
				moments[n][m] = calculateComponentMoment(n,order);
			}
		}
		
	}
	
	private double calculateComponentMoment(int specie, int order) {
		for ()
	}
	*/
	
	
	public double calculateEntropyMoment(int[] orders) {
	    double moment = 1.0;

	    for (int n = 0; n < numSpecies; n++) {
	        int order = orders[n]; // Order of moment for species n
	        double stateEnumeration = 0.0; // Marginalized sum for species n

	        // Precompute and store marginalized p_H(x) per iteration
	        double[] probability = new double[MAX_ITERATIONS + 1];
	        for (int x = 0; x <= MAX_ITERATIONS; x++) {
	            double exponent = -1.0;
	            for (int m = 0; m <= closureOrder; m++) {
	                int index = n * (closureOrder + 1) + m;
	                exponent -= multipliers[index] * Math.pow(x, m);
	            }
	            probability[x] = Math.exp(exponent);
	        }

	        // Calculate the summation term for <x^order>
	        for (int x = 0; x <= MAX_ITERATIONS; x++) {
	            double diff = (order > 0 ? Math.pow(x, order) : 1.0) * probability[x];
	            stateEnumeration += diff;

	            if (diff < TOLERANCE) {
	                break;
	            }
	        }

	        moment *= stateEnumeration;
	    }

	    return moment;
	}


}
