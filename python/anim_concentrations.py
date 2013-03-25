#!/usr/bin/python
# -*- coding:utf-8 -*-
from __future__ import print_function, division, unicode_literals

import sys
reload(sys)
sys.setdefaultencoding('utf-8')
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
parser.add_argument('--reactions', action='store_true')
parser.add_argument('--particles', action='store_true')
parser.add_argument('--stimulation', action='store_true')
parser.add_argument('--reaction', action='store_true')
parser.add_argument('--diffusion', action='store_true')

class Drawer(object):
    def __init__(self, f, ax, species, times, data, title=''):
        from matplotlib.colors import LogNorm

        N = numpy.arange(species.shape[0])
        V = numpy.arange(data.shape[0])
        self.data = data
        self.times = times
        self.title = title
        self.ax = ax

        ax.set_xlabel("species")
        ax.xaxis.set_ticks(N + 0.5)
        ax.xaxis.set_ticklabels(species, rotation=70)
        ax.set_ylabel("voxel#")
        # matplotlib gets confused if we draw all zeros
        initial = data.sum(axis=(1,2)).argmax()
        if data[initial].max() == 0:
            # work around colorbar issues
            data[initial, 0, 0] = 1
        self.image = ax.imshow(data[initial], origin='lower',
                               extent=(0, data.shape[2], 0, data.shape[1]),
                               interpolation='spline16', aspect='auto',
                               norm=LogNorm())
        f.colorbar(self.image)

    def update(self, i):
        self.ax.set_title("{}   step {:>3}, t = {:8.4f} ms"
                          .format(self.title, i, self.times[i]))
        self.image.set_data(self.data[i])

class DrawerSet(object):
    def __init__(self, model, sim, opts):
        from matplotlib import pyplot
        pyplot.ion()

        self.save = opts.save

        self.figure = f = pyplot.figure(figsize=(12, 9))
        f.clear()

        self.drawers = []
        species = model.species
        times = sim.times
        num = opts.particles + opts.stimulation + opts.diffusion + opts.reaction
        shape = [(1,1), (2,1), (2,2), (2,2)][num - 1]
        pos = 1

        if opts.particles:
            ax = f.add_subplot(shape[0], shape[1], pos)
            data = sim.concentrations[:]
            self.drawers += [Drawer(f, ax, species, times, data,
                                    title='Particle numbers')]
            pos += 1
        if opts.stimulation:
            ax = f.add_subplot(shape[0], shape[1], pos)
            data = sim.stimulation_events[:]
            self.drawers += [Drawer(f, ax, species, times, data,
                                    title='Stimulation events')]
            pos += 1
        if opts.diffusion:
            ax = f.add_subplot(shape[0], shape[1], pos)
            # reduce dimensionality by summing over all neighbours
            data = sim.diffusion_events[:].sum(axis=-1)
            self.drawers += [Drawer(f, ax, species, times, data,
                                    title='Diffusion events')]
            pos += 1
        if opts.reaction:
            ax = f.add_subplot(shape[0], shape[1], pos)
            data = sim.reaction_events[:]
            names = reaction_name(range(data.shape[2]), model)
            self.drawers += [Drawer(f, ax, names, times, data,
                                    title='Reaction events')]

        f.tight_layout()
        if not opts.save:
            f.show()

        self.items = range(data.shape[0])

    def animate(self, indexes):
        for i in indexes:
            for drawer in self.drawers:
                drawer.update(i)
            if not self.save:
                self.figure.canvas.draw()
            else:
                self.figure.savefig('{}-{:06}.png'.format(self.save, i))
                print('.', end='')
                sys.stdout.flush()

def make_movie(save):
    command = '''mencoder -mf type=png:w=800:h=600:fps=25
                 -ovc lavc -lavcopts vcodec=mpeg4 -oac copy -o'''.split()
    command += [save, 'mf://*.png'.format(save)]
    print("running {}", command)
    subprocess.check_call(command)

def _logclip(x, offset):
    return numpy.clip(numpy.log(x + 1e-10) + offset, 0.3, 5)

def dot_opts(**opts):
    opts = ['{}={}'.format(k, v)
            for k, v in opts.items() if v is not None]
    return ' [{}]'.format(','.join(opts)) if opts else ''

def _conn(dst, a, b, penwidth=None, label=None):
    opts = dot_opts(penwidth=penwidth, label=label)
    print('\t"{}" -> "{}"{};'.format(a, b, opts), file=dst)

def _connections(dst, regions, connections, couplings):
    print('digraph Connections {', file=dst)
    print('\trankdir=LR;', file=dst)
    print('\tsplines=true;', file=dst)
    print('\tnode [color=blue,style=filled,fillcolor=lightblue];', file=dst)
    for i in range(connections.shape[0]):
        for j, coupl in zip(connections[i], couplings[i]):
            if j < 0:
                break
            coupl = _logclip(coupl, 3)
            _conn(dst, regions[i], regions[j], coupl)
    print('}', file=dst)

def dot_connections(model):
    regions = model.regions[1:] # skip "default" in first position
    _connections(sys.stdout, regions, model.neighbors, model.couplings)

def _reaction_name(rr, rr_s, pp, pp_s, species):
    return ' ⇌ '.join(
        ('+'.join('{}{}'.format(s if s > 1 else '', species[r])
                  for r, s in zip(rr_, ss_)
                  if r >= 0)
         for rr_, ss_ in ((rr, rr_s), (pp, pp_s))))

def reaction_name(num, model):
    single = isinstance(num, int)
    l = [_reaction_name(model.reactions.reactants[num],
                        model.reactions.reactant_stochiometry[num],
                        model.reactions.products[num],
                        model.reactions.product_stochiometry[num],
                        model.species)
         for num in ([num] if single else num)]
    return l[0] if single else numpy.array(l)

def _productions(dst, species, reactants, r_stochio, products, p_stochio, rates):
    print('digraph Reactions {', file=dst)
    print('\trankdir=LR;', file=dst)
    print('\tsplines=true;', file=dst)
    print('\tnode [color=green,style=filled,fillcolor=lightgreen];', file=dst)
    for rr, rr_s, pp, pp_s, rate in zip(reactants, r_stochio,
                                        products, p_stochio, rates):
        name = _reaction_name(rr, rr_s, pp, pp_s, species)
        print('\t"{}" [color=black,shape=point,fillcolor=magenta];'.format(name))
        for j, s in zip(rr, rr_s):
            if j < 0:
                break
            _conn(dst, species[j], name, _logclip(rate, 10))
        for j, s in zip(pp, pp_s):
            if j < 0:
                break
            _conn(dst, name, species[j], _logclip(rate, 10))
        print()
    print('}', file=dst)

def dot_productions(model):
    _productions(sys.stdout, model.species,
                 model.reactions.reactants, model.reactions.reactant_stochiometry,
                 model.reactions.products, model.reactions.product_stochiometry,
                 model.reactions.rates)

if __name__ == '__main__':
    opts = parser.parse_args()
    if opts.connections:
        dot_connections(opts.file.root.model)
    elif opts.reactions:
        dot_productions(opts.file.root.model)
    else:
        import matplotlib
        if opts.save:
            matplotlib.use('Agg')

        ss = DrawerSet(opts.file.root.model, opts.file.root.simulation, opts)
        indexes = ss.items if opts.save else itertools.cycle(ss.items)
        ss.animate(indexes)

        if opts.save:
            make_movie(opts.save)
            for fname in glob.glob('{}-*.png'.format(opts.save)):
                os.unlink(fname)
