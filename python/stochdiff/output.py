#!/usr/bin/python
# -*- coding:utf-8 -*-

"""Wrapper which makes reading stochdiff HDF5 output easier to use

>>> out = Output('model.h5')
>>> 
"""

from __future__ import print_function, division, unicode_literals

import operator
import tables
import pandas as pd

class Model(object):
    def __init__(self, element):
        self._model = element

    def species(self):
        return list(sp.decode('utf-8') for sp in self._model.species)

class Simulation(object):
    def __init__(self, element, model):
        self._sim = element
        self.number = int(element._v_parent._v_name[5:])
        self.model = model

    def times(self):
        return self._sim.times
        
    def concentrations(self):
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
              voxel specie  trial    count
        time                              
        0         0      A      0  1204428
        >>> gb = counts.reset_index().groupby(['time', 'voxel', 'specie'])
        >>> gb.mean().head(3)
                           trial           count
        time voxel specie                       
        0    0     A        14.5  1204428.433333
        5    0     A        14.5  1192430.800000
        10   0     A        14.5  1180586.466667
        >>> gb.std()
        """
        sims = self.simulations()
        data = dict((i, sim.concentrations())
                    for (i, sim) in enumerate(sims))
        panel = pd.Panel(data)
        series = panel.to_frame().stack()
        series.index.names = 'voxel time specie trial'.split()
        frame = pd.DataFrame(dict(count=series))
        frame.reset_index(inplace=1)
        frame.set_index('time', inplace=1)
        return frame
        frame.set_index('time voxel trial specie'.split(), inplace=1)
        frame = frame.unstack()
        frame.columns = frame.columns.droplevel()
        return frame
