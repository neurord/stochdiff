package org.textensor.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


import javax.xml.bind.annotation.adapters.XmlAdapter;

public class DoubleListAdapter extends XmlAdapter<String, List<Double>> {
    @Override
    public List<Double> unmarshal(final String string) {
        StringTokenizer st = new StringTokenizer(string, " ,");
        List<Double> values = new ArrayList<>();

        int ntok = st.countTokens();
        for (int i = 0; i < ntok; i++)
            values.add( Double.parseDouble(st.nextToken()) );

        return values;
    }

    @Override
    public String marshal(final List<Double> values) {
        final StringBuilder sb = new StringBuilder();

        /* We prefer space separated lists... They are easier to parse visually. */
        for (final double d: values) {
            if (sb.length() > 0)
                sb.append(" ");

            sb.append(d);
        }

        return sb.toString();
    }
}
