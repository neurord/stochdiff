package org.textensor.xml;

import java.io.File;
import java.io.Writer;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.StringWriter;

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
import javax.xml.bind.JAXBException;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.textensor.stochdiff.model.SDRun;

public class ModelReader<T> {

    JAXBContext jc;

    public ModelReader(Class<T> klass) {
        try {
            this.jc = JAXBContext.newInstance(klass);
        } catch(JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    public T unmarshall(String filename)
        throws Exception
    {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setXIncludeAware(true);
        spf.setNamespaceAware(true);
        spf.setFeature("http://apache.org/xml/features/xinclude/fixup-base-uris", false);

        StreamSource schemaSource = new StreamSource(this.getClass().getResourceAsStream("/sdrun.xsd"));

        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = factory.newSchema(schemaSource);
        spf.setSchema(schema);

        XMLReader xr = spf.newSAXParser().getXMLReader();
        InputSource input = new InputSource(filename);
        SAXSource source = new SAXSource(xr, input);

        Unmarshaller u = jc.createUnmarshaller();
        u.setSchema(schema);
        
        return (T) u.unmarshal(source);
    }

    public T unmarshall(File filename)
        throws Exception
    {
        return this.unmarshall(filename.toString());
    }

    public static final String STOCHDIFF_NS = "http://stochdiff.textensor.org";

    public Marshaller getMarshaller(T object)
        throws Exception
    {
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        return marshaller;
    }

    public void marshall(T object, Writer out)
        throws Exception
    {
        this.getMarshaller(object).marshal(object, out);
    }

    public void marshall(T object, OutputStream out)
        throws Exception
    {
        this.getMarshaller(object).marshal(object, out);
    }

    public void marshall(T object, String filename)
        throws Exception
    {
        OutputStream out = new FileOutputStream(filename);
        this.getMarshaller(object).marshal(object, out);
    }

    public String marshall(T object)
        throws Exception
    {
        StringWriter out = new StringWriter();
        this.marshall(object, out);
        return out.toString();
    }

    public static void main(String... args) throws Exception {
        ModelReader<SDRun> loader = new ModelReader(SDRun.class);

        SDRun sdrun = loader.unmarshall(args[0]);

        loader.marshall(sdrun, System.out);
    }
}
