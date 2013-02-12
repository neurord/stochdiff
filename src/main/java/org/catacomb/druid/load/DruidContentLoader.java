package org.catacomb.druid.load;

import java.io.File;

import org.catacomb.interlish.service.ContentLoader;
import org.catacomb.interlish.structure.Factory;




import org.catacomb.be.FileSourcable;
import org.catacomb.interlish.resource.ResourceRole;
import org.catacomb.interlish.resource.Role;
import org.catacomb.interlish.service.ResourceAccess;
import org.catacomb.interlish.structure.*;
import org.catacomb.interlish.util.JUtil;
import org.catacomb.report.E;
import org.catacomb.serial.Deserializer;
import org.catacomb.druid.manifest.*;
import org.catacomb.util.FileUtil;
import org.catacomb.util.NetUtil;
import org.catacomb.util.PathUtil;

import java.net.URL;




public class DruidContentLoader implements ContentLoader {


    private XMLStore store;

    private RoleMap roleMap;

    // private Stack<String> pathStack;

    Class rootClass;



    public static void initLoader(Object projroot) {
        DruidContentLoader loader = new DruidContentLoader();
        ResourceAccess.setContentLoader(loader);
        loader.init(projroot);
    }

    private DruidContentLoader() {
    }



    public static Object getFromURL(URL url) {
        String s = NetUtil.readStringFromURL(url);
        Object obj = Deserializer.deserialize(s);
        return obj;
    }



    // POSERR reading the manifest shouldn't access the loader again via deserializer
    public void init(Object projroot) {
        store = new XMLStore();
        roleMap = new RoleMap();

        // pathStack = new Stack<String>();

        String xtxt = JUtil.getRelativeResource("DecManifest.xml");
        DecManifest dm = (DecManifest)(Deserializer.deserialize(xtxt));
        roleMap.addRoles(dm);
        store.addClasspathManifest(dm);


        if (projroot != null) {
            String atxt = JUtil.getRelativeResource(projroot, "DecManifest.xml");
            DecManifest adm = (DecManifest)(Deserializer.deserialize(atxt));
            roleMap.addRoles(adm);
            store.addClasspathManifest(adm);

            //  E.info("read dm " + adm);
        }
    }




    public void addFileSystemManifest(DecManifest dm, File fdtop) {
        store.addFileSystemManifest(dm, fdtop);
    }


    public Object loadFromFileNoCache(File f) {
        String s = FileUtil.readStringFromFile(f);
        Object obj = Deserializer.deserialize(s);
        return obj;
    }


    public Object getResource(String name, String selector) {
        DecFile decFile = getSource(name, selector);
        Object ret = null;
        if (decFile != null) {
            if (decFile.hasCachedObject()) {
                // nothing to do
            } else {
                //     E.info("loading to object " + name);

                loadToObject(decFile);
            }
            ret = decFile.getCachedObject();
        }
        return ret;
    }




    private void loadToObject(DecFile decFile) {
        if (decFile.hasCachedText()) {
            loadObject(decFile);

        } else {
            loadText(decFile);
            loadObject(decFile);
        }
    }


    private void loadObject(DecFile decFile) {
        // E.info("loading obj from " + decFile);
        E.cacheAction(decFile.getFullID());
        String ppath = PathUtil.parentPackage(decFile.getFullID());
        Object obj = readObject(decFile.getCachedText(), ppath);
        decFile.setCachedObject(obj);
        if (obj instanceof FileSourcable && decFile.inFileSystem()) {
            ((FileSourcable)obj).setSourceFile(decFile.getSourceFile());
        }
    }


    public Object readObject(String txt) {
        return readObject(txt, null);
    }


    public Object readObject(String txt, String srcPath) {

        Object obj = Deserializer.deserialize(txt);

        if (obj instanceof PathLocated) {
            ((PathLocated)obj).setPathLocation(srcPath);
        }


        if (obj instanceof Transitional) {
            obj = ((Transitional)obj).getOutcome();
        }

        if (obj instanceof PathLocated) {
            ((PathLocated)obj).setPathLocation(srcPath);
        }

        if (obj instanceof Resolvable) {
            ((Resolvable)obj).resolve();
        }
        return obj;
    }


