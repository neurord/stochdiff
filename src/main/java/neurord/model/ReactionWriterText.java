package neurord.model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The class represents reaction equations in their fully specified format and writes them 
 * to an output file.
 */
public class ReactionWriterText {
	private static final Logger log = LogManager.getLogger(ReactionWriterText.class.getName());
	private RuleExpansion expansion;
	private List<String> reactantCombinations;
	private List<String> productCombinations;
	private boolean reversible;
	
	private final File outputFile;
	private BufferedWriter writer;
	
	public ReactionWriterText(File output, ReactionRule rule) {
		this.expansion = new RuleExpansion(rule);
		this.expansion.generateReactionSets();
		this.reactantCombinations = expansion.getReactantCombinations();
		this.productCombinations = expansion.getProductCombinations();
		this.reversible = rule.isReversible();
		this.outputFile = new File(output + ".out");
		initializeWriter();
	}
	
	private void initializeWriter() {
        try {
            this.writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile)));
        } catch (IOException e) {
            log.error("Error initializing writer for output file: " + outputFile.getName(), e);
        }
    }
	
	public void writeReactionsToFile() {
        String arrow = getArrow();

        try {
            for (String reactants : reactantCombinations) {
                for (String products : productCombinations) {
                    String reaction = reactants + ' ' + arrow + ' ' + products;
                    writer.write(reaction);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            log.error("Error writing reactions to file: " + outputFile.getName(), e);
        } finally {
            closeWriter();
        }
    }
	
	private String getArrow() {
		return reversible ? "<->" : "->";
	}
	
	private void closeWriter() {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                log.error("Error closing writer for output file: " + outputFile.getName(), e);
            }
        }
    }
}
