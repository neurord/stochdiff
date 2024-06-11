from xml.etree import ElementTree as ET
import libsbml  #pip install python-libsbml
import simplesbml #pip install simplesbml
import sys
import argparse
import os
import time

micron_cube_to_litre = 1e-15

## Validation
class validateSBML:

    def __init__(self, ucheck):
        self.reader    = libsbml.SBMLReader()
        self.ucheck    = ucheck
        self.numinvalid = 0
  
    def validate(self, file):
        if not os.path.exists(file):
            print("[Error] %s : No such file." % file)
            self.numinvalid += 1
            return
  
        start    = time.time()
        sbmlDoc  = libsbml.readSBML(file)
        stop     = time.time()
        timeRead = (stop - start)*1000
        errors   = sbmlDoc.getNumErrors()
        
        seriousErrors = False
  
        numReadErr  = 0
        numReadWarn = 0
        errMsgRead  = ""
  
        if errors > 0:
            for i in range(errors):
                severity = sbmlDoc.getError(i).getSeverity()
                if (severity == libsbml.LIBSBML_SEV_ERROR) or (severity == libsbml.LIBSBML_SEV_FATAL):
                    seriousErrors = True
                    numReadErr += 1
                else:
                    numReadWarn += 1
  
                errMsgRead = sbmlDoc.getErrorLog().toString()
  
        # If serious errors are encountered while reading an SBML document, it
        # does not make sense to go on and do full consistency checking because
        # the model may be nonsense in the first place.
  
        numCCErr  = 0
        numCCWarn = 0
        errMsgCC  = ""
        skipCC    = False
        timeCC    = 0.0
  
        if seriousErrors:
            skipCC = True
            errMsgRead += "Further consistency checking and validation aborted."
            self.numinvalid += 1
        else:
            sbmlDoc.setConsistencyChecks(libsbml.LIBSBML_CAT_UNITS_CONSISTENCY, self.ucheck)
            start    = time.time()
            failures = sbmlDoc.checkConsistency()
            stop     = time.time()
            timeCC   = (stop - start)*1000
    
            if failures > 0:
                isinvalid = False
                for i in range(failures):
                    severity = sbmlDoc.getError(i).getSeverity()
                    if (severity == libsbml.LIBSBML_SEV_ERROR) or (severity == libsbml.LIBSBML_SEV_FATAL):
                        numCCErr += 1
                        isinvalid = True
                    else:
                        numCCWarn += 1
  
                if isinvalid:
                    self.numinvalid += 1
  
                errMsgCC = sbmlDoc.getErrorLog().toString()
  
        # print results  
        print("                 filename : %s" % file)
        print("         file size (byte) : %d" % (os.path.getsize(file)))
        print("           read time (ms) : %f" % timeRead)
  
        if not skipCC :
            print( "        c-check time (ms) : %f" % timeCC)
        else:
            print( "        c-check time (ms) : skipped")
  
        print( "      validation error(s) : %d" % (numReadErr  + numCCErr))
        if not skipCC :
            print( "    (consistency error(s)): %d" % numCCErr)
        else:
            print( "    (consistency error(s)): skipped")
  
        print( "    validation warning(s) : %d" % (numReadWarn + numCCWarn))
        if not skipCC :
            print( "  (consistency warning(s)): %d" % numCCWarn)
        else:
            print( "  (consistency warning(s)): skipped")
  
        if errMsgRead or errMsgCC: 
            print()
            print( "===== validation error/warning messages =====\n")
            if errMsgRead : 
                print( errMsgRead)
            if errMsgCC : 
                print( "*** consistency check ***\n")
                print( errMsgCC)


