package neurord.sscalc;

import org.apache.commons.math3.linear.*;

public class Jacobian {
	private final int numSpecies;
    private final int truncationLimit;
    private final EntropyMoment em;

    public Jacobian(int numSpecies, int truncationLimit) {
        this.numSpecies = numSpecies;
        this.truncationLimit = truncationLimit;
        this.em = new EntropyMoment(numSpecies, truncationLimit);
    }

    /**
     * Calculate the block diagonal Jacobian matrix for the system.
     * @param multipliers Array of Lagrange multipliers.
     * @return sparse Jacobian matrix.
     */
    public OpenMapRealMatrix calculateBlockDiagonalJacobian(double[] multipliers) {
        int numKnownMoments = truncationLimit + 1;
        int totalSize = numSpecies * numKnownMoments;

        // Sparse matrix for Jacobian
        OpenMapRealMatrix jacobian = new OpenMapRealMatrix(totalSize, totalSize);

        for (int n = 0; n < numSpecies; n++) {
            for (int i = 0; i < numKnownMoments; i++) {
                for (int j = 0; j < numKnownMoments; j++) {
                    int[] orders = new int[numSpecies];
                    orders[n] = i + j;

                    double value = -em.calculateEntropyMoment(orders, multipliers);

                    int globalRow = n * numKnownMoments + i;
                    int globalCol = n * numKnownMoments + j;
                    jacobian.setEntry(globalRow, globalCol, value);
                }
            }
        }

        return jacobian;
    }
    
    public OpenMapRealMatrix invertJacobian(OpenMapRealMatrix jacobian) {
        if (jacobian.getRowDimension() != jacobian.getColumnDimension()) {
            throw new IllegalArgumentException("Jacobian has to be square.");
        }

        LUDecomposition luDecomposition = new LUDecomposition(jacobian);

        if (!luDecomposition.getSolver().isNonSingular()) {
            throw new RuntimeException("Jacobian matrix is singular and cannot be inverted.");
        }
        
        RealMatrix denseInverse = luDecomposition.getSolver().getInverse();

        // Convert the inverted matrix to an OpenMapRealMatrix
        int nRows = denseInverse.getRowDimension();
        int nColumns = denseInverse.getColumnDimension();
        OpenMapRealMatrix sparseInverse = new OpenMapRealMatrix(nRows, nColumns);
        for (int i = 0; i < nRows; i++) {
            for (int j = 0; j < nColumns; j++) {
                double value = denseInverse.getEntry(i, j);
                if (value != 0.0) {
                    sparseInverse.setEntry(i, j, value);
                }
            }
        }

        return sparseInverse;
    }
    

}
