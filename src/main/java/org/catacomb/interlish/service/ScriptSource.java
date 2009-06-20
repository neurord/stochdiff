package org.catacomb.interlish.service;

import org.catacomb.act.ScriptStubs;


public interface ScriptSource {

    public static final int TYPE_STATE = 1;
    public static final int TYPE_STRUCTURE = 2;
    public static final int SIGNAL_TYPE = 3;

    public static final int MODEL_CONTROL = 10;

    public ScriptStubs getStubs(Object host, int role);

    public String getScriptInfo(Object host, int role);

    public void writeScripts(Object obj);

}
