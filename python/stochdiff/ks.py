import numpy as np
import pandas as pd

def bincount_axis1(data):
    """Count the number of occurences of each value.

    Returns an array of counts corresponding to bins
    [0-1), [1-2), [max-1, max]. The right bin is closed on both
    sides, unlike all others!
    """
    max = data.max()
    ans = [np.bincount(col, minlength=max+1)[:, None]
           for col in data.T]
    ans = np.hstack(ans)
    ans[-2] += ans[-1]
    return ans[:-1]

def histogram(items, normed=False, cumulative=False, bins=None):
    """Compute normed cdf of distribution of counts"""
    left = items.min()
    right = items.max()
    binsize = (right - left) / bins if bins is not None else 1
    where = np.empty_like(items, dtype=int)
    np.floor_divide(items - left, binsize, out=where)
    hist = bincount_axis1(where)
    hist = np.vstack((0*hist[0], hist, 0*hist[0]))
    if normed:
        out = np.empty_like(hist, dtype=float)
        hist = np.true_divide(hist, items.shape[0], out=out)
    if cumulative:
        hist = hist.cumsum(axis=0)
    x = np.arange(-0.5, hist.shape[0]-0.5) * binsize + left
    return x, hist

def apply_histogram(items, normed=False, cumulative=False, bins=None):
    x, hist = histogram(items.values, normed=normed, cumulative=cumulative, bins=bins)
    if normed:
        label = 'frequency'
    else:
        label = 'count'
    df = pd.DataFrame(hist, index=x)
    df.index.name = label
    df.columns = items.columns
    return df

def ks_distance(data, best=0):
    hists = apply_histogram(data, normed=True, cumulative=True)
    values = hists.values
    return np.abs(values - values[:, best:best+1]).max(axis=0)
