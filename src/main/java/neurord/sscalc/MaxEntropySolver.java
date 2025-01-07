package neurord.sscalc;

public class MaxEntropySolver {
	
	private static final double ALPHA = 0.1;       // Decay profile parameter
    	private static final double EPSILON = 1e-6;   // Small value for higher-order lambdas
    	private static final int M_CRITICAL = 6;      // Threshold for lambda decay
	
	private int[] population;
	private double[] multipliers;
	private int truncationLimit;
	private int numSpecies;
	private EntropyMoment em;
	
	public MaxEntropySolver(int[] population, int truncationLimit) {
		this.population = population;
		this.truncationLimit = truncationLimit;
		this.numSpecies = population.length;
		this.multipliers = new double[(truncationLimit + 1) * numSpecies];	// Space allocated for multipliers
		this.em = new EntropyMoment(truncationLimit, numSpecies);
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
			for (int m = 1; m < truncationLimit + 1; m++) {
				int index = n * (truncationLimit + 1) + m;
				multipliers[index] = m <= M_CRITICAL ? ALPHA / Math.pow(m, 3) : EPSILON;
			}
		}
	};

	// This method calculates the moment differences for the first two
	// moments only (0th and 1st moments), assuming that only the 1st moment is known.
	private double[] calculateMomentDifferences() {
	    double[] momentDifferences = new double[numSpecies * 2];
	    
	    for (int n = 0; n < numSpecies; n++) {
	        double[] knownMoments = {1.0, population[n]};
	        
	        int[] zerothOrder = new int[numSpecies];
	        double zerothEntropyMoment = em.calculateEntropyMoment(zerothOrder, multipliers);
	        
	        int[] firstOrder = new int[numSpecies];
	        firstOrder[n] = 1;
	        double firstEntropyMoment = em.calculateEntropyMoment(firstOrder, multipliers);
	        
	        momentDifferences[n * 2] = knownMoments[0] - zerothEntropyMoment; // 0th-order difference
	        momentDifferences[n * 2 + 1] = knownMoments[1] - firstEntropyMoment; // 1st-order difference
	    }
	    
	    return momentDifferences;
	}
	
	private double calculateNormError(double[] momentDifferences) {
	    double normError = 0.0;
	    for (double diff : momentDifferences) {
	        normError += diff * diff;
	    }
	    return normError;
	}



}