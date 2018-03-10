package po53.kuznetsov.wdad.learn.rmi;

import java.io.Serializable;
import java.util.HashMap;

public class Tariffs implements Serializable {
    static final HashMap<String, Double> values;
    static final String COLDWATER_KEY = "coldwater";
    static final String HOTWATER_KEY = "hotwater";
    static final String ELECTRICITY_KEY = "electricity";
    static final String GAS_KEY = "gas";

    static {
        values = new HashMap<>();
        values.put("coldwater", 0.0);
        values.put("hotwater", 0.0);
        values.put("electricity", 0.0);
        values.put("gas", 0.0);
    }
}
