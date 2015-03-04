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

# TODO: support --time in animations

def geometry(g):
    x,s,y = g.partition('x')
    x = float(x)
    if y:
        y = float(y)
    else:
        y = x * 3/4
    return (x, y)

def str_list(arg):
    parts = arg.split(',')
    return parts

def time_range(arg):
    a, b, c = arg.partition('-')
    if not b:
        raise ValueError("either t₁-t₂, t₁-, or -t₂ must be used")
    a = float(a) if a else None
    c = float(c) if c else None
    return (a, c)

def filter_times(limits, times):
    a = 0
    if limits[0] is not None:
        while times[a] < limits[0]:
            a += 1
    if limits[1] is not None:
        b = a
        while times[b] < limits[1]:
            b += 1
    else:
        b = None
    return slice(a, b)

parser = argparse.ArgumentParser()
parser.add_argument('file')
parser.add_argument('--save')
parser.add_argument('--connections', action='store_true')
parser.add_argument('--reactions', action='store_true')
parser.add_argument('--particles', action='store_true')
parser.add_argument('--stimulation', action='store_true')
parser.add_argument('--reaction', action='store_true')
parser.add_argument('--diffusion', action='store_true')
parser.add_argument('--geometry', type=geometry, default=(12, 9))
parser.add_argument('--history', type=str_list, nargs='?', const=())
parser.add_argument('--regions', type=str_list, nargs='?')
parser.add_argument('--yscale', choices=('linear', 'log', 'symlog'))
parser.add_argument('--style', default='-')
parser.add_argument('--time', type=time_range, default=(None, None))
parser.add_argument('--trial', type=int, default=0)

