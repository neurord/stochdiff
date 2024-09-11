package neurord.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
//import java.util.logging.Level;
//import java.util.logging.Logger;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import java.util.Map;
import java.util.Set;

/**
 * A representation of a reaction rule expanded to its full set of possible reactants and products.
 * The combinations of reactants and products are stored in separate lists.
 */
public class RuleExpansion {
	private ReactionRule reactionRule;
	private List<String> reactantCombinations;
	private List<String> productCombinations;
	private Map<String, StructuredMolecule> moleculeTypesMap;
	private Set<Pattern> patternSet;
	private static final Logger log = LogManager.getLogger(RuleExpansion.class.getName());
	
	public RuleExpansion (ReactionRule reactionRule) {
		this.reactionRule = reactionRule;
		this.moleculeTypesMap = new HashMap<>();
		this.patternSet = new HashSet<>();
		if (ReactionRule.moleculeTypes != null)
			for (StructuredMolecule molecule : ReactionRule.moleculeTypes)
				moleculeTypesMap.put(molecule.getName(), molecule);
	}
	
	public void generateReactionSets() {
		List<List<Pattern>> reactantLists = new ArrayList<>();
		List<List<Pattern>> productLists = new ArrayList<>();
		
		if (moleculeTypesMap == null || moleculeTypesMap.isEmpty())
			log.warn("Molecule types list is empty. The reaction rule cannot be expanded any further.");
		
		for (Pattern reactant : reactionRule.getReactants()) {
			List<Pattern> matchingPatterns = new ArrayList<>();
			matchingPatterns = constructSpeciesFromPattern(reactant, new HashSet<>());
			if (matchingPatterns != null && !matchingPatterns.isEmpty())
				reactantLists.add(matchingPatterns);
		}
		
		for (Pattern product : reactionRule.getProducts()) {
			List<Pattern> matchingPatterns = new ArrayList<>();
			matchingPatterns = constructSpeciesFromPattern(product, new HashSet<>());
			if (matchingPatterns != null && !matchingPatterns.isEmpty())
				productLists.add(matchingPatterns);
		}
		
		this.reactantCombinations = generateCombinations(reactantLists);
		this.productCombinations = generateCombinations(productLists);
		populatePatternSet(reactantLists, productLists);
	}
	
	private List<String> generateCombinations(List<List<Pattern>> patternLists) {
        List<String> combinations = new ArrayList<>();
        combinePatterns(combinations, patternLists, 0, "");
        return combinations;
    }

    private void combinePatterns(List<String> combinations, List<List<Pattern>> patternLists,
    		int index, String currentCombination) {
        if (index == patternLists.size()) {
            combinations.add(currentCombination);
            return;
        }

        List<Pattern> currentList = patternLists.get(index);
        for (Pattern pattern : currentList) {
            String newCombination = currentCombination.isEmpty() ? 
            		pattern.toFormalString() : currentCombination + " + " + pattern.toFormalString();
            combinePatterns(combinations, patternLists, index + 1, newCombination);
        }
    }
	
	private List<Pattern> constructSpeciesFromPattern(Pattern pattern, Set<Pattern> matchingPatterns) {
		if (moleculeTypesMap == null || moleculeTypesMap.isEmpty()) {
		    matchingPatterns.add(pattern);
		    return new ArrayList<>(matchingPatterns);
		}

	    List<StructuredMolecule> components = pattern.getMoleculeComponents();
	    
	    // Initial checks to determine if components sites are matching molecules'
	    boolean hasAllSites = true;
	    for (StructuredMolecule component : components) {
	        String name = component.getName();
	        StructuredMolecule molecule = moleculeTypesMap.get(name);
	        
	        if (molecule == null)	// Continue when there's no matching molecules
	            continue;
	        
	        int componentSitesSize = component.getSites().size();
	        int moleculeSitesSize = molecule.getSites().size();
	        
	        if (componentSitesSize != moleculeSitesSize) {
	            hasAllSites = false;
	            break;
	        }
	    }
	    
	    // Base case:
	    if (hasAllSites) {
	        matchingPatterns.add(pattern);
	        return new ArrayList<>(matchingPatterns);
	    }
	    
	    for (int i = 0; i < components.size(); i++) {
	        StructuredMolecule component = components.get(i);
	        String name = component.getName();
	        StructuredMolecule molecule = moleculeTypesMap.get(name);
	        
	        if (molecule == null)
	            continue;

	        Map<String, Site> componentSites = component.getSites();
	        Map<String, Site> moleculeSites = molecule.getSites();
	        if (componentSites.size() == moleculeSites.size())
	            continue;

	        for (Map.Entry<String, Site> entry : moleculeSites.entrySet()) {
	        	Site site = entry.getValue();
	        	String siteName = site.getName();
	        	// Handle states, including empty ones
	        	String[] states = site.getState().isEmpty() ? new String[]{""} : site.getState().split("~");
	            if (!componentSites.containsKey(siteName)) {
	                for (String state : states) {
	                    Pattern newPattern = new Pattern(pattern.toFormalString());
	                    newPattern.addSite(name, i, site, state);  // Specify the component index
	                    constructSpeciesFromPattern(newPattern, matchingPatterns);
	                }
	            }
	        }
	    }	    
	    return new ArrayList<>(matchingPatterns);
	}
	
	private void populatePatternSet(List<List<Pattern>> rLists, List<List<Pattern>> pLists) {
		for (int i = 0; i < rLists.size(); i++)
			for (int j = 0; j < rLists.get(i).size(); j++)
				this.patternSet.add(rLists.get(i).get(j));

		for (int i = 0; i < pLists.size(); i++)
			for (int j = 0; j < pLists.get(i).size(); j++)
				this.patternSet.add(pLists.get(i).get(j));
	}
	
	public List<String> getReactantCombinations() {
		return reactantCombinations;
	}
	
	public List<String> getProductCombinations() {
		return productCombinations;
	}
	
	public Set<Pattern> getPatternSet() {
		return patternSet;
	}

}
