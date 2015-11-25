/**
 *
 * These classes represent the model as it is stored in the input files. They are used in parsing
 * those files and reconstructing the references, eg from a reaction object to the reacting
 * species.
 *
 * They are not used themselves during the calculation, but rather export a more compact
 * version of the data in table form that has been preprocessed for ease of computation.
 *
 * Since they are only used at startup, efficiency is not an issue here — they are a good
 * place to do validation, error checking, reporting, etc.
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlJavaTypeAdapter(value=StringTrimAdapter.class, type=String.class)
@XmlSchema(namespace="http://stochdiff.textensor.org",
           elementFormDefault=XmlNsForm.QUALIFIED,
           xmlns={
               @XmlNs(prefix="", namespaceURI="http://stochdiff.textensor.org")
           })
package neurord.model;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import neurord.xml.StringTrimAdapter;
