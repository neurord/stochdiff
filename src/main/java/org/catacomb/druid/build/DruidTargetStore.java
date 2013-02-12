package org.catacomb.druid.build;

import org.catacomb.druid.gui.base.DruAssemblyPanel;
import org.catacomb.druid.market.HookupBoard;
import org.catacomb.interlish.report.PrintProgressReport;
import org.catacomb.interlish.structure.*;
import org.catacomb.report.E;


import java.util.HashMap;
import java.util.Map;



public class DruidTargetStore implements TargetStore {


    HashMap<String, Object> hashMap;

    HashMap<String, Object> shortHM;

    private HookupBoard hookupBoard;

    InfoReceiver infoReceiver;


    public DruidTargetStore() {
        hashMap = new HashMap<String, Object>();
        shortHM = new HashMap<String, Object>();
    }



    HashMap<String, Object> getHashMap() {
        return hashMap;
    }


    public HashMap<String, Object> getIdentifiedComponentMap() {
        return hashMap;
    }



    public SaverLoader getModel() {
        // TODO needed?
        return null;
    }




    public void addComponent(String id, Object cpt) {
        if (hashMap.containsKey(id)) {
            E.warning("duplicate target ids: " + id + " " + hashMap.get(id) + "  and " + cpt);
        }
        hashMap.put(id, cpt);

        String ss = id.substring(id.lastIndexOf(".")  + 1, id.length());
        if (shortHM.containsKey(ss)) {
            E.warning("duplicates in shortHM " + ss + " must use full path");
        } else {
            shortHM.put(ss, cpt);
        }
    }



    private Object quietGet(String id) {
        Object ret = null;

        if (id.indexOf(".") < 0) {
            ret = shortHM.get(id);


        } else if (id.startsWith("*")) {
            ret = wildcardGet(id);
        } else {
            if (hashMap.containsKey(id)) {
                ret = hashMap.get(id);
            }
        }
        return ret;
    }


    public Object get(String id) {
        Object ret = quietGet(id);

        if (ret == null) {
            //    E.error("target store - no such item " + id);

        }
        return ret;
    }


    public void printAvailable() {
        if (hashMap == null || hashMap.size() == 0) {
            E.info("there are no entries in the target store");
        } else {
            for (Map.Entry<String, Object> ment : hashMap.entrySet()) {
                E.info("known item : " + ment.getKey() + " " + ment.getValue());
            }
        }
    }


    public boolean has(String id) {
        Object obj = quietGet(id);
        return (obj != null);
    }



    // make this smarter!!!!!!! EFF
    private Object wildcardGet(String s) {
        Object ret = null;

        if (s.startsWith("*.")) {
            // bit sloppy - ignore the dot, maybe should still check for duplicate matches;
            ret = quietGet(s.substring(2, s.length()));
        }

        if (ret == null) {
            String sm = s.substring(1, s.length());

            String fm = null;
            for (String key : hashMap.keySet()) {
                if (key.endsWith(sm)) {
                    if (fm == null) {
                        fm = key;
                        ret = hashMap.get(key);
                    } else {
                        E.error("multiple items match wildcard " + s);
                        E.info("first=" + fm);
                        E.info("also: " + key);
                    }
                }
            }
        }
        return ret;
    }




    public void setStringValue(String id, String value) {
        Object obj = get(id);
        if (obj instanceof StringValueSettable) {
            ((StringValueSettable)obj).setStringValue(value);
        } else {
            E.error("cannot set " + value + " in " + id);
        }
    }

    public void setObjectValue(String id, Object val) {
        Object obj = get(id);
        if (obj  instanceof ObjectValueSettable) {
            ((ObjectValueSettable)obj).setObjectValue(val);

        } else {
            E.error("cannot set " + val + " in " + id);
        }

    }



    public ProgressReport getProgressReport(String id) {
        ProgressReport ret = null;
        Object obj = get(id);
        if (obj != null && obj instanceof ProgressReport) {
            ret = (ProgressReport)obj;

        } else {
            E.error("cannot get progress report " + id + " (got " + obj + ")");
            ret = new PrintProgressReport();
        }
        return ret;
    }




