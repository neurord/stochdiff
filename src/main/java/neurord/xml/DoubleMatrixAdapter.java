package neurord.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.jblas.DoubleMatrix;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class DoubleMatrixAdapter extends XmlAdapter<String, DoubleMatrix> {
    @Override
    public DoubleMatrix unmarshal(final String string) {
        /* DoubleMatrix expects semicolon separated rows */
        String text = string.trim().replace('\n', ';');
        return DoubleMatrix.valueOf(text);
    }

    @Override
    public String marshal(final DoubleMatrix values) {
        return values.toString("%f", "\n", "\n", " ", "\n");
    }
}
