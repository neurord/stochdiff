#!/usr/bin/python
from __future__ import print_function, division

import sys
import os
import glob
import argparse
import subprocess
import numpy
import tables

parser = argparse.ArgumentParser()
parser.add_argument('file', type=tables.openFile)
parser.add_argument('--save')
parser.add_argument('--connections', action='store_true')
parser.add_argument('--diffusion', action='store_true')

def draw(model, sim, opts):
    from matplotlib import pyplot
    from matplotlib.colors import LogNorm
    pyplot.ion()
    if not opts.diffusion:
        data = sim.concentrations[:]
        title = 'Particle numbers'
    else:
        # reduce dimensionality by summing over all neighbours
        data = sim.diffusion_events[:].sum(axis=-1)
        title = 'Diffusion events'
    N = numpy.arange(model.species.shape[0])
    V = numpy.arange(data.shape[0])

    f = pyplot.figure()
    f.clear()
    ax = f.gca()
    ax.set_xlabel("species")
    ax.xaxis.set_ticks(N + 0.5)
    ax.xaxis.set_ticklabels(model.species, rotation=70)
    ax.set_ylabel("voxel#")
    # matplotlib gets confused if we draw all zeros
    initial = data.sum(axis=(1,2)).argmax()
    im = ax.imshow(data[initial], origin='lower',
                   extent=(0, data.shape[2], 0, data.shape[1]),
                   interpolation='spline16', aspect='auto',
                   norm=LogNorm())
    f.colorbar(im)
    f.tight_layout()
    if not opts.save:
        f.show()

    while True:
        for i in range(0, data.shape[0]):
            ax.set_title("{}   step {:>3}, t = {:8.4f} ms"
                         .format(title, i, sim.times[i]))
            im.set_data(data[i])
            if not opts.save:
                f.canvas.draw()
            else:
                f.savefig('{}-{:06d}.png'.format(opts.save, i))
                print('.', end='')
                sys.stdout.flush()
        if opts.save:
            break

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

        draw(opts.file.root.model, opts.file.root.simulation, opts)
        if opts.save:
            make_movie(opts.save)
            for fname in glob.glob('{}-*.png'.format(opts.save)):
                os.unlink(fname)
