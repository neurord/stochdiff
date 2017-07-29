1. Simple model: Model_simple.xml

	- three species and single reaction: glutamate (glu) binding to buffer (buf): Rxn_glubuf.xml
	- single voxel morphology: Morph1.5um.xml
	- All species initialized to 0: IC_glubuf.xml.
	- No output file, just use the default output.
	- Stimulation: inject glutamate, and then its buffer.  Specified in the Model file.
	
	>>> Simulate this model.  Then make the following changes:
		initialize glu and buf to non-zero values and eliminate the stimulation
	
2. single compartment, cascade of reactions from metabotropic glutamate receptor through production of IP3

   - Model_mglur.xml
   - Rxn_mglur.xml
   - IC_mglur.xml
   - Morph1comp.xml
   - Out_mglur.xml
   
  >>> evaluate how changing morphology size (number of molecules) changes molecule noise
     Simulate this model.  Then, make the following changes:
	 change depth2D from 8 to 0.008 (and change stim rate from 6.25e3 to 6.25)
  ##### screen shots of neurordh5 output showing molecules = model_mglur_depth2D.png
  
3. Multiple voxels, cascade of reactions from metabotropic glutamate receptor through production of IP3

   - Model_mglur_diff.xml
   - Rxn_mglur_diff.xml
   - IC_mglur.xml
   - Morph1comp.xml
   - Out_mglur.xml 
   
   In Model file, defaultMaxElementSide = 3.0, to create multiple voxels.
   Uses Rxn_mglur_diff.xml which has non-zero diffusion constants.
  >>> Evaluate the effect of finer mesh elements using the "surface layers" specification
	Simulate this model.  Then, make the following changes:
	Uncomment the surface layers to make smaller submembrane voxels
  ##### screen shots using NeuroRDViz to see how mesh changes = morph3x3surfx.png
   
4. Multiple voxels and regions by adding a spine to the morphology

   - Model_mglur_spine-Ca(Y, N or Ntrain).xml
   - Rxn_Ca_mglur.xml: Same reactions as previous model: Glutamate through IP3.
   - Out_Ca_mglur.xml:  Same output file specification as previous model
   - Morph_1sp2umDend has dendrite 2 um long with 1 spine type to show how to create spines.
   - IC_mglur_Ca_spine shows how to specify submembrane initial conditions and region specific (e.g. spine head) initial conditions.
   - stimulation in 3 separate files described next
    
   >>> Three model files allows you to evaluate the effect of calcium, or a train of glutamate
   simulate the three models that differ only in the Stim file
   
   a. Stim_mglur_spine.xml
	Injects glutamate (and then buffer to remove the glutamate), but no calcium
   b. Stim_mglur_Ca_spine.xml
	Injects glutamate and calcium. Since calcium synergistically activates Plc, this shows interaction between calcium and glutamate
   c. Stim_glu_train.xml 
	Injects two trains of glutamate. Increased glutamate increases the production of IP3
   
   ######## visualize morphology: Model_morph_spine.png, surface density initial condition: Model_IC_spine.png
   plot together using facility of nrdh5_anal.py that allows plotting of several models that use same morphology, etc.
   ######## nrdh5_anal.py output shows the effect of calcium: Model_mglur_spine-Ca.png

The next set of models show variations in morphology that can be created in NeuroRD.  They use the single reaction model to speed simulation. 

5. 2D morphology with two types of spines shows how to create spine prototypes, and distribute them on a long dendrite.

- Model_2spinetypes.xml
- Rxn_glubuf.xml
- IC_glubuf.xml
- Morph20um_2spinetypes.xml
- Out_glu.xml
######## visualize morphology: Model_2spinetypes.png


6. 3D morphology with spines. Differs from above by 3D diffusion and curved voxels in model file.

- Model_2spinetypes3D.xml
- Rxn_glubuf.xml
- IC_glubuf.xml
- Morph20um_2spinetypes.xml
- Out_glu.xml

######## visualization of morphology may not work: Exception in thread "main" java.lang.NoClassDefFoundError: javax/vecmath/Tuple3d

7. dend + soma with different mesh element size for soma
Shows how to use a different mesh for a larger compartment
#### Model_dend_soma.xml, Model_dend_soma.png

>>> Examples (5), (6) and (7) above can be simulated using the cascade of reactions from metabotropic glutamate receptor 
through production of IP3 e.g. try these using Rxn_Ca_mglur.xml, IC_mglur_Ca_spine.xml and the stimulation file of your choice.

8. branching morphology model shows how to create branched dendrites
- Model_branchRad3.xml
- Rxn_glubuf.xml
- IC_glubuf.xml
- Morph20um_2spinetypes.xml
- Out_glu.xml

######## -> Model_branchRad3.png

>>> To simulate these with Rxn_Ca_mglur.xml will need to create an IC file that either specifies only general concentration
and surface density sets, or alternatively initialize molecules differently in the different branches. 
Similarly, the stimulation will need to be specified somewhere other than into spines, either at ends of branches 
or into the submembrane of one of the segments 


