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
import javax.xml.bind.UnmarshallerHandler;
import javax.xml.namespace.QName;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Schema;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBException;
import org.xml.sax.InputSource;
import org.xml.sax.Attributes;
import org.xml.sax.XMLReader;
import org.xml.sax.XMLFilter;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.textensor.stochdiff.model.SDRun;

public class ModelReader<T> {
    static final Logger log = LogManager.getLogger(ModelReader.class);

    public static final String STOCHDIFF_NS = "http://stochdiff.textensor.org";

    public static class NamespaceFiller extends XMLFilterImpl {
        boolean sdrun_seen = false;
        boolean warning = false;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts)
            throws SAXException
        {
            if (!this.sdrun_seen && localName.equals("SDRun"))
                this.sdrun_seen = true;
            if (this.sdrun_seen && uri.equals("")) {
                if (!this.warning) {
                    this.warning = true;
                    log.warn("Namespace not specified (seen at element {}), assuming {}",
                             qName, STOCHDIFF_NS);
                }
                uri = STOCHDIFF_NS;
            }
            super.startElement(uri, localName, qName, atts);
        }

        @Override
        public void endElement(String uri, String localName, String qName)
            throws SAXException
        {
            if (this.sdrun_seen && uri.equals(""))
                uri = STOCHDIFF_NS;
            super.endElement(uri, localName, qName);
        }
    }

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
        log.info("umarshalling {}", filename);

        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setXIncludeAware(true);
        spf.setNamespaceAware(true);
        spf.setFeature("http://apache.org/xml/features/xinclude/fixup-base-uris", false);

        StreamSource schemaSource = new StreamSource(this.getClass().getResourceAsStream("/sdrun.xsd"));

        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = factory.newSchema(schemaSource);
        spf.setSchema(schema);

        XMLFilter filter = new NamespaceFiller();
        XMLReader xr = spf.newSAXParser().getXMLReader();
        filter.setParent(xr);

        InputSource xml = new InputSource(filename);

        Unmarshaller u = jc.createUnmarshaller();
        UnmarshallerHandler uh = u.getUnmarshallerHandler();
        u.setSchema(schema);
        
        filter.setContentHandler(uh);
        filter.parse(xml);

        T result = (T) uh.getResult();
        if (result == null)
            throw new RuntimeException("Unmarshalling failed");

        return result;
    }

    public T unmarshall(File filename)
        throws Exception
    {
        return this.unmarshall(filename.toString());
    }

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
