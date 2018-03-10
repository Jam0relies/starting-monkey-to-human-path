package po53.kuznetsov.wdad.learn.xml;

import org.w3c.dom.*;
import org.xml.sax.SAXException;
import po53.kuznetsov.wdad.learn.rmi.Flat;
import po53.kuznetsov.wdad.learn.rmi.Registration;

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
import java.util.*;

public class XmlTask {
    private static final String[] TARIFFS_KEYS = new String[]{"coldwater", "hotwater", "electricity", "gas"};
    private static final String[] REGISTRATION_KEYS = new String[]{"coldwater/text()", "hotwater/text()",
            "electricity/text()", "gas/text()"};
    private static XPathExpression X_PATH_TARIFFS_EXPRESSION;
    private static XPathExpression X_PATH_REGISTRATIONS_EXPRESSION;
    private static XPathExpression X_PATH_MONTH_EXPRESSION;
    private static XPathExpression X_PATH_YEAR_EXPRESSION;
    private static XPath XPATH;
    private static DocumentBuilder BUILDER;
    private static Transformer TRANSFORMER;
    private static Comparator<Node> REGISTRIES_DATE_ORDER = (first, second) -> {
        int firstYear = getYear(first);
        int secondYear = getYear(second);
        int yearComparing = Integer.compare(firstYear, secondYear);
        if (yearComparing != 0) {
            return yearComparing;
        }
        int firstMonth = getMonth(first);
        int secondMonth = getMonth(second);
        return Integer.compare(firstMonth, secondMonth);
    };


    static {
        try {
            XPathFactory factory = XPathFactory.newInstance();
            XPATH = factory.newXPath();
            X_PATH_TARIFFS_EXPRESSION = XPATH.compile("/housekeeper/tariffs[1]");
            X_PATH_REGISTRATIONS_EXPRESSION = XPATH.compile("registration");
            X_PATH_MONTH_EXPRESSION = XPATH.compile("number(@month)");
            X_PATH_YEAR_EXPRESSION = XPATH.compile("number(@year)");
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
    }

    private String filename;
    private Document document;


    public XmlTask(String filename) {
        this.filename = filename;
        try {
            //System.out.println(new File(filename).exists());
            document = getBuilder().parse(filename);
            //System.out.println("document "+ document.getXmlVersion());
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
            throw new RuntimeException("");
        }
    }

    private static DocumentBuilder getBuilder() throws ParserConfigurationException {
        if (BUILDER == null) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(true);
            factory.setIgnoringElementContentWhitespace(true); // prevents adding extra whitespaces with every rewrite
            BUILDER = factory.newDocumentBuilder();
        }
        return BUILDER;
    }


    public double getBill(String street, int buildingNumber, int flatNumber) {
        NamedNodeMap tariffs = getTariffs();

        Node building = getUniqueNode(getBuildingExpression(street, buildingNumber), document);
        //System.out.println("document "+document.getTextContent());
        //System.out.println("building "+building.getTextContent());
        Node flat = getUniqueNode(getFlatSearchExpression(flatNumber), building);
        if (flat == null) {
            throw flatNotFoundException(street, buildingNumber, flatNumber);
        }
        List<Node> registries = getRegistritions(flat);
        if (registries.size() < 2) {
            return 0; // can not count bill without
        }
        /*
        for(Node node: registries){
            System.out.println("year " + getYear(node) + " month " + getMonth(node));
        }
        */
        registries.sort(REGISTRIES_DATE_ORDER);
        /*
        for(Node node: registries){
            System.out.println("year " + getYear(node) + " month " + getMonth(node));
        }
        */
        Node lastRegistry = registries.get(registries.size() - 1);
        //System.out.println("last year " + getYear(lastRegistry) + " month " + getMonth(lastRegistry));
        if (!isCurrentMonthRegistry(lastRegistry)) {
            return 0; // no current data
        }
        Node previousRegistry = registries.get(registries.size() - 2);
        return countBill(lastRegistry, previousRegistry, tariffs);
    }

