package neurord.sscalc;

import org.ejml.data.DMatrixRMaj;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.sparse.csc.factory.LinearSolverFactory_DSCC;
import org.ejml.interfaces.linsol.LinearSolverSparse;

public class Jacobian {
	private final int numSpecies;
    private final int truncationLimit;
    private final EntropyMoment em;

    public Jacobian(int numSpecies, int truncationLimit) {
        this.numSpecies = numSpecies;
        this.truncationLimit = truncationLimit;
        this.em = new EntropyMoment(truncationLimit, numSpecies);
    }

    /**
     * Calculate the block diagonal Jacobian matrix for the system.
     * @param multipliers Array of Lagrange multipliers.
     * @return Jacobian matrix as a 2D array.
     */
//    public double[][] calculateBlockDiagonalJacobian(double[] multipliers) {
//    	int numKnownMoments = truncationLimit + 1;
//    	int totalSize = numSpecies * numKnownMoments;
//        double[][] jacobian = new double[totalSize][totalSize];
//
//        for (int n = 0; n < numSpecies; n++) {
//            // Jacobian block for species n
//            double[][] block = new double[numKnownMoments][numKnownMoments];
//            for (int i = 0; i < numKnownMoments; i++) {
//                for (int j = 0; j < numKnownMoments; j++) {
//                    int[] orders = new int[numSpecies];
//                    orders[n] = i + j;
//                    block[i][j] = -em.calculateEntropyMoment(orders, multipliers);
//                }
//            }
//
//            for (int i = 0; i < numKnownMoments; i++) {
//                for (int j = 0; j < numKnownMoments; j++) {
//                    jacobian[n * numKnownMoments + i][n * numKnownMoments + j] = block[i][j];
//                }
//            }
//        }
//
//        return jacobian;
//    }
    
    public DMatrixSparseCSC calculateBlockDiagonalJacobian(double[] multipliers) {
        int numKnownMoments = truncationLimit + 1;
        int totalSize = numSpecies * numKnownMoments;

        // Sparse matrix for Jacobian
        DMatrixSparseCSC jacobian = new DMatrixSparseCSC(totalSize, totalSize, totalSize);

        for (int n = 0; n < numSpecies; n++) {
            for (int i = 0; i < numKnownMoments; i++) {
                for (int j = 0; j < numKnownMoments; j++) {
                    int[] orders = new int[numSpecies];
                    orders[n] = i + j;

                    double value = -em.calculateEntropyMoment(orders, multipliers);

                    int globalRow = n * numKnownMoments + i;
                    int globalCol = n * numKnownMoments + j;
                    jacobian.set(globalRow, globalCol, value);
                }
            }
        }

        return jacobian;
    }
    
    public DMatrixRMaj invertJacobian(DMatrixSparseCSC jacobian) {
        LinearSolverSparse<DMatrixSparseCSC, DMatrixRMaj> solver = LinearSolverFactory_DSCC.lu(null);

        if (!solver.setA(jacobian)) {
            throw new RuntimeException("Jacobian matrix is singular and cannot be inverted.");
        }

        // Create an identity matrix (rhs),
        // to solve for it. I.e. obtaining the inverse
        DMatrixRMaj identity = new DMatrixRMaj(jacobian.numCols, jacobian.numCols);
        for (int i = 0; i < jacobian.numCols; i++) {
            identity.set(i, i, 1.0);
        }

        // Create the dense inverted matrix
        DMatrixRMaj inverse = new DMatrixRMaj(jacobian.numCols, jacobian.numRows);
        solver.solve(identity, inverse);
        
        return inverse;
    }

}
