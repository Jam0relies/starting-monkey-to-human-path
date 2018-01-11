package po53.kuznetsov.wdad.learn.xml;

import java.time.LocalDate;

public class TestXmlTask {
    interface Task {
        void perform() throws Exception;
    }

    public static void main(String[] args) {
        String validXml = "out\\production\\starting-monkey-to-human-path" +
                "\\po53\\kuznetsov\\wdad\\learn\\xml\\validXml.xml";
        String xmlForChange = "out\\production\\starting-monkey-to-human-path" +
                "\\po53\\kuznetsov\\wdad\\learn\\xml\\XMLForChange.xml";
        String invalidXml = "out\\production\\starting-monkey-to-human-path" +
                "\\po53\\kuznetsov\\wdad\\learn\\xml\\invalidXml.xml";
        getBillTest(validXml, "someStreet", 5, 1);

        setTariffTest(xmlForChange);
        addRegistrationTest(xmlForChange);
        getBillTest(5000, xmlForChange, "someStreet", 5, 1);
        testForExceptionsThrowing(() -> getBillTest(invalidXml, "someStreet", 5, 1));
        testForExceptionsThrowing(() -> getBillTest(validXml, "someStreet", 5, 5));
        testForExceptionsThrowing(() -> getBillTest(validXml, "invalid street", -50, 10));
    }

    private static void testForExceptionsThrowing(Task task) {
        boolean throwed = false;
        try {
            task.perform();
        } catch (Exception e) {
            throwed = true;
        }
        if(!throwed){
            throw new RuntimeException("Expected exception was not throwed");
        }
    }

    private static void getBillTest(double expectedValue, String filename, String street, int buildingNumber, int flatNumber) {
        XmlTask task = new XmlTask(filename);
        double bill = task.getBill(street, buildingNumber, flatNumber);
        if (bill != expectedValue) {
            throw new RuntimeException(String.format("Wrong bill. Expected: %f, actual: %f", expectedValue, bill));
        }
    }

    private static void getBillTest(String filename, String street, int buildingNumber, int flatNumber) {
        XmlTask task = new XmlTask(filename);
        task.getBill("someStreet", buildingNumber, flatNumber);
    }


    private static void setTariffTest(String filename) {
        XmlTask task = new XmlTask(filename);
        task.setTariff("coldwater", 100);
        task.setTariff("hotwater", 200);
        task.setTariff("electricity", 300);
        task.setTariff("gas", 400);

    }

    private static void addRegistrationTest(String filename) {
        XmlTask task = new XmlTask(filename);
        LocalDate now = LocalDate.now();
        LocalDate previousMonth = now.minusMonths(1);
        task.addRegistration("someStreet", 5, 1, previousMonth.getYear(),
                previousMonth.getMonthValue(), 5, 5, 5, 5);
        task.addRegistration("someStreet", 5, 1, now.getYear(),
                now.getMonthValue(), 10, 10, 10, 10);
    }
}
