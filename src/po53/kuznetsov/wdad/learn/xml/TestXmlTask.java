package po53.kuznetsov.wdad.learn.xml;

public class TestXmlTask {
    public static void main(String[] args) {
        String filename = "out\\production\\starting-monkey-to-human-path" +
                "\\po53\\kuznetsov\\wdad\\learn\\xml\\validXml.xml";
        getBillTest(filename);
    }

    private static void getBillTest(String filename){
        XmlTask task = new XmlTask(filename);
        double bill = task.getBill("someStreet",5,1);
        System.out.println(bill);
    }
}
