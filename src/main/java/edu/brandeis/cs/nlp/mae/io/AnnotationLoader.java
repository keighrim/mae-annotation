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
import edu.brandeis.cs.nlp.mae.database.MaeDBException;
import edu.brandeis.cs.nlp.mae.database.MaeDriverI;
import edu.brandeis.cs.nlp.mae.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.*;
import java.util.*;

/**
 * Created by krim on 4/6/16.
 */
public class AnnotationLoader {

    private static final Logger logger = LoggerFactory.getLogger(AnnotationLoader.class.getName());

    private MaeDriverI driver;
    private String taskName;
    private String fileName;
    private Map<String, TagType> tagTypeMap = new HashMap<>();
    private Map<String, AttributeType> attTypeMap = new HashMap<>();
    private Map<String, ArgumentType> argTypeMap = new HashMap<>();
    private Map<String, ExtentTag> extTagMap = new HashMap<>();
    private Map<String, LinkTag> linkTagMap = new HashMap<>();
    private List<String> extTidOrder = new LinkedList<>();
    private List<String> linkTidOrder = new LinkedList<>();

    public AnnotationLoader(MaeDriverI driver) throws MaeDBException {
        this.driver = driver;
        this.taskName = driver.getTaskName();

        cacheTagTypeMap();
        cacheAttTypeMap();
        cacheArgTypeMap();

    }

    private void cacheTagTypeMap() throws MaeDBException {
        List<TagType> types = driver.getAllTagTypes();
        for (TagType type : types) {
            tagTypeMap.put(type.getName(), type);
        }
    }

    private void cacheArgTypeMap() throws MaeDBException {
        for (TagType tagType : tagTypeMap.values()) {
            if (tagType.isLink()) {
                for (ArgumentType argType : driver.getArgumentTypesOfLinkTagType(tagType)) {
                    argTypeMap.put(String.format("%s-%s", tagType.getName(), argType.getName()), argType);
                }
            }
        }
    }

    private void cacheAttTypeMap() throws MaeDBException {
        for (TagType tagType : tagTypeMap.values()) {
            for (AttributeType attType : driver.getAttributeTypesOfTagType(tagType)) {
                attTypeMap.put(String.format("%s-%s", tagType.getName(), attType.getName()), attType);
            }

        }
    }


