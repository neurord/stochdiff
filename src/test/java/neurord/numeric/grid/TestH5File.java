package neurord.numeric.grid;

import java.io.File;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Level;

import neurord.util.Logging;

import org.testng.annotations.*;

public class TestH5File {
    public static final Logger log = LogManager.getLogger();

    @Test
    public void testBasics()
        throws Exception
    {
        H5File f = new H5File("test1.h5");

        H5File.Group g = f.createGroup("/group1");
        g.setAttribute("title", "group1 title");
        g.setAttribute("short", 38047);
        g.setAttribute("long", 1089L);

        int[] _ints = {1,2,3,4,5,6};
        H5File.Dataset ints = g.writeVector("ints", _ints);
        ints.close();

        long[] _longs = {
            4294967295L,
            4294967296L,
            4294967297L,
            4294967298L,
            4294967299L,
            4294967300L,
        };
        H5File.Dataset longs = g.writeVector("longs", _longs);
        longs.close();

        double[] _doubles = {1.1,2.2,3.3,4.4,5.5,6.6};
        H5File.Dataset doubles = g.writeVector("doubles", _doubles);
        doubles.close();

        String[] _strings = {"string1", "string    2", "s3", "głąbczyński głąbik"};
        H5File.Dataset strings = g.writeVector("strings", _strings);
        strings.close();

        long att1 = g.getAttribute("short");
        assert att1 == 38047;
        long att2 = g.getAttribute("long");
        assert att2 == 1089;

        g.close();

        H5File.Group g2 = f.createGroup("/group2");
        g2.setAttribute("title", "group2 title");

        g2.writeArray("ints",
                      new int[][] {
                          {1,2,3,4,5,6},
                          {7,8,9,10,11,12},
                          {13,14,15}},
                      -1).close();

        g2.writeArray("doubles",
                      new double[][] {
                          {1.1,2.2,3.3,4.4,5.5,6.6},
                          {7.7,8.8,9.8,10,11,12},
                          {13,14,15,16,17,Double.NaN}}
                      ).close();

        HashMap<Object,Object> hm = new HashMap<>();
        hm.put("m_key1", "hello!");
        hm.put("m_key2", "world!");
        g2.writeMap(hm.entrySet());

        /* Test missing attribute read */
        try {
            g2.getAttribute("nosuchattribute");
        } catch (hdf.hdf5lib.exceptions.HDF5AttributeException e) {
            log.debug("Got exception for missing attribute: {}", e);
        }

        /* Test wrong-type attribute read */
        try {
            g2.getAttribute("m_key1");
        } catch (hdf.hdf5lib.exceptions.HDF5DatatypeInterfaceException e) {
            log.debug("Got exception for wrong-type attribute: {}", e);
        }

        H5File.Group g3 = g2.createSubGroup("subgroup");
        g3.close();

        g2.close();

        f.close();
    }

    @Test
    public void testExtensibleArray()
        throws Exception
    {
        H5File f = new H5File("test2.h5");

        H5File.Group g = f.createGroup("/group1");

        H5File.Dataset ext1 =
            g.createExtensibleArray("extensible1", int.class,
                                    "EXTENSIBLE1", "[INT]", "[(NONE)]",
                                    new long[]{3, 4});
        ext1.close();

        H5File.Dataset ext2 =
            g.createExtensibleArray("extensible2", int.class,
                                    "EXTENSIBLE2", "[INT]", "[(NONE)]",
                                    new long[]{3});
        ext2.extend(3, new int[] {0, 1, 2});
        ext2.extend(3, new int[] {3, 4, 5});
        ext2.extend(9, new int[] {6, 7, 8, 9, 10, 11, 12, 13, 14});
        ext2.close();

        g.close();
        f.close();
    }

    public static void main(String... args)
        throws Exception
    {
        Logging.configureConsoleLogging();

        TestH5File test = new TestH5File();
        test.testBasics();
        test.testExtensibleArray();
    }
}
