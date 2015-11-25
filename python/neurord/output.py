#!/usr/bin/python
# -*- coding:utf-8 -*-

r"""
Wrapper which makes reading neurord HDF5 output easier to use

>>> from neurord.output import Output
>>> out = Output('model.h5')

Units: concentrations are expressed in nM and volumes in cubic microns.
So, in these units, one Litre is 10¹⁵ and a 1M solution is 10⁹.
The conversion factor between concentrations and particle number is therefore

.. math::
    N = 6.022…\cdot10²³ \times V/10¹⁵ \times c/10⁹

i.e.

.. math ::
    c = N / 0.6022… / V
"""

from __future__ import print_function, division, unicode_literals

import operator
import enum
import math
import tables
import functools
import numpy as np
import pandas as pd
from lxml import etree

AVOGADRO = 6.02214179
"""Avogadro constant from CODATA 2006"""
PUVC = AVOGADRO / 10
"""Converts concentrations to particle numbers"""

class EventType(enum.IntEnum):
    """Event types matching IGridCalc.EventType enumeration"""
    REACTION = 0
    DIFFUSION = 1
    STIMULATION = 2

class EventKind(enum.IntEnum):
    """Event types matching IGridCalc.EventKind enumeration"""
    EXACT = 0
    LEAP = 1

class Dependencies(object):
    """Raw information about the dependency graph

    >>> out = Output('model.h5')
    >>> deps = out.model.dependencies
    >>> for t, e, d, dep in zip(deps.types(),
    ...                         deps.elements(),
    ...                         deps.descriptions(),
    ...                         deps.dependent()):
    ...     print('type {} in voxel {} "{}" dependent: {}'.format(t, e, d, dep))
    type 0 in voxel 0 "NextReaction el.0 A+B→C" dependent: [1]
    type 0 in voxel 0 "NextReaction el.0 2×C→D" dependent: [2]
    type 0 in voxel 0 "NextReaction el.0 D→2×C" dependent: [1]
    type 2 in voxel 0 "NextStimulation el.0 B" dependent: [0]
    """
    def __init__(self, element):
        self._element = element

    def indices(self):
        "Numbers of the elements"
        return range(self._element.descriptions.shape[0])

    def descriptions(self):
        "A generator of descriptions of nodes (by index)"
        # pytables bug?
        for row in self._element.descriptions[:]:
            yield row.decode('utf-8')

    def elements(self):
        """The numbers of voxels events are attached to

        In case of diffusion, those are the originating voxels.
        """
        return self._element.elements

    def types(self):
        # pytables bug?
        for row in self._element.types[:]:
            yield EventType(row)

    def dependent(self):
        "A generator of lists of dependent nodes (by index)"
        # pytables bug?
        for row in self._element.dependent[:]:
            yield list(n for n in row if n >= 0)

class Reactions(object):
    """Raw information about reactions

    >>> out = Output('model.h5')
    >>> reactions = out.model.reactions
    >>> list(reactions.reactants())
    [[0, 1], [2], [3]]
    >>> list(reactions.reactant_stoichiometry())
    [[1, 1], [2], [1]]
    >>> list(reactions.products())
    [[2], [3], [2]]
    >>> list(reactions.product_stoichiometry())
    [[1], [1], [2]]
    >>> list(reactions.rates())
    [1.0000000000000001e-05, 9.9999999999999995e-07, 1.0000000000000001e-05]
    """
    def __init__(self, element):
        self._element = element

    def reactants(self):
        "A generator of lists of reactants (by index)"
        for row in self._element.reactants[:]:
            yield list(n for n in row if n >= 0)

    def reactant_stoichiometry(self):
        "A generator of lists of stoichiometries (by index)"
        for row in self._element.reactant_stoichiometry[:]:
            yield list(n for n in row if n >= 0)

    def products(self):
        "A generator of lists of products (by index)"
        for row in self._element.products[:]:
            yield list(n for n in row if n >= 0)

    def product_stoichiometry(self):
        "A generator of lists of stoichiometries (by index)"
        for row in self._element.product_stoichiometry[:]:
            yield list(n for n in row if n >= 0)

    def rates(self):
        "Rates of the reactions"
        return self._element.rates

    def reversible_pairs(self):
        """Mapping to reverse reactions

        For reaction i which has a reverse reaction, reversible_pairs()[i]
        gives the index of the reverse reaction. Reactions which do not have
        a reverse are not included.
        """
        indices = self._element.reversible_pairs
        mapping = {i:indices[i] for i in range(len(indices))
                   if indices[i] >= 0}
        return mapping

class Model(object):
    """Information about the model, same for all trials
    """
    def __init__(self, element):
        self._element = element
        try:
            self.dependencies = Dependencies(self._element.dependencies)
        except tables.exceptions.NoSuchNodeError as e:
            self.dependencies = None
        self.reactions = Reactions(self._element.reactions)

    def species(self, indices=None):
        """List of specie names

        Species are order the same as in other tables, so this table can be used to map species
        indices to actual names.

        >>> model = Output('model.h5').model
        >>> model.species()
        ['A', 'B', 'C', 'D']
        """
        what = self._element.species[indices] if indices is not None else self._element.species
        return list(sp.decode('utf-8') for sp in what)

    def grid(self):
        """Voxels of the simulation

        Returns an recarray containing points defining the voxels and their volumes and regions.

        >>> model = Output('model.h5').model
        >>> grid = model.grid()

        >>> import textwrap
        >>> names = grid.dtype.names
        >>> print(textwrap.fill(' '.join(names), width=40))
        x0 y0 z0 x1 y1 z1 x2 y2 z2 x3 y3 z3
        volume deltaZ label region type group

        >>> grid.volume
        array([ 2.])

        """
        return self._element.grid.read().view(np.recarray)

    def element_regions(self):
        "Names of regions of elements (by index)"
        regions = np.array(self.region_names())
        return regions[self.grid().region]

    def indices(self):
        "Numbers of the elements"
        return range(self._element.grid.shape[0])

    def neighbors(self):
        "A generator of lists of neighboring nodes (by index)"
        # pytables bug?
        for row in self._element.neighbors[:]:
            yield list(n for n in row if n >= 0)

    def couplings(self):
        "A generator of coupling strengths to neighboring nodes (by index)"
        coupl = self._element.couplings
        for i, neigh in enumerate(self.neighbors()):
            yield list(coupl[i][:len(neigh)])

    def region_names(self, indices=None):
        "Region names (by index)"
        what = self._element.regions[indices] if indices is not None else self._element.regions
        return [row.decode('utf-8') for row in what]

