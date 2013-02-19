package org.textensor.util;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import static org.testng.Assert.assertEquals;
import org.testng.annotations.*;

public class TestResizableArray {
    static final Logger log = LogManager.getLogger(TestResizableArray.class);

    @DataProvider(name="arrays")
    public Object[][] createData() {
        return new Object[][] {
            new Object[] { new ResizableArray.Float(10) },
            new Object[] { new ResizableArray.Float(5) },
            new Object[] { new ResizableArray.Float(3) },
            new Object[] { new ResizableArray.Float(15) },
        };
    }

    @Test(dataProvider="arrays")
    void testFilling(ResizableArray.Float r) {
        assertEquals(r.size(), 0);
        assertEquals(r.used(), 0);
        assertEquals(r.remaining(), 0);

        for(int i = 0; i < 10; i++)
            r.put(11.1f);

        assertEquals(r.size(), 10);
        assertEquals(r.used(), 0);
        assertEquals(r.remaining(), 10);

        for(int i = 0; i < 10; i++)
            r.take(1);

        assertEquals(r.size(), 10);
        assertEquals(r.used(), 10);
        assertEquals(r.remaining(), 0);

        r.reset();

        assertEquals(r.size(), 10);
        assertEquals(r.used(), 0);
        assertEquals(r.remaining(), 10);

        for(int i = 0; i < 10; i++)
            assertEquals(r.get(i), 11.1f);
    }
}
