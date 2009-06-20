package org.catacomb.serial.quickxml;


import org.catacomb.report.E;


public class Element {

    String name;
    String value;
    Element[] elements;
    boolean free;

    public Element(String s) {
        name = s;
        elements = new Element[0];
        free = false;
    }


    public Element(String s, String sv) {
        name = s;
        elements = new Element[0];
        setValue(sv);
        free = false;
    }



    // free means it can contain any subelements - they rea deduced at read time
    // instead
    // of relyuing on existing contents when populated
    public void setFree() {
        free = true;
    }


    public boolean isFree() {
        return free;
    }


    public void setValue(String txtin) {
        String txt = txtin;
        clear();
        value = "";
        if (txt != null) {
            txt = txt.trim();

            if (txt.startsWith("<")) {
                addElementsFrom(txt);
            } else {
                value = txt;
            }
        }
    }


    public void addElementsFrom(String txtin) {
        String txt = txtin;
        txt = txt.trim();
        if (txt.length() > 0) {
            if (txt.startsWith("<")) {
                txt = txt.substring(1);
                int ic = txt.indexOf(">");
                if (ic <= 0) {
                    E.error(" parsing element (no closing bracket) " + txt);

                } else {
                    String enm = txt.substring(0, ic);
                    txt = txt.substring(ic + 1);

                    String sclo = "</" + enm + ">";
                    int iclo = txt.indexOf(sclo);
                    if (iclo >= 0) {
                        String ev = txt.substring(0, iclo);

                        addElement(enm, ev);
                        txt = txt.substring(iclo + sclo.length());
                        addElementsFrom(txt);
                    }
                }
            } else {
                E.error(" - Element adding sublets needs < but got " + txt);
            }
        }
    }



    public void set(String eltname, String val) {
        Element elt = getElement(eltname);
        if (elt == null) {
            E.error(" - no element " + eltname + "in " + this);

        } else {
            elt.setValue(val);
        }
    }


    // should be moer defensive??;
    public void set(String e1name, String e2name, String val) {
        getElement(e1name).set(e2name, val);
    }


    public void setSubelements(String enm, String[] vs) {
        clear();
        elements = new Element[vs.length];
        for (int i = 0; i < vs.length; i++) {
            elements[i] = new Element(enm);
            elements[i].setValue(vs[i]);
        }
    }


    public void clear() {
        elements = new Element[0];
    }


    public String get() {
        return value;
    }


    public String getValue() {
        return value;
    }


    public String getName() {
        return name;
    }


    public String get(String s) {
        String ret = null;
        Element elt = getElement(s);
        if (elt != null) {
            ret = elt.get();
        }
        return ret;
    }


    public String get(String s1, String s2) {
        Element elt1 = getElement(s1);
        Element elt2 = elt1.getElement(s2);
        return elt2.get();
    }


    public String[] getAll(String s1, String s2) {
        Element elt1 = getElement(s1);
        return elt1.getAll(s2);
    }


    public String[] getValues(String s) {
        return getAll(s);
    }


    public String[] getAll(String s) {
        int nr = 0;
        String[] retp = new String[elements.length];
        for (int i = 0; i < elements.length; i++) {
            if (elements[i].getName().equals(s)) {
                retp[nr++] = elements[i].get();
            }
        }
        String[] ret = new String[nr];
        for (int i = 0; i < nr; i++) {
            ret[i] = retp[i];
        }
        return ret;
    }



    public boolean hasElement(String s, String v) {
        boolean ret = false;
        for (int i = 0; i < elements.length; i++) {
            if (elements[i].getName().equals(s)) {
                if (elements[i].get().equals(v)) {
                    ret = true;
                    break;
                }
            }
        }
        return ret;
    }


    public boolean hasElement(String eltname) {
        boolean ret = false;
        for (int i = 0; i < elements.length; i++) {
            if (elements[i].getName().equals(eltname)) {
                ret = true;
                break;
            }
        }
        return ret;
    }


    public String getValue(String lname) {
        return get(lname);
    }



    public Element[] getElements() {
        return elements;
    }


