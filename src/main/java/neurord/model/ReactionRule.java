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

		parseReactionUnits(reaction[0],0);
		parseReactionUnits(reaction[1],1);
	}
	
	private void parseReactionUnits(String units, int index) {
		// Regular expression to match components of the form R1(...) and R1(...).R2(...)
		String regex = "\\w+\\([^\\)]+\\)(?:\\.\\w+\\([^\\)]+\\))*";
		// create a regular Pattern object (from java's util)
		java.util.regex.Pattern regPattern = java.util.regex.Pattern.compile(regex);
		Matcher matcher = regPattern.matcher(units);
		// Find all matches and create the corresponding Pattern object (from neurord.model)
		while (matcher.find()) {
			Pattern unit;
			if (moleculeTypes == null)
				unit = new Pattern(matcher.group());
			else
				unit = new Pattern(matcher.group(), moleculeTypes);
			
			if (index == 0)
				this.reactants.add(unit);
			else
				this.products.add(unit);
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
