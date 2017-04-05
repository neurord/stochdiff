#!/usr/bin/python
# -*- coding:utf-8 -*-
from __future__ import print_function, division, unicode_literals

import sys
import os
import math
import glob, fnmatch
import pathlib
import re
import collections
import itertools
import argparse
import tempfile
import subprocess
import numpy as np
import tables
import contextlib
from lxml import etree
from pygments import highlight
from pygments.lexers import XmlLexer
from pygments.formatters import Terminal256Formatter
import pandas as pd

from . import output, ks
from .output import EventKind

def printf(fmt, *args, file=None, **kwargs):
    print(fmt.format(*args, **kwargs), file=file)

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

def int_list(arg):
    parts = arg.split(',')
    return list(int(part) for part in parts)

def time_slice(arg):
    if not arg:
        raise ValueError("either t₁-t₂, t₁-, -t₂, or t₁ must be specified")
    a, b, c = arg.partition('-')
    if not b:
        return float(a)
    a = float(a) if a else None
    c = float(c) if c else None
    return slice(a, c)

parser = argparse.ArgumentParser()
parser.add_argument('file', type=output.Output)
parser.add_argument('--save', nargs='?', const=True)
parser.add_argument('--save-data', nargs='?', const=True)
parser.add_argument('--connections', action='store_true')
parser.add_argument('--reactions', action='store_true')
parser.add_argument('--units', choices={'ms', 'ns'}, default='ms')
parser.add_argument('--dependencies', action='store_true')
parser.add_argument('--particles', action='store_true')
parser.add_argument('--stimulation', action='store_true')
parser.add_argument('--reaction', action='store_true')
parser.add_argument('--diffusion', action='store_true')
parser.add_argument('--format', default='dot', choices=('dot', 'tex', 'plain', 'pickle', 'sif'))
parser.add_argument('--geometry', type=geometry, default=(12, 9))
parser.add_argument('--history', type=str_list, nargs='?', const=())
parser.add_argument('--concentrations', action='store_true')
parser.add_argument('--function')
parser.add_argument('--regions', type=str_list, nargs='?')
parser.add_argument('--sum-regions', action='store_true')
parser.add_argument('--sum-all', action='store_true')
parser.add_argument('--describe-leaps', type=int_list, nargs='?', const=())
parser.add_argument('--leaps', type=int_list, nargs='?', const=())
parser.add_argument('--weighted', action='store_true')
parser.add_argument('--multiplot', nargs='?', const=1, type=int)
parser.add_argument('--num-elements', type=int,
                    help='take only the first so many elements')
parser.add_argument('--yscale', choices=('linear', 'log', 'symlog'))
parser.add_argument('--style', default='r-')
parser.add_argument('--time', type=time_slice)
parser.add_argument('--trial', type=int, default=0)
parser.add_argument('--config', type=str, nargs='?', const='')
parser.add_argument('--diff', type=str)
parser.add_argument('--output-group', '-g', default='__main__')

def filename_for_saving(opts, descr):
    if opts.save_data == '-':
        return '/dev/stdout'
    return opts.save_data + descr.replace(' ', '_') + '.' + opts.format

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
    print('running', ' '.join(command))
    subprocess.check_call(command)

def _logclip(x, offset):
    return np.clip(np.log(x + 1e-10) + offset, 0.3, 5)

@contextlib.contextmanager
def save_or_dot(ident):
    if opts.save:
        file = open(opts.save + '-{}.svg'.format(ident), 'w')
    else:
        tmp = tempfile.NamedTemporaryFile(prefix='{}-'.format(ident), suffix='.svg')
        file = open(tmp.name, 'w')
    yield file
    file.flush()
    if opts.save:
        print('Written', file.name)
    elif opts.format == 'dot':
        command = ['neato', '-Tx11', file.name]
        print('running', ' '.join(command))
        subprocess.check_call(command)

def dot_opts(**opts):
    opts = ['{}={}'.format(k, v)
            for k, v in opts.items() if v is not None]
    return ' [{}]'.format(','.join(opts)) if opts else ''

def _conn(dst, a, b, penwidth=None, label=None, **kwargs):
    opts = dot_opts(penwidth=penwidth, label=label, **kwargs)
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
    with save_or_dot('connections') as file:
        _connections(file, output.model)

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
        desc = descriptions[i]
        if opts.num_elements is not None and elem >= opts.num_elements:
            continue
        print('\t"{}" {};'.format(desc, dot_opts(fillcolor=fillcolor)), file=dst)
        for j in children:
            elem = elements[j]
            if opts.num_elements is not None and elem >= opts.num_elements:
                continue
            _conn(dst, desc, descriptions[j])

    print('}', file=dst)

