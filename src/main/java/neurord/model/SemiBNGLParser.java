package neurord.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class SemiBNGLParser {
	private Map<String, Integer> parameters;
	private Map<Species, Integer> seedSpecies;
	private List<Molecule> moleculeTypes;
	private List<Pattern> observables; // Using a generic type instead?
	private List<String> reactionRules; // Using a list of objects instead?
	
	public SemiBNGLParser() {
		parameters = new HashMap<>();
		seedSpecies = new HashMap<>();
		moleculeTypes = new ArrayList<>();
		observables = new ArrayList<>();
		reactionRules = new ArrayList<>();
	}
	
	// Method to parse the entire 'rule-based model' input file
	public void parseFile(String filepath) throws IOException {
		try (BufferedReader reader = new BufferedReader(new FileReader(filepath))) {
			String line;
			while ((line = reader.readLine()) != null) {
				// Trim comments marked by '#'
				line = line.replaceAll("^\\s*#.*$", "");
				line = line.trim();
				if (line.startsWith("begin parameters"))
					parseParameters(reader);
				else if (line.startsWith("begin molecule types"))
					parseMoleculeTypes(reader);
				else if (line.startsWith("begin seed species"))
					parseSeedSpecies(reader);
				else if (line.startsWith("begin observables"))
					parseObservables(reader);
				else if (line.startsWith("begin reaction rules"))
					parseReactionRules(reader);
			}
		}
	}

/*
	private void parseParameters(BufferedReader reader) throws ParseException {
		String line;
		while (!(line = reader.readLine().replaceAll("^\\s*#.*$", "").trim()).equals("end parameters")) {
			String[] param =  line.split("\\s+");
			if (param.length == 2)
				parameters.put(param[0], param[1]);
			else if (param.length > 2) {
				// TODO: Add support for special math operators and chars ^()*,/+-e
				// Handle multi-word parameter values separated by white space
				String key = param[0];
				StringBuilder value = new StringBuilder(param[1]);
				for (int i = 2; i < param.length; i++)
					value.append(param[i]);
				
				parameters.put(key, value.toString());
			}
		}
	}
*/

    private void parseParameters(BufferedReader reader) throws IOException {
    	String line;
    	try {
    		while (!(line = reader.readLine().replaceAll("^\\s*#.*$", "").trim()).equals("end parameters")) {
    			String[] param =  line.split("\\s+");
    			if (param.length == 2) {
    				Integer value = constructPValueFromLine(param[1]);
    				parameters.put(param[0], value.intValue());
    			} else if (param.length > 2) {
    				// Handle multi-word parameter values separated by white space
    				StringBuilder sb = new StringBuilder(param[1]);
    				for (int i = 2; i < param.length; i++)
    					sb.append(" ").append(param[i]);
    				
    				// TODO: Add support for special math operators and chars ^()*,/+-e
    				Integer value = constructPValueFromLine(sb.toString());
    				parameters.put(param[0], value.intValue());
    			}
    		}
    	} catch (IOException e) {
    		throw new IOException("Error reading parameters", e);
    	}
    }
	
	private void parseMoleculeTypes(BufferedReader reader) throws IOException {
		String line;
		try {
			while (!(line = reader.readLine().replaceAll("^\\s*#.*$", "").trim()).equals("end molecule types")) {
				Molecule molecule = new  constructMoleculeFromLine(line);
				moleculeTypes.add(molecule);
			}
		} catch (IOException e) {
			throw new IOException("Error reading molecule types", e);
		}
	}
	
	private void parseSeedSpecies(BufferedReader reader) throws IOException {
		String line;
		try {
			while (!(line = reader.readLine().replaceAll("^\\s*#.*$", "").trim()).equals("end seed species")) {
				Species species = new constructSpeciesFromLine(line);
				Integer seed = new constructSeedFromLine(line);
				seedSpecies.put(species, seed.intValue());
			}
		} catch (IOException e) {
			throw new IOException("Error reading seed species", e);
		}
	}
	
	private void parseObservables(BufferedReader reader) throws IOException {
		String line;
		try {
			while (!(line = reader.readLine().replaceAll("^\\s*#.*$", "").trim()).equals("end observables")) {
				// Some logic here depending on the types declared
			}
		} catch (IOException e) {
			throw new IOException("Error reading observables", e);
		}
	}
	
	private void parseReactionRules(BufferedReader reader) throws IOException {
		// Some logic here depending on the types declared
	}
	
	private Integer constructPValueFromLine(String pValue) {
		// parameter evaluator 
		// parameterExpr
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	// Getters for the parsed data
	public HashMap<String, Integer> getParameters() {
		return parameters;
	}
	
	public HashMap<Species, Integer> getSeedSpecies() {
		return seedSpecies;
	}
	
	public List<Molecule> getMoleculeTypes() {
		return moleculeTypes;
	}
	
	public List<Pattern> getObservables () {
		return observables;
	}
	
	public List<String> getReactionRules () {
		return reactionRules;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
