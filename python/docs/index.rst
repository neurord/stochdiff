neurord.output module
***********************

.. automodule:: neurord.output
    :members:

Examples
~~~~~~~~

.. plot::
   :include-source:

   import matplotlib.pyplot as plt
   import numpy as np
   
   from neurord import output
   
   out = output.Output('model.h5')
   counts = out.counts()
   gb = counts.groupby(level='voxel time  specie'.split())
   
   def stdp3(x):
      return np.mean(x) + np.std(x)*100
   def stdm3(x):
      return np.mean(x) - np.std(x)*100
   
   data = gb.aggregate([np.mean, stdp3, stdm3])
   data.plot()
   plt.show()
