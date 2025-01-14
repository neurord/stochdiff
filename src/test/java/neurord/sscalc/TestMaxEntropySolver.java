package neurord.sscalc;

import junit.framework.TestCase;

/**
 * You might need to change the access modifiers of some methods in
 * MaxEntropySolver to run some of the tests.
 */
public class TestMaxEntropySolver extends TestCase {
	
//	public void testSingleSpeciesZeroOrderMoment() {
//        int[] population = {12};
//        int closureOrder = 12; // Closure order for the solver
//        MaxEntropySolver solver = new MaxEntropySolver(population, closureOrder);
//        solver.initializeMultipliers();
//        
//        // Zero-order moment should approximate to 1 (normalization condition)
//        int[] orders = {0};
//        double moment = solver.calculateEntropyMoment(orders);
//        assertEquals(1.0, moment, 1e-3);
//    }
//	
//	public void testMultiSpeciesZeroOrderMoment() {
//        int[] population = {12, 16, 8, 7};
//        int closureOrder = 12;
//        MaxEntropySolver solver = new MaxEntropySolver(population, closureOrder);
//        solver.initializeMultipliers();
//        
//        int[] orders = {0,0,0,0};
//        double moment = solver.calculateEntropyMoment(orders);
//        assertEquals(1.0, moment, 1e-3);
//    }
	
	
//	public void testMultiSpeciesMixedMoments() {
//        int[] population = {3};
//        int closureOrder = 3;
//        MaxEntropySolver solver = new MaxEntropySolver(population, closureOrder);
//        solver.initializeMultipliers();
//        
//        // Mixed moment <x1^1 * x2^2>
//        int[] orders = {1};
//        double moment = solver.calculateEntropyMoment(orders);
//        System.out.println(moment);
//        
//        // Expected value needs to be verified against a known solution
//        assertTrue(moment > 0); // Simple sanity check for positive moment
//    }
	
	public void testSolver () {
		int[] population = {120};
		int closureOrder = 1; // Closure order for the solver
		MaxEntropySolver solver = new MaxEntropySolver(population, closureOrder);
		
		double[] lambdas = solver.solve();
		
		for (double lambda : lambdas)
			System.out.println(lambda);
	
	}
}
