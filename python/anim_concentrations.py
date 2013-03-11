#!/usr/bin/python
import sys
import numpy
import tables
from matplotlib import pyplot
pyplot.ion()

file = tables.openFile(sys.argv[1])
concs = file.root.simulation.concentrations
times = file.root.simulation.times
species = file.root.simulation.species
N = numpy.arange(species.shape[0])
V = numpy.arange(concs.shape[0])

f = pyplot.figure()
f.clear()
ax = f.gca()
ax.set_xlabel("species")
ax.xaxis.set_ticks(N)
ax.xaxis.set_ticklabels(species, rotation=70)
ax.set_ylabel("voxel#")
im = ax.imshow(concs[0], origin='lower',
               extent=(0, concs.shape[2], 0, concs.shape[1]),
               interpolation='spline16', aspect='auto')

while True:
    for i in range(0, concs.shape[0]):
        ax.set_title("i = {:>3}  t = {:8.4f}".format(i, times[i]))
        im.set_data(concs[i])
        f.canvas.draw()
        pyplot.pause(0.05)