    public void limitNumber(String eltname, int nmax) {
        String[] sv = getAll(eltname);
        int nel = sv.length;
        int ntg = nel - nmax;
        if (ntg > 0) {
            Element[] ael = getElements();
            int nlst = ael.length;
            while (ntg > 0) {
                nlst = nlst - 1;
                if (ael[nlst].getName().equals(eltname)) {
                    ntg = ntg - 1;
                    removeElement(nlst);
                }
            }

        }
    }



    public void removeAll(String eltname) {
        Element[] ael = getElements();
        for (int i = ael.length - 1; i >= 0; i--) {
            if (ael[i].getName().equals(eltname)) {
                removeElement(i);
            }
        }
    }



    public void add(Element elt) {
        addElement(elt);
    }


    public void add(String eltname) {
        addElement(new Element(eltname));
    }


    public void addElement(String eltname, String ev) {
        addElement(new Element(eltname, ev));
    }


    public void addElement(String en1, String en2, String ev) {
        getElement(en1).addElement(new Element(en2, ev));
    }



    public void prependElementUnique(String eltname, String ev) {
        if (ev == null || ev.length() == 0) {
            return;
        }
        String[] sv = getValues(eltname);
        boolean got = false;
        int itg = -1;
        if (sv != null) {
            for (int i = 0; i < sv.length; i++) {
                if (ev.equals(sv[i])) {
                    got = true;
                    itg = i;
                }
            }
        }
        if (got) {
            removeElement(itg);
        }
        prependElement(eltname, ev);
    }



    public void prependElement(String eltname, String ev) {
        prependElement(new Element(eltname, ev));
    }


    public void addElement(Element elt) {
        int n = elements.length;
        Element[] en = new Element[n + 1];
        for (int i = 0; i < n; i++) {
            en[i] = elements[i];
        }
        en[n] = elt;
        elements = en;
    }


    public void removeElement(int itg) {
        int n = elements.length;
        Element[] en = new Element[n - 1];
        for (int i = 0; i < itg; i++) {
            en[i] = elements[i];
        }
        for (int i = itg; i < n - 1; i++) {
            en[i] = elements[i + 1];
        }
        elements = en;
    }


    public void setElement(String name, String value) {
        removeAll(name);
        addElement(name, value);

    }



    public void prependElement(Element elt) {
        int n = elements.length;
        Element[] en = new Element[n + 1];
        for (int i = 0; i < n; i++) {
            en[i + 1] = elements[i];
        }
        en[0] = elt;
        elements = en;
    }


    public void randomFill() {
        if (elements.length > 0) {
            for (int i = 0; i < elements.length; i++) {
                elements[i].randomFill();
            }
        } else {
            value = "" + Math.random();
        }
    }


    public Element getElement(String sn) {
        Element ret = null;
        for (int i = 0; i < elements.length; i++) {
            if (elements[i].getName().equals(sn)) {
                ret = elements[i];
            }
        }
        return ret;
    }


    public void populateFrom(String srcin) {
        String src = srcin;
        Element elt = this;

        String enm = elt.getName();
        String otag = "<" + enm + ">";
        String ctag = "</" + enm + ">";

        int io = src.indexOf(otag);
        if (io >= 0) {
            src = src.substring(io + otag.length(), src.length());
            int ic = src.indexOf(ctag);
            if (ic > 0) {
                src = src.substring(0, ic);

                Element[] ea = elt.getElements();
                if (!elt.isFree() && ea != null && ea.length > 0) {
                    for (int i = 0; i < ea.length; i++) {
                        ea[i].populateFrom(src);
                    }
                } else {
                    elt.setValue(src);
                }
            }
        }
    }



    public String dump() {
        StringBuffer sb = new StringBuffer();

        appendTo(sb, "");

        return sb.toString();
    }


    private void appendTo(StringBuffer sb, String indent) {
        sb.append(indent + "<" + name + ">");

        int nel = elements.length;
        if (nel > 0) {
            sb.append("\n");
            for (int i = 0; i < nel; i++) {
                elements[i].appendTo(sb, indent + "   ");
            }
            sb.append(indent);

        } else {
            String sv = value;
            if (sv == null) {
                sv = "";
            }
            sv = sv.trim();
            sb.append(sv);
        }
        sb.append("</" + name + ">\n");
        if (nel > 0) {
            sb.append("\n");
        }
    }

}
