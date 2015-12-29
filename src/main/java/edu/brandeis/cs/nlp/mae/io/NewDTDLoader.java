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
 * For feedback, reporting bugs, use the project repo on github
 * @see <a href="https://github.com/keighrim/mae-annotation">https://github.com/keighrim/mae-annotation</a>
 */

package edu.brandeis.cs.nlp.mae.io;

import edu.brandeis.cs.nlp.mae.database.DTD;
import edu.brandeis.cs.nlp.mae.database.MaeDBException;
import edu.brandeis.cs.nlp.mae.database.MaeDriverI;
import edu.brandeis.cs.nlp.mae.model.ArgumentType;
import edu.brandeis.cs.nlp.mae.model.AttributeType;
import edu.brandeis.cs.nlp.mae.model.TagType;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides methods for loading a DTD file into a DTD class
 *
 * @author Amber Stubbs, Keigh Rim
 * @see DTD
 */

public class NewDTDLoader {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    private MaeDriverI driver;
    private ArrayList<TagType> loadedTagTypes;
    private HashMap<String, String> prefixes;

    public NewDTDLoader(MaeDriverI driver) throws MaeIODTDException, FileNotFoundException {
        this.driver = driver;
        this.prefixes = new HashMap<>();
        this.loadedTagTypes = new ArrayList<>();
    }

