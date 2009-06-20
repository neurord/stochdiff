package org.catacomb.druid.load;

import java.util.HashMap;

import org.catacomb.druid.manifest.DecManifest;
import org.catacomb.interlish.resource.Role;


public class RoleMap {

    protected HashMap<String, HashMap<String, Role>> functionMaps;

    public RoleMap() {
        functionMaps = new HashMap<String, HashMap<String, Role>>();
    }



    public void addRoles(DecManifest dm) {
        for (Role r : dm.getRoles()) {
            addRole(r);
        }
    }


    private void addRole(Role r) {
        // E.info("adding role " + r.getFunction() + " " + r.getValue());
        (getFunctionMap(r.getFunction())).put(r.getValue(), r);
    }


    private HashMap<String, Role> getFunctionMap(String s) {
        HashMap<String, Role> ret = null;

        if (functionMaps.containsKey(s)) {
            ret = functionMaps.get(s);

        } else {
            ret = new HashMap<String, Role>();
            functionMaps.put(s, ret);

        }
        return ret;
    }



    public boolean hasRole(String role, String subject) {
        boolean ret = (getFunctionMap(role).containsKey(subject));

        if (!ret) {
            //  E.info("has role returning false for " + role + " " + subject);
        }
        return ret;
    }

    public Role getRole(String role, String subject) {
        return (getFunctionMap(role).get(subject));
    }

}
