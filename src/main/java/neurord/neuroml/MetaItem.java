package neurord.neuroml;

public class MetaItem {

    public String name;
    public String value;

    public MetaItem meta_tag;
    public MetaItem meta_name;
    public MetaItem meta_value;
    public MetaItem meta_property;


    meta meta;

    public MetaItem(String name) {
        this.name = name;
    }

    public void setName(String s) {
        name = s;
    }

    public void setValue(String s) {
        value = s;
    }

    public void setBodyValue(String s) {
        value = s;
    }

}