    private boolean isCurrentMonthRegistry(Node registry) {
        LocalDate now = LocalDate.now();
        return getYear(registry) == now.getYear() && getMonth(registry) == now.getMonthValue();
    }

    private static int getMonth(Node registry) {
        try {
            //return ((Double)XPATH.evaluate("number(/registration[1]/@month)",registry, XPathConstants.NUMBER)).intValue();
            return Integer.parseInt(X_PATH_MONTH_EXPRESSION.evaluate(registry));
        } catch (XPathExpressionException e) {
            e.printStackTrace();
            throw new RuntimeException("Can not get month from registry", e);
        }
    }

    private static int getYear(Node registry) {
        try {
            //return ((Double)XPATH.evaluate("number(@year)",registry, XPathConstants.NUMBER)).intValue();
            return Integer.parseInt(X_PATH_YEAR_EXPRESSION.evaluate(registry));
        } catch (XPathExpressionException e) {
            e.printStackTrace();
            throw new RuntimeException("Can not get year from registry", e);
        }
    }

    private List<Node> getRegistritions(Node flat) {
        try {
            //System.out.println("flat "+ flat.getTextContent());
            NodeList nodeList = (NodeList) X_PATH_REGISTRATIONS_EXPRESSION.evaluate(flat, XPathConstants.NODESET);
            int length = nodeList.getLength();
            //System.out.println("size " + length);
            //System.out.println(flat.getTextContent());
            List<Node> list = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                list.add(nodeList.item(i));
            }

            return list;
        } catch (XPathExpressionException e) {
            e.printStackTrace();
            throw new RuntimeException("Cannot get registries");
        }
    }

//    private void sortRegistritionsByDate(List<Node> registries) {
//        registries.sort(
//    }

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
        } catch (NullPointerException e) {
            NoSuchElementException newException = flatNotFoundException(street, buildingNumber, flatNumber);
            newException.addSuppressed(e);
            throw newException;
        }

        rewriteXML();
    }

    NoSuchElementException flatNotFoundException(String street, int buildingNumber, int flatNumber) {
        return new NoSuchElementException(
                String.format("No flat with address: street- %s, building number- %d, " +
                        "flat number-%d,", street, buildingNumber, flatNumber));
    }

    public Flat getFlat(String street, int buildingNumber, int flatNumber) {
        NamedNodeMap tariffs = getTariffs();
        Node building = getUniqueNode(getBuildingExpression(street, buildingNumber), document);
        Node flatNode = getUniqueNode(getFlatSearchExpression(flatNumber), building);
        if (flatNode == null) {
            throw flatNotFoundException(street, buildingNumber, flatNumber);
        }
        NamedNodeMap attributes = flatNode.getAttributes();
        double area = Double.parseDouble(attributes.getNamedItem("area").getNodeValue());
        int personsQuantity =Integer.parseInt(attributes.getNamedItem("personsquantity").getNodeValue());
        List<Node> registrationNodes = getRegistritions(flatNode);
        int registrationsQuantity = registrationNodes.size();
        List<Registration> registrations = new ArrayList<>();
        for (int i = 0; i < registrationsQuantity; i++) {
            registrations.add(toRegistration(registrationNodes.get(i)));
        }

        return new Flat(flatNumber, personsQuantity, area, registrations);
    }

    private Registration toRegistration(Node node) {
        NamedNodeMap attributes = node.getAttributes();
        int month = Integer.parseInt(attributes.getNamedItem("month").getNodeValue());
        int year = Integer.parseInt(attributes.getNamedItem("year").getNodeValue());
        Date date = new Date(year, month + 1, 1);
        double coldwater = Double.parseDouble(getUniqueNode("coldwater", node).getNodeValue());
        double hotwater = Double.parseDouble(getUniqueNode("hotwater", node).getNodeValue());
        double electricity = Double.parseDouble(getUniqueNode("electricity", node).getNodeValue());
        double gas = Double.parseDouble(getUniqueNode("gas", node).getNodeValue());
        return new Registration(date, coldwater, hotwater, electricity, gas);
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
        try {
            if (TRANSFORMER == null) {
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
        } catch (TransformerException | IOException e) {
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
