package org.catacomb.util;


import java.util.ArrayList;
import java.util.StringTokenizer;

import org.catacomb.interlish.structure.Tree;
import org.catacomb.interlish.structure.TreeChangeReporter;
import org.catacomb.interlish.structure.TreeNode;
import org.catacomb.report.E;

public class StringTree implements Tree, TreeNode {

    String path;
    String label;
    ArrayList<TreeNode> children;

    TreeNode parent;

    boolean exclude = false;


    public StringTree(String pth, String s) {
        path = pth;
        label = s;
        children = new ArrayList<TreeNode>();
    }


    // REFAC - not used yet
    public void setExcluded() {
        exclude = true; // applies to a dummy root that isnt part of the path;
    }


    public String toString() {
        return label;
    }

    public String getLabel() {
        return label;
    }

    public String getPath() {
        return path;
    }

    public void addChild(TreeNode tn) {
        children.add(tn);
        if (tn instanceof StringTree) {
            ((StringTree)tn).setParent(this);
        } else if (tn instanceof StringTreeLeaf) {
            ((StringTreeLeaf)tn).setParent(this);
        } else {
            E.error("wrong child type? " + tn);
        }
    }

    public ArrayList<TreeNode> getChildren() {
        return children;
    }

    public int nChildren() {
        return children.size();
    }


    private StringTree getChildTree(String s) {
        StringTree ret = null;
        for (Object obj : children) {
            if (obj instanceof StringTree && ((StringTree)obj).getLabel().equals(s)) {
                ret = (StringTree)obj;
            }
        }
        return ret;
    }


    public void addFromTokens(StringTokenizer st) {
        if (st.hasMoreTokens()) {
            String stok = st.nextToken();
            if (st.hasMoreTokens()) {

                StringTree sub = getChildTree(stok);
                if (sub == null) {
                    sub = new StringTree(getPath() + stok + ".", stok);
                    addChild(sub);
                }
                sub.addFromTokens(st);


            } else {
                addChild(new StringTreeLeaf(stok));
            }
        }
    }


    public void compress() {
        ArrayList<TreeNode> newChildren = new ArrayList<TreeNode>();
        for (Object obj : children) {
            if (obj instanceof StringTreeLeaf) {
                newChildren.add((StringTreeLeaf)obj);
            } else if (obj instanceof StringTree) {
                Object oc = ((StringTree)obj).getCompressedForm();
                if (oc instanceof StringTree) {
                    ((StringTree)oc).compress();
                }
                newChildren.add((TreeNode)oc);
            }
        }
        children = newChildren;
    }



    public void partialFlatten() {
        ArrayList<TreeNode> newChildren = new ArrayList<TreeNode>();

        for (Object obj : children) {
            if (obj instanceof StringTreeLeaf) {
                newChildren.add((StringTreeLeaf)obj);


            } else if (obj instanceof StringTree) {
                StringTree ost = (StringTree)obj;
                if (ost.nChildren() > 4) {
                    newChildren.add(ost);

                } else {
                    newChildren.addAll(ost.getGrandchildrenAsChildren());
                }
            } else {
                E.error("dropped child? " + obj);
            }
        }

        children = newChildren;

        for (Object oc : children) {
            if (oc instanceof StringTree) {
                ((StringTree)oc).partialFlatten();
            }
        }

    }


    private TreeNode getCompressedForm() {
        TreeNode ret = this;

        while (children.size() == 1) {
            Object obj = children.get(0);

            if (obj instanceof StringTree) {
                StringTree ct = (StringTree)obj;
                label = label + "." + ct.getLabel();
                children = ct.getChildren();
                path = ct.getPath();

            } else if (obj instanceof StringTreeLeaf) {
                ret = new StringTreeLeaf(label + "." + ((StringTreeLeaf)obj).getLabel());
                break;
            }
        }

        return ret;
    }



    public ArrayList<TreeNode> getGrandchildrenAsChildren() {
        ArrayList<TreeNode> ret = new ArrayList<TreeNode>();
        for (Object ob : children) {
            if (ob instanceof StringTreeLeaf) {
                ret.add((StringTreeLeaf)ob);

            } else if (ob instanceof StringTree) {
                StringTree chst = (StringTree)ob;

                for (Object subch : chst.getChildren()) {
                    if (subch instanceof StringTreeLeaf) {
                        ret.add(new StringTreeLeaf(chst.getLabel() + "." + subch));

                    } else if (subch instanceof StringTree) {
                        ((StringTree)subch).prefixLabel(chst.getLabel() + ".");
                        ret.add((StringTree)subch);
                    }
                }
            }
        }
        return ret;
    }



    private void prefixLabel(String pfx) {
        label = pfx + label;
    }


    public void print() {
        print("   ");
    }


    private void print(String indent) {
        System.out.println(indent + label + " (" + children.size() + "," + path + ")");
        for (Object obj : children) {
            if (obj instanceof String) {
                System.out.println(indent + "   " + obj);
            } else {
                ((StringTree)obj).print(indent + "   ");
            }
        }
    }


    public TreeNode getRoot() {
        return this;
    }


    public int getRootPolicy() {
        return Tree.SHOW_ROOT;
    }


    public void setTreeChangeReporter(TreeChangeReporter tcr) {

    }


    public Object[] getObjectPath(String s, boolean b) {
        E.warning("who needs the object path to " + s + " ?");
        return null;
    }


    public void setParent(TreeNode obj) {
        parent = obj;
    }

    public Object getParent() {
        return parent;
    }


    public int getChildCount() {
        return children.size();
    }


    public Object getChild(int index) {
        return children.get(index);
    }


    public int getIndexOfChild(Object child) {
        return children.indexOf(child);
    }


    public boolean isLeaf() {
        return (children == null || children.size() == 0);
    }

}
