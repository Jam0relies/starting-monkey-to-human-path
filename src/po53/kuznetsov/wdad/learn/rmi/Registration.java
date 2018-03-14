package po53.kuznetsov.wdad.learn.rmi;

import java.io.Serializable;
import java.util.Date;

public class Registration implements Serializable {
    private Date date;
    private double coldwater;
    private double hotwater;
    private double electricity;
    private double gas;

    public Registration() {
        this(null, 0, 0, 0, 0);
    }

    public Registration(Date date, double coldwater, double hotwater, double electricity, double gas) {
        this.date = date;
        this.coldwater = coldwater;
        this.hotwater = hotwater;
        this.electricity = electricity;
        this.gas = gas;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public double getColdwater() {
        return coldwater;
    }

    public void setColdwater(double coldwater) {
        this.coldwater = coldwater;
    }

    public double getHotwater() {
        return hotwater;
    }

    public void setHotwater(double hotwater) {
        this.hotwater = hotwater;
    }

    public double getElectricity() {
        return electricity;
    }

    public void setElectricity(double electricity) {
        this.electricity = electricity;
    }

    public double getGas() {
        return gas;
    }

    public void setGas(double gas) {
        this.gas = gas;
    }
}
