
package org.catacomb.interlish.structure;

/** For objects which can contain help text to be presented to the user on
 * request.
 * The interface includes both get and set methods, although non-mutable
 * objets, such as self-documenting code modules, will ignore the
 * setHelpText call.
 */
public interface Helpable {

    String getHelpText();

    void setHelpText(String s);

}