def main(args):
    rx = ET.parse(args.reactions_file)
    ic = ET.parse(args.initial_conditions_file)
    root = rx.getroot()
    ic_root = ic.getroot()
    conversion_dic = {"nanomolarity":1e-9, "millimolarity":1e-3, "molarity":1}
    
    ## Concentration in molar
    concDict = {}
    conc_unit = ""
    for child in ic_root:
        if child.tag == "ConcentrationSet":
            for child2 in child:
                conc_unit = child2.tag
                # Concentration conversion to molarity
                if conc_unit.lower() in conversion_dic:
                    concDict[child2.attrib['specieID']] = float(child2.attrib['value']) \
                    * conversion_dic[conc_unit.lower()]
                else:
                    raise ValueError(f"Unsupported concentration unit: {conc_unit}")

        #elif child.tag == "SurfaceDensitySet":
                #concDict[child2.attrib['specieID']] = float(child2.attrib['value'])*args.SD_to_conc_factor # Nanomolarity to Molarity
    print(f"Converted from {conc_unit.lower()} to molarity.")

    # Create SBML model from the NeuroRD model
    comp = 'compartment'
    size_micron_cubed = 0.5  #args.vol # micron-cube
    size = size_micron_cubed * micron_cube_to_litre # micron-cube to litres
    #comp = 'c1' # Using the default SBML compartment
    isConc = True # Input will be in concentration units (mol/L)
    model = simplesbml.SbmlModel(sub_units='mole', time_units='ms')
    compartment = model.addCompartment(0)
    compartment.setVolume(size)
    compartment.setId(comp)
    compartment.setName(comp)
    
    # Dictionary containing input unit definitions for SBML representation
    # Higher order reaction orders can be added.
    # Keys: (reaction order - 1) or unit identifier (e.g., 'concentration')
    # Values: corresponding unit definition string
    input_dic = {
        -1: 'M ms^{-1}',
        0: 'ms^{-1}', 
        1: 'M^{-1} ms^{-1}',
        2: 'M^{-2} ms^{-1}',
        3: 'M^{-3} ms^{-1}',
        4: 'M^{-4} ms^{-1}',
        'concentration': 'M',
        'substance': 'mol',
        'time': 'ms'
        }
    units = create_unit_definitions(model, input_dic)

    # create species
    species = {}
    reacNum = 0
    for i, child in enumerate(root):
        if child.tag == "Specie":
            if not child.attrib['id'][0].isdigit():        
                specie = child.attrib['id']
                if isConc:
                    spec_name = "[{}]".format(specie)
                else:
                    spec_name = specie
                print(i, spec_name, specie)
                model.addSpecies(spec_name, concDict[child.attrib['name']], comp=comp)
                
            else:
                specie = '_' + child.attrib['id']
                if isConc:
                    spec_name = "[{}]".format(specie)
                else:
                    spec_name = specie
                model.addSpecies( spec_name, concDict[child.attrib['name']], comp=comp)
                
            species.update({child.attrib['id']:specie})
            
        elif child.tag == "Reaction":
            reacNum += 1
            reactants, products = [], []
            stoich_reactants, stoich_products = [], []  # stoichiometric reactants and products
            local_params = {}
            rorder, porder = {}, {}

            for reactant in child:
                if reactant.tag == "Reactant":
                    reac_id = species[reactant.attrib['specieID']]
                    reactants.append(reac_id)
                    stoich_reactants.append(reac_id)

                    if 'n' in reactant.attrib:
                        stoich_reactants[-1] = reactant.attrib['n'] + " " + reac_id
                    if 'power' in reactant.attrib:
                        rorder[reac_id] = reactant.attrib['power']
                    else:
                        rorder[reac_id] = '1'

                elif reactant.tag == "Product":
                    reac_id = species[reactant.attrib['specieID']]
                    products.append(reac_id)
                    stoich_products.append(reac_id)

                    if 'n' in reactant.attrib:
                        stoich_products[-1] = reactant.attrib['n'] + " " + reac_id
                    if 'power' in reactant.attrib:
                        porder[reac_id] = reactant.attrib['power']
                    else:
                        porder[reac_id] = '1'

                elif reactant.tag == 'forwardRate':
                    # local_params.update({'kon':float(reactant.text)})
                    reac_ord = [r + '^' + rorder[r] if rorder[r]!='1' else r for r in reactants]
                    total_rord = sum(int(rorder[r]) for r in rorder.keys())

                    if total_rord<0:
                        print('reaction order<0 for', reacNum,'reactants:',reactants,rorder,total_rord)
                    elif total_rord == 0:
                        kin_law = '{}*(kon_{} )'.format(comp, reacNum) #'comp*(kon)'
                        model.addParameter('kon_{}'.format(reacNum), \
                                    float(reactant.text) * conversion_dic[conc_unit.lower()], \
                                    units=units[total_rord - 1].id)
                    else:
                        kin_law = '{}*(kon_{}*{} )'.format(comp, reacNum, '*'.join(reac_ord)) #'comp*(kon*E*S)'
                        model.addParameter('kon_{}'.format(reacNum), \
                                    float(reactant.text) * (conversion_dic[conc_unit.lower()]) ** (1 - total_rord), \
                                    units=units[total_rord - 1].id)
                    
                elif reactant.tag == 'reverseRate':
                    # local_params.update({'koff':float(reactant.text)})
                    prod_ord = [r + '^' + porder[r] if porder[r]!='1' else r for r in products]
                    total_pord = sum(int(porder[r]) for r in porder.keys())

                    if total_pord<0:
                        print('reaction order<0 for', reacNum,'products:',products,porder,total_pord)
                    elif total_pord == 0:
                        kin_law = '{}*(kon_{}*{} - koff_{})'.format(comp, reacNum,'*'.join(reac_ord), reacNum) #'comp*(kon*E*S-koff)' 
                        model.addParameter('koff_{}'.format(reacNum), \
                                    float(reactant.text) * conversion_dic[conc_unit.lower()], \
                                    units=units[total_pord - 1].id)
                    else:
                        kin_law = '{}*(kon_{}*{} - koff_{}*{})'.format(comp, reacNum,'*'.join(reac_ord), reacNum, '*'.join(prod_ord)) #'comp*(kon*E*S-koff*ES)' 
                        model.addParameter('koff_{}'.format(reacNum), \
                                    float(reactant.text) * (conversion_dic[conc_unit.lower()]) ** (1 - total_pord), \
                                    units=units[total_pord - 1].id)

            model.addReaction(stoich_reactants, stoich_products, kin_law, local_params=local_params, rxn_id='r{}'.format(reacNum))

    # serialization
    if args.display_only:
        print(model.toSBML())
    elif args.output_file:
        with open(args.output_file, 'w') as f:
            f.write(model.toSBML())
    else:
        raise argparse.ArgumentError(None, "Specify an 'Output' flag to display or store the output.")

    if args.validate:
        if args.unit_validation: 
            enableUnitCCheck = True
        else: 
            enableUnitCCheck = False
     
        validator = validateSBML(enableUnitCCheck)
        fnum = 0
        validator.validate(args.output_file)
        numinvalid = validator.numinvalid
     
        print( "---------------------------------------------------------------------------")
        print( "Validated %d files, %d valid files, %d invalid files" % (fnum, fnum - numinvalid, numinvalid))
        if not enableUnitCCheck:
            print( "(Unit consistency checks skipped)")
     
        if numinvalid > 0:
            sys.exit(1)


