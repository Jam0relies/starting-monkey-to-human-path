package po53.kuznetsov.wdad.data.managers;

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
import java.io.PrintWriter;
import java.util.*;

import static po53.kuznetsov.wdad.utils.PreferencesConstantManager.*;

public class PreferencesManager {
    private static final String FILENAME = "out\\production\\starting-monkey-to-human-path" +
            "\\po53\\kuznetsov\\wdad\\resources\\configuration\\appconfig.xml";
    private static PreferencesManager INSTANCE = new PreferencesManager();
    private static XPathExpression CLASS_PROVIDER_EXPRESSION;
    private static XPathExpression RMI_EXPRESSION;
    private static XPathExpression CREATE_REGISTRY_EXPRESSION;
    private static XPathExpression REGISTRIES_EXPRESSION;
    private static XPathExpression REGISTRY_ADDRESS_EXPRESSION;
    private static XPathExpression REGISTRY_PORT_EXPRESSION;
    private static XPathExpression POLICY_PATH_EXPRESSION;
    private static XPathExpression USE_CODEBASE_ONLY_EXPRESSION;
    private static XPathExpression BINDED_OBJECTS_EXPRESSION;
    private static XPathExpression SERVER_EXPRESSION;
    private static XPathExpression FIRST_SERVER_CREATE_REGISTRY_EXPRESSION;
    private static XPathExpression FIRST_SERVER_REGISTRY_ADDRESS_EXPRESSION;
    private static XPathExpression FIRST_SERVER_REGISTRY_PORT_EXPRESSION;
    private static XPathExpression CLASSNAME_EXPRESSION;
    private static XPathExpression DRIVER_TYPE_EXPRESSION;
    private static XPathExpression HOSTNAME_EXPRESSION;
    private static XPathExpression PORT_EXPRESSION;
    private static XPathExpression DB_NAME_EXPRESSION;
    private static XPathExpression USER_EXPRESSION;
    private static XPathExpression PASS_EXPRESSION;
    private static Transformer TRANSFORMER;
    private static Map<String, XPathExpression> xPathExpressions;

    static {
        try {
            XPathFactory factory = XPathFactory.newInstance();
            XPath XPATH = factory.newXPath();
            CLASS_PROVIDER_EXPRESSION = XPATH.compile("/appconfig/rmi/classprovider");
            RMI_EXPRESSION = XPATH.compile("/appconfig/rmi");
            CREATE_REGISTRY_EXPRESSION = XPATH.compile("createregistry");
            REGISTRIES_EXPRESSION = XPATH.compile("/appconfig/rmi/server/registry");
            REGISTRY_ADDRESS_EXPRESSION = XPATH.compile("registryaddress");
            REGISTRY_PORT_EXPRESSION = XPATH.compile("registryPort");
            POLICY_PATH_EXPRESSION = XPATH.compile("/appconfig/rmi/client/policypath");
            USE_CODEBASE_ONLY_EXPRESSION = XPATH.compile("/appconfig/rmi/client/usecodebaseonly");
            BINDED_OBJECTS_EXPRESSION = XPATH.compile("/appconfig/rmi/server/bindedobject");
            SERVER_EXPRESSION = XPATH.compile("/appconfig/rmi/server[1]");
            CLASSNAME_EXPRESSION = XPATH.compile("/appconfig/datasource/classname");
            DRIVER_TYPE_EXPRESSION = XPATH.compile("/appconfig/datasource/drivertype");
            HOSTNAME_EXPRESSION = XPATH.compile("/appconfig/datasource/hostName");
            PORT_EXPRESSION = XPATH.compile("/appconfig/datasource/port");
            DB_NAME_EXPRESSION = XPATH.compile("/appconfig/datasource/DBName");
            USER_EXPRESSION = XPATH.compile("/appconfig/datasource/user");
            PASS_EXPRESSION = XPATH.compile("/appconfig/datasource/pass");

            FIRST_SERVER_CREATE_REGISTRY_EXPRESSION = XPATH
                    .compile("/appconfig/rmi/server[1]/registry/createregistry/text()");
            FIRST_SERVER_REGISTRY_ADDRESS_EXPRESSION = XPATH
                    .compile("/appconfig/rmi/server[1]/registry/registryaddress/text()");
            FIRST_SERVER_REGISTRY_PORT_EXPRESSION = XPATH
                    .compile("/appconfig/rmi/server[1]/registry/registryPort/text()");

            xPathExpressions = new HashMap<>();
            xPathExpressions.put(CREATE_REGISTRY, FIRST_SERVER_CREATE_REGISTRY_EXPRESSION);
            xPathExpressions.put(REGISTRY_ADDRESS, FIRST_SERVER_REGISTRY_ADDRESS_EXPRESSION);
            xPathExpressions.put(REGISTRY_PORT, FIRST_SERVER_REGISTRY_PORT_EXPRESSION);
            xPathExpressions.put(POLICYPATH, POLICY_PATH_EXPRESSION);
            xPathExpressions.put(USE_CODEBASE_ONLY, USE_CODEBASE_ONLY_EXPRESSION);
            xPathExpressions.put(CLASS_PROVIDER, CLASS_PROVIDER_EXPRESSION);
            xPathExpressions.put(CLASSNAME, CLASSNAME_EXPRESSION);
            xPathExpressions.put(DRIVER_TYPE, DRIVER_TYPE_EXPRESSION);
            xPathExpressions.put(HOSTNAME, HOSTNAME_EXPRESSION);
            xPathExpressions.put(PORT, PORT_EXPRESSION);
            xPathExpressions.put(DB_NAME, DB_NAME_EXPRESSION);
            xPathExpressions.put(USER, USER_EXPRESSION);
            xPathExpressions.put(PASS, PASS_EXPRESSION);
        } catch (XPathExpressionException e) {
            System.err.println(e.getMessage());
        }
    }

