package po53.kuznetsov.wdad.data.managers;

import po53.kuznetsov.wdad.data.storage.DataSourceFactory;
import po53.kuznetsov.wdad.learn.rmi.*;

import javax.sql.DataSource;
import javax.xml.crypto.dsig.TransformException;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JDBCDataManager extends UnicastRemoteObject implements DataManager {
    private static final String INSERT_REGISTRATION_STATEMENT = "INSERT INTO `registrations`(`date`, `flats_id`)" +
            " VALUES (?, ?);";
    private static final String SELECT_REGISTRATION_ID = "SELECT `id` FROM `registrations` WHERE date=? AND flats_id=?";
    private static final String SELECT_FLAT = "SELECT flats.id, flats.persons_quantity, flats.area FROM" +
            " flats, buildings, street WHERE street.name=?" +
            " AND buildings.street_id = street.id AND buildings.number=? AND flats.buildings_id=buildings.id AND" +
            " flats.number=?;";
    private static final String INSERT_REGISTRATIONS_TARIFFS = "INSERT INTO `registrations-tariffs`(`amount`," +
            " `registrations_id`, `tariffs_name`) VALUES (?, ?, ?);";/* +
            "INSERT INTO `registrations-tariffs`(`amount`, `registrations_id`, `tariffs_name`) VALUES (?, ?, 'hotwater');" +
            "INSERT INTO `registrations-tariffs`(`amount`, `registrations_id`, `tariffs_name`) VALUES (?, ?, 'electricity');" +
            "INSERT INTO `registrations-tariffs`(`amount`, `registrations_id`, `tariffs_name`) VALUES (?, ?, 'gas');";*/
    private static final String UPDATE_TARIFF = "UPDATE `tariffs` SET `cost`=? WHERE `name`=?;";
    //    private static final String SELECT_FLAT = "SELECT flats.id, flats.number FROM flats, buildings, street WHERE street.name=?" +
//            " AND buildings.street_id = street.id AND buildings.number=? AND flats.buildings_id=buildings.id AND" +
//            " flats.number=?;";
    private static final String selectRegistrations = "SELECT registrations.id, registrations.date," +
            " `registrations-tariffs`.tariffs_name, `registrations-tariffs`.amount FROM registrations," +
            " `registrations-tariffs` WHERE registrations.flats_id =? AND " +
            "`registrations-tariffs`.registrations_id=registrations.id;";
    private static final String SELECT_TARIFFS = "SELECT `name`, `cost` FROM `tariffs`;";
    public static final String COLDWATER = "coldwater";
    public static final String HOTWATER = "hotwater";
    public static final String ELECTRICITY = "electricity";
    public static final String GAS = "gas";
    private DataSource dataSource;

    public JDBCDataManager() throws RemoteException {
        dataSource = DataSourceFactory.createDataSource();
    }


    @Override
    public double getBill(Building building, int flatNumber) throws RemoteException, TransformException {
        double cost = 0;
        try {
            Connection conn = dataSource.getConnection();
            PreparedStatement selectFlat = null;
            PreparedStatement selectRegistrations = null;
            try {
                selectFlat = selectFlatId(conn, building, flatNumber);
                ResultSet rs = selectFlat.executeQuery();
                rs.next();
                int flatId = rs.getInt(1);
                int personsQuantity = rs.getInt(2);
                double area = rs.getDouble(3);
                selectRegistrations = getRegistrations(conn, flatId);
                ResultSet registrationsSet = selectRegistrations.executeQuery();
                List<Registration> registrations = getRegistrations(registrationsSet);
                registrations.sort((a, b) -> -a.getDate().compareTo(b.getDate()));
                if (registrations.size() >= 2) {


                    Registration lastRegistration = registrations.get(0);
                    Registration previousMonthRegistration = registrations.get(1);
                    getTariffs(conn);
                    cost = countBill(lastRegistration, previousMonthRegistration);
                }


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
                if (conn != null) {
                    conn.close();
                }
                if (selectFlat != null) {
                    selectFlat.close();
                }
                if (selectRegistrations != null) {
                    selectRegistrations.close();
                }
            }
        } catch (SQLException e) {
            RemoteException remoteException = new RemoteException("SQLException", e);
            throw remoteException;
        }
        return cost;
    }

    @Override
    public Flat getFlat(Building building, int flatNumber) throws RemoteException, TransformException {
        try {
            Connection conn = null;
            PreparedStatement selectFlat = null;
            PreparedStatement selectRegistrations = null;
            try {
                conn = dataSource.getConnection();
                selectFlat = selectFlatId(conn, building, flatNumber);
                ResultSet rs = selectFlat.executeQuery();
                rs.next();
                int flatId = rs.getInt(1);
                int personsQuantity = rs.getInt(2);
                double area = rs.getDouble(3);
                selectRegistrations = getRegistrations(conn, flatId);
                ResultSet registrationsSet = selectRegistrations.executeQuery();
                List<Registration> registrations = getRegistrations(registrationsSet);
                Flat flat = new Flat(flatNumber, personsQuantity, area, registrations);
                return flat;
            } catch (SQLException e) {
                e.printStackTrace();
                System.err.print("Transaction is being rolled back");
                throw new RemoteException("SQLException", e);

            } finally {
                if (conn != null) {
                    conn.close();
                }
                if (selectFlat != null) {
                    selectFlat.close();
                }
                if (selectRegistrations != null) {
                    selectRegistrations.close();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RemoteException("SQLException", e);
        }
        //return null;
    }

    @Override
    public void setTariff(String tariffName, double newValue) throws RemoteException, TransformException {
        try {
            Connection conn = null;
            PreparedStatement update = null;
            try {
                conn = dataSource.getConnection();
                conn.setAutoCommit(false);
                update = conn.prepareStatement(UPDATE_TARIFF);
                update.setBigDecimal(1, BigDecimal.valueOf(newValue));
                update.setString(2, tariffName);
                update.execute();
                conn.setAutoCommit(true);
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
                if (conn != null) {
                    conn.close();
                }
                if (update != null) {
                    update.close();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RemoteException("SQLException", e);
        }
    }

    @Override
    public void addRegistration(Building building, int flatNumber, Registration registration) throws RemoteException, TransformException {
        try {
            Connection conn = null;
            // PreparedStatement selectFlatId = null;
            PreparedStatement insertRegistration = null;
            PreparedStatement select_red_id = null;
            PreparedStatement insertRegistrationTariffs = null;
            PreparedStatement selectFlat = null;
            try {
                conn = dataSource.getConnection();
                conn.setAutoCommit(false);
                /*
                selectFlatId = conn.prepareStatement(SELECT_FLAT);
                selectFlatId.setString(1, building.getStreet());
                selectFlatId.setInt(2, Integer.parseInt(building.getNumber()));
                selectFlatId.setInt(3, flatNumber);
                */
                selectFlat = selectFlatId(conn, building, flatNumber);
                ResultSet rs = selectFlat.executeQuery();
                rs.next();
                int flatId = rs.getInt(1);

                java.util.Date date = registration.getDate();
                Date sqlDate = Date.valueOf(LocalDate.of(date.getYear(), date.getMonth(), date.getDay()));
                insertRegistration = conn.prepareStatement(INSERT_REGISTRATION_STATEMENT);
                insertRegistration.setDate(1, sqlDate);
                insertRegistration.setInt(2, flatId);
                insertRegistration.execute();

                select_red_id = conn.prepareStatement(SELECT_REGISTRATION_ID);
                select_red_id.setDate(1, sqlDate);
                select_red_id.setInt(2, flatId);
                ResultSet rs2 = select_red_id.executeQuery();
                rs2.next();
                int reg_id = rs2.getInt(1);

                insertRegistrationTariffs = conn.prepareStatement(INSERT_REGISTRATIONS_TARIFFS);
                batch(insertRegistrationTariffs, reg_id, registration);
                insertRegistrationTariffs.executeBatch();

                conn.setAutoCommit(true);
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
                if (insertRegistration != null) {
                    insertRegistration.close();
                }
                if (select_red_id != null) {
                    select_red_id.close();
                }
                if (insertRegistrationTariffs != null) {
                    insertRegistrationTariffs.close();
                }
                if (selectFlat != null) {
                    selectFlat.close();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RemoteException("SQLException", e);
        }
    }

    private static PreparedStatement selectFlatId(Connection connection, Building building, int flatNumber) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(SELECT_FLAT);
        preparedStatement.setString(1, building.getStreet());
        preparedStatement.setInt(2, Integer.parseInt(building.getNumber()));
        preparedStatement.setInt(3, flatNumber);
        return preparedStatement;
    }

    private static void batch(PreparedStatement preparedStatement, int reg_id, Registration registration) throws SQLException {
        preparedStatement.setBigDecimal(1, BigDecimal.valueOf(registration.getColdwater()));
        preparedStatement.setInt(2, reg_id);
        preparedStatement.setString(3, TarifsKeys.COLDWATER.KEY);
        preparedStatement.addBatch();

        preparedStatement.setBigDecimal(1, BigDecimal.valueOf(registration.getHotwater()));
        preparedStatement.setInt(2, reg_id);
        preparedStatement.setString(3, TarifsKeys.HOTWATER.KEY);
        preparedStatement.addBatch();

        preparedStatement.setBigDecimal(1, BigDecimal.valueOf(registration.getElectricity()));
        preparedStatement.setInt(2, reg_id);
        preparedStatement.setString(3, TarifsKeys.ELECTRICITY.KEY);
        preparedStatement.addBatch();

        preparedStatement.setBigDecimal(1, BigDecimal.valueOf(registration.getGas()));
        preparedStatement.setInt(2, reg_id);
        preparedStatement.setString(3, TarifsKeys.GAS.KEY);
        preparedStatement.addBatch();
    }

    private static PreparedStatement getRegistrations(Connection connection, int flatId) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(selectRegistrations);
        preparedStatement.setInt(1, flatId);
        return preparedStatement;
    }

    private List<Registration> getRegistrations(ResultSet rs) throws SQLException {
        HashMap<Integer, Registration> registrationsById = new HashMap<>();
        while (rs.next()) {
            int id = rs.getInt(1);
            Date date = rs.getDate(2);
            String tarrifName = rs.getString(3);
            double amount = rs.getDouble(4);
            Registration registration = registrationsById.get(id);
            if (registration == null) {
                registration = new Registration(new java.util.Date(date.getYear(), date.getMonth(), date.getDate()),
                        0, 0, 0, 0);
                registrationsById.put(id, registration);
            }
            switch (tarrifName) {
                case COLDWATER: {
                    registration.setColdwater(amount);
                    break;
                }
                case HOTWATER: {
                    registration.setHotwater(amount);
                    break;
                }
                case ELECTRICITY: {
                    registration.setElectricity(amount);
                    break;
                }
                case GAS: {
                    registration.setGas(amount);
                    break;
                }
            }
        }
        return new ArrayList<>(registrationsById.values());
    }

    private static double countBill(Registration current, Registration previous) {
        double cost = 0;
        cost += (current.getColdwater() - previous.getColdwater()) * Tariffs.values.get(TarifsKeys.COLDWATER.KEY);
        cost += (current.getHotwater() - previous.getHotwater()) * Tariffs.values.get(TarifsKeys.HOTWATER.KEY);
        cost += (current.getElectricity() - previous.getElectricity()) * Tariffs.values.get(TarifsKeys.ELECTRICITY.KEY);
        cost += (current.getGas() - previous.getGas()) * Tariffs.values.get(TarifsKeys.GAS.KEY);
        return cost;
    }

    private static void getTariffs(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SELECT_TARIFFS)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String name = rs.getString(1);
                double cost = rs.getDouble(2);
                for (TarifsKeys tarifsKeys : TarifsKeys.values()) {
                    String key = tarifsKeys.KEY;
                    if (name.equals(key)) {
                        Tariffs.values.put(key, cost);
                    }
                }
            }
        }
    }
}
