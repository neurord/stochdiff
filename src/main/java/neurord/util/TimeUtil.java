package neurord.util;

import static java.lang.String.format;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public abstract class TimeUtil {
    public static final Logger log = LogManager.getLogger();

    public static String formatTimespan(long millis) {
        assert(millis >= 0);

        double sec = millis / 1000.;
        int mins = (int) (sec / 60);
        sec -= mins * 60;
        int hours = mins / 60;
        mins -= hours * 60;
        int days = hours / 24;
        hours -= days * 24;
        int weeks = days / 7;
        days -= weeks * 7;
        assert sec >= 0 && sec < 60: sec;
        assert mins >= 0 && mins < 60: mins;
        assert hours >= 0 && hours < 24: hours;
        assert days >= 0 && days < 7: days;
        assert weeks >= 0: weeks;

        log.debug("sec={} mins={} hours={} days={} weeks={}",
                  sec, mins, hours, days, weeks);
        return
            (weeks > 0 ? "" + weeks + "w " : "") +
            (days > 0 ? "" + days + "d " : "") +
            (weeks + days + hours > 0 ? "" + hours + ":" : "") +
            (weeks + days + hours + mins > 0 ? format("%02d:", mins) : "") +
            (weeks + days + hours > 0 ? format("%02d", (int) sec) : format("%.3f", sec)) +
            (weeks + days + hours + mins > 0 ? "" : " s");
    }
}
