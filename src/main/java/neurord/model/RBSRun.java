package neurord.model;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RBSRun {
	private static final Logger log = LogManager.getLogger(RBSRun.class.getName());
	private SemiBNGLParser parser;

	public RBSRun() {
		this.parser = new SemiBNGLParser();
	}

	public RBSRun loadFromFile(File modelFile) {
		RBSRun rbsRun = new RBSRun();
		boolean txt = modelFile.toString().endsWith(".txt");
		if (txt) {
			try {
				this.parser.parseFile(modelFile.toString());
			} catch (IOException e) {
				log.error("Error reading model file: " + modelFile.getName(), e);
				return null;
			}
		}
		return rbsRun;
	}
	
	public void toXML(File outputFile) {
		try {
			ReactionWriterXML writer = new ReactionWriterXML(outputFile, this.parser.getReactionRules());
			writer.writeToFile();
		} catch (Exception e) {
			log.error("Error writing to XML file: " + outputFile.getName(), e);
		}
	}
}