class Drawer(object):
    def __init__(self, f, ax, xlabel, names, times, data, title=''):
        from matplotlib.colors import LogNorm

        n = names.shape[0]
        N = numpy.arange(n)
        V = numpy.arange(data.shape[0])
        self.data = data
        self.times = times
        self.title = title
        self.ax = ax

        ax.set_xlabel(xlabel)
        ax.xaxis.set_ticks(N + 0.5)
        size = 9 if n < 10 else 8 if n < 20 else 7
        ax.xaxis.set_ticklabels(names, rotation=70, size=size)
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

        self.figure = f = pyplot.figure(figsize=opts.geometry)

        self.drawers = []
        species = model.species
        times = sim.times
        num = opts.particles + opts.stimulation + opts.diffusion + opts.reaction
        shape = [(1,1), (2,1), (2,2), (2,2)][num - 1]
        pos = 1

        if opts.particles:
            ax = f.add_subplot(shape[0], shape[1], pos)
            data = sim.concentrations[:]
            self.drawers += [Drawer(f, ax, 'species', species, times, data,
                                    title='Number of particles')]
            pos += 1
        if opts.stimulation:
            ax = f.add_subplot(shape[0], shape[1], pos)
            data = sim.stimulation_events[:]
            self.drawers += [Drawer(f, ax, 'species', species, times, data,
                                    title='Stimulation events')]
            pos += 1
        if opts.diffusion:
            ax = f.add_subplot(shape[0], shape[1], pos)
            # reduce dimensionality by summing over all neighbours
            data = sim.diffusion_events[:].sum(axis=-1)
            self.drawers += [Drawer(f, ax, 'species', species, times, data,
                                    title='Diffusion events')]
            pos += 1
        if opts.reaction:
            ax = f.add_subplot(shape[0], shape[1], pos)
            data = sim.reaction_events[:]
            names = reaction_name(range(data.shape[2]), model)
            self.drawers += [Drawer(f, ax, 'reactions', names, times, data,
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

def animate_drawing(opts):
    file = tables.openFile(opts.file)
    trial = file.get_node('/trial{}'.format(opts.trial))
    ss = DrawerSet(trial.model, trial.simulation, opts)
    indexes = itertools.cycle(ss.items)
    ss.animate(indexes)

def save_drawings(opts, suboffset=None, subtotal=1):
    import matplotlib
    if opts.save:
        matplotlib.use('Agg')

    file = tables.openFile(opts.file)
    trial = file.get_node('/trial{}'.format(opts.trial))
    ss = DrawerSet(trial.model, trial.simulation, opts)
    if opts.save:
        if suboffset is None:
            indexes = ss.items
        else:
            indexes = ss.items[suboffset::subtotal]
    else:
        indexes = itertools.cycle(ss.items)
    ss.animate(indexes)

def _save_drawings(args):
    save_drawings(*args)

def save_drawings_multi(opts):
    import multiprocessing as mp

    n = mp.cpu_count()
    pool = mp.Pool(processes=n)
    args = [(opts, i, n) for i in range(n)]
    pool.map(_save_drawings, args)

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

COLORDICT = {'dendrite':'lightblue'}
def _connections(dst, model):
    print('digraph Connections {', file=dst)
    print('\trankdir=LR;', file=dst)
    print('\tsplines=true;', file=dst)
    print('\tnode [color=blue,style=filled];', file=dst)
    for i in range(model.neighbors.shape[0]):
        region = model.regions[model.grid.cols.region[i]]
        fillcolor = COLORDICT.get(region, 'grey')
        print('\t"{}" {};'.format(i, dot_opts(fillcolor=fillcolor)), file=dst)
        for j, coupl in zip(model.neighbors[i], model.couplings[i]):
            if j < 0:
                break
            coupl = _logclip(coupl, 3)
            _conn(dst, i, j, coupl)
    print('}', file=dst)

def dot_connections(filename):
    file = tables.openFile(filename)
    trial = file.get_node('/trial{}'.format(opts.trial))
    _connections(sys.stdout, trial.model)

def _reaction_name(rr, rr_s, pp, pp_s, species):
    return ' ⇌ '.join(
        ('+'.join('{}{}'.format(s if s > 1 else '', species[r])
                  for r, s in zip(rr_, ss_)
                  if r >= 0)
         for rr_, ss_ in ((rr, rr_s), (pp, pp_s))))

def reaction_name(num, model):
    single = isinstance(num, int)
    l = [_reaction_name(model.reactions.reactants[num],
                        model.reactions.reactant_stoichiometry[num],
                        model.reactions.products[num],
                        model.reactions.product_stoichiometry[num],
                        model.species)
         for num in ([num] if single else num)]
    return l[0] if single else numpy.array(l)

def _productions(dst, species, reactants, r_stoichio, products, p_stoichio, rates):
    print('digraph Reactions {', file=dst)
    print('\trankdir=LR;', file=dst)
    print('\tsplines=true;', file=dst)
    print('\tnode [color=green,style=filled,fillcolor=lightgreen];', file=dst)
    for rr, rr_s, pp, pp_s, rate in zip(reactants, r_stoichio,
                                        products, p_stoichio, rates):
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
        if not len(rr) and not len(pp):
            print('\t"{}";'.format(name), file=dst)
        print()
    print('}', file=dst)

def dot_productions(filename):
    file = tables.openFile(filename)
    trial = file.get_node('/trial{}'.format(opts.trial))
    model = trial.model
    _productions(sys.stdout, model.species,
                 model.reactions.reactants, model.reactions.reactant_stoichiometry,
                 model.reactions.products, model.reactions.product_stoichiometry,
                 model.reactions.rates)

def specie_indices(items, species):
    species = list(species)
    return numpy.array([species.index(i) for i in items])

def _history(simul, species, indices, region_indices, region_labels,
             times, concentrations, title, opts):
    from matplotlib import pyplot

    full_title = 'particle numbers in {}: species {}'.format(title,
                                                             ', '.join(species))
    f = pyplot.figure(figsize=opts.geometry)
    f.canvas.set_window_title(full_title)

    ax = f.gca(yscale=opts.yscale)
    ax.set_xlabel('t / ms')
    ax.set_ylabel('particle numbers')
    colors = itertools.cycle('rgbkcmy')
    for sp, name in zip(indices, species):
        for rlabel, rindi in zip(region_labels, region_indices):
            y = concentrations[:, rindi, sp].sum(axis=1)
            ax.plot(times, y, opts.style, color=next(colors),
                    label='{} in {}'.format(name, rlabel))
    ax.legend(loc='best')
    pyplot.show(block=True)

def find_regions(regions, spec, grid):
    print('regions', regions, 'spec', spec)
    spec_ = range(len(regions)) if spec is None else spec
    for item in spec_:
        try:
            region = int(item)
        except ValueError:
            w = regions[:] == item
            if w.sum() != 1:
                raise ValueError("bad region: {}".format(item))
            region = w.argmax() # find True
        # skip regions with no voxels unless requested
        if spec or (grid.cols.region[:] == region).any():
            yield region

def plot_history(filename, species, opts):
    file = tables.openFile(filename)
    trial = file.get_node('/trial{}'.format(opts.trial))
    model = trial.model
    simul = trial.simulation
    if species:
        indices = specie_indices(species, model.species)
    else:
        indices = numpy.arange(len(model.species))
    when = filter_times(opts.time, simul.times)

    region_numbers = list(find_regions(model.regions, opts.regions, model.grid))
    region_indices = [model.grid.cols.region[:] == region
                      for region in region_numbers]
    assert sum(region_indices).min() >= 0
    assert sum(region_indices).max() <= 1
    region_labels = model.regions[region_numbers]

    _history(simul, model.species[indices], indices,
             region_indices, region_labels,
             simul.times[when], simul.concentrations[when],
             title=filename, opts=opts)

if __name__ == '__main__':
    opts = parser.parse_args()
    if opts.connections:
        dot_connections(opts.file)
    elif opts.reactions:
        dot_productions(opts.file)
    elif opts.history is not None:
        plot_history(opts.file, opts.history, opts)
    else:
        if not opts.save:
            animate_drawing(opts)
        else:
            save_drawings_multi(opts)
            make_movie(opts.save)
            for fname in glob.glob('{}-*.png'.format(opts.save)):
                os.unlink(fname)
