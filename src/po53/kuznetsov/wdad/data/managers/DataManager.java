package po53.kuznetsov.wdad.data.managers;

import po53.kuznetsov.wdad.learn.rmi.Building;
import po53.kuznetsov.wdad.learn.rmi.Flat;
import po53.kuznetsov.wdad.learn.rmi.Registration;

import javax.xml.crypto.dsig.TransformException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface DataManager extends Remote {
    // возвращающает cумму платежа за текущий месяц.
    double getBill(Building building, int flatNumber) throws RemoteException, TransformException;

    //возвращающет квартиру с указанным адресом
    Flat getFlat(Building building, int flatNumber) throws RemoteException, TransformException;

    //изменяющяет стоимость заданной единицы показания счетчика (ХВС, ГВС, электроэнергия, газ)
    void setTariff(String tariffName, double newValue) throws RemoteException, TransformException;

    //добавляет (или заменяющий) показания счетчиков к заданной квартире в заданный период
    void addRegistration(Building building, int flatNumber, Registration registration) throws RemoteException, TransformException;
}
