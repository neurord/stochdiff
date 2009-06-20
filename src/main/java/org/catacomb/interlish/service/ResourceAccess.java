package org.catacomb.interlish.service;

import org.catacomb.interlish.reflect.ExternInstantiator;
import org.catacomb.report.E;


public class ResourceAccess {

    static ResourceLoader resourceLoader;

    static ContentLoader contentLoader;

    static ExternInstantiator externInstantiator;

    public static void setResourceLoader(ResourceLoader rl) {
        resourceLoader = rl;
    }

    public static ResourceLoader getResourceLoader() {
        if (resourceLoader == null) {
            E.error("request for content loader, but none is available: should\n " +
                    "instantiate a loader (implementing ContentLoader) and set it\n " +
                    "in ResourceAccess");
        }
        return resourceLoader;
    }

    public static void setContentLoader(ContentLoader cl) {
        contentLoader = cl;
        resourceLoader = cl;
    }

    public static ContentLoader getContentLoader() {
        if (contentLoader == null) {
            E.error("request for content loader, but none is available: should\n " +
                    "instantiate a loader (implementiong ContentLoader) and set it\n " +
                    "in ResourceAccess");
        }
        return contentLoader;
    }

    public static boolean hasContentLoader() {
        return (contentLoader != null);
    }


    public static ExternInstantiator getExternInstantiator() {
        if (externInstantiator == null) {
            externInstantiator = new ExternInstantiator();
        }
        return externInstantiator;
    }

}
