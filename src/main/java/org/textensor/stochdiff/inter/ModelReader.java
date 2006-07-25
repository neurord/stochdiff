package org.textensor.stochdiff.inter;

import java.io.File;

import org.textensor.report.E;
import org.textensor.stochdiff.model.*;
import org.textensor.util.FileUtil;
import org.textensor.xml.ReflectionInstantiator;
import org.textensor.xml.XMLReader;


public class ModelReader {

    public static SDRun read(File modelFile) {

        ReflectionInstantiator rin = new ReflectionInstantiator();
        rin.checkAddPackage(new ModelBase());
        XMLReader xmlr = new XMLReader(rin);


        SDRun sdm = (SDRun)readFile(modelFile, xmlr);

        File parentFile = modelFile.getParentFile();
        File freact = getFile(parentFile, sdm.reactionSchemeFile);
        File fmorph = getFile(parentFile, sdm.morphologyFile);
        File fstim = getFile(parentFile, sdm.stimulationFile);
        File finitc = getFile(parentFile, sdm.initialConditionsFile);


        sdm.setReactionScheme((ReactionScheme)readFile(freact, xmlr));
        sdm.setMorphology((Morphology)readFile(fmorph, xmlr));
        sdm.setStimulationSet((StimulationSet)readFile(fstim, xmlr));
        sdm.setInitialConditions((InitialConditions)readFile(finitc, xmlr));

        return sdm;
    }


    private static File getFile(File fparent, String rpath) {
        File ret = new File(fparent, rpath);
        if (ret.exists()) {
            // OK;
        } else {
            ret = new File(fparent, rpath + ".xml");
            if (! ret.exists()) {
                E.error("cant find file " + rpath + " in folder " + fparent + " (tried plain and adding .xml)");
            }
        }
        return ret;
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