    public void read(File file) throws FileNotFoundException, MaeIODTDException, MaeDBException {
        try {
            logger.info("reading annotation scheme from: " + file.getName());
            driver.setTaskFileName(file.getName());
            this.read(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            String message = "file not found: " + file.getName();
            logger.error(message);
            throw new MaeIODTDException(message, e);
        }
    }

    public void read(String string) throws MaeIODTDException, MaeDBException {
        logger.info("reading annotation scheme from plain JAVA string");
        this.read(IOUtils.toInputStream(string));

    }
    public void read(InputStream stream) throws MaeIODTDException, MaeDBException {
        Scanner sc = new Scanner(stream, "UTF-8");
        int lineNum = 1;
        while (sc.hasNextLine()) {
            String next = sc.nextLine();
            // getting rid of comments
            if (next.contains("<!--")) {
                while (sc.hasNextLine() && !next.contains("-->")) {
                    next = sc.nextLine();
                    lineNum++;
                }
                next = sc.nextLine();
            }

            //then, concatenate lines about a tag into one string
            String element = "";
            if (next.contains("<")) {
                element += next;
                while (sc.hasNextLine() && !next.contains(">")) {
                    next = sc.nextLine();
                    lineNum++;
                    element += next;
                }
            }
            lineNum++;
            // remove some problematic unicode characters before processing
            element = normalizeLine(element);
            process(element, lineNum);
        }
        validateLinkTagTypes();
    }

    private void validateLinkTagTypes() throws MaeDBException {
        for (TagType linktag: driver.getLinkTagTypes()) {
            if (linktag.getArgumentTypes().size() == 0) {
                addDefaultArguments(linktag);
            }
        }
    }

    private void addDefaultArguments(TagType linktag) throws MaeDBException {
        // default arguments are NOT req, but note that args are always IDref
        driver.createArgumentType(linktag, "from");
        driver.createArgumentType(linktag, "to");

    }

    public String normalizeLine(String line) {

        return line.trim()
                .replaceAll("\\s+", " ")
                .replaceAll("[\u201C\u201D]", "\"")
                .replaceAll("[\u2018\u2019]", "'");

    }

    private void process(String element, int lineNum) throws MaeIODTDException, MaeDBException {

        if (element.startsWith("<!ELEMENT")) {
            processTagType(element, lineNum);
        }

        if (element.startsWith("<!ATTLIST")) {
            processAttribute(element, lineNum);
        }

        if (element.startsWith("<!ENTITY")) {
            processMeta(element, lineNum);
        }
    }

    private void processTagType(String element, int lineNum) throws MaeIODTDException, MaeDBException {
        Pattern tTypePattern = Pattern.compile( "<! *ELEMENT +(\\S+) +(\\bEMPTY\\b|\\( *(#\\bPCDATA\\b)\\s*\\)) *>" );
        Matcher tTypeMatcher = tTypePattern.matcher(element);
        if (tTypeMatcher.find()) {
            String name = tTypeMatcher.group(1);
            boolean isLink = tTypeMatcher.group(3) == null || !tTypeMatcher.group(3).equals("#PCDATA");
            String prefix = generatePrefix(name);
            logger.debug(String.format("adding a tag type: %s (%s)", name, prefix));
            loadedTagTypes.add(driver.createTagType(name, prefix, isLink));
        } else {
            this.error(String.format("DTD seems to be ill-formed: %s at %d", element, lineNum));
        }
    }

    private String generatePrefix(String fullname) throws MaeIODTDException {
        int prefixLen = 1;
        String prefix = fullname.substring(0, prefixLen);
        while (prefixes.values().contains(prefix)) {
            if (prefix.length() >= fullname.length()) {
                String message = "duplicate TagType name found: " + fullname;
                logger.error(message);
                throw new MaeIODTDException(message);
            }
            prefixLen++;
            prefix = fullname.substring(0, prefixLen);
        }
        prefixes.put(fullname, prefix);
        return prefix;
    }

    private void processMeta(String element, int lineNum) throws MaeIODTDException, MaeDBException {
        // currently it can only process "internal parsed entities" element of DTD
        Pattern elementPattern = Pattern.compile("<!\\s*ENTITY +(.+) +\"(.+)\">");
        Matcher elementMatcher = elementPattern.matcher(element);
        boolean add;
        add = elementMatcher.matches() && addMetadata(elementMatcher.group(1), elementMatcher.group(2));
        if (!add) {
            this.error(String.format("error while adding a metadata: %s at %d", element, lineNum));
        }
    }

    private boolean addMetadata(String key, String value) throws MaeDBException {
        boolean success;
        switch (key) {
            case "name":
                driver.setTaskName(value);
                logger.debug("adding DTD name: " + value);
                success = true;
                break;
            default:
                logger.debug("unresolved identifier: " + key);
                success = false;
        }
        return success;
    }

    private void processAttribute(String element, int lineNum) throws MaeIODTDException, MaeDBException {
        Pattern attPattern = Pattern.compile( "<! *ATTLIST +(\\S+) +(\\S+) +(\\( *.+ *\\)|\\bCDATA\\b|\\bID\\b|\\bIDREF\\b)? *(prefix=\"(.+)\")? *(#\\bREQUIRED\\b|#\\bIMPLIED\\b)? *(\"(.+)\")?" );
        Matcher attMatcher = attPattern.matcher(element);

        if (attMatcher.find()) {
            String tagTypeName = attMatcher.group(1);
            String attTypeName = attMatcher.group(2);
            String valueset = attMatcher.group(3);
            if (valueset == null) {
                valueset = "CDATA";
            }
            String prefix = attMatcher.group(5);
            boolean required = attMatcher.group(6) != null && attMatcher.group(6).equals("#REQUIRED");
            String defaultValue = attMatcher.group(8);

            TagType tagtype = isTagTypeLoaded(tagTypeName);
            if (tagtype == null) {
                this.error("tag type is not define for an attribute/argument: " + attTypeName);
            } else if (attTypeName.matches("arg[0-9]+")) {
                defineArgument(lineNum, tagtype, attTypeName, valueset, prefix, required, defaultValue);
            } else {
                defineAttribute(lineNum, tagtype, attTypeName, valueset, prefix, required, defaultValue);
            }
        } else {
            this.error(String.format("DTD seems to be ill-formed: \"%s\" at %d", element, lineNum));
        }
    }

    private AttributeType defineAttribute(int lineNum, TagType tagType, String attTypeName, String valueset, String prefix, boolean required, String defaultValue) throws MaeIODTDException, MaeDBException {
        AttributeType type = null;
        switch (valueset) {
            case "ID":
                if (!attTypeName.equals("id")) {
                    this.error("value type \"ID\" should have name \"id\": " + lineNum);
                } else if (prefix != null) {
                    if (prefixes.values().contains(prefix)) {
                        this.error(String.format("prefix \"%s\" is already being used", prefix));
                    }
                    logger.debug(String.format("setting a custom prefix to tag type \"%s\" : %s ",tagType.getName(), attTypeName));
                    driver.setTagTypePrefix(tagType, prefix);
                    prefixes.put(tagType.getName(), prefix);
                }
                break;
            case "IDREF":
                type = addAttributeType(tagType, attTypeName);
                logger.debug("setting as id-referencing attribute: " + attTypeName);
                driver.setAttributeTypeIDRef(type, true);
            case "CDATA":
                if ((attTypeName.equals("spans") || attTypeName.equals("start")) && !required) {
                    logger.debug("setting as non-consuming: " + tagType.getName());
                    driver.setTagTypeNonConsuming(tagType, true);
                } else {
                    type = addAttributeType(tagType, attTypeName);
                }
                break;
            default:
                String[] validValues = valueset.replaceAll("(\\( *| *\\))", "").split(" \\| ");

                if (validValues.length < 2) {
                    this.error(String.format("the set of values should have two or more values: \"%s\" at %d", valueset, lineNum));
                }
                type = addAttributeType(tagType, attTypeName);
                logger.debug(String.format("setting valid value set to \"%s\": %s", attTypeName, Arrays.toString(validValues)));
                driver.setAttributeTypeValueSet(type, Arrays.asList(validValues));
        }
        if (type != null) {
            if (defaultValue != null) {
                if (type.getValuesetAsList().size() == 0 || type.getValuesetAsList().contains(defaultValue)) {
                    logger.debug(String.format("setting default value to \"%s\": %s", attTypeName, defaultValue));
                    driver.setAttributeTypeDefaultValue(type, defaultValue);
                } else {
                    this.error(String.format("Default value \"%s\" is not in the pre-defined value set %s: at %d", defaultValue, type.getValuesetAsList().toString(), lineNum));
                }
            }
            if (required) {
                logger.debug("setting required: " + attTypeName);
                driver.setAttributeTypeRequired(type);
            }
        }
        return type;
    }

    private AttributeType addAttributeType(TagType tagType, String attTypeName) throws MaeDBException {
        logger.debug(String.format("adding a new attribute type attached to \"%s\": %s", tagType.getName(), attTypeName));
        return driver.createAttributeType(tagType, attTypeName);
    }

    private ArgumentType defineArgument(int lineNum, TagType tagType, String attTypeName, String valueset, String prefix, boolean required, String defaultValue) throws MaeIODTDException, MaeDBException {
        ArgumentType type = null;
        if (!tagType.isLink()) {
            this.error(String.format("extent tag \"%s\" can't have an argument \"%s\" at %d", tagType.getName(), attTypeName, lineNum));
        } else if (defaultValue != null) {
            this.error("arguments cannot have a default value: " + lineNum);
        } else if (prefix!=null && !valueset.equals("IDREF")) {
            this.error("argument definition should be set to \"IDREF\": " + lineNum);
        } else if (prefix!=null) {
            type = driver.createArgumentType(tagType, prefix);
        } else {
            type = driver.createArgumentType(tagType, attTypeName);
        }
        if (required && type != null) {
            logger.debug("setting required: " + attTypeName);
            driver.setArgumentTypeRequired(type);
        }
        return type;
    }

    private TagType isTagTypeLoaded(String name) throws MaeDBException {
        for (TagType tagtype : loadedTagTypes) {
            if (tagtype.getName().equals(name)) {
                return driver.getTagTypeByName(name);
            }
        }
        return null;
    }

    private void error(String message) throws MaeIODTDException {
        logger.error(message);
        throw new MaeIODTDException(message);

    }
}