def dot_dependencies(output):
    with save_or_dot('dependencies') as file:
        _dependencies(file, output.model)

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

def _productions_dot(dst, species, reactants, r_stoichio, products, p_stoichio, rates, reversibles):
    print('digraph Reactions {', file=dst)
    print('\trankdir=LR;', file=dst)
    print('\tsplines=true;', file=dst)
    print('\tnode [color=green,style=filled];', file=dst)
    for i, rr, rr_s, pp, pp_s, rate in zip(range(len(rates)),
                                           reactants, r_stoichio,
                                           products, p_stoichio, rates):
        name = _reaction_name(rr, rr_s, pp, pp_s, species)
        label = True
        if i in reversibles and i > reversibles[i]:
            label = False
            name = _reaction_name(pp, pp_s, rr, rr_s, species)
        if label:
            print('\t"{}" [color=black,shape=point,fillcolor=magenta];'.format(name), file=dst)
        # For each reversible pair, let one part do one direction,
        # other part the other direction.
        if i not in reversibles:
            for j, s in zip(rr, rr_s):
                _conn(dst, species[j], name, _logclip(rate, 10), style='dashed')
        for j, s in zip(pp, pp_s):
            _conn(dst, name, species[j], _logclip(rate, 10),
                  style=('dashed' if not label else None),
                  arrowhead=('none' if not label else None))
        if not len(rr) and not len(pp):
            print('\t"{}";'.format(name), file=dst)
        print()
    print('}', file=dst)

def format_num(num, unit):
    assert unit in {'ms', 'ns'}
    if unit == 'ns':
        num *= 1e3
    s = re.sub(r'e[+-]0*(\d+)', r'\cdot 10^{\1}', str(num))
    return s + r'/\mathrm{%s}' % (unit,)

def _reaction_name_tex(rr, rr_s, pp, pp_s, species, forward, backward, unit, align=True):
    forward = format_num(forward, unit)
    if backward:
        backward = format_num(backward, unit)
        arrow = r'\xleftrightarrow'
    else:
        backward = ''
        arrow = r'\xrightarrow'
    aligner = '&' if align else ''
    joiner = r'%s%s[%s]{%s}' % (aligner, arrow, backward, forward) # /\mathrm{ms}
    return joiner.join(
        ('+'.join('%s\species{%s}' % (s if s > 1 else '', species[r])
                  for r, s in zip(rr_, ss_)
                  if r >= 0)
         for rr_, ss_ in ((rr, rr_s), (pp, pp_s))))

def _tex_names(dst, species, reactants, r_stoichio, products, p_stoichio, rates, reversibles):
    for i, rr, rr_s, pp, pp_s, rate in zip(range(len(rates)),
                                           reactants, r_stoichio,
                                           products, p_stoichio, rates):
        if i in reversibles and i < reversibles[i]:
            yield _reaction_name_tex(rr, rr_s, pp, pp_s, species, rate, rates[reversibles[i]], opts.units)
        else:
            yield _reaction_name_tex(rr, rr_s, pp, pp_s, species, rate, None, opts.units)

def _productions_tex(dst, species, reactants, r_stoichio, products, p_stoichio, rates, reversibles):
    print(r'\begin{align*}')
    elements = list(_tex_names(dst, species, reactants, r_stoichio, products, p_stoichio, rates, reversibles))
    for left, right in itertools.zip_longest(elements[::2], elements[1::2]):
        print('  ' + left + ('\n    & ' + right if right else '') + r'\\')
    print(r'\end{align*}')

def _reaction_name_plain(rr, rr_s, pp, pp_s, species, forward, backward, unit, align=True):
    if backward:
        arrow = ' ←→ '
    else:
        arrow = ' → '
    return arrow.join(
        (' + '.join('%s%s' % (s if s > 1 else '', species[r])
                    for r, s in zip(rr_, ss_)
                    if r >= 0)
         for rr_, ss_ in ((rr, rr_s), (pp, pp_s))))

