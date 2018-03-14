package po53.kuznetsov.wdad.learn.rmi;

public enum TarifsKeys {
    COLDWATER("coldwater"), HOTWATER("hotwater"), ELECTRICITY("electricity"), GAS("gas");
    public final String KEY;

    TarifsKeys(String KEY) {
        this.KEY = KEY;
    }
}
