package neurord.model;

import java.io.File;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ReactionWriterXML {
	private static final Logger log = LogManager.getLogger(ReactionWriterXML.class.getName());
	private RuleExpansion expansion;
	private List<String> reactantCombinations;
	private List<String> productCombinations;
	private List<Integer> rateConsts;
	private Set<Pattern> patternSet;
	private boolean reversible;
	
	private final File outputFile;
	private Document document;
	
	public ReactionWriterXML(File output, ReactionRule rule) {
		this.expansion = new RuleExpansion(rule);
		this.expansion.generateReactionSets();
		this.reactantCombinations = expansion.getReactantCombinations();
		this.productCombinations = expansion.getProductCombinations();
		this.patternSet = expansion.getPatternSet();
		this.reversible = rule.isReversible();
		this.rateConsts = rule.getRateConsts();
		this.outputFile = new File(output + ".xml");
		appendDocument();
		writeToFile();
	}
	
	private void appendDocument() {
		try {
			DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
			this.document = documentBuilder.newDocument();

			Element root = document.createElement("ReactionScheme");
			document.appendChild(root);

			appendSpecieElms(document, root);
			appendReactionElms(document, root);
			
		} catch (ParserConfigurationException e) {
			log.error("Error appending document for output file: " + outputFile.getName(), e);
		}
	}
	
	private void writeToFile() {
		try {
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource domSource = new DOMSource(document);

			StreamResult streamResult = new StreamResult(outputFile);

			transformer.transform(domSource, streamResult);
		} catch (TransformerException e) {
			log.error("Error writing to output file: " + outputFile.getName(), e);
		}
	}
	
	private void appendSpecieElms(Document doc, Element root) {
		for (Pattern pattern : patternSet) {
			Element specie = doc.createElement("Specie");
			specie.setAttribute("name", pattern.toFormalString());
			specie.setAttribute("id", pattern.toFormalString());
			specie.setAttribute("kdiff", "0");
			specie.setAttribute("kdiffunit", "mu2/s");
			root.appendChild(specie);
		}
	}
	
	private void appendReactionElms(Document doc, Element root) {
		int fwdRateConst = rateConsts.get(0);
		int bwdRateConst = reversible ? rateConsts.get(1) : 0;

		for (String rc : reactantCombinations) {
			String[] reactants = rc.split("\\+");

			for (String pc : productCombinations) {
				Element reaction = doc.createElement("Reaction");
				
				// Set reaction name and ID
				reaction.setAttribute("name", rc + "--" + pc + " reac");
				reaction.setAttribute("id", rc + "--" + pc + "_id");

				// Add reactants
				for (String r : reactants) {
					Element reactant = doc.createElement("Reactant");
					reactant.setAttribute("specieID", r);
					reaction.appendChild(reactant);
				}

				// Add products
				String[] products = pc.split("\\+");
				for (String p : products) {
					Element product = doc.createElement("Product");
					product.setAttribute("specieID", p);
					reaction.appendChild(product);
				}

				// Add forward and reverse rates
				Element fwdRate = doc.createElement("forwardRate");
				fwdRate.appendChild(doc.createTextNode(String.valueOf(fwdRateConst)));
				reaction.appendChild(fwdRate);

				Element bwdRate = doc.createElement("reverseRate");
				bwdRate.appendChild(doc.createTextNode(String.valueOf(bwdRateConst)));
				reaction.appendChild(bwdRate);

				root.appendChild(reaction);
			}
		}
	}
}
