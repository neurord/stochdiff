#!/usr/bin/python
# -*- coding:utf-8 -*-
from __future__ import print_function, division, unicode_literals

import sys
import os
import glob
import collections
import itertools
import argparse
import subprocess
import numpy as np
import tables
from lxml import etree
from pygments import highlight
from pygments.lexers import XmlLexer
from pygments.formatters import Terminal256Formatter as Formatter
import pandas as pd

from . import output

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
parser.add_argument('file', type=output.Output)
parser.add_argument('--save', nargs='?', const=True)
parser.add_argument('--save-data', nargs='?', const=True)
parser.add_argument('--connections', action='store_true')
parser.add_argument('--reactions', action='store_true')
parser.add_argument('--dependencies', action='store_true')
parser.add_argument('--particles', action='store_true')
parser.add_argument('--stimulation', action='store_true')
parser.add_argument('--reaction', action='store_true')
parser.add_argument('--diffusion', action='store_true')
parser.add_argument('--geometry', type=geometry, default=(12, 9))
parser.add_argument('--history', type=str_list, nargs='?', const=())
parser.add_argument('--regions', type=str_list, nargs='?')
parser.add_argument('--sum-regions', action='store_true')
parser.add_argument('--num-elements', type=int,
                    help='take only the first so many elements')
parser.add_argument('--yscale', choices=('linear', 'log', 'symlog'))
parser.add_argument('--style', default='-')
parser.add_argument('--time', type=time_range, default=(None, None))
parser.add_argument('--trial', type=int, default=0)
parser.add_argument('--config', type=str, nargs='?', const='')

class Drawer(object):
    def __init__(self, f, ax, xlabel, names, times, data, title=''):
        from matplotlib.colors import LogNorm

        n = names.shape[0]
        N = np.arange(n)
        V = np.arange(data.shape[0])
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
        times = sim.times()
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
    sim = opts.file.simulation(opts.trial)
    ss = DrawerSet(sim.model, sim, opts)
    indexes = itertools.cycle(ss.items)
    ss.animate(indexes)

def save_drawings(opts, suboffset=None, subtotal=1):
    import matplotlib
    if opts.save:
        matplotlib.use('Agg')

    sim = opts.file.simulation(opts.trial)
    ss = DrawerSet(sim.model, sim, opts)
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
    return np.clip(np.log(x + 1e-10) + offset, 0.3, 5)

def dot_opts(**opts):
    opts = ['{}={}'.format(k, v)
            for k, v in opts.items() if v is not None]
    return ' [{}]'.format(','.join(opts)) if opts else ''

def _conn(dst, a, b, penwidth=None, label=None):
    opts = dot_opts(penwidth=penwidth, label=label)
    print('\t"{}" -> "{}"{};'.format(a, b, opts), file=dst)

REGION_COLORDICT = {'dendrite':'lightblue', 'soma':'cyan'}
def _connections(dst, model):
    print('digraph Connections {', file=dst)
    print('\trankdir=LR;', file=dst)
    print('\tsplines=true;', file=dst)
    print('\tnode [color=blue,style=filled];', file=dst)
    for i, neigh, coupl, region in zip(model.indices(),
                                       model.neighbors(),
                                       model.couplings(),
                                       model.element_regions()):
        fillcolor = REGION_COLORDICT.get(region, 'grey')
        print('\t"{}" {};'.format(i, dot_opts(fillcolor=fillcolor)), file=dst)
        for j, c in zip(neigh, coupl):
            c = _logclip(c, 3)
            _conn(dst, i, j, c)
    print('}', file=dst)

def dot_connections(output):
    _connections(sys.stdout, output.model)

TYPE_COLORDICT = {output.EventType.REACTION:'lightblue',
                  output.EventType.DIFFUSION:'grey',
                  output.EventType.STIMULATION:'orange'}
def _dependencies(dst, model):
    print('digraph Dependencies {', file=dst)
    print('\trankdir=LR;', file=dst)
    print('\tsplines=true;', file=dst)
    print('\tnode [color=blue,style=filled,shape=point];', file=dst)
    deps = model.dependencies
    elements = deps.elements()
    descriptions = list(deps.descriptions())
    for i, t, elem, children in zip(deps.indices(),
                                    deps.types(),
                                    elements,
                                    deps.dependent()):
        fillcolor = TYPE_COLORDICT.get(t, 'grey')
        desc = descriptions[i][4:]
        if opts.num_elements is not None and elem >= opts.num_elements:
            continue
        print('\t"{}" {};'.format(desc, dot_opts(fillcolor=fillcolor)), file=dst)
        for j in children:
            elem = elements[j]
            if opts.num_elements is not None and elem >= opts.num_elements:
                continue
            _conn(dst, desc, descriptions[j][4:])

    print('}', file=dst)

def dot_dependencies(output):
    _dependencies(sys.stdout, output.model)

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
    return l[0] if single else np.array(l)

def _productions(dst, species, reactants, r_stoichio, products, p_stoichio, rates):
    print('digraph Reactions {', file=dst)
    print('\trankdir=LR;', file=dst)
    print('\tsplines=true;', file=dst)
    print('\tnode [color=green,style=filled];', file=dst)
    for rr, rr_s, pp, pp_s, rate in zip(reactants, r_stoichio,
                                        products, p_stoichio, rates):
        name = _reaction_name(rr, rr_s, pp, pp_s, species)
        print('\t"{}" [color=black,shape=point,fillcolor=magenta];'.format(name))
        for j, s in zip(rr, rr_s):
            _conn(dst, species[j], name, _logclip(rate, 10))
        for j, s in zip(pp, pp_s):
            _conn(dst, name, species[j], _logclip(rate, 10))
        if not len(rr) and not len(pp):
            print('\t"{}";'.format(name), file=dst)
        print()
    print('}', file=dst)

