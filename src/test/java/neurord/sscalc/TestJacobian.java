package neurord.sscalc;

import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestJacobian {

	int numSpecies;
	int truncationLimit;
	private Jacobian jacobian;

    @Before
    public void setUp() {
        numSpecies = 2;
        truncationLimit = 1;
        jacobian = new Jacobian(numSpecies, truncationLimit);
    }

    @Test
    public void testCalculateBlockDiagonalJacobian() {
        double[] multipliers = new double[(truncationLimit + 1) * numSpecies];
        // Make sure to initialize the multipliers according to the size of the array.
        multipliers[0] = .1;
        multipliers[1] = .2;
        multipliers[2] = .5;
        multipliers[3] = .6;

        OpenMapRealMatrix result = jacobian.calculateBlockDiagonalJacobian(multipliers);

        int expectedSize = numSpecies * (truncationLimit + 1);
        assertEquals(expectedSize, result.getRowDimension());
        assertEquals(expectedSize, result.getColumnDimension());
    }
    
    public void testInvertJacobian() {
        OpenMapRealMatrix jMatrix = new OpenMapRealMatrix(3, 3);
        jMatrix.setEntry(0, 0, 1.0);
        jMatrix.setEntry(1, 1, 2.0);
        jMatrix.setEntry(2, 2, 3.0);

        OpenMapRealMatrix inverse = jacobian.invertJacobian(jMatrix);

        assertEquals(jMatrix.getRowDimension(), inverse.getRowDimension());
        assertEquals(jMatrix.getColumnDimension(), inverse.getColumnDimension());

        // Check specific entries in the inverse matrix
        assertEquals(1.0, inverse.getEntry(0, 0), 1e-6);
        assertEquals(0.5, inverse.getEntry(1, 1), 1e-6);
        assertEquals(1.0 / 3.0, inverse.getEntry(2, 2), 1e-6);

        // Check that J * J^-1 == I
        OpenMapRealMatrix identity = jMatrix.multiply(inverse);
        for (int i = 0; i < identity.getRowDimension(); i++) {
            for (int j = 0; j < identity.getColumnDimension(); j++) {
                if (i == j) {
                    assertEquals(1.0, identity.getEntry(i, j), 1e-6);
                } else {
                    assertEquals(0.0, identity.getEntry(i, j), 1e-6);
                }
            }
        }
    }

}
