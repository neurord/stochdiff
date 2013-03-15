package org.catacomb.interlish.reflect;

import org.catacomb.be.ReReferencable;
import org.catacomb.interlish.service.ContentLoader;
import org.catacomb.interlish.service.ResourceAccess;
import org.catacomb.interlish.structure.*;
import org.catacomb.report.E;




public class ObjectBuilder {

    Constructor instantiator;



    final static int UNKNOWN = 0;
    final static int CLASS = 1;
    final static int FACTORY = 2;
    final static int PROVIDER = 3;

    ContentLoader contentLoader;

    Object wkObject;
    String wkID;

    Element workElt;


    public ObjectBuilder(Constructor inst) {
        instantiator = inst;
        if (ResourceAccess.hasContentLoader()) {
            contentLoader = ResourceAccess.getContentLoader();
        }
    }

    /*
       private XMLLoader getLoader() {
          if (loader == null) {
             loader = XMLLoader.getLoader();
          }
          return loader;
       }
    */


    public Object buildFromElement(Element elt) {
        workElt = elt;
        Object ret = null;

        if (elt.hasAttribute("id")) {
            wkID = elt.getAttribute("id");
        }


        int mode = UNKNOWN;

        if (contentLoader == null ||
                elt.hasAttribute("class") ||
                elt.hasAttribute("package")) {
            mode = CLASS;

        } else if (contentLoader.hasFactoryFor(elt.getName())) {
            mode = FACTORY;

        } else if (contentLoader.hasProviderOf(elt.getName())) {
            mode = PROVIDER;
        }

        if (mode == CLASS) {
            ret = refBuildFromElement(null, elt);

        } else if (mode == FACTORY) {
            Object ospec = contentLoader.getFactoryFor(elt.getName());
            Factory fac = (Factory)ospec;

            if (fac == null) {
                E.error(" - loader returned null factory for " + elt.getName());

            } else {
                ret = fac.make(elt.getName());
                wkObject = ret;
                fac.populate(ret, elt);

                if (ret instanceof ReReferencable) {
                    ((ReReferencable)ret).reReference();
                }
            }


        } else if (mode == PROVIDER) {
            ret = contentLoader.getProviderOf(elt.getName());
            wkObject = ret;


            populate(ret, elt);

        } else {
            E.error("don't know what to do with element (no class, factory or provider) " + elt);
        }

        return ret;
    }



    private String simpleContentSerialization(Element elt) {
        StringBuffer sb = new StringBuffer();
        appendContent(elt, sb);
        return sb.toString();
    }

    private void appendContent(Element topelt, StringBuffer sb) {
        if (topelt.getText() != null) {
            sb.append(topelt.getText());
        }
        if (topelt.hasElements()) {
            for (Element elt : topelt.getElements()) {

                if (elt.hasAttributes()) {
                    sb.append("\n<" + elt.getName());
                    for (Attribute att : elt.getAttributes()) {
                        sb.append(" " + att.getName() + "=\"" + att.getValue() + "\"");
                    }
                    sb.append(">");
                } else {
                    sb.append("<" + elt.getName() + ">");
                }
                appendContent(elt, sb);
                sb.append("</" + elt.getName() + ">\n");
            }
        }

    }



    public Object refBuildFromElement(Object parent, Element elt) {
        Object ret = null;

        ret = instantiator.getChildObject(parent, elt.getName(), elt.getAttributeArray());

        if (ret instanceof String) {

            ret = simpleContentSerialization(elt);


        } else if (ret instanceof String[]) {
            ret = readStringArray(elt);

        } else {
            populate(ret, elt);
        }

        return ret;
    }


    private String[] readStringArray(Element elt) {
        Element[] ea = elt.getElementArray();
        String[] ret = null;
        if (ea == null) {
            ret = new String[0];

        } else {
            ret = new String[ea.length];
            for (int i = 0; i < ea.length; i++) {
                Element sub = ea[i];
                if (sub.getName().equals("item")) {
                    ret[i] = sub.getAttribute("value");
                } else {
                    E.warning("wrong element type in a string array " + sub);
                }
            }
        }
        return ret;
    }

    /*
    private String[][] readQuantityArray(Element elt) {
       Element[] ea = elt.getElementArray();
       String[][] ret = null;
       if (ea == null) {
          ret = new String[0][];

       } else {
          ret = new String[ea.length][2];
          for (int i = 0; i < ea.length; i++) {
             Element sub = ea[i];
             if (sub.getName().equals("quantity")) {
                ret[i][0] = sub.getAttribute("amount");
                ret[i][1] = sub.getAttribute("of");
             } else {
                E.warning("wrong element type in a string array " + sub);
             }
          }
       }
       return ret;
    }
    */


    public void populate(Object target, Element elt) {

        if (target instanceof ElementReader) {
            ((ElementReader)target).populateFrom(elt);


        } else {

            if (elt.hasAttributes()) {

                for (Attribute att : elt.getAttributes()) {
                    if (att.getName().equals("archive-hash")) {
                        E.deprecate();

                    } else {
                        instantiator.setAttributeField(target, att.getName(), att.getValue());
                    }
                }
            }

            if (elt.hasElements()) {
                for (Element childelt : elt.getElements()) {


                    Object child = refBuildFromElement(target, childelt);

                    if (child == null) {
                        E.error("got null object from element " + childelt + " " + target.getClass().getName());
                    } else {
                        instantiator.setField(target, childelt.getName(), child);
                    }
                }
            }


            if (elt.hasText()) {
                if (target instanceof BodyValued) {
                    ((BodyValued)target).setBody(elt.getText());
                } else {
                    String ss = elt.serialize();
                    int idis = ss.indexOf("//DISABLED");
                    if (idis >= 0 && idis < 50) {
                        // just ignore it
                    } else {
                        E.linkToWarning("nowhere to put body text: in (" +
                                        target.getClass().getName() + ") may relate to " + wkID + " " + wkObject, target);
                    }
                }
            }

        }






        if (target instanceof ReReferencable) {
            ((ReReferencable)target).reReference();
        }
    }


}
