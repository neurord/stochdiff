#!/usr/bin/python
# -*- coding:utf-8 -*-

r"""
Wrapper which makes reading stochdiff HDF5 output easier to use

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
import tables
import numpy as np
import pandas as pd

AVOGADRO = 6.02214179
"""Avogadro constant from CODATA 2006"""
PUVC = AVOGADRO / 10
"""Converts concentrations to particle numbers"""

class Model(object):
    """Information about the model, same for all trials
    """
    def __init__(self, element):
        self._model = element

    def species(self):
        """List of specie names

        Species are order the same as in other tables, so this table can be used to map species
        indices to actual names.

        >>> model = Output('model.h5').model
        >>> model.species
        ['A']
        """
        return list(sp.decode('utf-8') for sp in self._model.species)

    def grid(self):
        """Voxels of the simulation

        Returns an recarray containing points defining the voxels and their volumes and regions.

        >>> model = Output('model.h5').model
        >>> grid = model.grid()
        >>> names = grid.dtype.names
        >>> print(textwrap.fill(' '.join(names), width=40))
        x0 y0 z0 x1 y1 z1 x2 y2 z2 x3 y3 z3
        volume deltaZ label region type group
        >>> grid.volume
        array([ 2.])

        """
        return self._model.grid.read().view(np.recarray)

class Simulation(object):
    """Information about the results of a trial
    """
    def __init__(self, element, model):
        self._sim = element
        self.number = int(element._v_parent._v_name[5:])
        self.model = model

    def times(self):
        return self._sim.times

    def counts(self):
        data = self._sim.concentrations
        panel= pd.Panel(data.read(),
                        items=self.times(),
                        major_axis=range(data.shape[1]),
                        minor_axis=self.model.species())
        frame = panel.transpose(2, 1, 0).to_frame()
        frame.index.names = ['voxel', 'time']
        return frame

class Output(object):
    """The output for a single model, 0 or more experiments

    >>> out = Output('model.h5')
    """
    def __init__(self, filename):
        self.file = tables.openFile(filename)
        self.model = Model(self.file.root.trial0.model)

    def simulations(self):
        trials = self.file.list_nodes('/')
        sims = [Simulation(trial.simulation, self.model) for trial in trials]
        sims.sort(key=operator.attrgetter('number'))
        return sims

    def counts(self):
        """Aggregated table of particle counts

        >>> out = Output('model.h5')
        >>> counts = out.counts()
        >>> counts.head(1)
                                   count
        voxel time specie trial
        0     0    A      0      1204428

        Calculate average over trials

        >>> gb = counts.groupby(level='voxel time  specie'.split())
        >>> gb.mean().head(1)
                                    count
        voxel time specie
        0     0    A       1204428.433333

        Calculate mean and standard deviation

        >>> gb.aggregate([np.mean, np.std]).head(3)
                                    count
                                     mean         std
        voxel time specie
        0     0    A       1204428.433333    0.504007
              5    A       1192430.800000  123.919440
              10   A       1180586.466667  179.969870
        """
        sims = self.simulations()
        data = dict((i, sim.counts())
                    for (i, sim) in enumerate(sims))
        panel = pd.Panel(data)
        series = panel.to_frame().stack()
        series.index.names = 'voxel time specie trial'.split()
        frame = pd.DataFrame(dict(count=series))
        return frame

    def concentrations(self):
        """Counts converted to concentrations using voxel volumes

        >>> out = Output('model.h5')
        >>> out.counts().head(1)
                                   count
        voxel time specie trial
        0     0    A      0      1204428
        >>> out.concentrations().head(1)
                                 concentration
        voxel time specie trial
        0     0    A      0      999999.702764
        """
        counts = self.counts()
        volumes = self.model.grid().volume
        ans = counts / volumes / PUVC
        ans.rename(columns={'count':'concentration'}, inplace=1)
        return ans
