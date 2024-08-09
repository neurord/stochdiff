package neurord.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Molecule: Represents the full set of possible states and sites.
 */
public class StructuredMolecule implements Particle {
	private String name;
	private Map<String, Site> sites;
	
	public StructuredMolecule(String moleculeStr) {
		this.sites = new HashMap<>();
		parseMolecule(moleculeStr);
	}
	
	public StructuredMolecule(StringBuilder name) {
		this.name = name.toString();
		this.sites = new HashMap<>();
	}
	
	private void parseMolecule(String moleculeStr) {
		int openParenIndex = moleculeStr.indexOf('(');
		if (openParenIndex == -1) {
            throw new IllegalArgumentException("Invalid molecule format: " + moleculeStr);
        }
		this.name = moleculeStr.substring(0, openParenIndex);
		String sitesStr = moleculeStr.substring(openParenIndex + 1, moleculeStr.indexOf(')'));
		
		String[] sites = sitesStr.split(",");
		for (String siteStr : sites) {
			Site site = parseSite(siteStr);
			addSite(site);
		}
	}
	
	@Override
	public Site parseSite(String siteStr) {
		String[] components = siteStr.split("~");
		String name = components[0];
		String states = "";
		
		StringBuilder sb = new StringBuilder();
		if (components.length > 1) {
			for (int i = 1; i < components.length; i++) {
				sb.append(components[i]).append("~");
		    }
			sb.setLength(sb.length() - 1); // Drop the last tilde
			states = sb.toString();
		}
		
		boolean bound = name.contains("!");
		if (bound) {
			String bondIndex = name.substring(name.indexOf("!") + 1);
			name = name.substring(0, name.indexOf("!"));
			// A bond in a "molecule" can only be specified with "+"
			if (!bondIndex.equals("+"))
				throw new IllegalArgumentException("Invalid bond index: " + bondIndex);
			
			states = bondIndex;
		}
		return new Site(name, states);
	}
	
	public void addSite(Site site) {
		sites.put(site.getName(), site);
	}
	
	public String getName() {
		return this.name;
	}
	
	@Override
	public Site getSite(String siteName) {
		return sites.get(siteName);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Site> entry : sites.entrySet()) {
//            sb.append(entry.getKey()).append("=").append(entry.getValue().toString()).append(", ");
        	sb.append(entry.getValue().toString()).append(", ");
        }
        // Drop the trailing comma and space
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 2);
        }
        return "Molecule{name='" + name + "', sites={" + sb.toString() + "}}";
    }
}
