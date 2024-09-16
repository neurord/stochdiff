package neurord.model;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class RuleExpansionTest {
	
	private String[] reaction;
	List<Integer> rateConsts;
	List<StructuredMolecule> moleculeTypes;
	private RuleExpansion ruleExpansion;
	private ReactionRule rule;
	
	
	@Before
    public void setUp() {

		// Sample data:
		this.reaction = new String[] {"R(Y~U)", "S(Kin~inact).R(Y~U) + R(DD!+).R(DD!+,Y~P)"};
		this.rateConsts = Arrays.asList(1);
		
        StructuredMolecule molecule1 = new StructuredMolecule("R(DD!+,Y~U~P)");
        StructuredMolecule molecule2 = new StructuredMolecule("S(Y~U~P,SH2,Kin~inact~act,W~U~P)");
        
        molecule1.addSite(new Site("Inja", "bala~paein"));
        molecule2.addSite(new Site("Anja", "chap~raast"));
        
        this.moleculeTypes = new ArrayList<>();
        moleculeTypes.add(molecule1);
        moleculeTypes.add(molecule2);
        
        rule = new ReactionRule(reaction, rateConsts, false, moleculeTypes);
        ruleExpansion = new RuleExpansion(rule);
        ruleExpansion.generateReactionSets();
    }

    @Test
    public void testInitialization() {
        assertNotNull(ruleExpansion);
        assertNotNull(ruleExpansion.getReactantCombinations());
        assertNotNull(ruleExpansion.getProductCombinations());
    }

    @Test
    public void testConstructSpeciesFromPattern() {
    	// Modify these according to the sample data block
        List<String> expectedR = Arrays.asList("R(DD,Y~U)");
        List<String> expectedP = Arrays.asList("S(Y~U,SH2,Kin~inact)","S(Y~P,SH2,Kin~inact)");

        List<String> reactantCombo = ruleExpansion.getReactantCombinations();
        List<String> productCombo = ruleExpansion.getProductCombinations();
        
        for (String s : reactantCombo)
        	System.out.println(s);
        for (String s : productCombo)
        	System.out.println(s);

        assertNotNull(reactantCombo);
        assertFalse(reactantCombo.isEmpty());
        // These assertions are likely going to fail since maps won't adhere to the original order of
        // objects passed to them. Better to rely on printing out the outputs.
        assertEquals(expectedR, reactantCombo);
        assertEquals(expectedP, productCombo);
    }

}