def dot_productions(output):
    model = output.model
    reactions = model.reactions
    _productions(sys.stdout, model.species(),
                 reactions.reactants(), reactions.reactant_stoichiometry(),
                 reactions.products(), reactions.product_stoichiometry(),
                 reactions.rates())

def specie_indices(items, species):
    species = list(species)
    return np.array([species.index(i) for i in items])

def generate_element_histories(species, region_indices, region_labels,
                               times, counts):
    for name in species:
        for rlabel, rindi in zip(region_labels, region_indices):
            y = counts.loc[rindi][name].values
            yield times, y, name, rlabel

def generate_region_histories(species, region_indices, region_labels,
                               times, counts):
    for name in species:
        ans = collections.defaultdict(lambda: 0)
        for rlabel, rindi in zip(region_labels, region_indices):
            y = counts.loc[rindi][name].values
            ans[rlabel] += y
        for rlabel, y in ans.items():
            yield times, y, name, rlabel

def generate_histories(species, region_indices, region_labels,
                       times, counts, opts):
    func = generate_region_histories if opts.sum_regions else generate_element_histories
    return func(species, region_indices, region_labels, times, counts)

def _history(simul, species, region_indices, region_labels,
             times, counts, title, opts):

    import matplotlib
    if opts.save:
        matplotlib.use('Agg')
    from matplotlib import pyplot

    full_title = '{}, particle numbers of species {}'.format(title,
                                                             ', '.join(species))
    f = pyplot.figure(figsize=opts.geometry)
    f.canvas.set_window_title(full_title)

    ax = f.gca(yscale=opts.yscale)
    ax.set_xlabel('t / ms')
    ax.set_ylabel('particle numbers')
    colors = itertools.cycle('rgbkcmy')
    for x, y, name, rlabel in generate_histories(species, region_indices, region_labels,
                                                 times, counts, opts):
        ax.plot(x, y, opts.style, color=next(colors),
                label='{} in {}'.format(name, rlabel))
    ax.legend(loc='best', fontsize=7)
    if opts.save:
        fname = opts.save + ', particle numbers of species {}.svg'.format(', '.join(species))
        f.savefig(fname)
        print('saved {}'.format(fname))
    else:
        pyplot.show(block=True)

def _history_data(simul, species, region_indices, region_labels,
                  times, counts, title, opts):
    data = generate_histories(species, region_indices, region_labels,
                              times, counts, opts)
    xx, yy, names, rlabels = zip(*data)
    d = {(n, r):y for n, r, y in zip(names, rlabels, yy)}
    df = pd.DataFrame(d, index=xx[0])
    fname = opts.save_data + ', particle numbers.pickle'
    df.to_pickle(fname)
    print('saved', fname)

def find_regions(regions, spec):
    if spec:
        for item in spec_:
            try:
                region = int(item)
            except ValueError:
                w = regions[:] == item
                if w.sum() != 1:
                    raise ValueError("bad region: {}".format(item))
                region = w.argmax() # find True
            yield region
    else:
        yield from sorted(regions)

def plot_history(output, species):
    model = output.model
    simul = output.simulation(opts.trial)
    if not species:
        species = model.species()
    when = filter_times(opts.time, simul.times())

    regions = model.grid().region
    region_numbers = list(find_regions(regions, opts.regions))
    region_indices = np.arange(len(regions))[(regions[:, None] == region_numbers).any(axis=1)]
    region_labels = model.region_names(region_numbers)

    if opts.save_data:
        _history_data(simul, species,
                      region_indices, region_labels,
                      simul.times()[when], simul.counts()[when],
                      title=output.file.filename, opts=opts)
    else:
        _history(simul, species,
                 region_indices, region_labels,
                 simul.times()[when], simul.counts()[when],
                 title=output.file.filename, opts=opts)

def print_config(output):
    tree = output.simulation(0).config()
    if opts.config:
#        snippets = tree.xpath(opts.config,
#                              namespaces=dict(ns2="http://stochdiff.textensor.org"))
        expr = '//*[local-name() = $name]'
        snippets = tree.xpath(expr, name=opts.config)
    else:
        snippets = [tree]
    for i, what in enumerate(snippets):
        if i > 0:
            print()
        text = etree.tostring(what, encoding='unicode')
        print(highlight(text, XmlLexer(), Formatter()))

if __name__ == '__main__':
    opts = parser.parse_args()
    if opts.save is True:
        opts.save = os.path.splitext(opts.file.file.filename)[0]
    if opts.save_data is True:
        opts.save_data = os.path.splitext(opts.file.file.filename)[0]

    if opts.connections:
        dot_connections(opts.file)
    elif opts.dependencies:
        dot_dependencies(opts.file)
    elif opts.reactions:
        dot_productions(opts.file)
    elif opts.history is not None:
        plot_history(opts.file, opts.history)
    elif opts.config is not None:
        print_config(opts.file)
    else:
        if not opts.save:
            animate_drawing(opts)
        else:
            save_drawings_multi(opts)
            make_movie(opts.save)
            for fname in glob.glob('{}-*.png'.format(opts.save)):
                os.unlink(fname)
