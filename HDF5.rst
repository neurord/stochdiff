NeuroRD HDF5 output structure
=============================

The file contains one ``manifest`` and one ``model`` top-level groups, and one or more ``trialXXX``
groups, each one corresponding to a different execution. Each table has metadata fields which describe the table.

/manifest
---------

This group contains information about NeuroRD version that was used to create this output file.

Example
~~~~~~~

.. code-block::
   
    /manifest (Group) 'information about the program'
    /manifest/build_time (Array(1,)) ''
      Data dump:
    [0] 2015-11-18 01:14:16
    /manifest/git_version (Array(1,)) ''
      Data dump:
    [0] v2.1.10-549-gc812a37fcf+

/model
------

This group contains the input model, the grid structure created from the morphology description, and the calculated dependency graph.

/model/species
--------------

A simple table with the names of all species.

/model/neighbors and /model/couplings
-------------------------------------

A pair of tables listing neighbours of each voxel (indices, -1 is used fill empty positions), and the coupling coefficient for each neighbour.

/model/regions
--------------

Names of regions.

/model/grid
-----------

This table describes the voxels.

.. code-block::

    /model/grid (Table(1,)) 'voxels'
      description := {
      "x0": Float64Col(shape=(), dflt=0.0, pos=0),
      "y0": Float64Col(shape=(), dflt=0.0, pos=1),
      "z0": Float64Col(shape=(), dflt=0.0, pos=2),
      "x1": Float64Col(shape=(), dflt=0.0, pos=3),
      "y1": Float64Col(shape=(), dflt=0.0, pos=4),
      "z1": Float64Col(shape=(), dflt=0.0, pos=5),
      "x2": Float64Col(shape=(), dflt=0.0, pos=6),
      "y2": Float64Col(shape=(), dflt=0.0, pos=7),
      "z2": Float64Col(shape=(), dflt=0.0, pos=8),
      "x3": Float64Col(shape=(), dflt=0.0, pos=9),
      "y3": Float64Col(shape=(), dflt=0.0, pos=10),
      "z3": Float64Col(shape=(), dflt=0.0, pos=11),
      "volume": Float64Col(shape=(), dflt=0.0, pos=12),
      "deltaZ": Float64Col(shape=(), dflt=0.0, pos=13),
      "label": StringCol(itemsize=100, shape=(), dflt='', pos=14),
      "region": Int32Col(shape=(), dflt=0, pos=15),
      "type": StringCol(itemsize=100, shape=(), dflt='', pos=16),
      "group": StringCol(itemsize=100, shape=(), dflt='', pos=17)}

(x0, y0, z0), (x1, y1, z1), (x2, y2, z2) are in one plane, and (x3, y3, z3) is the opposite corner in the other plane.

``region`` is an index into the ``regions`` table described above.

/model/serialized_config
------------------------

This "table" contains one string element with the input XML configuration.

/model/reactions
----------------

This group describes all reactions (not diffusion or stimulation):

* ``reactants`` and ``reactant_stoichiometry`` contain the indices of consumed species and their stoichiometry;
* ``products`` and ``product_stoichiometry`` contain the indices of produced species and their stoichiometry;
* ``rates`` contain the rate constants of the reactions;
* ``reversible_pairs`` contains the indices of reverse reactions (-1 is used to fill empty positions).

/model/stimulation
------------------

This group desribes the stimulation channels:

* ``target_names`` contains the names of stimulation targets (this corresponds to the ``injectionSite=`` attribute),
* ``targets`` contains the indices of stimulated voxels.

/model/dependencies
-------------------

Calculated dependency graph.

* ``descriptions`` contains the label of each reactions channel used in logging;
* ``elements`` specifies in which voxels where reactions take place, where diffusion starts, or where molecules are injected;
* ``types`` describes the type of each reaction
* ``dependent`` constains the indices of dependent reaction channels

/model/output
-------------

This group contains a number of subgroups, each describing an a set of tables containing particle populations. One set (``__main__``) is configured through ``<outputInterval>`` in the main configuration file and contains all species for all voxels. The other sets are configured through ``<OutputScheme>``. Each corresponds to a different ``<OutputSet>``, and the name is taken from the ``filename`` attribute.

For each set, ``elements`` contains the indices of output elements, and
``species`` contains the names of output species.

``/trialXXX/output`` groups contain corresponding tables with the simulation results and are described below.

Example
~~~~~~~

.. code-block:: xml
   
  <outputInterval>   5   </outputInterval>

  <OutputScheme>
    <OutputSet filename = "all"  outputInterval="100.0">
      <OutputSpecie name="A"/>
      <OutputSpecie name="B"/>
      <OutputSpecie name="C"/>
      <OutputSpecie name="D"/>
    </OutputSet>

    <OutputSet filename = "some"  outputInterval="50">
      <OutputSpecie name="C"/>
      <OutputSpecie name="D"/>
    </OutputSet>
  </OutputScheme>
  
.. code-block::

    /model/output (Group) 'output species'
    /model/output/__main__ (Group) ''
    /model/output/__main__/elements (Array(1,)) ''
    /model/output/__main__/species (Array(4,)) 'names of output species'
    /model/output/all (Group) ''
    /model/output/all/elements (Array(1,)) 'indices of output elements'
    /model/output/all/species (Array(4,)) 'names of output species'
    /model/output/some (Group) ''
    /model/output/some/elements (Array(1,)) 'indices of output elements'
    /model/output/some/species (Array(2,)) 'names of output species'


/trialXXX
---------

The number of trials is specified with ``-Dneurord.trials=N``. Each trial results in one top-level group with names ``trial0``, ``trial``, ..., ``trial<N-1>``.


/trialXXX/simulation_seed
~~~~~~~~~~~~~~~~~~~~~~~~~

This specifies the simulation seed that was used for this trial. Even if the input XML file species a simulation seed, it is ignored for trials with numbers above 0.

/trialXXX/output
~~~~~~~~~~~~~~~~

This group mirrors the ``/model/output`` group described above. Each output set contains two tables:

* ``times`` is a uni-dimensional array containg the times at which the state of the system was reported,
* ``population`` is an array of size T×V×S, where T is the size of the ``times`` table, V is the number of voxel in this output set, and S is the number of species in this output set.
