package org.catacomb.druid.manifest;



import org.catacomb.interlish.resource.ResourceRole;
import org.catacomb.interlish.resource.Role;
import org.catacomb.interlish.structure.Element;
import org.catacomb.report.E;
import org.catacomb.serial.ElementXMLReader;
import org.catacomb.serial.Serializer;
import org.catacomb.util.FileUtil;



import java.io.File;
import java.util.ArrayList;



public class DecManifest {



    public String rootPath; // "org/catacomb/ime" or whatever - gets put back on paths
    // later;

    public ArrayList<DecFile> files;
    public ArrayList<Role> roles;



    public DecManifest() {
    }


    public DecManifest(File basedir, String path) {
        rootPath = path;

        init();
        addFilesFrom(basedir, rootPath);
    }


    public ArrayList<DecFile> getFiles() {
        return files;
    }


    public ArrayList<Role> getRoles() {
        return roles;
    }


    public String getRootPath() {
        return rootPath;
    }



    public void init() {
        files = new ArrayList<DecFile>();
        roles = new ArrayList<Role>();
    }



    void addFilesFrom(File fdir, String psf) {
        File[] af = fdir.listFiles();
        for (int i = 0; i < af.length; i++) {
            File f = af[i];

            if (f.getName().endsWith(".xml")) {

                addFile(psf, f);

            } else if (f.isDirectory()) {

                String psc = psf;
                if (psc.length() > 0) {
                    psc += "/";
                }
                psc += f.getName();

                addFilesFrom(f, psc);
            }
        }
    }


    private void addFile(String psf, File f) {
        String fnm = f.getName();

        DecFile xmf = new DecFile(psf, fnm);

        String jpath = psf.replaceAll("/", ".");

        // E.info("adding file " + f);
        String stxt = FileUtil.readStringFromFile(f);

        if (stxt.trim().length() > 10) {

            Element elt = (Element)(ElementXMLReader.deserialize(stxt));


            if (elt.getName().equals("DeclarationReading")) {
                addInfoFrom(elt, jpath);

            } else {
                addResource(elt, xmf);
            }
        }
    }



    private void addInfoFrom(Element parent, String jpath) {
        for (Element elt : parent.getElements()) {

            if (elt.getName().equals("Instantiable")) {
                String cls = elt.getAttribute("class");
                String prv = elt.getAttribute("provides");

                if (cls != null) {
                    if (prv == null) {
                        prv = cls.substring(cls.lastIndexOf(".") + 1, cls.length());
                    }
                    if (cls.indexOf(".") < 0) {
                        cls = jpath + "." + cls;
                    }
                    roles.add(new ClassRole(cls, "provides", prv));

                } else {
                    E.error("cannot extract info from " + elt);
                }

            } else if (elt.getName().equals("Factory")) {
                String cls = elt.getAttribute("class");
                String val = elt.getAttribute("makes");
                if (cls != null && val != null) {
                    roles.add(new ClassRole(cls, "makes", val));
                } else {
                    E.error("cannot extract info from " + elt);
                }


            } else {
                E.error("unhandled info element " + elt);
            }
        }
    }



    private void addResource(Element elt, DecFile xmf) {

        files.add(xmf);

        if (false) { // was  if id == Type

        } else {
            ArrayList<Element> subelements = new ArrayList<Element>();
            if (elt.hasElements()) {
                subelements.addAll(elt.getElements());
            }

            for (Element sub : subelements) {
                if (sub.getName().equals("Role")) {
                    ResourceRole role = new ResourceRole();
                    role.populateFrom(sub);
                    role.setResource(xmf.getName());

                    roles.add(role);
                }
            }
        }
    }




    public static DecManifest rebuildManifest(File ftop) {
        File fdest = new File(ftop, "DecManifest.xml");
        FileUtil.writeStringToFile("", fdest);
        DecManifest xm = new DecManifest(ftop, "");

        String ser = Serializer.serialize(xm);
        FileUtil.writeStringToFile(ser, fdest);
        return xm;
    }



}
