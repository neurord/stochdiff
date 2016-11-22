import sys
import pathlib
import math
import numpy as np
import pandas as pd
from matplotlib import pyplot
from scipy import stats

def rnd(x):
    return round(x, -math.floor(math.log10(x))+1 if x > 0 else 0)

name = sys.argv[1]

stem = pathlib.Path(name).stem
desc = stem.split('-')
generator, n, p = desc[1], int(desc[2]), float(desc[3])

dat = pd.read_table(name, header=None)
left, right = float(dat.min()), float(dat.max())
bins=np.r_[left-0.5 : right+1]
ax = dat.hist(bins=bins)[0,0]
ax.set_xlim(left-0.5, right+0.5)
N = dat.size

dist = stats.binom(n=n, p=p)
xx = np.r_[left : right+0.5]
ax.plot(xx, dist.pmf(xx) * N, 'ro')

# is the mean 0.5
expected = n * p
expected_std = n * (p * (1-p))**0.5 / N**0.5

ax.axvline(expected, linewidth=3, color='r')

mean = float(dat.mean())
msg = 'n={} p={} mean_exp={}±{}, mean_act={}, diff={:.2f}σ' \
      .format(n, p, expected, rnd(expected_std), rnd(mean), (mean-expected)/expected_std)
ax.set_title(msg)
ax.figure.savefig(stem + '.svg')
