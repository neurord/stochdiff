package org.catacomb.interlish.service;

import java.io.File;


public interface AppPersistProvider {

    boolean hasValueFor(String pel);

    String getValueFor(String pel);

    void addRecentFile(File f);

    void setValue(String lab, String val);

    void forceExit();

    boolean hasValue(String tag, String val);

    String[] getRecentPaths();

}
