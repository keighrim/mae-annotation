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

import edu.brandeis.cs.nlp.mae.database.MaeDBException;
import edu.brandeis.cs.nlp.mae.database.MaeDriverI;
import edu.brandeis.cs.nlp.mae.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
        if (file.exists()) {
            Scanner scanner = null;
            try {
                scanner = new Scanner(file);
            } catch (FileNotFoundException ignored) {
                // check if file exists at the beginning
            }
            while (scanner.hasNextLine()) {
                String nextLine = scanner.nextLine().trim();
                if (nextLine.length() > 1) {
                    return nextLine.startsWith("<?xml");
                }
            }
        }
        return false;
    }

    public static boolean isTaskNameMatching(File file, String taskName) throws MaeIOException {

        try {
            MaeXMLParser loader = new MaeXMLParser();
            return loader.isTaskNameMatching(file, taskName);
        } catch (IOException e) {
            catchIOError(file, e);
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
        } catch (SAXException e) {
            catchSAXError(file, e);
        } catch (IOException e) {
            catchIOError(file, e);
        }
        return false;

    }

    public void loadFile(File file) throws MaeIOException,  MaeDBException {
        if (fileName == null) fileName = file.getAbsolutePath();
        if (isXml(file)) {
            if (isTaskNameMatching(file, taskName)) {
                logger.info("reading annotations from file: " + file.getAbsolutePath());
                readAsXml(file);
            } else {
                logger.info("file does not match working DTD, reading as the primary text: " + file.getAbsolutePath());
                readAsTxt(file);
//                throw new MaeIOXMLException("Does not match current DTD");
            }
        } else {
            logger.info("file is not an XML, reading as the primary text: " + file.getAbsolutePath());
            readAsTxt(file);
        }
        insertFilenameToDB(fileName);

    }

    public void readAsXml(File file) throws MaeDBException, MaeIOException {
        try {
            if (fileName == null) fileName = file.getAbsolutePath();
            MaeXMLParser parser = new MaeXMLParser(driver);
            parser.readAnnotationFile(file);
            driver.setPrimaryText(parser.getParsedPrimaryText());
            insertTagsToDB(parser.getParsedTags());
            insertAttsToDB(parser.getParsedAtts());
            insertArgsToDB(parser.getParsedArgs());
        } catch (MaeDBException e) {
            throw e;
        } catch (IOException e) {
            catchIOError(file, e);
        } catch (SAXException e) {
            catchSAXError(file, e);
        }

    }

    private void readAsTxt(File file) throws MaeDBException, MaeIOTXTException {
        try {
            if (fileName == null) fileName = file.getAbsolutePath();
            Scanner scanner = new Scanner(file, "UTF-8");
            String primaryText = scanner.useDelimiter("\\A").next();
            driver.setPrimaryText(primaryText);
            scanner.close(); // Put this call in a finally block
        } catch (NoSuchElementException ex) {
            String message = "failed to read the file, may be a binary file? " + file.getAbsolutePath();
            logger.error(message);
            throw new MaeIOTXTException(message);
        } catch (FileNotFoundException ignored) {
        } catch (MaeDBException e) {
            throw e;
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
                ExtentTag tag = new ExtentTag(parsedTag.getTid(), tagTypeMap.get(parsedTag.getTagTypeName()), this.fileName);
                tag.setText(parsedTag.getText());
                for (CharIndex ci : tag.setSpans(parsedTag.getSpans())) {
                    anchors.add(ci);
                }
                extTagMap.put(parsedTag.getTid(), tag);
            } else {
                LinkTag tag = new LinkTag(parsedTag.getTid(), tagTypeMap.get(parsedTag.getTagTypeName()), this.fileName);
                linkTagMap.put(parsedTag.getTid(), tag);
            }
        }
        driver.batchCreateExtentTags(extTagMap.values());
        driver.batchCreateAnchors(anchors);
        driver.batchCreateLinkTags(linkTagMap.values());
    }

    private void insertAttsToDB(List<ParsedAtt> parsedAtts) throws MaeDBException {
        try {
            List<Attribute> attributes = new ArrayList<>();
            for (ParsedAtt att : parsedAtts) {
                Tag tag = extTagMap.get(att.getTid());
                if (tag == null) tag = linkTagMap.get(att.getTid());
                String attTypeKey = String.format("%s-%s", att.getTagTypeName(), att.getAttTypeName());
                attributes.add(new Attribute(tag, attTypeMap.get(attTypeKey), att.getAttValue()));
            }
            driver.batchCreateAttributes(attributes);
        } catch (MaeModelException e) {
            e.printStackTrace();
        }

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
        String message = "IO error while reading the file: " + file.getName();
        logger.error(message);
        throw new MaeIOException(message, e);
    }

    private static void catchSAXError(File file, SAXException e) throws MaeIOXMLException {
        String message = "failed to parse XML: " + file.getName();
        logger.error(message);
        throw new MaeIOXMLException(message, e);
    }

}