def _plain_names(dst, species, reactants, r_stoichio, products, p_stoichio, rates, reversibles):
    for i, rr, rr_s, pp, pp_s, rate in zip(range(len(rates)),
                                           reactants, r_stoichio,
                                           products, p_stoichio, rates):
        if i in reversibles:
            if i < reversibles[i]:
                yield _reaction_name_plain(rr, rr_s, pp, pp_s, species, rate, rates[reversibles[i]], opts.units)
        else:
            yield _reaction_name_plain(rr, rr_s, pp, pp_s, species, rate, None, opts.units)

def _productions_plain(dst, species, reactants, r_stoichio, products, p_stoichio, rates, reversibles):
    print(r'reactions:')
    for react in _plain_names(dst, species, reactants, r_stoichio, products, p_stoichio, rates, reversibles):
        print('  ' + react)

def _productions_sif(dst, species, reactants, r_stoichio, products, p_stoichio, rates, reversibles):
    for reaction, lhs_rhs in enumerate(zip(reactants, products)):
        lhs, rhs = lhs_rhs
        for i in lhs:
            printf('{} Compound-Reaction reaction{}', species[i], reaction, file=dst)
        for i in rhs:
            printf('reaction{} Reaction-Compound {}', reaction, species[i], file=dst)

def dot_productions(output):
    model = output.model
    reactions = model.reactions
    func = {'dot':_productions_dot, 'tex':_productions_tex, 'plain':_productions_plain, 'sif':_productions_sif}[opts.format]
    with save_or_dot('reactions') as file:
        func(file, model.species(),
             reactions.reactants(), reactions.reactant_stoichiometry(),
             reactions.products(), reactions.product_stoichiometry(),
             reactions.rates(), reactions.reversible_pairs())

def specie_indices(items, species):
    species = list(species)
    return np.array([species.index(i) for i in items])

def generate_element_histories(model, species, element_indices, element_regions, values):
    fmt = '{name} el.{element}'
    if len(set(element_regions)) > 1:
        fmt += ' {region}'

    for name in species:
        for rlabel, elem in zip(element_regions, element_indices):
            series = values.loc[elem][name]
            times = series.index.values
            y = series.values
            label = fmt.format(name=name, element=elem, region=rlabel)
            yield times, y, name, label

def generate_region_histories(model, species, element_indices, element_regions, values):
    fmt = '{name}'
    if len(set(element_regions)) > 1:
        fmt += ' {region}'
    for name in species:
        ans = collections.defaultdict(lambda: 0)
        assert len(element_regions) == len(element_indices)
        for rlabel, elem in zip(element_regions, element_indices):
            series = values.loc[elem][name]
            times = series.index.values
            ans[rlabel] += series.values
        for rlabel, y in sorted(ans.items(),
                                key=lambda pair: model.region_names().index(pair[0])):
            label = fmt.format(name=name, region=rlabel)
            yield times, y, name, label

def generate_total_histories(model, species, element_indices, element_regions, values):
    assert len(element_regions) == len(element_indices)
    for name in species:
        ans = 0
        for rlabel, elem in zip(element_regions, element_indices):
            series = values.loc[elem][name]
            times = series.index.values
            ans += series.values
        yield times, ans, name, name

def generate_histories(model, species, element_indices, element_regions, values, opts):
    if opts.sum_regions:
        func = generate_region_histories
    elif opts.sum_all:
        func = generate_total_histories
    else:
        func = generate_element_histories
    return func(model, species, element_indices, element_regions, values)

def _history(model, simul, species, element_indices, element_regions,
             values, title, opts):

    import matplotlib
    if opts.save:
        matplotlib.use('Agg')
    from matplotlib import pyplot

    quantity = 'concentration / nM' if opts.concentrations else 'particle numbers'
    full_title = '{}, {} of species {}'.format(title, quantity, ', '.join(species))
    f = pyplot.figure(figsize=opts.geometry)
    f.canvas.set_window_title(full_title)

    data = list(generate_histories(model, species, element_indices, element_regions,
                                   values, opts))

    sharex = None
    if opts.multiplot:
        i = 1
        cols = opts.multiplot
        rows = math.ceil(len(data) / cols)
        for x, y, name, label in data:
            ax = f.add_subplot(rows, cols, i, yscale=opts.yscale, sharex=sharex)
            ax.plot(x, y, opts.style, label=label)
            ax.legend(loc='best', fontsize=8)
            ax.locator_params(nbins=3)
            i += 1
            if sharex is None:
                sharex = ax
    else:
        ax = f.gca(yscale=opts.yscale)
        ax.set_ylabel(quantity)
        colors = itertools.cycle('rgbkcmy')
        for x, y, name, label in data:
            ax.plot(x, y, opts.style, color=next(colors), label=label)
            if opts.function:
                yy = eval(opts.function, sys.modules, dict(x=x, y=y))
                ax.plot(x, yy, label=opts.function, ls='--')
        ax.legend(loc='best', fontsize=7)
    ax.set_xlabel('t / ms')
    if opts.save:
        fname = opts.save + ',_particle_numbers_of_species_{}.svg'.format(','.join(species))
        f.savefig(fname)
        print('saved {}'.format(fname))
    else:
        pyplot.show(block=True)

