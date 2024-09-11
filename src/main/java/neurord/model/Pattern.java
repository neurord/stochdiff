package neurord.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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
	
	// Add a site to a component specified by its index (not just name)
	// Sites are passed to this method from a Molecule type hence '+' is the only possible bond index
	public void addSite(String moleculeName, int index, Site site, String state) {
	    StructuredMolecule component = getMoleculeComponents().get(index);
	    if (!component.getName().equals(moleculeName)) {
	        throw new IllegalArgumentException("Component name does not match the specified index.");
	    }
	    
	    Site s = new Site(site.getName(), state);
	    if (state.equals("+")) {
	    	this.addBond(index, new Bond(moleculeName, site.getName(), state));
	    }
	    component.addSite(s);
	}
	
	// Returns the pattern in its exact string form
	public String toFormalString() {
	    StringBuilder formalString = new StringBuilder();
	    List<StructuredMolecule> components = getMoleculeComponents();

	    for (int i = 0; i < components.size(); i++) {
	        StructuredMolecule component = components.get(i);
	        formalString.append(component.getName()).append("(");

	        Map<String, Site> sites = component.getSites();
	        List<Bond> bonds = getComponentBonds(i);

	        for (Map.Entry<String, Site> entry : sites.entrySet()) {
	            String siteName = entry.getKey();
	            Site site = entry.getValue();

	            formalString.append(siteName);

	            if (site.getState() != null && !site.getState().equals("")
	            		&& !site.getState().equals("+")) {
	                formalString.append("~").append(site.getState());
	            }

	            // Check if the site has a bond associated with it
	            for (Bond bond : bonds) {
	                if (bond.getSiteName().equals(siteName)) {
	                    formalString.append("!").append(bond.getBondIndex());
	                }
	            }
	            formalString.append(",");
	        }
	        
	        if (formalString.charAt(formalString.length() - 1) == ',') {
	            formalString.deleteCharAt(formalString.length() - 1);
	        }
	        formalString.append(").");
	    }

	    if (formalString.charAt(formalString.length() - 1) == '.') {
	        formalString.deleteCharAt(formalString.length() - 1);
	    }
	    return formalString.toString();
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
	
	@Override
	public boolean equals(Object obj) {
	    if (this == obj) {
	        return true;
	    }
	    if (obj == null || getClass() != obj.getClass()) {
	        return false;
	    }
	    Pattern other = (Pattern) obj;
	    
	    // If number of components (molecules) is different, the patterns are not equal
	    if (this.getMoleculeComponents().size() != other.getMoleculeComponents().size()) {
	        return false;
	    }
	    
	    // Use a set to track unmatched molecules in the "other" pattern and keep "other" intact
	    Set<StructuredMolecule> unmatchedMolecules = new HashSet<>(other.getMoleculeComponents());
	    
	    return patternsAreEqual(this.getMoleculeComponents(), unmatchedMolecules, 0);
	}

	// Compares patterns regardless od ordering of their components
	private boolean patternsAreEqual(List<StructuredMolecule> thisMolecules, Set<StructuredMolecule> unmatchedMolecules, int index) {
	    
	    // Base case: if all molecules are matched
	    if (index == thisMolecules.size()) {
	        return unmatchedMolecules.isEmpty();
	    }
	    
	    StructuredMolecule thisComponent = thisMolecules.get(index);
	    
	    for (StructuredMolecule otherComponent : new HashSet<>(unmatchedMolecules)) {
	        if (thisComponent.getName().equals(otherComponent.getName()) &&
	            thisComponent.getSites().size() == otherComponent.getSites().size()) {
	            
	            boolean allSitesMatch = true;
	            for (String siteName : thisComponent.getSites().keySet()) {
	                Site thisSite = thisComponent.getSite(siteName);
	                Site otherSite = otherComponent.getSite(siteName);
	                
	                if (otherSite == null || !thisSite.getState().equals(otherSite.getState())) {
	                    allSitesMatch = false;
	                    break;
	                }
	            }
	            
	            if (allSitesMatch) {
	                unmatchedMolecules.remove(otherComponent);
	                return patternsAreEqual(thisMolecules, unmatchedMolecules, index + 1);
	            }
	        }
	    }
	    return false;
	}

	@Override
	public int hashCode() {
	    int code = 17;
	    
	    // Sort components by name to make hashing insensitive to the order of components
	    List<StructuredMolecule> sortedMolecules = new ArrayList<>(this.getMoleculeComponents());
	    sortedMolecules.sort(Comparator.comparing(StructuredMolecule::getName));
	    
	    for (StructuredMolecule molecule : sortedMolecules) {
	        code *= 31 + molecule.getName().hashCode();
	        
	        List<String> sortedSiteNames = new ArrayList<>(molecule.getSites().keySet());
	        sortedSiteNames.sort(String::compareTo);
	        
	        for (String siteName : sortedSiteNames) {
	            Site site = molecule.getSite(siteName);
	            code *= 31 + siteName.hashCode();
	            code *= 31 + site.getState().hashCode();
	        }
	    }    
	    return code;
	}


}
