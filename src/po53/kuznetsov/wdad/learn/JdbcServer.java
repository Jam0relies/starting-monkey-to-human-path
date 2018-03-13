package po53.kuznetsov.wdad.learn;

import po53.kuznetsov.wdad.data.managers.DataManager;
import po53.kuznetsov.wdad.data.managers.JDBCDataManager;
import po53.kuznetsov.wdad.data.managers.PreferencesManager;
import po53.kuznetsov.wdad.utils.PreferencesConstantManager;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class JdbcServer {
    /*путь к файлу с политиками безопасности. В примере указан относительный путь - относительно текущей директории. Чтобы файл был найден без проблем, он должен находиться рядом с файлом Server.class (именно *.class, а не *.java). Но можете использовать и абсолютный путь.
     */
    static final private String SECURITY_POLICY_PATH = "out\\production\\starting-monkey-to-human-path\\po53\\kuznetsov\\wdad\\learn\\rmi\\security.policy";
    /*путь к *.class файлам, участвующим в RMI-взаимодействии. Отсюда, при необходимости, они будут подгружаться. Есть пара вариантов -
1) Загружаемся с локальной директории, тогда используется протокол file://, указывающий либо на файл с расширением *.jar, либо на директорию содержащую библиотеку *.class файлов. Если указываем директорию, то последний символ в пути должен быть '/' (либо "\\", если Windows)
2) Загружаемся по http - тогда URL должна указывать на *.jar файл. Например, http://mastefanov.com/test/codebase/base.jar
*/

    private static final String REMOTE_NAME = "JDBCDataManager";

    public static void main(String[] args) {
        PreferencesManager prefManager = PreferencesManager.getInstance();
        //String securityPolicyPath = prefManager.getProperty(SECURITY_POLICY_PATH);
        int registryPort = Integer.parseInt(prefManager.getProperty(PreferencesConstantManager.REGISTRY_PORT));
        System.setProperty("java.rmi.server.logCalls", "true");
        System.setProperty("java.security.policy", SECURITY_POLICY_PATH);
        System.setSecurityManager(new SecurityManager());
        Registry registry = null;
        try {
            if (prefManager.getProperty(PreferencesConstantManager.CREATE_REGISTRY).equals("yes"))
                registry = LocateRegistry.createRegistry(registryPort);
            else
                registry = LocateRegistry.getRegistry(registryPort);
        } catch (RemoteException ex) {
            System.err.println("cant locate object");
            ex.printStackTrace();
        }
        try {
            System.out.println("export");
            DataManager xdmi = new JDBCDataManager();
            registry.rebind(REMOTE_NAME, xdmi);
            prefManager.addBindedObject(REMOTE_NAME, DataManager.class.getCanonicalName());
            System.out.println("idle");
            /*
            while(true){
                Thread.sleep(1000);
            }*/
        } catch (Exception ex) {
            System.err.println("cant export");
            ex.printStackTrace();
        }
    }
}
