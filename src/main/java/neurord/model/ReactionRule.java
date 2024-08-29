package neurord.model;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class ReactionRule {
	private String[] reaction;
	private List<Pattern> reactants;
	private List<Pattern> products;
	private List<Integer> rateConsts;
	protected static List<StructuredMolecule> moleculeTypes;
	private boolean reversible;
	
	public ReactionRule(String[] reaction, List<Integer> rateConsts, boolean reversible) {
		this.reaction = reaction;
		this.rateConsts = rateConsts;
		this.reversible = reversible;
		this.reactants = new ArrayList<>();
		this.products = new ArrayList<>();
		parseReaction();
	}
	
	public ReactionRule(String[] reaction, List<Integer> rateConsts, boolean reversible,
			List<StructuredMolecule> moleculeTypes) {
		this.reaction = reaction;
		this.rateConsts = rateConsts;
		this.reversible = reversible;
		ReactionRule.moleculeTypes = moleculeTypes;
		this.reactants = new ArrayList<>();
		this.products = new ArrayList<>();
		parseReaction();
	}
	
	private void parseReaction() {
		if (reaction.length != 2)
			throw new IllegalArgumentException("Invalid reaction format.");
			
		parseReactants(reaction[0]);
		parseProducts(reaction[1]);
	}
	
	private void parseReactants(String reactants) {
		// Regular expression to match components of the form R1(...) and R1(...).R2(...)
		String regex = "\\w+\\([^\\)]+\\)(?:\\.\\w+\\([^\\)]+\\))*";
		// create a regular Pattern object (from java's util)
		java.util.regex.Pattern regPattern = java.util.regex.Pattern.compile(regex);
		Matcher matcher = regPattern.matcher(reactants);
		// Find all matches and create the corresponding Pattern object (from neurord.model)
		while (matcher.find()) {
			Pattern reactant;
			if (moleculeTypes.isEmpty())
				reactant = new Pattern(matcher.group());
			else
				reactant = new Pattern(matcher.group(), moleculeTypes);
			this.reactants.add(reactant);
		}	
	}
	
	private void parseProducts(String products) {
		String regex = "\\w+\\([^\\)]+\\)(?:\\.\\w+\\([^\\)]+\\))*";
		java.util.regex.Pattern regPattern = java.util.regex.Pattern.compile(regex);
		Matcher matcher = regPattern.matcher(products);
		while (matcher.find()) {
			Pattern product;
			if (moleculeTypes.isEmpty())
				product = new Pattern(matcher.group());
			else
				product = new Pattern(matcher.group(),moleculeTypes);
			this.products.add(product);
		}
	}
	
//	private void parseRateConsts(String rateConsts) {
//		String[] rc = Arrays.stream(rateConsts.split(","))
//							.map(String::trim)
//							.toArray(String[]::new);
//		
//	}
	
	public List<Pattern> getReactants() {
		return reactants;
	}
	
	public List<Pattern> getProducts() {
		return products;
	}
	
	public List<Integer> getRateConsts() {
		return rateConsts;
	}
	
	public boolean isReversible() {
		return reversible;
	}

}
