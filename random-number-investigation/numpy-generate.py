import numpy as np
import sys

spec = sys.argv[1]
N = int(sys.argv[2])
n = int(sys.argv[3])
p = float(sys.argv[4])
name = sys.argv[5]

xx = np.random.binomial(n, p, N)
with open(name, 'wt') as f:
    f.write('\n'.join(xx.astype(str)) + '\n')
