package org.catacomb.interlish.service;


public interface ResourceLoader {

    Object getResource(String configPath, String selector);

}
