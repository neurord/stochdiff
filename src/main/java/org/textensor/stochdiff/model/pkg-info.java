/**
 *
 * These classes represent the model as it is stored in the input files. They are used in parsing
 * those files and reconstructing the references, eg from a reaction object to the reacting
 * species.
 *
 * They are not used themselves during the calculation, but rather export a more compact
 * version of the data in table form that has been preprocessed for ease of computation.
 *
 * Since they are only used at startul, efficeincey is not an issue here - they are a good
 * place to do validation, error checking, reporting etc. Theyinclude public fields
 * that can be accessed by reflection which is good for the initialization process but
 * would not be ideal for the computation itself.
 *
 *
 */
