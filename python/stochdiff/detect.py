from __future__ import division
import functools
import numpy as np
import math

def arrayize(type):
    def decorator(func):
        def wrapper(*args, **kwargs):
            return np.fromiter(func(*args, **kwargs), type)
        return functools.update_wrapper(wrapper, func)
    return decorator

def listize(func):
    def wrapper(*args, **kwargs):
        return list(func(*args, **kwargs))
    return functools.update_wrapper(wrapper, func)

def nanize(func):
    def wrapper(*args, **kwargs):
        try:
            return func(*args, **kwargs)
        except IndexError:
            return (np.nan, np.nan)
    return functools.update_wrapper(wrapper, func)

@arrayize(int)
def detect_peaks(y, min_high_ratio=0.25, P_low=0.5, P_high=0.5, both=False):
    low_i, low = 0, y[0]
    high_i, high = 0, y[0]

    i = (y > y.max() * min_high_ratio).argmax() # find True
    while True:
        for i in range(i, len(y)):
            if y[i] < low:
                low_i, low = i, y[i]
            if y[i] > high:
                high_i, high = i, y[i]

            if y[i] - low < (high - low) * P_high:
                break
        else:
            break

        yield high_i
        low_i, low = i, y[i]

        for i in range(i, len(y)):
            if y[i] < low:
                low_i, low = i, y[i]
            if y[i] > high:
                high_i, high = i, y[i]

            if y[i] - low > (high - low) * P_low:
                break
        else:
            break

        if both:
            yield low_i
        high_i, high = i, y[i]


def peak_period(t, y, min_height):
    k = detect_peaks(y)
    k = k[k > min_height]
    if not k.size:
        return np.nan
    else:
        return np.diff(k).mean()

@listize
def detect_upstates(y, min_high_ratio=0.50, P_high=0.25, P_low=0.60, upstates=None, downstates=None):
    """Find upstates, i.e. periods where the system is above average.
    """
    if upstates is None and downstates is None:
        upstates, downstates = True, False
    elif upstates is None:
        upstates = not downstates
    elif downstates is None:
        downstates = not upstates

    low_i, low = 0, y[0]
    high_i, high = 0, y[0]

    last_high_end = -1

     # Assume we start in the low state
    i = ((y-y.min()) > (y.max()-y.min()) * min_high_ratio).argmax() # find True
    while True:
        start_i = high_i
        for i in range(start_i, len(y)):
            if y[i] < low:
                low_i, low = i, y[i]
            if y[i] > high:
                high_i, high = i, y[i]

            if y[i] - low < (high - low) * P_high:
                break
        else:
            break

        # We are again in the low area.
        # Classify the part which is before the first halfway point as low,
        # and the part until the last halfway point as high.
        thresh = (high - low) / 2
        above = np.flatnonzero( y[start_i : i] >= thresh ) + start_i
        print(low_i, i, above)
        if downstates:
            yield (last_high_end + 0.5, above[0] - 0.5)
        if upstates:
            yield (above[0] - 0.5, above[-1] + 0.5)
        last_high_end = above[-1]

        low_i, low = i, y[i]
        print('low:', low_i, low)

        for i in range(low_i, len(y)):
            if y[i] < low:
                low_i, low = i, y[i]
            if y[i] > high:
                high_i, high = i, y[i]

            if y[i] - low > (high - low) * P_low:
                break
        else:
            break

        high_i, high = i, y[i]
        print('high:', high_i, high)

def state_lifetime(states):
    times = np.fromiter((end - start for (start, end) in states), dtype=float)
    return (times.mean(), times.std(ddof=1) / times.size**0.5)

@nanize
def state_population(states, population):
    all = np.hstack((population[math.ceil(beg):math.ceil(end)]
                     for beg,end in states))
    return (all.mean(), all.std(ddof=1) / all.size**0.5)
