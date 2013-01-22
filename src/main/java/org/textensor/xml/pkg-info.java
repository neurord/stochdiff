/**
 *
 * <p>
 * This contains a minimal XML parser. Its nothing to do with the stochastid diffusion calculation,
 *  but is needed to turn XML input files into java objects in the stochdiff.model package.
 *
 * <p>
 *  It works by recursion and reflection: pulling xml tokens from the input file and trying to
 *  instantiate the corresponding java objects.  We're not particularly fussy about the
 *  arcane details of the XML specification. IT just cares about normal XML elements such
 *  as
 * <pre>
 *  &lt;Thing x="1" y="2"&gt;
 *     &lt;z&gt;4&lt;/z&gt;
 *      &lt;subthing&gt;
 *             &lt;subx&gt;5&lt;/subx&gt;
 *      &lt;/subthing&gt;
 *  &lt;/Thing&gt;
 * </pre>
 *
 *  This will get turned into  a java object of clas Thing with felds x, y, z, and subthing where the
 *  field subthing is of some object type that has a field called subx. Note that the specification
 *  of simple field values (strings, numbers etc) can be via attributes 'x="1"' or by elements
 *  &lt;x&gt;1&lt;/x&gt;.  This allows you to write the XML files in whichever form is more readable.
 *
 *
 */
