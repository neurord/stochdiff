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
	private Map<String, String> parameters;
	private Map<Species, Integer> seedSpecies;
	private List<Molecule> moleculeTypes;
	private List<Pattern> observables;
	private List<String> reactionRules;
	
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
	
	private void parseParameters(BufferedReader reader) throws ParseException {
		String line;
		while (!(line = reader.readLine().replaceAll("^\\s*#.*$", "").trim()).equals("end parameters")) {
			String[] param =  line.split("\\s+");
			if (param.length == 2)
				parameters.put(param[0], param[1]);
			else if (param.length > 2) {
				// TODO: Add support for special math operators and chars ^()*/+-e
				// Handle multi-word parameter values separated by white space
				String key = param[0];
				StringBuilder value = new StringBuilder(param[1]);
				for (int i = 2; i < param.length; i++)
					value.append(param[i]);
				
				parameters.put(key, value.toString());
			}
		}
	}
	
	private void parseMoleculeTypes(BufferedReader reader) throws ParseException {
		String line;
		while (!(line = reader.readLine().replaceAll("^\\s*#.*$", "").trim()).equals("end molecule types")) {
			// Add the logic that constructs a molecule and adds it to moleculeTypes
			
		}
	}
	
	private void parseSeedSpecies(BufferedReader reader) throws ParseException {
		String line;
		while (!(line = reader.readLine().replaceAll("^\\s*#.*$", "").trim()).equals("end seed species")) {
			String[] param = line.split("\\s+");
//			if (param.length == 2)
				// Add the logic to construct Species and Integer objects
				// from the line input.
//				seedSpecies.put(param[0], param[1]);
//			else if (param.length > 2) {
//				// TODO: Add support for special math operators and chars ^()*/+-e
//				// Handle multi-word parameter values separated by white space
//				String key = param[0];
//				StringBuilder value = new StringBuilder(param[1]);
//				for (int i = 2; i < param.length; i++)
//					value.append(param[i]);
//				
//				seedSpecies.put(key, value.toString());
//			}
		}
	}
	
	private void parseObservables(BufferedReader reader) throws ParseException {
		
	}
	
	private void parseReactionRules(BufferedReader reader) throws ParseException {
		
	}
	
	// Getters for the parsed data
	public HashMap<String, String> getParameters() {
		return parameters;
	}
	
	public HashMap<Species, Integer> getSeedSpecies() {
		return seedSpecies;
	}
	
	public List<Molecule> getMoleculeTypes() {
		
	}
	
	public List<Pattern> getObservables () {
		
	}
	
	public List<String> getReactionRules () {
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
