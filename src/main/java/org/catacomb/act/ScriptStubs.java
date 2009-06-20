package org.catacomb.act;

import java.util.ArrayList;


public class ScriptStubs {


    private ArrayList<MethodStub> stubs;

    public ScriptStubs() {
        stubs = new ArrayList<MethodStub>();
    }


    public ArrayList<MethodStub> getStubs() {
        return stubs;
    }


    public void add(MethodStub ms) {
        stubs.add(ms);
    }

}
