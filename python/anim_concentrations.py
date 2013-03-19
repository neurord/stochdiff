#!/usr/bin/python
from __future__ import print_function, division

import sys
import os
import glob
import itertools
import argparse
import subprocess
import numpy
import tables

parser = argparse.ArgumentParser()
parser.add_argument('file', type=tables.openFile)
parser.add_argument('--save')
parser.add_argument('--connections', action='store_true')
parser.add_argument('--particles', action='store_true')
parser.add_argument('--stimulation', action='store_true')
parser.add_argument('--reaction', action='store_true')
parser.add_argument('--diffusion', action='store_true')

class Drawer(object):
    def __init__(self, species, times, data, save, title=''):
        from matplotlib import pyplot
        from matplotlib.colors import LogNorm
        pyplot.ion()

        N = numpy.arange(species.shape[0])
        V = numpy.arange(data.shape[0])
        self.data = data
        self.times = times
        self.save = save
        self.title = title

        self.figure = f = pyplot.figure()
        f.clear()
        ax = f.gca()
        ax.set_xlabel("species")
        ax.xaxis.set_ticks(N + 0.5)
        ax.xaxis.set_ticklabels(species, rotation=70)
        ax.set_ylabel("voxel#")
        # matplotlib gets confused if we draw all zeros
        initial = data.sum(axis=(1,2)).argmax()
        self.image = ax.imshow(data[initial], origin='lower',
                               extent=(0, data.shape[2], 0, data.shape[1]),
                               interpolation='spline16', aspect='auto',
                               norm=LogNorm())
        f.colorbar(self.image)
        f.tight_layout()
        if not save:
            f.show()

    def update(self, i):
        ax = self.figure.gca()
        ax.set_title("{}   step {:>3}, t = {:8.4f} ms"
                     .format(self.title, i, self.times[i]))
        self.image.set_data(self.data[i])
        if not self.save:
            self.figure.canvas.draw()
        else:
            self.figure.savefig('{}-{:06d}.png'.format(self.save, i))

class DrawerSet(object):
    def __init__(self, model, sim, opts):
        self.drawers = []
        species = model.species
        times = sim.times
        if opts.particles:
            data = sim.concentrations[:]
            self.drawers += [Drawer(species, times, data, opts.save,
                                    title='Particle numbers')]
        if opts.stimulation:
            data = sim.stimulation_events[:]
            self.drawers += [Drawer(species, times, data, opts.save,
                                    title='Stimulation events')]
        if opts.diffusion:
            # reduce dimensionality by summing over all neighbours
            data = sim.diffusion_events[:].sum(axis=-1)
            self.drawers += [Drawer(species, times, data, opts.save,
                                    title='Diffusion events')]
        if opts.reaction:
            data = sim.reaction_events[:]
            self.drawers += [Drawer(species, times, data, opts.save,
                                    title='Reaction events')]

        items = range(data.shape[0])
        if opts.save:
            self.range = items
        else:
            self.range = itertools.cycle(items)

    def animate(self):
        for i in self.range:
            for drawer in self.drawers:
                drawer.update(i)
            print('.', end='')
            sys.stdout.flush()

def make_movie(save):
    command = '''mencoder -mf type=png:w=800:h=600:fps=25
                 -ovc lavc -lavcopts vcodec=mpeg4 -oac copy -o'''.split()
    command += [save, 'mf://*.png'.format(save)]
    print("running {}", command)
    subprocess.check_call(command)

def dottify(dst, connections, couplings):
    print('digraph Connections {', file=dst)
    print('\trankdir=LR;', file=dst)
    print('\tsplines=true;', file=dst)
    print('\tnode [color=blue,style=filled,fillcolor=lightblue];', file=dst)
    for i in range(connections.shape[0]):
        for j, coupl in zip(connections[i], couplings[i]):
            if j < 0:
                break
            coupl = min(max(numpy.log(coupl)+3, 0.3), 5)
            print('\t{} -> {} [penwidth={}];'.format(i, j, coupl), file=dst)
    print('}', file=dst)

def dot_connections(model):
    dottify(sys.stdout, model.neighbors, model.couplings)

if __name__ == '__main__':
    opts = parser.parse_args()
    if opts.connections:
        dot_connections(opts.file.root.model)
    else:
        import matplotlib
        if opts.save:
            matplotlib.use('Agg')

        ss = DrawerSet(opts.file.root.model, opts.file.root.simulation, opts)
        ss.animate()

        if opts.save:
            make_movie(opts.save)
            for fname in glob.glob('{}-*.png'.format(opts.save)):
                os.unlink(fname)
