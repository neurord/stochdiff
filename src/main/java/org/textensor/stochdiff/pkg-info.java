/**
 *
 * Stochastic diffusion segments of dendrits with spines.
 *
 * <p>
 * Each subpackage has its own ABOUT file.
 * The problem is broken down as:
 *
 * <ul>
 * <li>model - classes to represent the input data for reaction schemes morphology etc
 *               these mirror the file formats used for the input, and are instantiated as the
 *               files are read.
 *               They then typically have a a resolve method to dereference the input back to
 *               a proper linked object tree and a method to re-export the data in a format
 *               more suitable for calculations.
 *
 * <li>disc - discretization of morphologies: turning the structure as specified in the input files into
 *          a grid of volume elements and the coupling constants between them
 *
 * <li>geom - various bits of geometry used in the discretization process - Points, Vectors, Translations
 *             and Rotations
 *
 * <li>inter - things to do with reading models - should go elsewhere in the end
 *
 * <li>numeric - numerical methods for reaction and diffusion systems. There are a varity of methods
 *                 included as well as the final stochastic diffusion method to provide cross-checks and
 *                 measure accuracy etc.
 *                 The methods are broken down into subpackages - see the about file...
 *
 * <li>phys - A whole package for Avagadro's number... maybe not called for, unless we find more
 *            stuff that goes in here
 * </ul>
 *
 */
