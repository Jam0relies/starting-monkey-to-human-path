package po53.kuznetsov.wdad.learn.rmi;

import java.io.Serializable;

public class Building implements Serializable{
    private String street;
    private String number;

    public Building(String street, String number) {
        this.street = street;
        this.number = number;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
