package neurord.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Pattern: Similar to a Specie but with only a subset of state variables specified.
 */
public class Pattern extends StructuredSpecie {
	private Map<String, StructuredMolecule> moleculeTypesMap;
	
	public Pattern(String pattern) {
		super(pattern);
	}
	
	public Pattern(String pattern, List<StructuredMolecule> moleculeTypes) {
		super(pattern);
		this.moleculeTypesMap = new HashMap<>();
		for (StructuredMolecule sm : moleculeTypes)
		    moleculeTypesMap.put(sm.getName(), sm);
		validatePattern();
	}
	
	// Checks the compatibility of a Pattern with the Molecule types defined
	private void validatePattern() {
		List<StructuredMolecule> patternMolecules = this.getMoleculeComponents();
		
		for (StructuredMolecule pMolecule : patternMolecules) {
			String pName = pMolecule.getName();
			StructuredMolecule molecule = moleculeTypesMap.get(pName);
			
			if (molecule == null) {
	            throw new IllegalArgumentException("No molecule type found for pattern molecule: " + pName);
	        }
			
			Map<String, Site> pSites = pMolecule.getSites();
	        Map<String, Site> mSites = molecule.getSites();
	        
	        for (String pSiteName : pSites.keySet()) {
	            Site pSite = pSites.get(pSiteName);
	            Site mSite = mSites.get(pSiteName);

	            if (mSite == null) {
	                throw new IllegalArgumentException("No site found in molecule " + pName + " for site " + pSiteName);
	            }

	            String pState = pSite.getState();
	            Set<String> mStateSet = new HashSet<>(Arrays.asList(mSite.getState().split("~")));

	            // Check if mStateSet contains pState? Throw an exception otherwise.
	            if (!mStateSet.contains(pState)) {
	                throw new IllegalArgumentException(
	                    "Validation failed for site " + pSiteName + " in molecule " + pName +
	                    " Valid states: " + mStateSet);
	            }
	        }
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (StructuredMolecule molecule : this.getMoleculeComponents()) {
			sb.append(molecule.toString()).append(".");
		}	
		if (sb.length() > 0)
			sb.setLength(sb.length() - 1);
		
		return "Pattern{" + sb.toString() + "}";
	}
}