    private Document document;


    private PreferencesManager() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(true);
            factory.setIgnoringElementContentWhitespace(true); // prevents adding extra whitespaces with every rewrite
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(FILENAME);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            System.err.println(e.getMessage());
        }
    }

    public static PreferencesManager getInstance() {
        return INSTANCE;
    }

    public void setProperty(String key, String value) {
        getNode(xPathExpressions.get(key)).setTextContent(value);
    }

    public String getProperty(String key) {
        try {
            return (String) xPathExpressions.get(key).evaluate(document, XPathConstants.STRING);
        } catch (XPathExpressionException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    public void setProperties(Properties prop) {
        prop.forEach((key, value) -> setProperty((String) key, (String) value));
    }

    public Properties getProperties() {
        Properties properties = new Properties();
        xPathExpressions.forEach((key, value) -> properties.put(key, getProperty(key)));
        return properties;
    }

    public void addBindedObject(String name, String className) {
        Node serverNode = getNode(SERVER_EXPRESSION);
        Element node = document.createElement("bindedobject");
        node.setAttribute("class", className);
        node.setAttribute("name", name);
        serverNode.appendChild(node);
        rewriteXML();
    }

    public String getBindedObjectName(String className) {
        NodeList nodes = getNodes(BINDED_OBJECTS_EXPRESSION);
        boolean remooved = false;
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            NamedNodeMap attributes = node.getAttributes();
            if (attributes.getNamedItem("class").getNodeValue().equals(className)) {
                return attributes.getNamedItem("name").getNodeValue();
            }
        }
        return null;
    }

    public void removeBindedObject(String name) {
        NodeList nodes = getNodes(BINDED_OBJECTS_EXPRESSION);
        boolean remooved = false;
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            NamedNodeMap attributes = node.getAttributes();
            if (attributes.getNamedItem("name").getNodeValue().equals(name)) {
                Node serverNode = getNode(SERVER_EXPRESSION);
                serverNode.removeChild(node);
                remooved = true;
                break;
            }
        }
        if (remooved) {
            rewriteXML();
        }
    }


    @Deprecated
    public boolean hasClassProvider() {
        Node classProvider = getNode(CLASS_PROVIDER_EXPRESSION);
        return classProvider != null && classProvider.getTextContent().length() > 0;
    }

    @Deprecated
    public String getClassProvider() {
        return getNode(CLASS_PROVIDER_EXPRESSION).getTextContent();
    }

    @Deprecated
    public String setClassProvider(String value) {
        if (!hasClassProvider()) {
            try {
                Node rmi = ((Node) RMI_EXPRESSION.evaluate(document, XPathConstants.NODE));
                rmi.appendChild(document.createElement("classprovider"));
            } catch (XPathExpressionException e) {
                System.err.println(e.getMessage());
            }
        }
        getNode(CLASS_PROVIDER_EXPRESSION).setTextContent(value);
        rewriteXML();
        return null;
    }

    @Deprecated
    public void addRegistry(Registry registry) {
        Node serverNode = getNode(SERVER_EXPRESSION);
        serverNode.appendChild(registryToNode(registry));
        rewriteXML();
    }

    @Deprecated
    public boolean removeRegistry(Registry registry) {
        NodeList registriesNodes = getNodes(REGISTRIES_EXPRESSION);
        int registriesNodesLength = registriesNodes.getLength();
        for (int i = 0; i < registriesNodesLength; i++) {
            Node node = registriesNodes.item(i);
            if (equals(node, registry)) {
                Node serverNode = getNode(SERVER_EXPRESSION);
                serverNode.removeChild(node);
                return true;
            }
        }
        return false;
    }

    @Deprecated
    public String getPolicyPath() {
        return getNode(POLICY_PATH_EXPRESSION).getTextContent();
    }

    @Deprecated
    public void setPolicyPath(String policyPath) {
        getNode(POLICY_PATH_EXPRESSION).setTextContent(policyPath);
        rewriteXML();
    }

    @Deprecated
    public boolean isUseCodebaseOnly() {
        return booleanFromString(getNode(USE_CODEBASE_ONLY_EXPRESSION).getTextContent());
    }

    @Deprecated
    public void setUseCodebaseOnly(boolean value) {
        getNode(USE_CODEBASE_ONLY_EXPRESSION).setTextContent(booleanToString(value));
        rewriteXML();
    }

    @Deprecated
    public List<Registry> getRegistries() {
        NodeList registriesNodes = getNodes(REGISTRIES_EXPRESSION);
        int quantity = registriesNodes.getLength();
        List<Registry> registries = new ArrayList<>(quantity);
        for (int i = 0; i < quantity; i++) {
            Node node = registriesNodes.item(i);
            boolean isCreateRegistry = booleanFromString(getNode(node, CREATE_REGISTRY_EXPRESSION).getTextContent());
            String address = getNode(node, REGISTRY_ADDRESS_EXPRESSION).getTextContent();
            Integer port = Integer.parseInt(getNode(node, REGISTRY_PORT_EXPRESSION).getTextContent());
            registries.add(new Registry(isCreateRegistry, address, port));
        }
        return registries;
    }

    @Deprecated
    public List<BindedObject> getBindedObjects() {
        NodeList bindedObjectsNodes = getNodes(BINDED_OBJECTS_EXPRESSION);
        int quantity = bindedObjectsNodes.getLength();
        List<BindedObject> bindedObjects = new ArrayList<>(quantity);
        for (int i = 0; i < quantity; i++) {
            Node node = bindedObjectsNodes.item(i);
            NamedNodeMap attributes = node.getAttributes();
            String name = attributes.getNamedItem("name").getNodeValue();
            String className = attributes.getNamedItem("class").getNodeValue();
            bindedObjects.add(new BindedObject(name, className));
        }
        return bindedObjects;
    }

    @Deprecated
    public List<BindedObject> getBindedObjects(Registry registry) {
        List<BindedObject> bindedObjects = new ArrayList<>();
        Node serverNode = getNode(SERVER_EXPRESSION);
        NodeList serverNodeChildNodes = serverNode.getChildNodes();
        int serverNodeChildNodesLength = serverNodeChildNodes.getLength();
        for (int i = 0; i < serverNodeChildNodesLength; i++) {
            Node node = serverNodeChildNodes.item(i);
            if (equals(node, registry)) {
                while (++i < serverNodeChildNodesLength &&
                        (node = serverNodeChildNodes.item(i)).getNodeName().equals("bindedobject")) {
                    NamedNodeMap attributes = node.getAttributes();
                    String name = attributes.getNamedItem("name").getNodeValue();
                    String className = attributes.getNamedItem("class").getNodeValue();
                    bindedObjects.add(new BindedObject(name, className));
                }
                break;
            }
        }
        return bindedObjects;
    }

    @Deprecated
    public void addBindedObject(Registry registry, BindedObject bindedObject) {
        Node serverNode = getNode(SERVER_EXPRESSION);
        NodeList serverNodeChildNodes = serverNode.getChildNodes();
        List<Node> newChildren = new ArrayList<>(serverNodeChildNodes.getLength() + 1);
        int serverNodeChildNodesQuantity = serverNodeChildNodes.getLength();
        boolean registryFound = false;
        for (int i = 0; i < serverNodeChildNodesQuantity; i++) {
            Node node = serverNodeChildNodes.item(i);
            newChildren.add(node);
            if (!registryFound && equals(node, registry)) {
                newChildren.add(bindedObjectToNode(bindedObject));
                registryFound = true;
            }
        }
        serverNodeChildNodes = serverNode.getChildNodes();
        serverNodeChildNodesQuantity = serverNodeChildNodes.getLength();
        if (registryFound) {
            for (int i = 0; i < serverNodeChildNodesQuantity; i++) {
                Node node = serverNodeChildNodes.item(i);
                if (node != null) {
                    serverNode.removeChild(serverNodeChildNodes.item(i));
                }
            }
            for (Node node : newChildren) {
                serverNode.appendChild(node);
            }
        }
        rewriteXML();
    }

    @Deprecated
    public boolean removeBindedObject(BindedObject bindedObject) {
        NodeList nodes = getNodes(BINDED_OBJECTS_EXPRESSION);
        String exampleName = bindedObject.getName();
        String exampleClassName = bindedObject.getClassName();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            NamedNodeMap attributes = node.getAttributes();
            String name = attributes.getNamedItem("name").getNodeValue();
            String className = attributes.getNamedItem("class").getNodeValue();
            if (name.equals(exampleName) && className.equals(exampleClassName)) {
                Node serverNode = getNode(SERVER_EXPRESSION);
                serverNode.removeChild(node);
                return true;
            }
        }
        rewriteXML();
        return false;
    }

    private boolean booleanFromString(String yesOrNo) {
        return yesOrNo.equals("yes");
    }

    private String booleanToString(boolean value) {
        return (value) ? "yes" : "no";
    }

    private Node getNode(XPathExpression expression) {
        return getNode(document, expression);
    }

    private Node getNode(Object context, XPathExpression expression) {
        Node node = null;
        try {
            node = ((Node) expression.evaluate(context, XPathConstants.NODE));
        } catch (XPathExpressionException e) {
            System.err.println(e.getMessage());
        }
        return node;
    }

    private NodeList getNodes(XPathExpression expression) {
        NodeList nodes = null;
        try {
            nodes = ((NodeList) expression.evaluate(document, XPathConstants.NODESET));
        } catch (XPathExpressionException e) {
            System.err.println(e.getMessage());
        }
        return nodes;
    }

    private boolean equals(Node node, Registry registry) {
        return node.getNodeName().equals("registry") &&
                getNode(node, REGISTRY_ADDRESS_EXPRESSION).getTextContent().equals(registry.getAddress()) &&
                getNode(node, REGISTRY_PORT_EXPRESSION).getTextContent().equals(Integer.toString(registry.getPort()));
    }

    private Node bindedObjectToNode(BindedObject bindedObject) {
        Element node = document.createElement("bindedobject");
        node.setAttribute("class", bindedObject.getClassName());
        node.setAttribute("name", bindedObject.getName());
        return node;
    }

    private Node registryToNode(Registry registry) {
        Element registryNode = document.createElement("registry");

        Element createRegistryNode = document.createElement("createregistry");
        createRegistryNode.setTextContent(booleanToString(registry.isCreateRegistry()));
        registryNode.appendChild(createRegistryNode);

        Element registryAddressNode = document.createElement("registryaddress");
        registryAddressNode.setTextContent(registry.getAddress());
        registryNode.appendChild(registryAddressNode);

        Element registryPortNode = document.createElement("registryPort");
        registryPortNode.setTextContent(Integer.toString(registry.getPort()));
        registryNode.appendChild(registryPortNode);

        return registryNode;
    }

    private void rewriteXML() {
        try {
            if (TRANSFORMER == null) {
                TRANSFORMER = TransformerFactory.newInstance().newTransformer();
                TRANSFORMER.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                TRANSFORMER.setOutputProperty(OutputKeys.METHOD, "xml");
                TRANSFORMER.setOutputProperty(OutputKeys.INDENT, "yes");
                TRANSFORMER.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            }
            DOMSource src = new DOMSource(document);
            FileOutputStream fos = new FileOutputStream(FILENAME);

            DocumentType docType = document.getDoctype();
            PrintWriter writer = new PrintWriter(fos);
            writer.write("<?xml version=\"");
            writer.write(document.getXmlVersion());
            writer.write("\" encoding=\"");
            writer.write(document.getXmlEncoding());
            writer.write("\" ?>\n" +
                    "<!DOCTYPE appconfig [\n" +
                    docType.getInternalSubset() +
                    "]>\n");
            writer.flush();

            StreamResult result = new StreamResult(fos);

            TRANSFORMER.transform(src, result);
        } catch (TransformerException | IOException e) {
            e.printStackTrace(System.out);
        }
    }
}
