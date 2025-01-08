package neurord.sscalc;

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;

public class MaxEntropySolver {
	
	private static final double ALPHA = 0.1;       // Decay profile parameter
    private static final double DELTA = 1e-6;   // Small value for higher-order lambdas
    private static final int M_CRITICAL = 6;      // Threshold for lambda decay
    private static final double EPSILON = 1e-16;
	
	private int[] population;
	private double[] multipliers;
	private int truncationLimit;
	private int numSpecies;
	private EntropyMoment em;
	private Jacobian jacobian;
	
	public MaxEntropySolver(int[] population, int truncationLimit) {
		this.population = population;
		this.truncationLimit = truncationLimit;
		this.numSpecies = population.length;
		this.multipliers = new double[(truncationLimit + 1) * numSpecies];	// Space allocated for multipliers
		this.em = new EntropyMoment(numSpecies, truncationLimit);
		this.jacobian = new Jacobian(numSpecies, truncationLimit);
	}
	
//	private double[] solve() {
		// Returns an array of the multipliers, basically the answer array that we are looking for.
		// Initialize the multipliers matrix (step 1)
		// Calculate the lower-order maximum entropy moments, μ_H , using p_H(x) (step 3)
		// Calculate the difference between the known moments and maximum entropy moments (step 4)
		// Calculate the 2-norm error: $\DELTA = \Delta\mu^T * \Delta\mu$ (step 5)
		
//	}

	
	private void initializeMultipliers() {
		for (int n = 0; n < numSpecies; n++) {
			for (int m = 1; m < truncationLimit + 1; m++) {
				int index = n * (truncationLimit + 1) + m;
				multipliers[index] = m <= M_CRITICAL ? ALPHA / Math.pow(m, 3) : DELTA;
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

	/**
	 * Calculate \Delta\lambda = J^{-1} * \Delta\mu
	 * @param inverseJ: the inverted Jacobian with dimensions (w * w)
	 * @param momentDifferences: \Delta\mu vector with dimensions (w * 1)
	 * @return deltaLambda: with dimensions (w * 1)
	 */
	private double[] calculateMultipliersStep(DMatrixRMaj inverseJ, double[] momentDifferences) {
		int w = momentDifferences.length;

        DMatrixRMaj deltaMu = new DMatrixRMaj(w, 1);
        for (int i = 0; i < w; i++)
            deltaMu.set(i, 0, momentDifferences[i]);

        DMatrixRMaj deltaLambda = new DMatrixRMaj(w, 1);
        CommonOps_DDRM.mult(inverseJ, deltaMu, deltaLambda);
        
        double[] deltaLambdaArray = new double[w];
        for (int i = 0; i < w; i++)
            deltaLambdaArray[i] = deltaLambda.get(i, 0);

        return deltaLambdaArray;
	}
	
	private void updateMultipliers(double[] deltaLambda) {
		if (deltaLambda.length != multipliers.length) {
	        throw new IllegalArgumentException("Size mismatch: deltaLambda and "
	        		+ "multipliers must have the same length.");
	    }
		
		for (int n = 0; n < numSpecies; n++) {
			for (int m = 0; m <= truncationLimit; m++) {
				int index = n * (truncationLimit + 1) + m;
				this.multipliers[index] += deltaLambda[index];
			}
		}
	}

}