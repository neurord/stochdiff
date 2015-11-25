import itertools
import numpy as np
from matplotlib import pyplot as plt

from . import output

class Diffusion:
    def __init__(self, species, element, element2, specie):
        self.element = element
        self.element2 = element2
        self.specie = specie
        self._specie = species.index(specie)

    def execute(self, particles, count):
        particles[self.element, self._specie] -= count
        particles[self.element2, self._specie] += count

    def __str__(self):
        return 'Diffusion {self.specie} el.{self.element}→{self.element2}'.format(self=self)

def _description_side(vv, species):
    return ('+'.join('{}×{}'.format(v, l) if v != 1 else str(l)
                     for (v, l)
                     in zip(vv, species))
            or '∅')

class Reaction:
    def __init__(self, species, element,
                 reactants, reactant_stoichiometry,
                 products, product_stoichiometry):
        self.element = element
        self.reactants = reactants
        self._reactants = [species.index(r) for r in reactants]
        self.reactant_stoichiometry = reactant_stoichiometry
        self.products = products
        self._products = [species.index(p) for p in products]
        self.product_stoichiometry = product_stoichiometry

    def execute(self, particles, count):
        for i, s in zip(self._reactants, self.reactant_stoichiometry):
            particles[self.element, i] -= count * s
        for i, s in zip(self._products, self.product_stoichiometry):
            particles[self.element, i] += count * s

    def __str__(self):
        a = _description_side(self.reactant_stoichiometry, self.reactants)
        b = _description_side(self.product_stoichiometry, self.products)
        return 'Reaction el.{.element} {}→{}'.format(self, a, b)

class Stimulation:
    def __init__(self, species, element, specie):
        self.element = element
        self.specie = specie
        self._specie = species.index(specie)

    def execute(self, particles, count):
        particles[self.element, self._specie] += count

    def __str__(self):
        return 'Stimulation el.{self.element} {self.specie}'.format(self=self)

def parse(species, desc):
    if desc.startswith('NextDiffusion'):
        prefix, specie, fromto = desc.split(' ');
        assert fromto.startswith('el.')
        element, element2 = fromto[3:].split('→')
        element = int(element)
        element2 = int(element2)
        return Diffusion(species, element, element2, specie)
    if desc.startswith('NextReaction'):
        prefix, element, what = desc.split(' ');
        assert element.startswith('el.')
        element = int(element[3:])
        left, right = what.split('→')
        lefts = left.split('+')
        rights = right.split('+')
        return Reaction(species, element,
                        lefts, np.ones(len(lefts)),    # higher orders
                        rights, np.ones(len(rights)))
    if desc.startswith('NextStimulation'):
        prefix, element, specie = desc.split(' ');
        assert element.startswith('el.')
        element = int(element[3:])
        return Stimulation(species, element, specie)
    raise ValueError(desc)

class Wrapper:
    def __init__(self, model):
        species = list(model.species())
        nel = model.grid().shape[0]
        self.events = [parse(species, desc)
                       for desc in model.dependencies.descriptions()]
        self.particles = np.zeros((nel, len(species)))

    @classmethod
    def create(cls, filename):
        out = output.Output(filename)
        wrapper = cls(out.model)
        return wrapper

    def execute(self, simulation):
        times = simulation.times()
        events = simulation.events()
        particles = self.particles.copy()
        history = np.empty((len(times),) + particles.shape)

        j = 0

        time = events.index
        event = events.event
        extent = events.extent
        
        for i in range(event.size):
            while times[j] < time[i]:
                history[j] = particles
                print('saved', times[j])
                j += 1
                if j == len(times) - 1:
                    return history

            type = self.events[event.iloc[i]]
            type.execute(particles, extent.iloc[i])

        history[j:] = particles
        print('saved', ' '.join(str(t) for t in times[j:]))
        return history

def plot_history(sim, history='history.npy'):
    history = np.load(history).astype(int)
    offset = np.array([sim.counts().iloc[0, i] for i in range(history.shape[1])])
    print(offset)
    history += offset
    print(history[0])
    f, axes = plt.subplots(12, 3)
    for i in range(12):
        for j in range(3):
            axes[i,j].plot(history[:, j, i])
    return f

