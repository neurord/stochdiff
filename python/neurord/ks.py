import numpy as np
import pandas as pd

def bincount_axis1(data, max, weights=None):
    """Count the number of occurences of each value.

    Returns an array of counts corresponding to bins
    [0-1), [1-2), [max-1, max]. The right bin is closed on both
    sides, unlike all others!
    """
    if weights is None:
        ans = [np.bincount(col, minlength=max+1)[:, None]
               for col in data.T]
    else:
        ans = [np.bincount(col, wei, minlength=max+1)[:, None]
               for col, wei in zip(data.T, weights.T)]
    ans = np.hstack(ans)
    if ans.size >= 2:
        ans[-2] += ans[-1]
        return ans[:-1]
    else:
        return ans

def histogram(items, weights=None, normed=False, cumulative=False, min_max=None, bins=None):
    """Compute normed cdf of distribution of counts"""
    if min_max is None:
        left = items.min()
        right = items.max()
    else:
        left, right = min_max
    binsize = (max(right - left, 1)) / bins if bins is not None else 1
    where = np.empty_like(items, dtype=int)
    np.floor_divide(items - left, binsize, out=where)
    hist = bincount_axis1(where, (right-left)/binsize, weights)
    hist = np.vstack((0*hist[0], hist, 0*hist[0]))
    if normed:
        out = np.empty_like(hist, dtype=float)
        hist = np.true_divide(hist, items.shape[0], out=out)
    if cumulative:
        hist = hist.cumsum(axis=0)
    x = np.arange(0, hist.shape[0]) * binsize + left
    return x, hist

def apply_histogram(items, weights=None, normed=False, cumulative=False, min_max=None, bins=None):
    x, hist = histogram(items.values,
                        weights.values if weights is not None else None,
                        normed=normed, cumulative=cumulative, min_max=min_max, bins=bins)
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
