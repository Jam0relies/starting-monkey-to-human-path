package po53.kuznetsov.wdad.learn.rmi.client;

import po53.kuznetsov.wdad.data.managers.DataManager;
import po53.kuznetsov.wdad.data.managers.PreferencesManager;
import po53.kuznetsov.wdad.learn.rmi.Building;
import po53.kuznetsov.wdad.learn.rmi.Flat;
import po53.kuznetsov.wdad.learn.rmi.Registration;
import po53.kuznetsov.wdad.utils.PreferencesConstantManager;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.Instant;
import java.util.Date;

public class Client {
    static final private String SECURITY_POLICY_PATH = "out\\production\\starting-monkey-to-human-path\\po53\\kuznetsov\\wdad\\learn\\rmi\\security.policy";

    public static void main(String[] args) throws Exception {

        PreferencesManager prefManager = PreferencesManager.getInstance();
        System.setProperty("java.security.policy", SECURITY_POLICY_PATH);
        System.setSecurityManager(new SecurityManager());
        Registry registry = null;
        try {
            registry = LocateRegistry.getRegistry(Integer.parseInt(prefManager.getProperty(PreferencesConstantManager.REGISTRY_PORT))); // получает ссылку на реестр
        } catch (RemoteException re) {
            System.err.println("cant locate registry");
            re.printStackTrace();
        }
        System.out.println("Binded:");
        for (String s : registry.list()) {
            System.out.println(s);
        }

        String rmiName = prefManager.getBindedObjectName(DataManager.class.getCanonicalName());
        System.out.println("RMI name:" + rmiName);
        DataManager xmlDataManager = (DataManager) registry.lookup(rmiName);

        Building building = new Building("Some street", "1");
        xmlDataManager.addRegistration(building, 1, new Registration(Date.from(Instant.now()),
                6, 7, 8, 9));
        xmlDataManager.setTariff("gas", 5);
        System.out.println(xmlDataManager.getBill(building, 1));
        Flat flat = xmlDataManager.getFlat(building, 2);
        System.out.println(flat.getPersonsQuantity());
    }
}
