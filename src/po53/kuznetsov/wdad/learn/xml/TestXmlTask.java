package po53.kuznetsov.wdad.learn.xml;

public class TestXmlTask {
    interface Task {
        void perform() throws Exception;
    }

    public static void main(String[] args) {
        String validXml = "out\\production\\starting-monkey-to-human-path" +
                "\\po53\\kuznetsov\\wdad\\learn\\xml\\validXml.xml";
        String invalidXml = "out\\production\\starting-monkey-to-human-path" +
                "\\po53\\kuznetsov\\wdad\\learn\\xml\\invalidXml.xml";
        getBillTest(validXml, "someStreet",5, 1);
        setTariffTest(validXml);
        addRegistrationTest(validXml);
        testForExceptionsThrowing(() -> getBillTest(invalidXml, "someStreet",5, 1));
        testForExceptionsThrowing(() -> getBillTest(validXml,"someStreet", 10,1 ));
    }

    private static void testForExceptionsThrowing(Task task) {
        boolean throwed = false;
        try {
            task.perform();
        } catch (Exception e) {
            throwed = true;
        }
        assert throwed : "Exception was not throwed";
    }

    private static void getBillTest(String filename,String street, int buildingNumber, int flatNumber) {
        XmlTask task = new XmlTask(filename);
        double bill = task.getBill("someStreet", buildingNumber, flatNumber);
    }

    private static void setTariffTest(String filename) {
        XmlTask task = new XmlTask(filename);
        task.setTariff("gas", 500);
    }

    private static void addRegistrationTest(String filename) {
        XmlTask task = new XmlTask(filename);
        task.addRegistration("someStreet", 5, 1, 2017, 1, 1, 2, 3, 4);
    }
}
