package po53.kuznetsov.wdad.learn.rmi;

import java.io.Serializable;
import java.util.List;
import java.util.OptionalInt;

public class Flat implements Serializable{
    private int number;
    private int personsQuantity;
    private double area;
    private List<Registration> registrations;

    public Flat(int number, int personsQuantity, double area, List<Registration> registrations) {
        this.number = number;
        this.personsQuantity = personsQuantity;
        this.area = area;
        this.registrations = registrations;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getPersonsQuantity() {
        return personsQuantity;
    }

    public void setPersonsQuantity(int personsQuantity) {
        this.personsQuantity = personsQuantity;
    }

    public double getArea() {
        return area;
    }

    public void setArea(double area) {
        this.area = area;
    }

    public List<Registration> getRegistrations() {
        return registrations;
    }

    public void setRegistrations(List<Registration> registrations) {
        this.registrations = registrations;
    }
}
