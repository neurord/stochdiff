package org.textensor.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.textensor.util.inst;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class StringListAdapter extends XmlAdapter<String, List<String>> {
    @Override
    public List<String> unmarshal(final String string) {
        StringTokenizer st = new StringTokenizer(string, " ,");
        List<String> values = inst.newArrayList();

        int ntok = st.countTokens();
        for (int i = 0; i < ntok; i++)
            values.add( st.nextToken() );

        return values;
    }

    @Override
    public String marshal(final List<String> values) {
        final StringBuilder sb = new StringBuilder();

        /* We prefer space separated lists... They are easier to parse visually. */
        for (final String s: values) {
            if (sb.length() > 0)
                sb.append(" ");

            sb.append(s);
        }

        return sb.toString();
    }
}
