package neurord.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

public class SemiBNGLParser {
	private HashMap<String, String> parameters;
	private HashMap<Species, Integer> seedSpecies;
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
	
}
