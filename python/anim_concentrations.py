#!/usr/bin/python
from __future__ import print_function, division

import sys
import os
import glob
import random
import argparse
import subprocess
import numpy
import tables
import matplotlib
matplotlib.use('Agg')
from matplotlib import pyplot

parser = argparse.ArgumentParser()
parser.add_argument('file', type=tables.openFile)
parser.add_argument('--save')
parser.add_argument('--connections', action='store_true')

def draw(sim, save):
    concs = sim.concentrations
    N = numpy.arange(sim.species.shape[0])
    V = numpy.arange(concs.shape[0])
    times = sim.times

    f = pyplot.figure()
    f.clear()
    ax = f.gca()
    ax.set_xlabel("species")
    ax.xaxis.set_ticks(N)
    ax.xaxis.set_ticklabels(sim.species, rotation=70)
    ax.set_ylabel("voxel#")
    im = ax.imshow(concs[0], origin='lower',
                   extent=(0, concs.shape[2], 0, concs.shape[1]),
                   interpolation='spline16', aspect='auto')
    if not save:
        f.show()

    while True:
        for i in range(0, concs.shape[0]):
            ax.set_title("step {:>3}, t = {:8.4f} ms".format(i, times[i]))
            im.set_data(concs[i])
            if not save:
                f.canvas.draw()
            else:
                f.savefig('{}-{:06d}.png'.format(save, i))
                print('.', end='')

def make_movie(save):
    command = '''mencoder -mf type=png:w=800:h=600:fps=25
                 -ovc lavc -lavcopts vcodec=mpeg4 -oac copy -o'''.split()
    subprocess.check_call(command + [save, 'mf://*.png'.format(save)])

def dottify(dst, connections, couplings):
    print('digraph Connections {', file=dst)
    print('\trankdir=LR;', file=dst)
    print('\tsplines=true;', file=dst)
    print('\tnode [color=blue,style=filled,fillcolor=lightblue];', file=dst)
    for i in range(connections.shape[0]):
        for j, coupl in zip(connections[i], couplings[i]):
            if j < 0:
                break
            print('\t{} -> {} [penwidth={}];'.format(i, j, 10*coupl + random.random()), file=dst)
    print('}', file=dst)

def dot_connections(sim):
    connections = sim.root.simulation.neighbors
    couplings = sim.root.simulation.couplings
    dottify(sys.stdout, connections, couplings)

if __name__ == '__main__':
    opts = parser.parse_args()
    if opts.connections:
        dot_connections(opts.file)
    else:
        draw(opts.file.root.simulation, opts.save)
        if opts.save:
            make_movie(opts.save)
            for fname in glob.glob('{}-*.png'.format(save)):
                os.unlink(fname)
