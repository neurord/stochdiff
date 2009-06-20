package org.catacomb.druid.xtext.edit;

import org.catacomb.druid.xtext.base.Guise;
import org.catacomb.interlish.annotation.Editable;
import org.catacomb.interlish.content.ColorValue;
import org.catacomb.interlish.content.StringValue;
import org.catacomb.interlish.structure.*;
import org.catacomb.report.E;


import java.awt.Color;


public class GuiseController implements Controller, Targeted {

    Guise guise;


    @Editable(xid="family")
    public StringValue fontFamily;

    @Editable(xid="size")
    StringValue fontSize;

    @Editable(xid="style")
    StringValue fontStyle;

    @Editable(xid="color")
    ColorValue fontColor;


    ChangeNotifiable changeNotifiable;


    public GuiseController() {
        super();
        fontFamily = new StringValue("serif");
        fontSize  = new StringValue("12");
        fontStyle = new StringValue("plain");
        fontColor = new ColorValue(0);
    }


    public void setChangeNotifiable(ChangeNotifiable cn) {
        changeNotifiable = cn;
    }



    public void setTarget(Object obj) {
        guise = (Guise)obj;

        fontFamily.reportableSetString(guise.getFontFamily(), this);
        fontSize.reportableSetString(guise.getFontSize(), this);
        fontStyle.reportableSetString(guise.getFontStyle(), this);
        fontColor.reportableSetColor(guise.getFontColor().getRGB(), this);

    }



    public Object getTarget() {
        return guise;
    }



    public void attached() {
    }


    public void applyFontProperties() {
        if (guise != null) {
            guise.setFontFamily(fontFamily.getString());
            guise.setFontSize(fontSize.getString());
            guise.setFontStyle(fontStyle.getString());
            guise.setFontColor(new Color(fontColor.getIntColor()));
        } else {
            E.warning("not applying properties - no guise set");
        }

        if (changeNotifiable != null) {
            changeNotifiable.changed(this);
        }
    }


}
