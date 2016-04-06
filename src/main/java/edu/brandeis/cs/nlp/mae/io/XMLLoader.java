/*
 * MAE - Multi-purpose Annotation Environment
 *
 * Copyright Keigh Rim (krim@brandeis.edu)
 * Department of Computer Science, Brandeis University
 * Original program by Amber Stubbs (astubbs@cs.brandeis.edu)
 *
 * MAE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, @see <a href="http://www.gnu.org/licenses">http://www.gnu.org/licenses</a>.
 *
 * For feedback, reporting bugs, use the project on Github
 * @see <a href="https://github.com/keighrim/mae-annotation">https://github.com/keighrim/mae-annotation</a>.
 */

package edu.brandeis.cs.nlp.mae.io;

import edu.brandeis.cs.nlp.mae.MaeException;
import edu.brandeis.cs.nlp.mae.MaeStrings;
import edu.brandeis.cs.nlp.mae.database.MaeDBException;
import edu.brandeis.cs.nlp.mae.database.MaeDriverI;
import edu.brandeis.cs.nlp.mae.model.*;
import edu.brandeis.cs.nlp.mae.util.SpanHandler;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.*;
import java.io.*;
import java.util.*;

/**
 * Created by krim on 12/27/2015.
 */
public class XMLLoader {


    private static final Logger logger = LoggerFactory.getLogger(XMLLoader.class.getName());

    private MaeDriverI driver;
    private Map<TagType, Map<String, AttributeType>> attTypeMap;
    String primaryText;

    public XMLLoader(MaeDriverI driver) {
        this.attTypeMap = new HashMap<>();
        this.driver = driver;
    }

    public void read(File file) throws MaeIOXMLException, MaeDBException {
        try {
            try {
                logger.info("reading annotations from file: " + file.getAbsolutePath());
                driver.setAnnotationFileName(file.getAbsolutePath().replace("/./", "/"));
                this.readXmlStream(new FileInputStream(file));
            } catch (SAXException e) {
                try {
                    logger.info("file is not an XML or does not match DTD, reading as the primary text: " + file.getAbsolutePath());
                    Scanner scanner = new Scanner(file, "UTF-8");
                    primaryText = scanner.useDelimiter("\\A").next();
                    driver.setPrimaryText(primaryText);
                    scanner.close(); // Put this call in a finally block
                } catch (NoSuchElementException ex) {
                    String message = "failed to read the file, may be a binary file? " + file.getAbsolutePath();
                    driver.setAnnotationFileName(null);
                    logger.error(message);
                    throw new MaeIOXMLException(message);
                }

            }
        } catch (FileNotFoundException e) {
            driver.setAnnotationFileName(null);
            catchFileNotFoundError(file, e);
        }
    }

    public void read(String string) throws MaeIOXMLException, MaeDBException, SAXException {
        logger.debug("reading annotations from plain JAVA string");
        this.readXmlStream(IOUtils.toInputStream(string));

    }

    public void readXmlStream(InputStream stream) throws MaeIOXMLException, MaeDBException, SAXException {
        try {
            Document doc = xmlInputStreamToDomWithLineNum(stream);
            doc.getDocumentElement().normalize();
            Node root = doc.getDocumentElement();
            assertRootNodeMatches(root);
            NodeList taskNodes = root.getChildNodes();
            boolean textExists = false;
            for (int i = 0; i < taskNodes.getLength(); i++) {
                Node child = taskNodes.item(i);
                if (child.getNodeName().equalsIgnoreCase("text")) {
                    readPrimaryText(child);
                    textExists = true;
                } else if (child.getNodeName().equalsIgnoreCase("tags")) {
                    readAnnotations(child);
                } else if (child.getNodeType() == Node.ELEMENT_NODE) {
                    logger.warn(String.format("undefined element found in input XML: %s at %s", child.getNodeName(), child.getUserData("lineNum")));
                }
                // ignore non-element, such as <!--commend-->, or #text
            }
            if (!textExists) {
                String message = "input document does not contains a primary text!";
                logger.error(message);
                throw new MaeIOXMLException(message);
            }
        } catch (ParserConfigurationException e) {
            catchInputFileParseError(e);
        } catch (IOException e) {
            catchFileIOError(e);
        }
    }

