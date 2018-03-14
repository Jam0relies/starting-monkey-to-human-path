package po53.kuznetsov.wdad.learn.rmi;

import java.io.Serializable;
import java.util.HashMap;

public class Tariffs implements Serializable {
    public static final HashMap<String, Double> values;

    static {
        values = new HashMap<>();
        values.put("coldwater", 0.0);
        values.put("hotwater", 0.0);
        values.put("electricity", 0.0);
        values.put("gas", 0.0);
    }
}
