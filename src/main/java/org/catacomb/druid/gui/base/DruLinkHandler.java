package org.catacomb.druid.gui.base;

import org.catacomb.interlish.structure.*;
import org.catacomb.report.E;


import java.net.URL;

public class DruLinkHandler implements HyperlinkHandler {


    PageSupplier pageSupplier;
    PageDisplay pageDisplay;

    String currentAddress;


    public DruLinkHandler(PageSupplier ps, PageDisplay pd) {
        pageSupplier = ps;
        pageDisplay = pd;

    }


    public void hyperlinkClicked(String sin) {
        String s = sin;
        if (currentAddress != null) {

            if (s.equals("...")) {
                s = "";

            } else if (s.equals(".")) {
                if (currentAddress.endsWith("/")) {
                    s = currentAddress.substring(0, currentAddress.length()-1);
                } else {
                    s = currentAddress;
                }

            } else if (s.startsWith("./")) {
                if (currentAddress.endsWith("/")) {
                    s = currentAddress + s.substring(2, s.length());
                } else {
                    s = currentAddress + s.substring(1, s.length());
                }

            } else {
                if (currentAddress.endsWith("/")) {
                    s = currentAddress + s;
                } else {
                    s = currentAddress + "/" + s;
                }
            }
        }



        if (pageSupplier.canGet(s)) {
            pageDisplay.showPage(pageSupplier.getPage(s));
            currentAddress = s;

        } else {
            E.warning("page supplier cannot get " + s);
        }
    }


    public void follow(URL u) {
        E.error("dru link handler wont follow urls " + u);
    }

}