def create_unit_definitions(model, input_dic):
    unit_dic = {}

    for order, unit_str in input_dic.items():
        # Create a UnitDefinition object
        unit_def = model.model.createUnitDefinition()
        unit_id = ""

        units = unit_str.split()
        for unit in units:
            u = unit_def.createUnit()

            if 'mol' in unit:
                u.setKind(libsbml.UNIT_KIND_MOLE)
                u.setScale(0)
                u.setExponent(1)
                u.setMultiplier(1)
                unit_id = 'mol'

            if 'M' in unit:
                # Define the units of quantity in mol
                u.setKind(libsbml.UNIT_KIND_MOLE)
                u.setScale(0)

                # Check if there's an exponent (e.g. M^{-1})
                if '{' in unit:
                    exponent = int(unit.split('{')[-1].split('}')[0])
                    u.setExponent(exponent)
                    vol_exponent = abs(exponent)
                    if (abs(exponent) > 1):
                        unit_id = 'per_' + 'M' + str(abs(exponent))
                    else:
                        unit_id = 'per_' + 'M'
                else:
                    u.setExponent(1)
                    vol_exponent = -1
                    unit_id = 'M'
                u.setMultiplier(1)

                # Multilpy by liter^{vol_exponent} to get the concentration units
                u = unit_def.createUnit()
                u.setKind(libsbml.UNIT_KIND_LITRE)
                u.setScale(0)
                u.setExponent(vol_exponent)
                u.setMultiplier(1)
            
            if 'ms' in unit:
                u.setKind(libsbml.UNIT_KIND_SECOND)
                u.setScale(-3)

                if '{' in unit:
                    exponent = int(unit.split('{')[-1].split('}')[0])
                    u.setExponent(exponent)
                    if (len(unit_id) == 0):
                        unit_id = 'per_' + 'ms'
                    else:
                        unit_id += '_per_' + 'ms'
                else:
                    u.setExponent(1)
                    unit_id = 'ms'
                u.setMultiplier(1)

        unit_def.setId(unit_id)
        unit_dic[order] = unit_def

    return unit_dic


def get_parser():

    ### Command line input parsing here. ###########
    parser = argparse.ArgumentParser(prog='python ' + sys.argv[0], description='Combines NeuroRD files and units into an SBML file')

    neurord_group = parser.add_argument_group('neuroRD files')
    neurord_group.add_argument('-r', '--reactions-file', help='NeuroRD file with reactions.', default='')
    neurord_group.add_argument('-ic','--initial-conditions-file', help='NeuroRD file with initial conditions for the model', default='')
    #neurord_group.add_argument('-vol', help='volume for SBML model', default=1)
    #neurord_group.add_argument('-SD_to_conc', help='conversion factor for surface density spec', default=1)

    sbml_group = parser.add_argument_group('Output')
    sbml_group.add_argument('-d', '--display-only', help='Use to display SBML only', default= False, action='store_true')
    sbml_group.add_argument('-v', '--validate',  help='Validate SBML file', default= False, action='store_true')
    sbml_group.add_argument('-u', '--unit-validation',  help='Validate units', default= False, action='store_true')
    sbml_group.add_argument('-o', '--output-file', type=str, help='SBML file to output model')

    return parser

if __name__ == "__main__":
    
    parser = get_parser() 
    args = parser.parse_args(["-r", "Reac_1xdiff_simplified.xml", "-ic", "IC_dendonly_simplified_noSD.xml", "-o", "neurord_export_to_sbml.xml"])

    # if args.signature_file:
    #    with open (args.signature_file, 'r') as f:
    #        args.signatures.extend(f.read().splitlines())

    # if not args.cspace_files and not args.signatures: 
    #    parser.error ('Either --files or --signatures is required.') 
    main(args)