    private void loadText(DecFile df) {
        if (df.inClasspath()) {
            String stxt = JUtil.getXMLResource(df.getFullID());
            if (stxt != null) {
                df.setCachedText(stxt);
            } else {
                E.error("cannot read " + df.getFullID() + " no such resource");
                df.setCachedText("");
            }

        } else if (df.inFileSystem()) {
            File fsrc = df.getSourceFile();

            String s = FileUtil.readStringFromFile(fsrc);
            if (s != null) {
                df.setCachedText(s);
            } else {
                E.error("cannot read " + fsrc + " no such file");
                df.setCachedText("");
            }
        }
    }





    private DecFile getSource(String location, String selectorin) {
        DecFile decFile = null;
        String selector = selectorin;

        if (selector == null) {
            selector = " ";
        }

        if (store.containsSource(location)) {
            if (store.hasMultipleSources(location)) {
                // use the selector to pick one;
                int ibm = 1000;
                for (DecFile df : store.getSources(location)) {
                    if (!df.hasCachedText()) {
                        loadText(df);
                    }
                    String s = df.getCachedText();
                    int isel = s.indexOf(selector);
                    if (isel >= 0 && isel < ibm) {
                        decFile = df;
                        ibm = isel;
                        //  E.info("DID match " + selector + " in " + s);
                    } else {
                        // E.info("didnt match " + selector + " in " + s);
                    }
                }
                if (decFile == null) {
                    E.error("there are multiple sources named " + location + " but the selector " +
                            selector + " does not match text in any of them");
                } else {
                    // E.info("disambiguated " + location + " to " + decFile);
                }


            } else {
                decFile = store.getSource(location);
            }

        } else if (location.startsWith("file:")) {
            decFile = new DecFile(location);
            store.addSource(decFile);
        } else {
            E.error("XMLStore cannot find " + location + " ");
        }
        return decFile;
    }





    public boolean hasEditorOf(String s) {
        return hasRolePlayer("edits", s);
    }



    public String getEditorPath(String s) {
        Role r = roleMap.getRole("edits", s);
        DecFile decFile = getSource(r.getResource(), null);
        return decFile.getFullID();
    }



    public boolean hasFactoryFor(String s) {
        return (hasRolePlayer("makes", s) || hasRolePlayer("domainof", s));
    }


    public Factory getFactoryFor(String s) {
        Factory fac = null;
        if (hasRolePlayer("makes", s)) {
            fac = (Factory)(getRolePlayer("makes", s));
        } else {
            fac = (Factory)(getRolePlayer("domainof", s));
        }
        return fac;
    }





    public boolean hasRunMappingFor(String s) {
        return hasRolePlayer("runmaps", s);
    }


    public Object getRunMappingFor(String s) {
        return getRolePlayer("runmaps", s);
    }



    public boolean hasProviderOf(String s) {
        return hasRolePlayer("provides", s);
    }


    public Object getProviderOf(String s) {
        return getNewRolePlayer("provides", s);
    }



    public boolean hasExposerOf(String s) {
        return hasRolePlayer("exposes", s);
    }


    public Object getExposerOf(String s) {
        return getRolePlayer("exposes", s);
    }



    public boolean hasTablizerOf(String s) {
        return hasRolePlayer("tablizes", s);
    }


    public Object getTablizerOf(String s) {
        return getRolePlayer("tablizes", s);
    }


    private boolean hasRolePlayer(String role, String subject) {
        return roleMap.hasRole(role, subject);
    }

    private Object getRolePlayer(String role, String subject) {
        return getRolePlayer(role, subject, true);
    }

    private Object getNewRolePlayer(String role, String subject) {
        return getRolePlayer(role, subject, false);
    }


    private Object getRolePlayer(String role, String subject, boolean cacheOK) {
        Object ret = null;
        Role r = roleMap.getRole(role, subject);
        if (r.hasCachedPlayer() && cacheOK) {
            ret = r.getCachedPlayer();

        } else {
            if (r instanceof ClassRole) {
                ret = readClassRole((ClassRole)r);

            } else if (r instanceof ResourceRole) {
                ret = readResourceRole((ResourceRole)r);
            }
        }
        r.cachePlayer(ret);
        return ret;
    }



    private Object readClassRole(ClassRole cr) {
        String cnm = cr.getResource();
//	       E.info("reading class role " + cr);
        return JUtil.newInstance(cnm);
    }


    private Object readResourceRole(ResourceRole rr) {
        String resnm = rr.getResource();
        Object ret =  getResource(resnm, "ole");
        return ret;
    }


    public void newSourceFile(File f, File rootFolder) {
        store.newSourceFile(f, rootFolder);

    }










}
