/**
 *
 *
 * These are various ways of turning a morphology into a set of elements of constrained volume.
 * The output is a set of boxes each with a volume and coupling constants to its neighbors.
 *
 * The SegmentSlicer cuts segments along their axis but doesn't slice them up
 * longitudionally (that is, it 'segments' as in segments of a dendrite, not segments of
 * an orange)
 *
 * The DiscBoxer chops a short segment into boxes longitudinally
 * (cutting a cake into cubes of side equal to the height of the cake).
 *
 * The TreeBoxDiscretizer discretizes a tree structure into boxes. It first chops it into discs and then
 * chops these into boxes.  It does fairly sensible things with carrotoid segments as long as the two
 * ends are not too different in radius.
 *
 */

package org.textensor.stochdiff.disc;
