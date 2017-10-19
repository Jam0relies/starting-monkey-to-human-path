package po53.kuznetsov.wdad.learn.xml;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.NoSuchElementException;

public class XmlTask {
    private static final String[] TARIFFS_KEYS = new String[]{"coldwater", "hotwater", "electricity", "gas"};
    private static final String[] REGISTRATION_KEYS = new String[]{"coldwater/text()", "hotwater/text()",
            "electricity/text()", "gas/text()"};
    private static XPathExpression X_PATH_TARIFFS_EXPRESSION;
    private static XPath XPATH;
    private static DocumentBuilder BUILDER;
    private static Transformer TRANSFORMER;

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
            document = getBuilder().parse(filename);
        } catch (ParserConfigurationException e) {
            System.err.println(e.getMessage());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } catch (SAXException e) {
            System.err.println(e.getMessage());
        }
    }

    private static DocumentBuilder getBuilder()throws ParserConfigurationException{
        if(BUILDER ==null) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(true);
            //prevents adding extra whitespaces with every rewrite
            factory.setIgnoringElementContentWhitespace(true);
            BUILDER = factory.newDocumentBuilder();
        }
        return BUILDER;
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
        NamedNodeMap tariffs = getTariffs();
        Node item = tariffs.getNamedItem(tariffName);
        item.setNodeValue(Double.toString(newValue));
        rewriteXML();
    }

    public void addRegistration(String street, int buildingNumber, int flatNumber, int Year, int month,
                                double coldWater, double hotWater, double electricity, double gas) {
        Node building = getUniqueNode(getBuildingExpression(street, buildingNumber), document);
        Node flat = getUniqueNode(getFlatSearchExpression(flatNumber), building);

        Node oldRegistration = getUniqueNode(getRegistrationSearchExpression(month, Year), flat);
        try {
            if (oldRegistration != null) {
                flat.removeChild(oldRegistration);
            }
            flat.appendChild(registration(Year, month, coldWater, hotWater, electricity, gas));
        }catch (NullPointerException e){
            NoSuchElementException newException = new NoSuchElementException(
                    String.format("No flat with address: street- %s, building number- %d, " +
                            "flat number-%d,",street, buildingNumber,flatNumber));
            newException.addSuppressed(e);
            throw  newException;
        }

        rewriteXML();
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
        } catch (XPathExpressionException e) {
            System.err.println(e.getMessage());
        }
        if (nodes == null || nodes.getLength() == 0) {
            return null;
        }
        if (nodes.getLength() > 1) {
            throw new RuntimeException("multiple nodes");
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
                bill += difference * tariff;
            }
        }
        return bill;
    }

    private String getRegistrationSearchExpression(int month, int year) {
        return String.format("registration[@month=\"%d\"][@year=\"%d\"]", month, year);
    }


    private void rewriteXML() {
        /*
        Transformer transformer = null;
        DOMSource src = null;
        FileOutputStream fos = null;
        */
        try {
            if(TRANSFORMER == null) {
                TRANSFORMER = TransformerFactory.newInstance().newTransformer();
            }
            DOMSource src = new DOMSource(document);
            FileOutputStream fos = new FileOutputStream(filename);

            StreamResult result = new StreamResult(fos);
            TRANSFORMER.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "housekeeper.dtd");
            TRANSFORMER.setOutputProperty(OutputKeys.METHOD, "xml");
            TRANSFORMER.setOutputProperty(OutputKeys.INDENT, "yes");
            TRANSFORMER.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            document.normalizeDocument();
            TRANSFORMER.transform(src, result);
        } catch (TransformerException e) {
            e.printStackTrace(System.out);
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }

    }

    private Node registration(int year, int month, double coldWater, double hotWater, double electricity, double gas) {
        Element registration = document.createElement("registration");
        registration.setAttribute("month", Integer.toString(month));
        registration.setAttribute("year", Integer.toString(year));

        Node node = document.createElement("coldwater");
        node.setTextContent(Double.toString(coldWater));
        registration.appendChild(node);

        node = document.createElement("hotwater");
        node.setTextContent(Double.toString(hotWater));
        registration.appendChild(node);

        node = document.createElement("electricity");
        node.setTextContent(Double.toString(electricity));
        registration.appendChild(node);

        node = document.createElement("gas");
        node.setTextContent(Double.toString(gas));
        registration.appendChild(node);

        return registration;
    }

}