class Simulation(object):
    """Information about the results of a trial
    """
    def __init__(self, element, model):
        self._element = element
        self.number = int(element._v_name[5:])
        self.model = model

    def times(self):
        times = self._element.output.__main__.times[:]
        diff = times[1] - times[0]
        return np.round(times, decimals=max(-math.floor(math.log10(diff)), 0))

    def counts(self):
        data = self._element.output.__main__.population
        panel= pd.Panel(data.read(),
                        items=self.times(),
                        major_axis=range(data.shape[1]),
                        minor_axis=self.model.species())
        frame = panel.transpose(2, 1, 0).to_frame()
        frame.index.names = ['voxel', 'time']
        return frame

    def concentrations(self):
        "Counts converted to concentrations using voxel volumes"
        counts = self.counts()
        volumes = self.model.grid().volume
        ans = counts / volumes / PUVC
        ans.rename(columns={'count':'concentration'}, inplace=1)
        return ans

    def config(self):
        """lxml etree of de-serialized config the simulation was run with

        Accessing serialized config

        >>> out = Output('model.h5')
        >>> xml = out.simulation(0).config()
        >>> xml
        <Element {http://neurord.textensor.org}SDRun at 0x...>
        >>> xml.find('./ns:geometry', {'ns':'http://neurord.textensor.org'}).text
        '2D'
        """

        xml = self.model._element.serialized_config
        # FIXME: overwrite seed?
        return etree.fromstring(xml.read()[0])

    def events(self):
        "A full history of events"
        times = self._element.events.times[:]
        waited = self._element.events.waited[:]
        original = self._element.events.original_wait[:]
        events = self._element.events.events[:]
        extents = self._element.events.extents[:]
        kinds = self._element.events.kinds[:]
        df = pd.DataFrame(dict(time=times,
                               waited=waited,
                               original=original,
                               event=events,
                               extent=extents,
                               kind=kinds))
        df.set_index('time', inplace=True)
        return df.reindex_axis('waited original event kind extent'.split(), axis=1)

class Output(object):
    """The output for a single model, 0 or more experiments

    >>> out = Output('model.h5')
    """
    def __init__(self, filename):
        self.file = tables.openFile(filename)
        self.model = Model(self.file.root.model)

    def __enter__(self):
        return self

    def __exit__(self, *args):
        self.file.close()

    def simulation(self, num):
        """Get simulation by number

        >>> out = Output('model.h5')
        >>> sim = out.simulation(0)
        >>> sim.number
        0
        >>> sim.config()
        <Element {http://neurord.textensor.org}SDRun at 0x...>
        """
        trial = self.file.get_node('/trial{}'.format(num))
        return Simulation(trial, self.model)

    @functools.lru_cache()
    def simulations(self):
        nodes = self.file.list_nodes('/')
        sims = [Simulation(node, self.model) for node in nodes
                if node._v_name.startswith('trial')]
        sims.sort(key=operator.attrgetter('number'))
        return sims

    @functools.lru_cache()
    def counts(self):
        """Aggregated table of particle counts

        >>> out = Output('model.h5')
        >>> counts = out.counts()
        >>> counts.head(1)
                                 count
        voxel time specie trial       
        0     0    A      0       1265

        Calculate average over trials

        >>> gb = counts.groupby(level='voxel time  specie'.split())
        >>> gb.mean().head(1)
                             count
        voxel time specie         
        0     0    A       1264.67

        Calculate mean and standard deviation

        >>> import numpy as np
        >>> gb.aggregate([np.mean, np.std]).head(3)
                             count          
                              mean       std
        voxel time specie                   
        0     0    A       1264.67  0.472582
                   B       1204.47  0.501614
                   C          0.00  0.000000
        """
        sims = self.simulations()
        data = dict((i, sim.counts())
                    for (i, sim) in enumerate(sims))
        panel = pd.Panel(data)
        series = panel.to_frame().stack()
        series.index.names = 'voxel time specie trial'.split()
        frame = pd.DataFrame(dict(count=series))
        return frame

    @functools.lru_cache()
    def concentrations(self):
        """Counts converted to concentrations using voxel volumes

        >>> out = Output('model.h5')
        >>> out.counts().head(1)
                                 count
        voxel time specie trial       
        0     0    A      0       1265
        >>> out.concentrations().head(1)
                                 concentration
        voxel time specie trial               
        0     0    A      0         1050.29078
        """
        counts = self.counts()
        volumes = self.model.grid().volume
        ans = counts / volumes / PUVC
        ans.rename(columns={'count':'concentration'}, inplace=1)
        return ans

    @functools.lru_cache()
    def events(self):
        "A log of events from all simulations"
        sims = self.simulations()
        data = dict((i, sim.events())
                    for (i, sim) in enumerate(sims))
        return pd.concat(data)
