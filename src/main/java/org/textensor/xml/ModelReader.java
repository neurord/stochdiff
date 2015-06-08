package org.textensor.xml;

import java.io.File;
import java.io.Writer;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.StringWriter;
import java.util.ArrayDeque;
import java.net.URL;
import java.net.URLDecoder;
import java.net.MalformedURLException;

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
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.XMLFilterImpl;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.textensor.stochdiff.model.SDRun;
import org.textensor.util.inst;

public class ModelReader<T> {
    static final Logger log = LogManager.getLogger(ModelReader.class);

    public static final String STOCHDIFF_NS = "http://stochdiff.textensor.org";

    public static class NamespaceFiller extends XMLFilterImpl {
        boolean sdrun_seen = false;
        boolean ns_warning = false;
        boolean failed = false;

        SAXParseException exception = null;

        ArrayDeque<String> names = inst.newArrayDeque();

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts)
            throws SAXException
        {
            this.names.push(qName);

            /* support for namespace-less elements */
            if (!this.sdrun_seen && localName.equals("SDRun"))
                this.sdrun_seen = true;
            if (this.sdrun_seen && uri.equals("")) {
                if (!this.ns_warning) {
                    this.ns_warning = true;
                    log.info("{}: namespace not specified, assuming {}",
                             this.location(), STOCHDIFF_NS);
                }
                uri = STOCHDIFF_NS;
            }
            if (this.exception != null && this.exception.getMessage().contains("cvc-complex-type.2.4.a"))
                /* clear the exception if it seems to be the appropriate type */
                this.exception = null;

            /* rename MaxElementSide to maxElementSide */
            if (this.sdrun_seen && uri.equals(STOCHDIFF_NS) && localName.equals("MaxElementSide"))
                localName = "maxElementSide";

            super.startElement(uri, localName, qName, atts);
        }

        @Override
        public void endElement(String uri, String localName, String qName)
            throws SAXException
        {
            this.names.pop();
            if (this.sdrun_seen && uri.equals(""))
                uri = STOCHDIFF_NS;
            super.endElement(uri, localName, qName);
        }

        void log_error(SAXParseException e) {
            String id = e.getSystemId();
            String path;
            try {
                String dec = URLDecoder.decode(id, "UTF-8");
                URL url = new URL(dec);
                path = url.getPath();
            } catch(Exception error){
                path = id;
            }

            log.error("{}:line {}:column {}: {}: {}",
                      path, e.getLineNumber(), e.getColumnNumber(),
                      this.location(),
                      e.getMessage());
        }

        String location() {
            StringBuilder sb = new StringBuilder();
            String[] names = this.names.toArray(new String[]{});
            for (int i = this.names.size() - 1; i >= 0; i--)
                sb.append("/" + names[i]);
            return sb.toString();
        }

        @Override
        public void error(SAXParseException e)
            throws SAXException
        {
            if (this.exception != null) {
                log_error(this.exception);
                this.failed = true;
            }
            this.exception = e;

            super.error(e);
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

        NamespaceFiller filter = new NamespaceFiller();
        XMLReader xr = spf.newSAXParser().getXMLReader();
        filter.setParent(xr);

        InputSource xml = new InputSource(filename);

        Unmarshaller u = jc.createUnmarshaller();
        UnmarshallerHandler uh = u.getUnmarshallerHandler();
        u.setSchema(schema);

        filter.setContentHandler(uh);
        filter.parse(xml);

        T result = (T) uh.getResult();
        if (result == null || filter.failed)
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
