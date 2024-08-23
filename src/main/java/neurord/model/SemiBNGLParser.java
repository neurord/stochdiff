package neurord.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class SemiBNGLParser {
	private Map<String, Double> parameters;
	private Map<StructuredSpecie, Integer> seedSpecies;
	private List<StructuredMolecule> moleculeTypes;
	private Map<String, Map<String, List<Pattern>>> observables;
	private List<ReactionRule> reactionRules;
	
	public SemiBNGLParser() {
		parameters = new HashMap<>();
		seedSpecies = new HashMap<>();
		moleculeTypes = new ArrayList<>();
		observables = new HashMap<>();
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

    private void parseParameters(BufferedReader reader) throws IOException {
    	String line;
    	try {
    		while (!(line = reader.readLine().replaceAll("^\\s*#.*$", "").trim()).equals("end parameters")) {
    			String[] param =  line.split("\\s+");
    			ParameterResolver pr = new ParameterResolver();
    			if (param.length == 2) {
    				Double value = pr.constructPValue(param[1], parameters);
    				parameters.put(param[0], value);
    			} else if (param.length > 2) {
    				// Concatenate multi-word parameter values separated by white space
    				StringBuilder sb = new StringBuilder(param[1]);
    				for (int i = 2; i < param.length; i++)
    					sb.append(param[i]);

    				Double value = pr.constructPValue(sb.toString(), parameters);
    				parameters.put(param[0], value);
    			}
    		}
    	} catch (IOException e) {
    		throw new IOException("Error reading parameters", e);
    	} catch (Exception e) {
    		throw new RuntimeException("Error parsing parameters", e);
    	}
    }
	
	private void parseMoleculeTypes(BufferedReader reader) throws IOException {
		String line;
		try {
			while (!(line = reader.readLine().replaceAll("^\\s*#.*$", "").trim()).equals("end molecule types")) {
				String allCharMolecule = line.replaceAll("\\s+", "");
				StructuredMolecule molecule = new StructuredMolecule(allCharMolecule);
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
	            Integer seed;
	            String[] parts = line.split("\\s+");

	            // Concatenate multi-word specie chars separated by white spaces
	            StringBuilder sb = new StringBuilder(parts[0]);
	            for (int i = 1; i < parts.length - 1; i++)
	                sb.append(parts[i]);

	            StructuredSpecie specie;
	            String allCharSpecie = sb.toString().replaceAll("\\s+", "");
	            if (moleculeTypes.isEmpty()) {
	                specie = new StructuredSpecie(allCharSpecie);
	            } else {
	                specie = new StructuredSpecie(allCharSpecie, moleculeTypes);
	            }

	            String lastPart = parts[parts.length - 1];
	            seed = getOrResolve(lastPart);
	            seedSpecies.put(specie, seed);
	        }
	    } catch (IOException e) {
	        throw new IOException("Error reading seed species", e);
	    }
	}

	private void parseObservables(BufferedReader reader) throws IOException {
	    String line;
	    try {
	        while (!(line = reader.readLine().replaceAll("^\\s*#.*$", "").trim()).equals("end observables")) {
	            String[] parts = line.split("\\s+");

	            // Make sure the input has at least 3 parts: Type, PatternName and at least one Pattern
	            if (parts.length < 3) {
	                throw new IllegalArgumentException("Invalid input format. Expected: Type PatternName Pattern1 ... PatternN");
	            }

	            String type = parts[0];
	            String patternName = parts[1];

	            if (!type.equals("Molecules") && !type.equals("Species")) {
	                throw new IllegalArgumentException("Invalid observable type. Expected 'Molecules' or 'Species', received " + type);
	            }

	            Map<String, List<Pattern>> innerMap = observables.getOrDefault(patternName, new HashMap<>());
	            List<Pattern> patterns = innerMap.getOrDefault(type, new ArrayList<>());

	            for (int i = 2; i < parts.length; i++) {
	                Pattern pattern;
	                if (moleculeTypes.isEmpty()) {
	                    pattern = new Pattern(parts[i]);
	                } else {
	                    pattern = new Pattern(parts[i], moleculeTypes);
	                }
	                patterns.add(pattern);
	            }

	            innerMap.put(type, patterns);
	            observables.put(patternName, innerMap);
	        }
	    } catch (IOException e) {
	        throw new IOException("Error reading observables", e);
	    }
	}

	private void parseReactionRules(BufferedReader reader) throws IOException {
		String line;
		boolean reversible;
		String[] reaction;
		String[] arrowsRight;
//		int[] rateConsts = new int[2];
		List<Integer> rateConsts = new ArrayList<>();
		try {
			while (!(line = reader.readLine().replaceAll("^\\s*#.*$", "").trim()).equals("end reaction rules")) {
				if (line.contains("->")) {
	                reversible = false;
	                reaction = line.split("->");
	            } else if (line.contains("<->")) {
	                reversible = true;
	                reaction = line.split("<->");
	            } else {
	                throw new IllegalArgumentException("Invalid reaction rule format");
	            }
				
				reaction[0] = reaction[0].trim(); // Reactants
				reaction[1] = reaction[1].trim(); // Products plus rate constants
				arrowsRight = reaction[1].split("\\s+");
				reaction[1] = arrowsRight[0]; // Products
				arrowsRight[1] = String.join("", Arrays.copyOfRange(arrowsRight, 1, arrowsRight.length));
				String[] consts = arrowsRight[1].split(",");
				
				if (reversible) {
					assert consts.length == 2;
					for (int i = 0; i < 2; i++)
						rateConsts.add(getOrResolve(consts[i]));
				} else {
					assert consts.length == 1;
					rateConsts.add(getOrResolve(consts[0]));
				}
				
				ReactionRule rr;
				if (moleculeTypes.isEmpty())
					rr = new ReactionRule(reaction, rateConsts,reversible);
				else
					rr = new ReactionRule(reaction, rateConsts, reversible, moleculeTypes);
				
				reactionRules.add(rr);
			}
		} catch (IOException e) {
			throw new IOException("Error reading reaction rules", e);
		}
	}	
	
	// Resolves constant parameters
	private int getOrResolve(String s) {
		if (parameters.containsKey(s))
			return parameters.get(s).intValue();
		else {
			try {
				return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                throw new NumberFormatException("Invalid constatnt value: " + s);
            }
		}
	}
	
	// Getters for the parsed data
	public Map<String, Double> getParameters() {
		return parameters;
	}
	
	public Map<StructuredSpecie, Integer> getSeedSpecies() {
		return seedSpecies;
	}
	
	public List<StructuredMolecule> getMoleculeTypes() {
		return moleculeTypes;
	}
	
	public Map<String, Map<String, List<Pattern>>> getObservables () {
		return observables;
	}
	
	public List<ReactionRule> getReactionRules () {
		return reactionRules;
	}
}
