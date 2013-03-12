//5 16 2007: modified by RO
//written by Robert Cannon
package org.textensor.stochdiff.inter;

import java.io.File;

import org.textensor.report.E;
import org.textensor.stochdiff.model.*;
import org.textensor.stochdiff.neuroml.NeuroMLBase;
import org.textensor.util.FileUtil;
import org.textensor.xml.ReflectionInstantiator;
import org.textensor.xml.XMLReader;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class ModelReader {
    static final Logger log = LogManager.getLogger(ModelReader.class);

    public static SDRun read(File modelFile) {

        ReflectionInstantiator rin = new ReflectionInstantiator();
        rin.checkAddPackage(new ModelBase());
        rin.checkAddPackage(new NeuroMLBase());

        XMLReader xmlr = new XMLReader(rin);


        SDRun sdm = (SDRun)readFile(modelFile, xmlr);

        File parentFile = modelFile.getParentFile();
        File freact = getFile(parentFile, sdm.reactionSchemeFile);
        File fmorph = getFile(parentFile, sdm.morphologyFile);
        File fstim = getFile(parentFile, sdm.stimulationFile);
        File finitc = getFile(parentFile, sdm.initialConditionsFile);
        //<--RO
        //------------------
        File foutput = getFile(parentFile, sdm.outputSchemeFile);
        //------------------
        //RO-->


        sdm.setReactionScheme((ReactionScheme)readFile(freact, xmlr));
        Object ob = readFile(fmorph, xmlr);
        if (ob instanceof Transitional) {
            ob = ((Transitional)ob).getFinal();
        }
        sdm.setMorphology((Morphology)ob);
        sdm.setStimulationSet((StimulationSet)readFile(fstim, xmlr));
        sdm.setInitialConditions((InitialConditions)readFile(finitc, xmlr));
        //<--RO
        //------------------
        sdm.setOutputScheme((OutputScheme)readFile(foutput, xmlr));
        //------------------
        //RO-->
        return sdm;
    }


    private static File getFile(File fparent, String rpath) {
        File ret = new File(fparent, rpath);
        if (ret.exists())
            return ret;

        ret = new File(fparent, rpath + ".xml");
        if (ret.exists())
            return ret;

        log.error("cannot find file '{}' or '{}.xml' in folder '{}'",
                  rpath, rpath, fparent != null ? fparent : '.');
        throw new RuntimeException("cannot find file '" + rpath + "'");
    }

    private static Object readFile(File f, XMLReader xmlr) {
        Object ret = null;
        if (f.exists()) {
            String txt = FileUtil.readStringFromFile(f);
            ret = xmlr.read(txt);
        }
        return ret;
    }


}
