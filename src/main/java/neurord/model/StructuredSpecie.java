package neurord.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Specie: Represents a specific instance of a Molecule with all state variables
 * in a defined state. It is supposed to contain all the state and site variables
 * introduced in the corresponding Molecule type.
 */

public class StructuredSpecie {
	
	public static class Bond {
		private String moleculeName;
		private String bondIndex;
		private String siteName;
		
		Bond(String moleculeName, String siteName, String bondIndex) {
			this.moleculeName = moleculeName;
			this.siteName = siteName;
			this.bondIndex = bondIndex;
		}
		
		public String getMoleculeName() {
			return moleculeName;
		}
		
		public String getSiteName() {
            return siteName;
        }
        
        public String getBondIndex() {
            return bondIndex;
        }
	}
	
	private List<StructuredMolecule> molecules;
	private Map<String, StructuredMolecule> moleculeTypesMap;
	private List<Bond> bonds;
	
	public StructuredSpecie(String specie) {
		this.molecules = new ArrayList<>();
		this.bonds = new ArrayList<>();
		parseSpecie(specie);
		validateBonds();
	}
	
	public StructuredSpecie(String specie, List<StructuredMolecule> moleculeTypes) {
		this.molecules = new ArrayList<>();
		this.bonds = new ArrayList<>();
		this.moleculeTypesMap = new HashMap<>();
		for (StructuredMolecule smt : moleculeTypes)
		    moleculeTypesMap.put(smt.getName(), smt);
		parseSpecie(specie);
		validateSpecie();
		validateBonds();
	}
	
	public void parseSpecie(String specie) {
		String[] components = specie.split("\\.");
		for (String component : components)
			molecules.add(parseMolecule(component));
//			molecules.add(new StructuredMolecule(component));
	}
	
	private StructuredMolecule parseMolecule(String component) {
		int openParenIndex = component.indexOf('(');
		StringBuilder name = new StringBuilder(component.substring(0, openParenIndex));
		String sitesStr = component.substring(openParenIndex + 1, component.indexOf(')'));
		
		StructuredMolecule molecule = new StructuredMolecule(name);
		String[] sites = sitesStr.split(",");
		for (String siteStr : sites) {
			Site site = parseSite(name, siteStr);
			molecule.addSite(site);
		}
		
		return molecule;
	}
	
	private Site parseSite(StringBuilder moleculeName, String siteStr) {
		String[] components = siteStr.split("~");
		if (components.length > 2) {
			throw new IllegalArgumentException("Site " + components[0]
					+ " has multiple states specified: " + siteStr);
		}
		
		String siteName = components[0];
		String state = components.length > 1 ? components[1] : "";
		
		boolean bound = siteName.contains("!");
		if (bound) {
			String bondIndex = siteName.substring(siteName.indexOf("!") + 1 , siteName.length());
			siteName = siteName.substring(0, siteName.indexOf("!"));
			
			// In case bond index == '+', add it as a site state too
			if (bondIndex.equals("+")) {
				state = bondIndex;
			}
			bonds.add(new Bond(moleculeName.toString(), siteName, bondIndex));
		}
		
		return new Site(siteName, state);
	}
	
	// Checks the compatibility of a Specie with the Molecules defined
	private void validateSpecie() {
		for (StructuredMolecule sms : molecules) {
	        String sName = sms.getName();
	        StructuredMolecule smt = moleculeTypesMap.get(sName);

	        if (smt == null) {
	            throw new IllegalArgumentException("No molecule type found for species molecule: " + sName);
	        }

	        Map<String, Site> sSites = sms.getSites();
	        Map<String, Site> mSites = smt.getSites();

	        for (String sSiteName : sSites.keySet()) {
	            Site sSite = sSites.get(sSiteName);
	            Site mSite = mSites.get(sSiteName);

	            if (mSite == null) {
	                throw new IllegalArgumentException("No site found in molecule " + sName + " for site " + sSiteName);
	            }

	            String sState = sSite.getState();
	            String[] mStates = mSite.getState().split("~");

	            // Check if mStates contains sState? If not throw an error.
	            if (!Arrays.asList(mStates).contains(sState)) {
	                throw new IllegalArgumentException(
	                    "Validation failed for site " + sSiteName + " in molecule " + sName +
	                    " Valid states: " + Arrays.toString(mStates));
	            }
	        }
	    }
	}
	
	// Make sure that we have no lone bond indices
	private void validateBonds() {
	    HashMap<String, Integer> indexMap = new HashMap<>();
	    
	    for (Bond bond : bonds) {
	        String key = bond.bondIndex;
	        if (!key.equals("+")) {
	            int value = indexMap.getOrDefault(key, 0) + 1;
	            indexMap.put(key, value);
	        }
	    }

	    for (Map.Entry<String, Integer> entry : indexMap.entrySet()) {
	        String bondIndex = entry.getKey();
	        int count = entry.getValue();
	        
	        if (count < 2) {
	        	Bond unpairedBond = bonds.stream()
	                    .filter(b -> b.bondIndex.equals(bondIndex))
	                    .findFirst()
	                    .orElseThrow(() -> new IllegalArgumentException("Unexpected error during bond validation"));

	            throw new IllegalArgumentException(
	                "Validation failed: Unpaired bond index " + bondIndex + " at site " + unpairedBond.siteName
	            );
	        }
	    }
	    // TODO: Add checks for available bonding sites in a multi-component specie
	}

	public List<Site> getSite(String moleculeName, String siteName) {
		List<Site> matchingSites = new ArrayList<>();

	    for (StructuredMolecule molecule : this.molecules) {
	        if (molecule.getName().equals(moleculeName)) {
	        	Site site = molecule.getSite(siteName);
	            if (site != null)
	                matchingSites.add(site);
	        }
	    }
	    return matchingSites;
	}
	
	public List<StructuredMolecule> getMoleculeComponents() {
		return Collections.unmodifiableList(molecules);
	}
	
	public List<Bond> getBonds() {
	    return Collections.unmodifiableList(bonds);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (StructuredMolecule molecule : molecules) {
			sb.append(molecule.toString()).append(".");
		}	
		if (sb.length() > 0)
			sb.setLength(sb.length() - 1);	// Drop the last dot
		
		return "Specie{" + sb.toString() + "}";
	}
}