    public static boolean isXml(File file) throws MaeIOException {
        Scanner scanner = null;
        try {
            scanner = new Scanner(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            while (scanner.hasNext()) {
                String nextLine = scanner.nextLine().trim();
                if (nextLine.length() > 1) {
                    return nextLine.startsWith("<?xml");
                }
            }
        } catch (FileNotFoundException e) {
            throw new MaeIOException("file not found", e);
            // checked if file exists at the beginning
        } catch (UnsupportedEncodingException e) {
            throw new MaeIOException(e.getMessage());
        } finally {
            assert scanner != null;
            scanner.close();
        }
        return false;
    }

    public static boolean isTaskNameMatching(File file, String taskName) throws MaeIOException {

        try {
            MaeXMLParser loader = new MaeXMLParser();
            return loader.isTaskNameMatching(file, taskName);
        } catch (IOException e) {
            catchIOError(file, e);
        } catch (SAXParseException e) {
            catchSAXParseError(file, e);
        } catch (SAXException e) {
            catchSAXError(file, e);
        }

        return false;

    }

    public static boolean isPrimaryTextMatching(File file, String primaryText) throws MaeIOException {

        try {
            MaeXMLParser loader = new MaeXMLParser();
            return loader.isPrimaryTextMatching(file, primaryText);
        } catch (IOException e) {
            catchIOError(file, e);
        } catch (SAXParseException e) {
            catchSAXParseError(file, e);
        } catch (SAXException e) {
            catchSAXError(file, e);

        }
        return false;
    }

    public boolean isFileMatchesCurrentWork(File file) throws MaeIOException, MaeDBException {

        try {
            MaeXMLParser loader = new MaeXMLParser();
            String currentTaskName = driver.getTaskName();
            String currentPrimaryText = driver.getPrimaryText();
            return loader.isTaskNameMatching(file, currentTaskName) &&
                    loader.isPrimaryTextMatching(file, currentPrimaryText);

        } catch (MaeDBException e) {
            throw e;
        } catch (SAXParseException e) {
            catchSAXParseError(file, e);
        } catch (SAXException e) {
            catchSAXError(file, e);
        } catch (IOException e) {
            catchIOError(file, e);
        }
        return false;

    }

    public String loadFile(File file) throws MaeException {
        String fileParseWarning = "";
        if (fileName == null) fileName = file.getAbsolutePath();
        if (isXml(file)) {
            if (isTaskNameMatching(file, taskName)) {
                logger.info("reading annotations from file: " + file.getAbsolutePath());
                fileParseWarning = readAsXml(file);
            } else {
                readAsTxt(file);
                String notXmlWarning = "file does not match working DTD, read as the primary text and a new XML file is generated:\n" + fileName;
                logger.info(notXmlWarning);
                fileParseWarning += notXmlWarning;
           }
        } else {
            readAsTxt(file);
            String notXmlWarning = "file is not an XML, read as the primary text and a new XML file generated:\n" + fileName;
            logger.info(notXmlWarning);
            fileParseWarning += notXmlWarning;
        }
        insertFilenameToDB(fileName);
        return fileParseWarning;

    }

    public String readAsXml(File file) throws MaeDBException, MaeIOException {
        try {
            if (fileName == null) fileName = file.getAbsolutePath();
            MaeXMLParser parser = new MaeXMLParser(driver);
            parser.readAnnotationFile(file);
            driver.setPrimaryText(parser.getParsedPrimaryText());
            insertTagsToDB(parser.getParsedTags());
            insertAttsToDB(parser.getParsedAtts());
            insertArgsToDB(parser.getParsedArgs());
            return parser.getParseWarnings();
        } catch (MaeDBException e) {
            throw e;
        } catch (IOException e) {
            catchIOError(file, e);
        } catch (SAXParseException e) {
            catchSAXParseError(file, e);
        } catch (SAXException e) {
            catchSAXError(file, e);
        }
        return "";
    }

    private void readAsTxt(File file) throws MaeException {
        Scanner scanner = null;
        int suffix = 1;
        String filePath = file.getAbsolutePath();
        String fileExt = filePath.substring(filePath.length() - 4, filePath.length());
        String xmlizeBaseName = fileExt.equalsIgnoreCase(".xml") ? filePath.substring(0, filePath.length() - 4) : filePath;

        File xmlized = new File(xmlizeBaseName + ".xml");
        while (xmlized.exists()) {
            String xmlizeName = String.format("%s_%d.xml", xmlizeBaseName, suffix);
            suffix++;
            xmlized = new File(xmlizeName);
        }
        try {
            fileName = xmlized.getAbsolutePath();
            scanner = new Scanner(file, "UTF-8");
            scanner.useDelimiter("\\A");
            String primaryText = "";
            while (scanner.hasNext()) {
                primaryText += scanner.next();
            }
            FileWriter.writeTextToEmptyXML(primaryText, driver.getTaskName(), xmlized);
            try {
                readAsXml(xmlized);
            } catch (MaeIOException e) {
                xmlized.delete();
            }
        } catch (NoSuchElementException ex) {
            String message = "failed to read the file, maybe a binary file? " + file.getAbsolutePath();
            logger.error(message);
            throw new MaeIOTXTException(message);
        } catch (FileNotFoundException ignored) {
        } catch (MaeException e) {
            throw e;
        } finally {
            assert scanner != null;
            scanner.close();
        }


    }


    private void insertFilenameToDB(String annotationFileName) throws MaeDBException {
        driver.setAnnotationFileName(annotationFileName);

    }

    private void removeFilenameFromDB() throws MaeDBException {
        insertFilenameToDB(null);

    }

    private void insertTextToDB(String primaryText) throws MaeDBException {
        driver.setPrimaryText(primaryText);

    }

    private void insertTagsToDB(List<ParsedTag> parsedTags) throws MaeDBException {
        List<CharIndex> anchors = new ArrayList<>();
        for (ParsedTag parsedTag : parsedTags) {
            if (!parsedTag.isLink()) {
                ExtentTag tag = new ExtentTag(parsedTag.getTid(), tagTypeMap.get(parsedTag.getTagTypeName()), fileName);
                tag.setText(parsedTag.getText());
                for (CharIndex ci : tag.setSpans(parsedTag.getSpans())) {
                    anchors.add(ci);
                }
                String tid = parsedTag.getTid();
                extTagMap.put(tid, tag);
                extTidOrder.add(tid);
            } else {
                LinkTag tag = new LinkTag(parsedTag.getTid(), tagTypeMap.get(parsedTag.getTagTypeName()), fileName);
                linkTagMap.put(parsedTag.getTid(), tag);
                linkTidOrder.add(parsedTag.getTid());
            }
        }
        List<ExtentTag> extTagsOrderOfAppearance = new LinkedList<>();
        for (String tid : extTidOrder) {
            extTagsOrderOfAppearance.add(extTagMap.get(tid));
        }
        List<LinkTag> linkTagsOrderOfAppearance = new LinkedList<>();
        for (String tid : linkTidOrder) {
            linkTagsOrderOfAppearance.add(linkTagMap.get(tid));
        }
        driver.batchCreateExtentTags(extTagsOrderOfAppearance);
        driver.batchCreateAnchors(anchors);
        driver.batchCreateLinkTags(linkTagsOrderOfAppearance);
    }

    private void insertAttsToDB(List<ParsedAtt> parsedAtts) throws MaeDBException {
        List<Attribute> attributes = new ArrayList<>();
        for (ParsedAtt att : parsedAtts) {
            Tag tag = extTagMap.get(att.getTid());
            if (tag == null) tag = linkTagMap.get(att.getTid());
            if (tag == null || att.getAttValue() == null || att.getAttValue().length() == 0) {
                continue;
            }
            String attTypeKey = String.format("%s-%s", att.getTagTypeName(), att.getAttTypeName());
            try {
                attributes.add(new Attribute(tag, attTypeMap.get(attTypeKey), att.getAttValue()));
            } catch (MaeModelException ignored) {
                // model exception is thrown when att value is invalid, which is already checked during XML parsing
                // thus here, model exception is ignored.
            }
        }
        driver.batchCreateAttributes(attributes);

    }

    private void insertArgsToDB(List<ParsedArg> parsedArgs) throws MaeDBException {
        List<Argument> arguments = new ArrayList<>();
        for (ParsedArg arg : parsedArgs) {
            LinkTag tag = linkTagMap.get(arg.getTid());
            String argTypeKey = String.format("%s-%s", arg.getTagTypeName(), arg.getArgTypeName());
            arguments.add(new Argument(tag, argTypeMap.get(argTypeKey), extTagMap.get(arg.getArgTid())));
        }
        driver.batchCreateArguments(arguments);
    }

    private static void catchFileNotFoundError(File file, FileNotFoundException e) throws MaeIOException {
        String message = "file not found: " + file.getAbsolutePath();
        logger.error(message);
        throw new MaeIOException(message, e);
    }

    private static void catchIOError(File file, IOException e) throws MaeIOException {
        String message = String.format("IO error while reading the file: %s, %s", file.getName(), e.getMessage());
        logger.error(message);
        throw new MaeIOException(message, e);
    }

    private static void catchSAXError(File file, SAXException e) throws MaeIOXMLException {
        String message = String.format("failed to parse XML: %s, %s", file.getName(), e.getMessage());
        logger.error(message);
        throw new MaeIOXMLException(message, e);
    }

    private static void catchSAXParseError(File file, SAXParseException e) throws MaeIOXMLException {
        String message = String.format("invalid XML string: %s, %s", file.getName(), e.getMessage());
        logger.error(message);
        throw new MaeIOXMLException(message, e);
    }

}
