package org.textensor.xml;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Schema;
import javax.xml.XMLConstants;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import org.textensor.stochdiff.model.*;

public class JAXBLoader {
    public static SDRun load(String filename)
        throws Exception
    {
        JAXBContext jc = JAXBContext.newInstance(SDRun.class);

        Unmarshaller u = jc.createUnmarshaller();
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setXIncludeAware(true);
        spf.setNamespaceAware(true);

        StreamSource schemaSource = new StreamSource(JAXBLoader.class.getResourceAsStream("/sdrun.xsd"));

        Schema schema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(schemaSource);
        spf.setSchema(schema);

        XMLReader xr = spf.newSAXParser().getXMLReader();
        InputSource input = new InputSource(filename);
        SAXSource source = new SAXSource(xr, input);

        SDRun sdrun = (SDRun) u.unmarshal(source);
        return sdrun;
    }

    public static void main(String... args) throws Exception {
        SDRun sdrun = load(args[0]);

        JAXBContext jc = JAXBContext.newInstance(SDRun.class);

        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(sdrun, System.out);
    }
}