    public PopulableMenu getSubMenu(String id) {
        Object obj = get(id);
        PopulableMenu ret = null;

        if (obj instanceof PopulableMenu) {
            ret = (PopulableMenu)obj;

        } else {
            E.error("cannot get subMenu " + id + " (got " + obj + ")");
        }
        return ret;
    }


    public Button getButton(String id) {
        Object obj = get(id);
        Button ret = null;

        if (obj instanceof Button) {
            ret = (Button)obj;

        } else {
            E.error("cannot get button " + id + " (got " + obj + ")");
        }
        return ret;
    }



    public AssemblyEditor getAssemblyEditor() {
        DruAssemblyPanel ret = null;

        for (Object obj : hashMap.values()) {
            if (obj instanceof DruAssemblyPanel) {
                ret = (DruAssemblyPanel)obj;
                break;
            }
        }

        if (ret == null) {
            E.error("no assembly panel in target store");
        }
        return ret;
    }



    public void setHookupBoard(HookupBoard hb) {
        hookupBoard = hb;

        hb.addVisible("TargetStore", this, "access");
    }


    public Marketplace getMarketplace() {
        return hookupBoard;
    }



    public HookupBoard getHoookupBoard() {
        return hookupBoard;
    }



    public void clear() {
        hashMap.clear();
    }



    public Dialog getDialog(String s) {
        Object obj = get(s);

        Dialog ret = null;
        if (obj instanceof Dialog) {
            ret = (Dialog)obj;
        } else {
            E.error("wrong type " + s);
        }

        return ret;
    }



    public TextField getTextField(String s) {
        Object obj = get(s);

        TextField ret = null;
        if (obj instanceof TextField) {
            ret = (TextField)obj;
        } else {
            E.error("wrong type " + s);
        }

        return ret;
    }



    public TextArea getTextArea(String s) {
        Object obj = get(s);

        TextArea ret = null;
        if (obj instanceof TextArea) {
            ret = (TextArea)obj;
        } else {
            E.error("wrong type " + s);
        }

        return ret;
    }



    public Choice getChoice(String s) {
        Object obj = get(s);

        Choice ret = null;
        if (obj instanceof Choice) {
            ret = (Choice)obj;
        } else {
            E.error("wrong type " + s);
        }

        return ret;
    }



    public Toggle getToggle(String s) {
        Object obj = get(s);

        Toggle ret = null;
        if (obj instanceof Toggle) {
            ret = (Toggle)obj;
        } else {
            E.error("wrong type " + s);
        }

        return ret;
    }


    public void setInfoReceiver(InfoReceiver ir) {
        infoReceiver = ir;
    }

    public InfoReceiver getInfoReceiver() {
        return infoReceiver;
    }



    public StringValueEditor getStringValueEditor(String sid) {
        Object obj = get(sid);
        StringValueEditor ret = null;
        if (obj instanceof StringValueEditor) {
            ret = (StringValueEditor)obj;
        } else {
            E.error("wrong type item " + sid + " - " + obj);
        }

        return ret;
    }

    public IntegerValueEditor getIntegerValueEditor(String sid) {
        Object obj = get(sid);
        IntegerValueEditor ret = null;
        if (obj instanceof IntegerValueEditor) {
            ret = (IntegerValueEditor)obj;
        } else {
            E.error("wrong type item " + sid + " - " + obj);
        }

        return ret;
    }



    public DoubleValueEditor getDoubleValueEditor(String sid) {
        Object obj = get(sid);
        DoubleValueEditor ret = null;
        if (obj instanceof DoubleValueEditor) {
            ret = (DoubleValueEditor)obj;
        } else {
            E.error("wrong type item " + sid + " - " + obj);
        }

        return ret;
    }

    public ColorValueEditor getColorValueEditor(String sid) {
        Object obj = get(sid);
        ColorValueEditor ret = null;
        if (obj instanceof ColorValueEditor) {
            ret = (ColorValueEditor)obj;
        } else {
            E.error("wrong type item " + sid + " - " + obj);
        }

        return ret;
    }


}
