package po53.kuznetsov.wdad.learn.rmi.server;

import po53.kuznetsov.wdad.learn.rmi.Flat;
import po53.kuznetsov.wdad.data.managers.DataManager;
import po53.kuznetsov.wdad.learn.rmi.Building;
import po53.kuznetsov.wdad.learn.rmi.Registration;
import po53.kuznetsov.wdad.learn.xml.XmlTask;

import javax.xml.crypto.dsig.TransformException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Date;

class XmlDataManagerImpl extends UnicastRemoteObject implements DataManager {
    private static final String FILENAME = "out\\production\\starting-monkey-to-human-path" +
            "\\po53\\kuznetsov\\wdad\\learn\\rmi\\server\\data.xml";
    XmlTask xmlTask;

    public XmlDataManagerImpl() throws Exception {
        xmlTask = new XmlTask(FILENAME);
    }

    @Override
    public double getBill(Building building, int flatNumber) throws RemoteException, TransformException {
        return xmlTask.getBill(building.getStreet(), Integer.parseInt(building.getNumber()), flatNumber);
    }

    @Override
    public Flat getFlat(Building building, int flatNumber) throws RemoteException, TransformException {
        return xmlTask.getFlat(building.getStreet(), Integer.parseInt(building.getNumber()), flatNumber);
    }

    @Override
    public void setTariff(String tariffName, double newValue) throws RemoteException, TransformException {
        xmlTask.setTariff(tariffName, newValue);
    }

    @Override
    public void addRegistration(Building building, int flatNumber, Registration registration) throws RemoteException, TransformException {
        Date date = registration.getDate();
        xmlTask.addRegistration(building.getStreet(), Integer.parseInt(building.getNumber()), flatNumber,
                date.getYear(), date.getMonth() + 1, registration.getColdwater(), registration.getHotwater(),
                registration.getElectricity(), registration.getGas());
    }
}
