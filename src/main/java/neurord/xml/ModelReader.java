package neurord.xml;

import java.io.File;
import java.io.Writer;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.StringWriter;
import java.io.StringReader;
import java.util.ArrayDeque;
import java.util.Properties;
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
import org.xml.sax.helpers.AttributesImpl;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import neurord.model.SDRun;

public class ModelReader<T> {
    static final Logger log = LogManager.getLogger(ModelReader.class);

    public static final String NEURORD_NS = "http://stochdiff.textensor.org";

    public static class NamespaceFiller extends XMLFilterImpl {
        boolean sdrun_seen = false;
        boolean ns_warning = false;
        boolean failed = false;

        SAXParseException exception = null;

        final ArrayDeque<String> names = new ArrayDeque<>();
        final HashMap<String, String>[] overrides;

        public NamespaceFiller(HashMap<String, String> ...overrides) {
            this.overrides = overrides;

            boolean first = true;
            for (HashMap<String,String> map: overrides)
                if (map != null) {
                    if (first) {
                        log.debug("Overrides (higher priority first):");
                        first = false;
                    }
                    log.debug("{}", map);
                }
            if (first)
                log.debug("No overrides");
        }

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
                             this.location(false), NEURORD_NS);
                }
                uri = NEURORD_NS;
            }
            if (this.exception != null && this.exception.getMessage().contains("cvc-complex-type.2.4.a"))
                /* clear the exception if it seems to be the appropriate type */
                this.exception = null;

            /* rename MaxElementSide to maxElementSide */
            if (this.sdrun_seen && uri.equals(NEURORD_NS) && localName.equals("MaxElementSide"))
                localName = "maxElementSide";

            /* rename dt on OutputSet to outputInterval */
            if (this.sdrun_seen && uri.equals(NEURORD_NS) && localName.equals("OutputSet") &&
                atts.getIndex("dt") >= 0) {

                log.info("Renaming attribute dt to outputInterval");
                AttributesImpl filtered = new AttributesImpl(atts);
                filtered.setLocalName(atts.getIndex("dt"), "outputInterval");
                atts = filtered;
            }

            super.startElement(uri, localName, qName, atts);
        }

        @Override
        public void endElement(String uri, String localName, String qName)
            throws SAXException
        {
            this.names.pop();
            if (this.sdrun_seen && uri.equals(""))
                uri = NEURORD_NS;
            super.endElement(uri, localName, qName);
        }

        @Override
        public void characters(char[] ch, int start, int length)
            throws SAXException
        {
            String loc = this.location(true);

            for (HashMap<String,String> map: this.overrides)
                if (map != null) {
                    String override = map.get(loc);
                    if (override != null) {
                        String s = new String(ch, start, length).trim();
                        log.info("Overriding {}: {} → {}", loc, s, override);

                        ch = override.toCharArray();
                        start = 0;
                        length = ch.length;
                        break;
                    }
                }

            super.characters(ch, start, length);
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
                      this.location(false),
                      e.getMessage());
        }

        String location(boolean dots) {
            StringBuilder sb = new StringBuilder();
            String[] names = this.names.toArray(new String[]{});
            for (int i = this.names.size() - 1; i >= 0; i--)
                sb.append((dots ? (sb.length() > 0 ? "." : "") : "/") + names[i]);
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

    protected HashMap<String, String> propertyOverrides() {
        HashMap<String, String> overrides = new HashMap<>();
        Properties props = System.getProperties();
        for (String key : props .stringPropertyNames())
            if (key.startsWith("neurord.sdrun") || key.startsWith("neurord.SDRun"))
                overrides.put("SDRun" + key.substring(13), props.getProperty(key));
        return overrides;
    }

    public T unmarshall(InputSource xml, HashMap<String,String> extra_overrides)
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

        NamespaceFiller filter = new NamespaceFiller(this.propertyOverrides(), extra_overrides);
        XMLReader xr = spf.newSAXParser().getXMLReader();
        filter.setParent(xr);

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

    public T unmarshall(File filename, HashMap<String,String> extra_overrides)
        throws Exception
    {
        log.info("Umarshalling file {}", filename);

        InputSource source = new InputSource(filename.toString());
        return unmarshall(source, extra_overrides);
    }

    public T unmarshall(String xml, HashMap<String,String> extra_overrides)
        throws Exception
    {
        log.info("Umarshalling string");

        InputSource source = new InputSource(new StringReader(xml));
        return unmarshall(source, extra_overrides);
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

        SDRun sdrun = loader.unmarshall(args[0], null);

        loader.marshall(sdrun, System.out);
    }
}
