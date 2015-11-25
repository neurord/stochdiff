package neurord.neuroml;

import java.util.ArrayList;

public class meta {

    String content = "";

    public String tag;
    public String value;


    public ArrayList<MetaItem> items;
    public ArrayList<meta> metas;

    public String getLabel() {
        return content;
    }
}