def _history_data(model, simul, species, element_indices, element_regions,
                  values, title, opts):
    data = generate_histories(model, species, element_indices, element_regions,
                              values, opts)
    xx, yy, names, rlabels = zip(*data)
    d = {(n, r):y for n, r, y in zip(names, rlabels, yy)}
    df = pd.DataFrame(d, index=xx[0])
    quantity = 'concentration / nM' if opts.concentrations else 'particle numbers'
    fname = filename_for_saving(opts, quantity)
    if opts.format == 'pickle':
        df.to_pickle(fname)
    elif opts.format == 'plain':
        print(df)
    else:
        raise ValueError("don't know how to save {}".format(opts.format))
    print('saved', fname)

def find_regions(regions, region_names, spec):
    if spec:
        for item in spec:
            try:
                yield int(item)
            except ValueError:
                yield region_names.index(item)
    else:
        yield from sorted(set(regions))

def find_species(output, output_group, specie_spec):
    all_species = output.model.output_group(output_group).species()
    if not specie_spec:
        return all_species

    have_globs = any(glob.escape(pat) != pat for pat in specie_spec)
    if not have_globs:
        # preserve the specified order
        return specie_spec

    # use the order in the file (globs can match more than once, so glob order is not useful)
    matches = [sp for sp in all_species
               if any(fnmatch.fnmatchcase(sp, pat)
                      for pat in specie_spec)]
    if not matches:
        raise ValueError('no species matched by {}'.format(specie_spec))
    return matches

def plot_history(output, species):
    model = output.model
    simul = output.simulation(opts.trial)
    # filter time. level 0 is voxel, level 1 is time
    if opts.concentrations:
        values = simul.concentrations(opts.output_group)
    else:
        values = simul.counts(opts.output_group)
    if opts.time is not None:
        values = values.loc[(slice(None), opts.time), :]

    regions = model.grid().region
    region_numbers = list(find_regions(regions, model.region_names(), opts.regions))
    element_indices = np.arange(len(regions))[(regions[:, None] == region_numbers).any(axis=1)]
    element_regions = model.element_regions()[element_indices]

    if opts.save_data:
        _history_data(model, simul, species,
                      element_indices, element_regions,
                      values,
                      title=output.file.filename, opts=opts)
    else:
        _history(model, simul, species,
                 element_indices, element_regions,
                 values,
                 title=output.file.filename, opts=opts)

def grouped_histogram(events, group_keys, what_key, weight_key=None):
    min_max = (events[what_key].min(), events[what_key].max())
    gb = events.groupby(group_keys)
    hists = dict(
        (key, ks.apply_histogram(group[[what_key]],
                                 group[[weight_key]] if weight_key else None,
                                 min_max=min_max,
                                 bins=50)[what_key])
        for key, group in gb)
    df = pd.DataFrame(hists)
    return df

def reverse_legend(ax, *, ax2=None, **kwargs):
    handles, labels = ax.get_legend_handles_labels()
    if ax2 is not None:
        handles2, labels2 = ax2.get_legend_handles_labels()
        handles += handles2
        labels += labels2
    ax.legend(handles[::-1], labels[::-1], **kwargs)

def describe_leaps(output):
    model = output.model
    simul = output.simulation(opts.trial)
    descriptions = list(model.dependencies.descriptions())
    species = model.species()

    events = simul.events()
    if opts.describe_leaps:
        events = events.loc[events['event'].isin(opts.describe_leaps)]
    events['extent'] = np.abs(events['extent'])

    gb = events.groupby(['event', 'kind'])
    for key, group in gb:
        event, kind = key
        label = '{}, {}'.format(descriptions[event], EventKind(kind).name.lower())
        waited = group['waited']
        if waited.max() < 10:
            continue
        print(label, waited.size, waited.min(), waited.max(), np.median(waited))

