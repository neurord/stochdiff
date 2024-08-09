package neurord.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Specie: Represents a specific instance of a Molecule with all state variables
 * in a defined state. It is suppose to contain all the states and site variables
 * introduced in the corresponding Molecule object.
 */

public class StructuredSpecie {
	
	private static class Bond {
		private String bondIndex;
		private String siteName;
		
		Bond(String siteName, String bondIndex) {
			this.siteName = siteName;
			this.bondIndex = bondIndex;
		}
	}
	
	private List<StructuredMolecule> molecules;
	private List<Bond> bonds;
	
	public StructuredSpecie(String specie) {
		this.molecules = new ArrayList<>();
		this.bonds = new ArrayList<>();
		parseSpecie(specie);
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
			Site site = parseSite(siteStr);
			molecule.addSite(site);
		}
		
		return molecule;
	}
	
	private Site parseSite(String siteStr) {
		String[] components = siteStr.split("~");
		if (components.length > 2) {
			throw new IllegalArgumentException("Site " + components[0]
					+ " has multiple states specified: " + siteStr);
		}
		
		String name = components[0];
		String state = components.length > 1 ? components[1] : "";
		
		boolean bound = name.contains("!");
		if (bound) {
			String bondIndex = name.substring(name.indexOf("!") + 1 , name.length());
			name = name.substring(0, name.indexOf("!"));
			bonds.add(new Bond(name, bondIndex));
//			TODO: Maybe handling special cases of unspecified bonds with '+' char?
		}
		
		return new Site(name, state);
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
