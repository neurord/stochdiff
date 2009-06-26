package org.catacomb.interlish.service;

import java.util.HashMap;

import org.catacomb.act.ScriptStubs;
import org.catacomb.be.Instantiator;
import org.catacomb.report.E;



public class ScriptManager {



    static ScriptManager instance;

    private HashMap<String, ScriptSource> sourceHM;

    private ScriptMerger scriptMerger;
    private ScriptChecker scriptChecker;
    private ScriptInfo scriptInfo;

    private Instantiator typeInstantiator;
    private Instantiator modelInstantiator;



    public static ScriptManager get() {
        if (instance == null) {
            instance = new ScriptManager();
        }
        return instance;
    }


    public ScriptManager() {
        sourceHM = new HashMap<String, ScriptSource>();
    }


    public void addUtility(Object obj) {
        if (obj instanceof ScriptMerger) {
            scriptMerger = (ScriptMerger)obj;

        } else if (obj instanceof ScriptChecker) {
            scriptChecker = (ScriptChecker)obj;

        } else if (obj instanceof ScriptInfo) {
            scriptInfo = (ScriptInfo)obj;

        } else {
            E.warning("unknown utility? " + obj);
        }
    }


    public ScriptInfo getScriptInfo() {
        return scriptInfo;
    }


    public void setModelInstantiator(Instantiator inst) {
        modelInstantiator = inst;
        //    E.info("set model instantiator " + inst);
    }

    public Instantiator getModelInstantiator() {
        if (modelInstantiator == null) {
            E.error("asked for instantiator before it is set?");
        }
        return modelInstantiator;
    }

    public void setTypeInstantiator(Instantiator inst) {
        typeInstantiator = inst;
    }

    public Instantiator getTypeInstantiator() {
        if (typeInstantiator == null) {
            E.error("asked for instantiator before it is set?");
        }
        return typeInstantiator;
    }


    public ScriptReport checkScripts(Object obj) {
        ScriptReport ret = null;
        if (scriptChecker == null) {
            // return an error report?
        } else {
            ret = scriptChecker.checkScript(obj);
        }
        return ret;
    }



    public void addScriptSource(Object hostExample, ScriptSource p) {
        String hcnm = null;
        if (hostExample instanceof String) {
            hcnm = (String)hostExample;
        } else {
            hcnm = hostExample.getClass().getName();
        }
        sourceHM.put(hcnm, p);
    }

    private ScriptSource getScriptSource(Object host, int role) {
        ScriptSource ss = null;
        String hcl = host.getClass().getName();
        if (sourceHM.containsKey(hcl)) {
            ss = sourceHM.get(hcl);
        } else {
            E.error("need to generate code for " + hcl + " but no suitable script source is registered");
        }
        return ss;
    }

    public ScriptStubs getStubs(Object host, int role) {
        ScriptStubs ret = null;
        ScriptSource ss = getScriptSource(host, role);
        if (ss != null) {
            ret = ss.getStubs(host, role);
        }
        return ret;
    }

    public String getStubInfo(Object host, int role) {
        String ret = null;
        ScriptSource ss = getScriptSource(host, role);
        if (ss != null) {
            ret = ss.getScriptInfo(host, role);
        }
        return ret;
    }

    public String mergeStubs(String oldscript, String stubs) {
        return scriptMerger.mergeStubs(oldscript, stubs);
    }


    public void writeScripts(Object obj, int role) {
        ScriptSource ss = getScriptSource(obj, role);
        if (ss != null) {
            ss.writeScripts(obj);
        }
    }



}
