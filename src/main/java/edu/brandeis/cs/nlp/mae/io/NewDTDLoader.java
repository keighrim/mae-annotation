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
import edu.brandeis.cs.nlp.mae.database.DatabaseDriver;
import edu.brandeis.cs.nlp.mae.model.ArgumentType;
import edu.brandeis.cs.nlp.mae.model.AttributeType;
import edu.brandeis.cs.nlp.mae.model.TagProperty;
import edu.brandeis.cs.nlp.mae.model.TagType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    private DatabaseDriver driver;
    private ArrayList<TagType> loadedTagTypes;
    private ArrayList<String> prefixes;

    public NewDTDLoader(File f, DatabaseDriver driver) throws MaeIODTDException, FileNotFoundException, SQLException {
        this.driver = driver;
        this.prefixes = new ArrayList<>();
        this.loadedTagTypes = new ArrayList<>();
        readFile(f);
    }

    private void readFile(File f) throws MaeIODTDException, FileNotFoundException, SQLException {
        logger.info("loading file: " + f.getName());
        Scanner sc = new Scanner(f, "UTF-8");
        String next = sc.nextLine();
        int i = 1;
        while (sc.hasNextLine()) {
            // getting rid of comments
            if (next.contains("<!--")) {
                while (!next.contains("-->")) {
                    sc.nextLine();
                    i++;
                }
                next = sc.nextLine();
                i++;
            }

            //then, concatenate lines about a tag into one string
            String element = "";
            if (next.contains("<")) {
                element += next;
                while (!next.contains(">")) {
                    next = sc.nextLine();
                    i++;
                    element += next;
                }
            }
            // remove some problematic unicode characters before processing
            element = normalizeLine(element);
            process(element, i);
        }
    }

    public String normalizeLine(String line) {

        return line.trim()
                .replaceAll("\\s+", " ")
                .replaceAll("[\u201C\u201D]", "\"")
                .replaceAll("[\u2018\u2019]", "'");

    }

    private void process(String element, int lineNum) throws MaeIODTDException, SQLException {

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

    private void processTagType(String element, int lineNum) throws MaeIODTDException, SQLException {
        Pattern tTypePattern = Pattern.compile( "<! *ELEMENT +(\\S+) +(EMPTY|\\( *#PCDATA\\s*\\)) *>" );
        Matcher tTypeMatcher = tTypePattern.matcher(element);
//        if (tTypeMatcher.matches()) {
//        while (tTypeMatcher.find()) {
        if (tTypeMatcher.find()) {
            String name = tTypeMatcher.group(1);
            String prefix = generatePrefix(name);
            logger.debug(String.format("adding a tag type: %s (%s)", name, prefix));
            loadedTagTypes.add(driver.createTagType(name, prefix));
        } else {
            this.error(String.format("DTD seems to be ill-formed: %s at %d", element, lineNum));
        }
    }

    private String generatePrefix(String fullname) throws MaeIODTDException {
        int prefixLen = 1;
        String prefix = fullname.substring(0, prefixLen);
        while (prefixes.contains(prefix)) {
            if (prefix.length() >= fullname.length()) {
                String message = "duplicate TagType name found: " + fullname;
                logger.error(message);
                throw new MaeIODTDException(message);
            }
            prefixLen++;
            prefix = fullname.substring(0, prefixLen);
        }
        return prefix;
    }

    private void processMeta(String element, int lineNum) throws MaeIODTDException {
        // currently it can only process "internal parsed entities" element of DTD
        Pattern elementPattern = Pattern.compile("<!\\s*ENTITY +(.+) +\"(.+)\">");
        Matcher elementMatcher = elementPattern.matcher(element);
        boolean add;
        add = elementMatcher.matches() && addMetadata(elementMatcher.group(1), elementMatcher.group(2));
        if (!add) {
            this.error(String.format("error while adding a metadata: %s at %d", element, lineNum));
        }
    }

    private boolean addMetadata(String key, String value) {
        boolean success;
        switch (key) {
            case "name":
                driver.setDtdName(value);
                logger.debug("adding DTD name: " + value);
                success = true;
                break;
            default:
                logger.debug("unresolved identifier: " + key);
                success = false;
        }
        return success;
    }

    private void processAttribute(String element, int lineNum) throws MaeIODTDException, SQLException {
        Pattern attPattern = Pattern.compile( "<! *ATTLIST +(\\S+) +(\\S+) +(\\( *.+ *\\)|CDATA|ID|IDREF +)?(prefix=\"(.+)\" *)?(#REQUIRED|#IMPLIED)? *(\"(.+)\")?" );
        Matcher attMatcher = attPattern.matcher(element);

        List<String> allMatches = new ArrayList<>();

        TagProperty type;

        if (attMatcher.matches()) {
            // TODO 151226 this way or .group(n) ?
            while (attMatcher.find()) {
                allMatches.add(attMatcher.group());
            }
            String tagTypeName = allMatches.get(1);
            String attTypeName = allMatches.get(2);
            String valueset = allMatches.get(3);
            if (valueset == null) {
                valueset = "CDATA";
            }
            String prefix = allMatches.get(5);
            boolean required = allMatches.get(6).equals("#REQUIRED");
            String defaultValue = allMatches.get(8);

            TagType tagtype = isTagTypeLoaded(tagTypeName);
            if (tagtype == null) {
                this.error("tag type is not define for an attribute/argument: " + attTypeName);
            } else if (attTypeName.matches("arg[0-9]+")) {
                type = defineArgument(lineNum, tagtype, attTypeName, valueset, prefix, required, defaultValue);
            } else {
                type = defineAttribute(lineNum, tagtype, attTypeName, valueset, prefix, required, defaultValue);
            }
        } else {
            this.error(String.format("DTD seems to be ill-formed: %s at %d", element, lineNum));
        }
    }

    private AttributeType defineAttribute(int lineNum, TagType tagType, String attTypeName, String valueset, String prefix, boolean required, String defaultValue) throws MaeIODTDException, SQLException {
        AttributeType type = null;
        switch (valueset) {
            case "ID":
                if (!attTypeName.equals("id")) {
                    this.error("value type \"ID\" should have name \"id\": " + lineNum);
                } else if (prefix != null) {
                    if (prefixes.contains(prefix)) {
                        this.error(String.format("prefix \"%s\" is already being used", prefix));
                    }
                    logger.debug(String.format("setting a custom prefix to tag type \"%s\" : %s ",tagType.getName(), attTypeName));
                    driver.setTagTypePrefix(tagType, prefix);
                }
                break;
            case "IDREF":
                type = addAttributeType(tagType, attTypeName);
                logger.debug("setting as non-consuming: " + attTypeName);
                driver.setAttributeIDRef(type, true);
            case "CDATA":
                if ((attTypeName.equals("spans") || attTypeName.equals("start")) && !required) {
                    logger.debug("setting as non-consuming: " + attTypeName);
                    driver.setTagTypeNonConsuming(tagType, false);
                } else {
                    type = addAttributeType(tagType, attTypeName);
                }
                break;
            default:
                String[] validValues = valueset.split("\\s*|\\s*");
                if (validValues.length < 2) {
                    this.error(String.format("the set of values should have two or more values: %s at %d", valueset, lineNum));
                }
                type = addAttributeType(tagType, attTypeName);
                logger.debug(String.format("setting valid value set to %s: %s", attTypeName, Arrays.toString(validValues)));
                driver.setAttributeValueSet(type, validValues);
        }
        if (type != null) {
            if (defaultValue != null) {
                logger.debug(String.format("setting default value to %s: %s", attTypeName, defaultValue));
                driver.setAttributeDefaultValue(type, defaultValue);
            }
            if (required) {
                logger.debug("setting required: " + attTypeName);
                driver.setAttributeRequired(type);
            }
        }
        return type;
    }

    private AttributeType addAttributeType(TagType tagType, String attTypeName) throws SQLException {
        logger.debug("adding a new attribute type to DB: " + attTypeName);
        return driver.createAttributeType(tagType, attTypeName);
    }

    private ArgumentType defineArgument(int lineNum, TagType tagType, String attTypeName, String valueset, String prefix, boolean required, String defaultValue) throws MaeIODTDException, SQLException {
        ArgumentType type = null;
        // TODO 151226 change isLink(), store boolean in the tagtype object, this is for checking null-argument linktag, later to create default binary from-to arguments
        if (!tagType.isLink()) {
            this.error(String.format("extent tag \"%s\" can't have an argument %s: %d", tagType.getName(), attTypeName, lineNum));
        } else if (defaultValue != null) {
            this.error("argument can have a default value: " + lineNum);
        } else if (prefix!=null && !valueset.equals("IDREF")) {
            this.error("argument definition should be set to \"IDREF\": " + lineNum);
        } else if (prefix!=null) {
            type = driver.createArgumentType(tagType, prefix);
        } else {
            type = driver.createArgumentType(tagType, attTypeName);
        }
        if (required && type != null) {
            logger.debug("setting required: " + attTypeName);
            driver.setArgumentRequired(type);
        }
        return type;
    }

    private TagType isTagTypeLoaded(String name) {
        for (TagType tagtype : loadedTagTypes) {
            if (tagtype.getName().equals(name)) {
                return tagtype;
            }
        }
        return null;
    }

    private void error(String message) throws MaeIODTDException {
        logger.error(message);
        throw new MaeIODTDException(message);

    }
}