def plot_leaps(output, leaps):
    title = output.file.filename
    model = output.model
    simul = output.simulation(opts.trial)
    descriptions = list(model.dependencies.descriptions())

    import matplotlib
    if opts.save:
        matplotlib.use('Agg')
    from matplotlib import pyplot

    joined = ','.join(str(leap) for leap in leaps)
    full_title = '{}, event timings {}'.format(title, joined)
    f = pyplot.figure(figsize=opts.geometry)
    f.canvas.set_window_title(full_title)
    ax = f.gca(yscale=opts.yscale)
    ax2 = ax.twinx()
#    ax2.set_yscale(opts.yscale)
#    ax2.yaxis.tick_right()

    events = simul.events()
    if leaps:
        events = events.loc[events['event'].isin(leaps)]
    if events.size == 0:
        return
    events['extent'] = np.abs(events['extent'])
    hist = grouped_histogram(events, 'event kind'.split(), 'waited',
                             'extent' if opts.weighted else None)
    base = 0
    for event_kind in hist:
        event, kind = event_kind
        label = '{}, {}'.format(descriptions[event], EventKind(kind).name.lower())
        values = hist[event_kind].values
        _ax = ax2 if kind == EventKind.LEAP else ax
        _ax.step(hist.index, base + values,
                 fillstyle='none',
                 linestyle='-' if kind==1 else '--',
                 label=label)
        values[np.isnan(values)] = 0
        base += values
    ax.legend(loc='upper right')
    reverse_legend(ax, ax2=ax2, fontsize=8)
    pyplot.setp(ax.xaxis.get_majorticklabels(), fontsize=8)
    pyplot.setp(ax.yaxis.get_majorticklabels(), fontsize=8)
    pyplot.setp(ax2.yaxis.get_majorticklabels(), fontsize=8)
    if opts.save:
        fname = opts.save + ', event timings of events {}.svg'.format(joined)
        f.savefig(fname)
        print('saved {}'.format(fname))
    else:
        pyplot.show(block=True)

def print_config(output, config_spec):
    tree = output.simulation(0).config()
    if config_spec:
        expr = '//*[local-name() = $name]'
        snippets = tree.xpath(expr, name=config_spec)
    else:
        snippets = [tree]
    for i, what in enumerate(snippets):
        if i > 0:
            print()
        text = etree.tostring(what, encoding='unicode')
        if sys.stdout.isatty():
            formatter = Terminal256Formatter()
            print(highlight(text, XmlLexer(), formatter))
        else:
            print(text)

def temporary_config_file(what):
    if isinstance(what, str) and not what.endswith('.h5'):
        # assume a real file, process for xi::include
        tree = etree.parse(what)
        tree.xinclude()
        parser = etree.XMLParser(remove_blank_text=True, remove_comments=True)
        tree = etree.fromstring(etree.tostring(tree), parser)
        prefix = pathlib.Path(what).stem + '-'
        file = tempfile.NamedTemporaryFile(prefix=prefix, suffix='.xml')
    else:
        if not isinstance(what, output.Output):
            what = output.Output(what)
        tree = what.simulation(0).config()
        file = tempfile.NamedTemporaryFile(prefix=what.file.filename + '-', suffix='.xml')
    text = etree.tostring(tree, pretty_print=True)
    if isinstance(what, str):
        text = text.replace(b'  ', 4*b' ')
    file.write(text)
    file.flush()
    return file

def print_diff(this, other):
    file1 = temporary_config_file(this)
    file2 = temporary_config_file(other)
    subprocess.run(['git', 'diff', '--no-index', '--ignore-all-space', file1.name, file2.name])

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
        species = find_species(opts.file, opts.output_group, opts.history)
        plot_history(opts.file, species)
    elif opts.leaps is not None:
        plot_leaps(opts.file, opts.leaps)
    elif opts.describe_leaps is not None:
        describe_leaps(opts.file)
    elif opts.config is not None:
        print_config(opts.file, opts.config)
    elif opts.diff is not None:
        print_diff(opts.file, opts.diff)
    else:
        if not opts.save:
            animate_drawing(opts)
        else:
            save_drawings_multi(opts)
            make_movie(opts.save)
            for fname in glob.glob('{}-*.png'.format(opts.save)):
                os.unlink(fname)
    opts.file.file.close()
