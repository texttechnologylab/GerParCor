package org.texttechnologylab.parliament.helper;

import org.javatuples.Pair;
import org.javatuples.Tuple;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

public class XMLParserHelper {

    public static List<Pair<Integer, String>> getSpeechAndComments(Node node){
        List<Pair<Integer, String>> content = new ArrayList<>();

        if(!node.hasChildNodes())
            return content;

        for (int i = 0; i < node.getChildNodes().getLength(); i++) {

            Node child  = node.getChildNodes().item(i);
            if(child.getNodeName().equals("p") && hasAttribute(child, "klasse", "J_1", "J", "O")){
                content.add(Pair.with(0, child.getTextContent()));
            }else if(child.getNodeName().equals("kommentar"))
                content.add(Pair.with(1, child.getTextContent()));
        }

        return content;
    }

    /**
     * Gets the content of the first child node with a specified name
     * @param node the parent node
     * @param name the child nodes name
     * @return the content of the nodes found, "" if no node such node found
     */
    public static String getSubNodeText(Node node, String name){

        Node n = getFirstSubNodeByName(node, name);

        if(n == null)
            return "";

        return n.getTextContent();
    }

    /**
     * Gets the content of every child node with a specified name
     * @param node the parent node
     * @param name the child nodes name
     * @return the content of the nodes found
     */
    public static List<String> getSubNodesText(Node node, String name){

        List<Node> ns = getAllSubNodesByName(node, name);
        List<String> s = new ArrayList<>();

        for(Node n : ns){
            s.add(n.getTextContent());
        }

        return s;
    }

    /**
     * Gets the content of every child node with a specified name and a specified attribute and value
     * @param node the parent node
     * @param name the child nodes name
     * @param attributeName the attribute name
     * @param attributeValues every viable attribute value
     * @return
     */
    public static List<String> getAllSubNodesWithAttributeText(Node node, String name, String attributeName, String... attributeValues){

        List<Node> nodes = getAllSubNodesWithAttributeByName(node, name, attributeName, attributeValues);

        List<String> output = new ArrayList<>();

        for(Node n : nodes)
            output.add(n.getTextContent());

        return output;
    }

    /**
     * Gets the content of child nodes with a specified name and a specified attribute and value
     * @param node the parent node
     * @param name the child nodes name
     * @param attributeName the attribute name
     * @param attributeValues every viable attribute value
     * @return
     */
    public static List<String> getChildSubNodesWithAttributeText(Node node, String name, String attributeName, String... attributeValues){

        List<Node> nodes = getChildSubNodesWithAttributeByName(node, name, attributeName, attributeValues);

        List<String> output = new ArrayList<>();

        for(Node n : nodes)
            output.add(n.getTextContent());

        return output;
    }

    /**
     * Gets the first child node with a specified name
     * @param node the parent node
     * @param name the child nodes name
     * @return the first child node found
     */
    public static Node getFirstSubNodeByName(Node node, String name){
        List<Node> n = getAllSubNodesByName(node, name);

        if(n.isEmpty())
            return null;

        return n.get(0);
    }

    /**
     * Gets all child nodes with a specified name
     * @param node the parent node
     * @param name the child nodes name
     * @return the child nodes found
     */
    public static List<Node> getAllSubNodesByName(Node node, String name){
        List<Node> nodes = new ArrayList<>();

        if(node.getNodeName().equals(name)) {
            nodes.add(node);
        }else{
            if(!node.hasChildNodes())
                return nodes;

            for (int i = 0; i < node.getChildNodes().getLength(); i++) {
                nodes.addAll(getAllSubNodesByName(node.getChildNodes().item(i), name));
            }
        }
        return nodes;
    }

    /**
     * Gets every child node with a specified name and a specified attribute and value
     * @param node the parent node
     * @param name the child nodes name
     * @param attributeName the attributes name
     * @param attributeValues all viable attribute values
     * @return the child nodes found
     */
    public static List<Node> getAllSubNodesWithAttributeByName(Node node, String name, String attributeName, String... attributeValues){
        List<Node> nodes = new ArrayList<>();

        if(node.getNodeName().equals(name) && hasAttribute(node, attributeName, attributeValues)) {
            nodes.add(node);
        }else{
            if(!node.hasChildNodes())
                return nodes;

            for (int i = 0; i < node.getChildNodes().getLength(); i++) {
                nodes.addAll(getAllSubNodesWithAttributeByName(node.getChildNodes().item(i), name, attributeName, attributeValues));
            }
        }
        return nodes;
    }

    /**
     * Gets every child node with a specified name and a specified attribute and value
     * @param node the parent node
     * @param name the child nodes name
     * @param attributeName the attributes name
     * @param attributeValues all viable attribute values
     * @return the child nodes found
     */
    public static List<Node> getChildSubNodesWithAttributeByName(Node node, String name, String attributeName, String... attributeValues){
        List<Node> nodes = new ArrayList<>();

        if(!node.hasChildNodes())
                return nodes;

        for (int i = 0; i < node.getChildNodes().getLength(); i++) {

            Node child  = node.getChildNodes().item(i);
            if(child.getNodeName().equals(name) && hasAttribute(child, attributeName, attributeValues)){
                nodes.add(child);
            }
        }

        return nodes;
    }

    /**
     * Checks if a nodes has an attribute with the specified name and value
     * @param node the node to check
     * @param attributeName the attributes name
     * @param attributeValue all viable attribute values
     * @return true if attributeName and at least one attributeValue match
     */
    public static boolean hasAttribute(Node node, String attributeName, String... attributeValue) {

        Element element = (Element) node;

        if(!element.hasAttribute(attributeName))
            return false;

        for(String value : attributeValue){
            if(element.getAttribute(attributeName).equals(value))
                return true;
        }

        return false;
    }
}