package org.catacomb.interlish.content;


public class StringValue extends PrimitiveValue {


    private String text;

    private int highlight;


    public StringValue() {
        super();
        text = null;
    }

    public StringValue(String s) {
        super();
        text = s;
    }

    public StringValue(String s, boolean b) {
        super(b);
        text = s;
    }


    public String toString() {
        return "StringValue: " + text;
    }

    public void silentSetString(String s) {
        text = s;
        logChange();
    }

    public String getString() {
        reportUse(this);
        return text;
    }

    public String getAsString() {
        reportUse(this);
        return (text != null ? text : "");
    }


    public void clear() {
        silentSetString("");
    }

    public void set(String s) {
        reportableSetString(s, this);
    }

    public void reportableSetString(String s, Object src) {
        silentSetString(s);
        reportValueChange(src);
        reportUse(src);
    }

    public boolean hasNonTrivialValue() {
        return (text != null && text.trim().length() > 0);
    }

    public void copyFrom(StringValue src) {
        silentSetString(src.getString());
    }

    public void append(String string) {
        reportableSetString(text + string, null);
    }

    public void highlightLine(int i) {
        highlight = i;
        // E.info("highlighting line " + i + " of " + text);
        reportValueChange("HIGHLIGHT");  // ADHOC
    }

    public void clearHighlight() {
        highlight = -1;
    }

    public int getHighlight() {
        return highlight;
    }

    public void silentAppendLine(String txt) {
        if (text == null) {
            text = "";
        }
        if (text.length() == 0 || text.endsWith("\n")) {
            // stays as is;
        } else {
            // terminate the previous line;
            text += "\n";
        }
        text += txt;
        text += "\n";
    }


    public String silentGetAsString() {
        return (text != null ? text : "");
    }


}
