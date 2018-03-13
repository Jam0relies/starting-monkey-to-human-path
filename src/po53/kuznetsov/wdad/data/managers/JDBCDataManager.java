package po53.kuznetsov.wdad.data.managers;

import po53.kuznetsov.wdad.data.storage.DataSourceFactory;
import po53.kuznetsov.wdad.learn.rmi.Building;
import po53.kuznetsov.wdad.learn.rmi.Flat;
import po53.kuznetsov.wdad.learn.rmi.Registration;

import javax.sql.DataSource;
import javax.xml.crypto.dsig.TransformException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;
import java.time.LocalDate;

public class JDBCDataManager extends UnicastRemoteObject implements DataManager {
    private static final String INSERT_REGISTRATION_STATEMENT = "INSERT INTO `registrations`(`date`, `flats_id`)" +
            " VALUES (?, ?);";
    private static final String SELECT_REGISTRATION_ID = "SELECT `id` FROM `registrations` WHERE date=? AND flats_id=?";
    private static final String SELECT_FLAT_ID = "SELECT flats.id FROM flats, buildings, street WHERE street.name=?" +
            " AND buildings.street_id = street.id AND buildings.number=? AND flats.buildings_id=buildings.id AND" +
            " flats.number=?;";
    private static final String INSERT_REGISTRATIONS_TARIFFS = "INSERT INTO `registrations-tariffs`(`amount`," +
            " `registrations_id`, `tariffs_name`) VALUES (?, ?, ?);";/* +
            "INSERT INTO `registrations-tariffs`(`amount`, `registrations_id`, `tariffs_name`) VALUES (?, ?, 'hotwater');" +
            "INSERT INTO `registrations-tariffs`(`amount`, `registrations_id`, `tariffs_name`) VALUES (?, ?, 'electricity');" +
            "INSERT INTO `registrations-tariffs`(`amount`, `registrations_id`, `tariffs_name`) VALUES (?, ?, 'gas');";*/

    private DataSource dataSource;

    public JDBCDataManager() throws RemoteException {
        dataSource = DataSourceFactory.createDataSource();
    }


    @Override
    public double getBill(Building building, int flatNumber) throws RemoteException, TransformException {
        try {
            Connection conn = dataSource.getConnection();
        } catch (SQLException e) {
            RemoteException remoteException = new RemoteException("SQLException", e);
            throw remoteException;
        }
        return 0;
    }

    @Override
    public Flat getFlat(Building building, int flatNumber) throws RemoteException, TransformException {
        return null;
    }

    @Override
    public void setTariff(String tariffName, double newValue) throws RemoteException, TransformException {

    }

    @Override
    public void addRegistration(Building building, int flatNumber, Registration registration) throws RemoteException, TransformException {
        try {
            Connection conn = null;
            PreparedStatement selectFlatId = null;
            PreparedStatement insertRegistration = null;
            PreparedStatement select_red_id = null;
            PreparedStatement insertRegistrationTariffs = null;
            try {
                conn = dataSource.getConnection();
                conn.setAutoCommit(false);
                selectFlatId = conn.prepareStatement(SELECT_FLAT_ID);
                selectFlatId.setString(1, building.getStreet());
                selectFlatId.setInt(2, Integer.parseInt(building.getNumber()));
                selectFlatId.setInt(3, flatNumber);
                int flatId = selectFlatId.executeQuery().getInt(1);
                selectFlatId.close();

                java.util.Date date = registration.getDate();
                Date sqlDate = Date.valueOf(LocalDate.of(date.getYear(), date.getMonth(), date.getDay()));
                insertRegistration = conn.prepareStatement(INSERT_REGISTRATION_STATEMENT);
                insertRegistration.setDate(1, sqlDate);
                insertRegistration.setInt(2, flatId);
                insertRegistration.execute();

                select_red_id = conn.prepareStatement(SELECT_REGISTRATION_ID);
                select_red_id.setDate(1, sqlDate);
                select_red_id.setInt(2, flatId);
                int reg_id = select_red_id.executeQuery().getInt(1);

                insertRegistrationTariffs = conn.prepareStatement(INSERT_REGISTRATIONS_TARIFFS);
                batch(insertRegistration, reg_id, registration);
                insertRegistrationTariffs.executeBatch();

            } catch (SQLException e) {
                e.printStackTrace();
                if (conn != null) {
                    try {
                        System.err.print("Transaction is being rolled back");
                        conn.rollback();
                    } catch (SQLException excep) {
                        excep.printStackTrace();
                    }
                    throw new RemoteException("SQLException", e);
                }
            } finally {
                if (selectFlatId != null) {
                    selectFlatId.close();
                }
                if (insertRegistration != null) {
                    insertRegistration.close();
                }
                if (select_red_id != null) {
                    select_red_id.close();
                }
                if (insertRegistrationTariffs != null) {
                    insertRegistrationTariffs.close();
                }
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RemoteException("SQLException", e);
        }
    }

    private static int getFlatId(Connection connection, Building building, int flatNumber) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(SELECT_FLAT_ID);
        preparedStatement.setString(1, building.getStreet());
        preparedStatement.setInt(2, Integer.parseInt(building.getNumber()));
        preparedStatement.setInt(3, flatNumber);
        ResultSet resultSet = preparedStatement.executeQuery();
        preparedStatement.close();
        return resultSet.getInt(1);
    }

    private static void batch(PreparedStatement preparedStatement, int reg_id, Registration registration) throws SQLException {
        preparedStatement.setBigDecimal(1, BigDecimal.valueOf(registration.getColdwater()));
        preparedStatement.setInt(2, reg_id);
        preparedStatement.setString(3, "coldwater");
        preparedStatement.addBatch();

        preparedStatement.setBigDecimal(1, BigDecimal.valueOf(registration.getHotwater()));
        preparedStatement.setInt(2, reg_id);
        preparedStatement.setString(3, "hotwater");
        preparedStatement.addBatch();

        preparedStatement.setBigDecimal(1, BigDecimal.valueOf(registration.getElectricity()));
        preparedStatement.setInt(2, reg_id);
        preparedStatement.setString(3, "electricity");
        preparedStatement.addBatch();

        preparedStatement.setBigDecimal(1, BigDecimal.valueOf(registration.getGas()));
        preparedStatement.setInt(2, reg_id);
        preparedStatement.setString(3, "gas");
        preparedStatement.addBatch();
    }
}
