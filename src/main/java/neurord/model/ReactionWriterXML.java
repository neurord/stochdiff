package neurord.model;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

public class ReactionWriterXML {
private static final Logger log = LogManager.getLogger(ReactionWriterXML.class.getName());
    
    private final File outputFile;
    private Document document;
    private final List<RuleExpansion> expansions = new ArrayList<>();
    private final List<Boolean> reversibles = new ArrayList<>();
    private final List<Integer> rateConsts = new ArrayList<>();
    private final Set<Pattern> patternSet = new HashSet<>();

    public ReactionWriterXML(File output, List<ReactionRule> rules) {
        this.outputFile = new File(output + ".xml");
        processRules(rules);
    }

    private void processRules(List<ReactionRule> rules) {
        for (ReactionRule rule : rules) {
            RuleExpansion expansion = new RuleExpansion(rule);
            expansion.generateReactionSets();
            expansions.add(expansion);
            reversibles.add(rule.isReversible());
            patternSet.addAll(expansion.getPatternSet());
            rateConsts.addAll(rule.getRateConsts());
        }
    }

    public void writeToFile() {
        try {
            appendDocument();
            saveDocument();
        } catch (Exception e) {
            log.error("Error writing to output file: " + outputFile.getName(), e);
        }
    }

    private void appendDocument() throws ParserConfigurationException {
        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
        this.document = documentBuilder.newDocument();

        Element root = document.createElement("ReactionScheme");
        document.appendChild(root);

        appendSpecieElms(document, root);

        int count = 0;
        for (int i = 0; i < expansions.size(); i++) {
            boolean reversible = reversibles.get(i);
            appendReactionElms(document, root, expansions.get(i), reversible, count);
            count = reversible ? count + 2 : ++count;
        }
    }

    private void saveDocument() throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        DOMSource domSource = new DOMSource(document);
        StreamResult streamResult = new StreamResult(outputFile);
        transformer.transform(domSource, streamResult);
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
        root.appendChild(doc.createTextNode("\n"));
    }

    private void appendReactionElms(Document doc, Element root, RuleExpansion expansion, boolean reversible, int count) {
        List<String> reactantCombinations = expansion.getReactantCombinations();
        List<String> productCombinations = expansion.getProductCombinations();
        int fwdRateConst = rateConsts.get(count);
        int bwdRateConst = reversible ? rateConsts.get(++count) : 0;

        for (String rc : reactantCombinations) {
            String[] reactants = rc.split("\\+");

            for (String pc : productCombinations) {
                Element reaction = doc.createElement("Reaction");
                reaction.setAttribute("name", rc + "--" + pc + " reac");
                reaction.setAttribute("id", rc + "--" + pc + "_id");

                appendSpecies(doc, reaction, "Reactant", reactants);
                appendSpecies(doc, reaction, "Product", pc.split("\\+"));

                appendRateElement(doc, reaction, "forwardRate", fwdRateConst);
                appendRateElement(doc, reaction, "reverseRate", bwdRateConst);

                root.appendChild(reaction);
                root.appendChild(doc.createTextNode("\n"));
            }
        }
    }

    private void appendSpecies(Document doc, Element reaction, String type, String[] species) {
        for (String specie : species) {
            Element elm = doc.createElement(type);
            elm.setAttribute("specieID", specie.trim());
            reaction.appendChild(elm);
        }
    }

    private void appendRateElement(Document doc, Element reaction, String tagName, int rate) {
        Element rateElm = doc.createElement(tagName);
        rateElm.appendChild(doc.createTextNode(String.valueOf(rate)));
        reaction.appendChild(rateElm);
    }
}