    void catchFileNotFoundError(File file, FileNotFoundException e) throws MaeIOXMLException {
        String message = "file not found: " + file.getAbsolutePath();
        logger.error(message);
        throw new MaeIOXMLException(message, e);
    }

    void catchInputFileParseError(ParserConfigurationException e) throws MaeIOXMLException {
        String message = "failed to create XML-parser/DOM-builder";
        logger.error(message);
        throw new MaeIOXMLException(message, e);
    }

    void catchFileIOError(IOException e) throws MaeIOXMLException {
        String message = "found an error in input XML";
        logger.error(message);
        throw new MaeIOXMLException(message, e);
    }

    void assertRootNodeMatches(Node rootNode) throws MaeDBException, SAXException {
        if (!rootNode.getNodeName().equals(driver.getTaskName())) {
            String message = "file does not match to DTD!";
            logger.error(message);
            throw new SAXException(message);
        }
    }

    void assertPrimaryTextMatches(Node textNode) throws MaeDBException, MaeIOXMLException {
        if (!textNode.getTextContent().equals(driver.getPrimaryText())) {
            String message = "file's text is different from current work!";
            logger.error(message);
            throw new MaeIOXMLException(message);
        }
    }

    public boolean isFileMatchesCurrentWork(File file) throws MaeIOXMLException, MaeDBException {
        try {
            Document doc = xmlInputStreamToDomWithLineNum(new FileInputStream(file));
            doc.getDocumentElement().normalize();
            Node root = doc.getDocumentElement();
            assertRootNodeMatches(root);
            NodeList children = root.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child.getNodeName().equalsIgnoreCase("text")) {
                    assertPrimaryTextMatches(child);
                }
            }
        } catch (ParserConfigurationException e) {
            catchInputFileParseError(e);
        } catch (FileNotFoundException e) {
            catchFileNotFoundError(file, e);
        } catch (IOException e) {
            catchFileIOError(e);
        } catch (SAXException e) {
            String message = "file is not an XML: " + file.getAbsolutePath();
            logger.error(message);
            throw new MaeIOXMLException(message, e);
        }
        return true;

    }

    private void readAnnotations(Node tagsNode) throws MaeDBException, MaeIOXMLException {
        logger.debug("reading annotations... at " + tagsNode.getUserData("lineNum"));
        Map<String, TagType> tagTypeMap = new HashMap<>();
        for (TagType type : driver.getAllTagTypes()) {
            tagTypeMap.put(type.getName(), type);
        }
        List<Node> linkTags = new LinkedList<>();
        NodeList annotations = tagsNode.getChildNodes();
        for (int i = 0; i < annotations.getLength(); i++) {
            Node annotation = annotations.item(i);
            if (annotation.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            TagType tagType = tagTypeMap.get(annotation.getNodeName());
            if (tagType.isLink()) {
                linkTags.add(annotation);
            } else {
                readExtentTag(annotation, tagType);
            }
        }
        for (Node linkTag : linkTags) {
            readLinkTag(linkTag);
        }
    }

    private void readExtentTag(Node tagNode, TagType tagType) throws MaeDBException, MaeIOXMLException {
        Map<String, AttributeType> possibleAttTypeMap = getAttributeTypeMap(tagType);
        NamedNodeMap nodeAttributes = tagNode.getAttributes();
        String tid = nodeAttributes.getNamedItem("id").getNodeValue();
        if (tid == null) {
            String message = "annotation must have id attribute, found at " + tagNode.getUserData("lineNum");
            logger.error(message);
            throw new MaeIOXMLException(message);
        }
//        nodeAttributes.removeNamedItem("id");

        int[] spansArray;
        if (nodeAttributes.getNamedItem("spans") != null) {
            try {
                spansArray = SpanHandler.convertStringToArray(nodeAttributes.getNamedItem("spans").getNodeValue());
            } catch (MaeException e) {
                String message = "spans string is ill-formed";
                throw new MaeIOXMLException(message, e);
            }
//            nodeAttributes.removeNamedItem("spans");
        } else {
            try {
                int start = Integer.parseInt(nodeAttributes.getNamedItem("start").getNodeValue());
                int end = Integer.parseInt(nodeAttributes.getNamedItem("end").getNodeValue());
//                nodeAttributes.removeNamedItem("start");
//                nodeAttributes.removeNamedItem("end");
                spansArray = SpanHandler.range(start, end);
            } catch (NumberFormatException | NullPointerException e) {
                String message = "an extent tag must have either spans or start-end attributes at " + tagNode.getUserData("lineNum");
                logger.error(message);
                throw new MaeIOXMLException(message);
            }
        }

        String text = "";
        if (spansArray.length > 0) {
            List<int[]> spanPairs = SpanHandler.convertArrayToPairs(spansArray);
            Iterator<int[]> iter = spanPairs.iterator();

            while (iter.hasNext()) {
                int[] pair = iter.next();
                text += primaryText.substring(pair[0], pair[1]);
                if (iter.hasNext()) {
                    text += MaeStrings.SPANTEXTTRUNC;
                }
            }
        }
//        String text = nodeAttributes.getNamedItem("text").getNodeValue();
//        nodeAttributes.removeNamedItem("text");

        ExtentTag newTag = driver.createExtentTag(tid, tagType, text, spansArray);

        addAllAttributes(possibleAttTypeMap, nodeAttributes, newTag);

    }

    private void readLinkTag(Node tagNode) throws MaeDBException {
        TagType tagType = driver.getTagTypeByName(tagNode.getNodeName());
        Map<String, AttributeType> possibleAttTypeMap = getAttributeTypeMap(tagType);
        Map<String, ArgumentType> possibleArgTypeMap = getArgumentTypeMap(tagType);

        NamedNodeMap nodeAttributes = tagNode.getAttributes();

        String tid = nodeAttributes.getNamedItem("id").getNodeValue();
//        nodeAttributes.removeNamedItem("id");

        LinkTag newTag = driver.createLinkTag(tid, tagType);

        addAllAttributes(possibleAttTypeMap, nodeAttributes, newTag);
        addAllArguments(possibleArgTypeMap, nodeAttributes, newTag);

    }

    private void addAllArguments(Map<String, ArgumentType> argTypeMap, NamedNodeMap nodeAttributes, LinkTag tag) throws MaeDBException {
        for (String argName : argTypeMap.keySet()) {
            if (nodeAttributes.getNamedItem(argName + "ID") != null) {
                String attValue = nodeAttributes.getNamedItem(argName + "ID").getNodeValue();
                if (attValue.length() > 0) {
                    ExtentTag argumentTag = (ExtentTag) driver.getTagByTid(attValue);
                    driver.addArgument(tag, argTypeMap.get(argName), argumentTag);
                }
//                nodeAttributes.removeNamedItem(argName + "ID");
//                nodeAttributes.removeNamedItem(argName + "Text");
            }
        }
    }

    private void addAllAttributes(Map<String, AttributeType> attTypeMap, NamedNodeMap nodeAttributes, Tag tag) throws MaeDBException {
        Map<AttributeType, String> attributes = new HashMap<>();
        int attributesCount = 0;
        for (String attName : attTypeMap.keySet()) {
            if (nodeAttributes.getNamedItem(attName) != null) {
                String attValue = nodeAttributes.getNamedItem(attName).getNodeValue();
                if (attValue.length() > 0) {
                    attributes.put(attTypeMap.get(attName), attValue);
                    attributesCount++;
                }
            }
        }
        Set<Attribute> newAttrbutes = driver.addAttributes(tag, attributes);
        if (newAttrbutes.size() != attributesCount) {
            logger.error(String.format("asked for %d attributes to be added, %d were actually added: reverting changes", attributesCount, newAttrbutes.size()));
            for (Attribute att : newAttrbutes) {
                driver.deleteAttribute(tag, att.getAttributeType());
            }
        }
    }

    private Map<String, ArgumentType> getArgumentTypeMap(TagType tagType) throws MaeDBException {
        List<ArgumentType> possibleArgTypes = driver.getArgumentTypesOfLinkTagType(tagType);
        Map<String, ArgumentType> possibleArgTypeMap = new HashMap<>();
        for (ArgumentType type : possibleArgTypes) {
            possibleArgTypeMap.put(type.getName(), type);
        }
        return possibleArgTypeMap;
    }

    private Map<String, AttributeType> getAttributeTypeMap(TagType tagType) throws MaeDBException {
        if (attTypeMap.containsKey(tagType)) {
            return attTypeMap.get(tagType);
        }
        List<AttributeType> possibleAttTypes = driver.getAttributeTypesOfTagType(tagType);
        Map<String, AttributeType> possibleAttTypeMap = new HashMap<>();
        for (AttributeType type : possibleAttTypes) {
            possibleAttTypeMap.put(type.getName(), type);
        }
        attTypeMap.put(tagType, possibleAttTypeMap);
        return possibleAttTypeMap;
    }

    private void readPrimaryText(Node textNode) throws MaeDBException {
        logger.debug("reading primary document... at " + textNode.getUserData("lineNum"));
        primaryText = textNode.getTextContent();
        driver.setPrimaryText(primaryText);

    }

    private static Document xmlInputStreamToDomWithLineNum(InputStream is) throws IOException, ParserConfigurationException, SAXException {
        // adopted from https://eyalsch.wordpress.com/2010/11/30/xml-dom-2/
        final Document doc;
        SAXParser parser;

        SAXParserFactory factory = SAXParserFactory.newInstance();
        parser = factory.newSAXParser();
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        doc = docBuilder.newDocument();

        final Stack<Element> elementStack = new Stack<>();
        final StringBuilder textBuffer = new StringBuilder();
        DefaultHandler handler = new DefaultHandler() {
            private Locator locator;

            @Override
            public void setDocumentLocator(Locator locator) {
                this.locator = locator; //Save the locator, so that it can be used later for line tracking when traversing nodes.
            }

            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                addTextIfNeeded();
                Element el = doc.createElement(qName);
                for (int i = 0;i < attributes.getLength(); i++) {
                    el.setAttribute(attributes.getQName(i), attributes.getValue(i));
                }
                el.setUserData("lineNum", String.valueOf(locator.getLineNumber()), null);
                elementStack.push(el);
            }

            @Override
            public void endElement(String uri, String localName, String qName){
                addTextIfNeeded();
                Element closedEl = elementStack.pop();
                if (elementStack.isEmpty()) { // Is this the root element?
                    doc.appendChild(closedEl);
                } else {
                    Element parentEl = elementStack.peek();
                    parentEl.appendChild(closedEl);
                }
            }

            @Override
            public void characters (char ch[], int start, int length) throws SAXException {
                textBuffer.append(ch, start, length);
            }

            // Outputs text accumulated under the current node
            private void addTextIfNeeded() {
                if (textBuffer.length() > 0) {
                    Element el = elementStack.peek();
                    Node textNode = doc.createTextNode(textBuffer.toString());
                    el.appendChild(textNode);
                    textBuffer.delete(0, textBuffer.length());
                }
            }
        };


        Reader reader = new InputStreamReader(is,"UTF-8");

        InputSource source = new InputSource(reader);
        parser.parse(source, handler);

        return doc;
    }
}
