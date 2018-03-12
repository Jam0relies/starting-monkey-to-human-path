package po53.kuznetsov.wdad.data.storage;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import po53.kuznetsov.wdad.data.managers.PreferencesManager;

import java.util.Properties;

import static po53.kuznetsov.wdad.utils.PreferencesConstantManager.*;

public class DataSourceFactory {
    private static final String MYSQL = "com.mysql.jdbc.jdbc2.optional.MysqlDataSource";

    public static javax.sql.DataSource createDataSource() {
        PreferencesManager preferencesManager = PreferencesManager.getInstance();
        Properties properties = preferencesManager.getProperties();
        String className = properties.getProperty(CLASSNAME);
        String driverType = properties.getProperty(DRIVER_TYPE);
        String host = properties.getProperty(HOSTNAME);
        int port = Integer.parseInt(properties.getProperty(PORT));
        String dbName = properties.getProperty(DB_NAME);
        String user = properties.getProperty(USER);
        String password = properties.getProperty(PASS);
        return createDataSource(className, driverType, host, port, dbName, user, password);
    }

    public static javax.sql.DataSource createDataSource(String className, String
            driverType, String host, int port, String dbName, String user, String password) {
        /*
        javax.sql.DataSource mysqlDS = null;
        try {
            mysqlDS = (DataSource) Class.forName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource").newInstance();
            MysqlDataSource
            mysqlDS.setURL(props.getProperty("MYSQL_DB_URL"));
            mysqlDS.setPort();
            mysqlDS.setUser(props.getProperty("MYSQL_DB_USERNAME"));
            mysqlDS.setPassword(props.getProperty("MYSQL_DB_PASSWORD"));

        } catch (/*IOException |  ClassNotFoundException | IllegalAccessException |InstantiationException e) {
            e.printStackTrace();
        }
        */
        switch (className) {
            case MYSQL: {
                //Class.forName(className);
                MysqlDataSource dataSource = new MysqlDataSource();
                //dataSource.setDriverType("thin");
                dataSource.setServerName(host);
                dataSource.setPortNumber(port);
                dataSource.setDatabaseName(dbName);
                dataSource.setUser(user);
                dataSource.setPassword(password);
                return dataSource;
            }
            default: {
                throw new IllegalArgumentException("Unknown server");
            }
        }
    }
}
