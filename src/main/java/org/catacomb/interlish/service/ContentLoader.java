package org.catacomb.interlish.service;

import java.io.File;


import org.catacomb.interlish.structure.Factory;


public interface ContentLoader extends ResourceLoader {



    boolean hasProviderOf(String name);

    Object getProviderOf(String name);

    boolean hasFactoryFor(String name);

    Factory getFactoryFor(String name);

    Object readObject(String s);

    void newSourceFile(File f, File rootFolder);

    boolean hasTablizerOf(String ocnm);

    Object getTablizerOf(String ocnm);

    boolean hasEditorOf(String type);

    String getEditorPath(String type);

    boolean hasExposerOf(String s);

    Object getExposerOf(String s);


}
