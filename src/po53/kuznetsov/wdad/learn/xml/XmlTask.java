package po53.kuznetsov.wdad.learn.xml;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.print.Doc;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.IOException;
import java.lang.annotation.Documented;
import java.time.LocalDate;

public class XmlTask {
    private static XPathExpression X_PATH_TARIFFS_EXPRESSION;
    private static XPath XPATH;
    private static final String[] TARIFFS_KEYS = new String[]{"coldwater", "hotwater", "electricity", "gas"};
    private static final String[] REGISTRATION_KEYS = new String[]{"coldwater/text()", "hotwater/text()",
            "electricity/text()", "gas/text()"};

    static {
        try {
            XPathFactory factory = XPathFactory.newInstance();
            XPATH = factory.newXPath();
            X_PATH_TARIFFS_EXPRESSION = XPATH.compile("/housekeeper/tariffs[1]");
        } catch (XPathExpressionException e) {
            System.err.println(e.getMessage());
        }
    }

    private String filename;
    private Document document;


    public XmlTask(String filename) {
        this.filename = filename;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(filename);
        } catch (ParserConfigurationException e) {
            System.err.println(e.getMessage());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } catch (SAXException e) {
            System.err.println(e.getMessage());
        }
    }

    public double getBill(String street, int buildingNumber, int flatNumber) {
        NamedNodeMap tariffs = getTariffs();

        Node building = getUniqueNode(getBuildingExpression(street, buildingNumber), document);
        Node flat = getUniqueNode(getFlatSearchExpression(flatNumber), building);

        LocalDate now = LocalDate.now();
        Node thisMonthRegistration = getUniqueNode(getRegistrationSearchExpression(now.getMonthValue(), now.getYear()), flat);

        LocalDate previousMonth = now.minusMonths(1);
        Node previousMonthRegistration = getUniqueNode(getRegistrationSearchExpression(previousMonth.getMonthValue(),
                previousMonth.getYear()), flat);

        return countBill(thisMonthRegistration, previousMonthRegistration, tariffs);
    }

    public void setTariff(String tariffName, double newValue) {
        getTariffs().getNamedItem(tariffName).setNodeValue(Double.toString(newValue));
        rewriteXML();
    }

    public void addRegistration(String street, int buildingNumber, int flatNumber, int Year, int month,
                                double coldWater, double hotWater, double electricity, double gas) {

    }

    private NamedNodeMap getTariffs() {
        //TODO: remove null
        NamedNodeMap tariffs = null;
        try {
            tariffs = ((Node) X_PATH_TARIFFS_EXPRESSION.evaluate(document, XPathConstants.NODE)).getAttributes();
        } catch (XPathExpressionException e) {
            System.err.println(e.getMessage());
        }

        return tariffs;
    }

    private Node getUniqueNode(String expression, Object context) {
        NodeList nodes = null;
        try {
            nodes = (NodeList) XPATH.evaluate(expression, context, XPathConstants.NODESET);
            //System.err.println("Нод: " + nodes.getLength());
        } catch (XPathExpressionException e) {
            System.err.println(e.getMessage());
        }
        if (nodes == null || nodes.getLength() == 0) {
            return null;
        }
        if (nodes.getLength() > 1) {
            //TODO: throw exception
        }
        return nodes.item(0);
    }

    private String getBuildingExpression(String street, int buildingNumber) {
        return String.format("/housekeeper/building[@street=\"%s\"] [@number=\"%d\"]", street, buildingNumber);
    }

    private String getFlatSearchExpression(int flatNumber) {
        return String.format("flat[@number=\"%d\"][1]", flatNumber);
    }

    private double countBill(Node thisMontRegistration, Node previousMonthRegistration, NamedNodeMap tariffs) {
        if (thisMontRegistration == null || previousMonthRegistration == null) {
            return 0;
        }
        double tariff, difference;
        Node thisMonthRegistrationElement, previousMonthRegistrationElement;
        double bill = 0;
        for (int i = 0; i < REGISTRATION_KEYS.length; i++) {
            thisMonthRegistrationElement = getUniqueNode(REGISTRATION_KEYS[i], thisMontRegistration);
            previousMonthRegistrationElement = getUniqueNode(REGISTRATION_KEYS[i], previousMonthRegistration);
            if (thisMonthRegistrationElement != null && previousMonthRegistrationElement != null) {
                tariff = Double.parseDouble(tariffs.getNamedItem(TARIFFS_KEYS[i]).getNodeValue());
                difference = (Double.parseDouble(thisMonthRegistrationElement.getNodeValue())) -
                        Double.parseDouble(previousMonthRegistrationElement.getNodeValue());
                if (difference < 0 || tariff < 0) {
                    //TODO is it an error?
                }
                bill += difference * tariff;
            }
        }
        return bill;
    }

    private String getRegistrationSearchExpression(int month, int year) {
        return String.format("registration[@month=\"%d\"][@year=\"%d\"]", month, year);
    }

    private void rewriteXML() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            DOMSource source = new DOMSource(document);
            StreamResult out_stream = new StreamResult(filename);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            DocumentType documentType = document.getDoctype();
            String systemID = documentType.getSystemId();
            String publicID = documentType.getPublicId();
            String res = publicID + "\" \"" + systemID;
            transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, res);
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","2");
                    transformer.transform(source, out_stream);
        } catch (ParserConfigurationException e) {
            System.err.println(e.getMessage());
        } catch (TransformerConfigurationException e) {
            System.err.println(e.getMessage());
        } catch (TransformerException e) {
            System.err.println(e.getMessage());
        }
    }
}
