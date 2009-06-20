package org.catacomb.interlish.service;


public interface ScriptReport {


    public boolean allOK();

    public String getText();

    public String getErrorText();

    public int getErrorLine();


}
