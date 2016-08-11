package neurord.util;

import static org.testng.Assert.assertEquals;
import static neurord.util.TestUtil.assertArrayEquals;
import org.testng.annotations.*;

import neurord.util.Logging;

public class TestTimeUtil {

    public static void main(String args[]) {
        Logging.configureConsoleLogging();

        System.out.println(TimeUtil.formatTimespan(0));
        System.out.println(TimeUtil.formatTimespan(100));
        System.out.println(TimeUtil.formatTimespan(999));
        System.out.println(TimeUtil.formatTimespan(1000));
        System.out.println(TimeUtil.formatTimespan(1001));
        System.out.println(TimeUtil.formatTimespan(10001));
        System.out.println(TimeUtil.formatTimespan(99991));
        System.out.println();
        System.out.println(TimeUtil.formatTimespan(100001));
        System.out.println(TimeUtil.formatTimespan(1000001));
        System.out.println(TimeUtil.formatTimespan(10010201));
        System.out.println(TimeUtil.formatTimespan(102099991));
        System.out.println(TimeUtil.formatTimespan(102300001));
        System.out.println(TimeUtil.formatTimespan(1092300001));
        System.out.println(TimeUtil.formatTimespan(10992300001L));
    }
}
