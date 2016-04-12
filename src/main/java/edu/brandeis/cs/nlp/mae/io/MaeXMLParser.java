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
import edu.brandeis.cs.nlp.mae.model.ArgumentType;
import edu.brandeis.cs.nlp.mae.model.TagType;
import edu.brandeis.cs.nlp.mae.util.MappedSet;
import edu.brandeis.cs.nlp.mae.util.SpanHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by krim on 4/6/16.
 */
public class MaeXMLParser {
    private static final Logger logger = LoggerFactory.getLogger(MaeXMLParser.class.getName());

    private MaeDriverI driver;
    private MaeSAXHandler xmlHandler;

    public MaeXMLParser() {

    }

    public MaeXMLParser(MaeDriverI driver) throws MaeDBException {
        this.driver = driver;
    }

    public void readAnnotationFile(File file) throws SAXException, IOException  {
        try {
            List<String> extTagTypeNames = new ArrayList<>();
            for (TagType type : driver.getExtentTagTypes()) {
                extTagTypeNames.add(type.getName());
            }
            List<String> linkTagTypeNames = new ArrayList<>();
            for (TagType type : driver.getLinkTagTypes()) {
                linkTagTypeNames.add(type.getName());
            }

            this.xmlHandler = new MaeSAXHandler(extTagTypeNames, linkTagTypeNames);
            parse(file);
        } catch (MaeDBException e) {
            e.printStackTrace();
        }
    }

    private void parse(File file) throws IOException, SAXException  {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(file, xmlHandler);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    public boolean isTaskNameMatching(File file, String taskName) throws IOException, SAXException  {
        this.xmlHandler = new MaeSAXSimpleHandler();
        parse(file);
        return xmlHandler.getTaskName().equals(taskName);
    }

    public boolean isPrimaryTextMatching(File file, String primaryText) throws SAXException, IOException {
        this.xmlHandler = new MaeSAXSimpleHandler();
        parse(file);
        return xmlHandler.getPrimaryText().equals(primaryText);
    }

    public List<ParsedTag> getParsedTags() {
        return xmlHandler.getParsedTags();
    }

    public List<ParsedAtt> getParsedAtts() {
        return xmlHandler.getParsedAtts();
    }

    public List<ParsedArg> getParsedArgs() {
        return xmlHandler.getParsedArgs();
    }

    public String getParsedPrimaryText() {
        return xmlHandler.getPrimaryText();
    }

    private class MaeSAXHandler extends DefaultHandler {
        private List<ParsedTag> tags;
        private List<ParsedAtt> atts;
        private List<ParsedArg> args;
        private boolean hasTextElem = false;
        private boolean hasRootElem = false;
        private String primaryText;
        private String taskName;
        private List<String> extTagTypeNames;
        private List<String> linkTagTypeName;
        private MappedSet<String, String> argTypeMap;

        public MaeSAXHandler() {
            initParsedLists();
        }

        public MaeSAXHandler(List<String> extTagTypeNames, List<String> linkTagTypeName) throws MaeDBException {
            this.extTagTypeNames = extTagTypeNames;
            this.linkTagTypeName = linkTagTypeName;
            this.argTypeMap = new MappedSet<>();
            for (String linkTypeName : linkTagTypeName) {
                for (ArgumentType argType : driver.getArgumentTypesOfLinkTagType(driver.getTagTypeByName(linkTypeName))) {
                    argTypeMap.putItem(linkTypeName, argType.getName());
                }
            }
            initParsedLists();

        }

        private void initParsedLists() {
            this.tags = new ArrayList<>();
            this.atts = new ArrayList<>();
            this.args = new ArrayList<>();
        }

        @Override
        public void startElement(String nsURI, String localName, String qName,
                                 Attributes attributes) throws SAXException {

            if (!hasRootElem) {
                logger.debug("found root node: " + qName);
                if (qName.equalsIgnoreCase("text") || attributes.getLength() > 0) {
                    throw new SAXException("Root node should be the task name");
                } else {
                    setTaskName(qName);
                    hasRootElem = true;
                }
            } else if (qName.equalsIgnoreCase("text")) {
                logger.debug("found text node: " + qName);
                hasTextElem = true;
            } else if (qName.equalsIgnoreCase("tags")) {
            } else {
                parseTag(qName, attributes);
            }
        }

        private void parseTag(String tagTypeName, Attributes attributes) throws SAXException {
            ParsedTag tag = new ParsedTag();
            if (extTagTypeNames.contains(tagTypeName)) {
                logger.debug(String.format("found extent tag: %s(%s)", attributes.getValue("id"), tagTypeName));
                parseExtentTag(tagTypeName, tag, attributes);
            } else if (linkTagTypeName.contains(tagTypeName)) {
                logger.debug(String.format("found link tag: %s(%s)", attributes.getValue("id"), tagTypeName));
                parseLinkTag(tagTypeName, tag, attributes);
            } else {
                throw new SAXException("unexpected tag type found: " + tagTypeName);
            }
        }

        private void parseExtentTag(String tagTypeName, ParsedTag tag, Attributes attributes) throws SAXException {

            tag.setTagTypeName(tagTypeName);
            tag.setLink(false);
            String tempStart = null;
            String tempEnd = null;
            String tid = null;
            for(int i = 0; i < attributes.getLength(); i++){
                String attName = attributes.getQName(i);
                String attValue = attributes.getValue(i);
                if (attName.equalsIgnoreCase("id")) {
                    tag.setTid(attValue);
                    tid = attValue;
                } else if (attName.equalsIgnoreCase("spans")) {
                    try {
                        int[] spans = SpanHandler.convertStringToArray(attValue);
                        tag.setSpans(spans);
                        tag.setText(getSubstringFromPrimaryText(spans));
                    } catch (MaeException e) {
                        throw new SAXException(e.getMessage());
                    }
                } else if (attName.equalsIgnoreCase("start")) {
                    if (tempEnd != null) {
                        int[] spans = convertStartEndToSpansArray(attValue, tempEnd);
                        tag.setSpans(spans);
                        tag.setText(getSubstringFromPrimaryText(spans));
                    } else {
                        tempStart = attValue;
                    }
                } else if (attName.equalsIgnoreCase("end")) {
                    if (tempStart != null) {
                        int[] spans = convertStartEndToSpansArray(tempStart, attValue);
                        tag.setSpans(spans);
                        tag.setText(getSubstringFromPrimaryText(spans));
                    } else {
                        tempEnd = attValue;
                    }
                } else if (attName.equalsIgnoreCase("text")) {
//                tag.setText(value);
                    // to avoid the bug in reading unicode high surrogates,
                    // text fields are directly sliced from primary text that is on memory
                } else {
                    parseAttribute(tagTypeName, tid, attName, attValue);
                }
            }
            tags.add(tag);

        }

        private String getSubstringFromPrimaryText(int[] spans) {
            String text = "";
            if (spans.length > 0) {
                List<int[]> spanPairs = SpanHandler.convertArrayToPairs(spans);
                Iterator<int[]> iter = spanPairs.iterator();

                while (iter.hasNext()) {
                    int[] pair = iter.next();
                    text += primaryText.substring(pair[0], pair[1]);
                    if (iter.hasNext()) {
                        text += MaeStrings.SPANTEXTTRUNC;
                    }
                }
            }
            return text;
        }

        private int[] convertStartEndToSpansArray(String start, String end) {
            int s = Integer.parseInt(start);
            int e = Integer.parseInt(end);
            if (s == -1 && e == -1) {
                return new int[0];
            } else {
                return SpanHandler.range(s, e);
            }
        }

        private void parseLinkTag(String tagTypeName, ParsedTag tag, Attributes attributes) throws SAXException {

            tag.setTagTypeName(tagTypeName);
            tag.setLink(true);
            String tid = null;
            for(int i = 0; i < attributes.getLength(); i++){
                String name = attributes.getQName(i);
                String value = attributes.getValue(i);
                if (name.equalsIgnoreCase("id")) {
                    tag.setTid(value);
                    tid = value;
                } else if (name.endsWith(MaeStrings.ARG_IDCOL_SUF) && value.length() > 0
                        && argTypeMap.get(tagTypeName).contains(name.substring(0, name.length() - MaeStrings.ARG_IDCOL_SUF.length()))) {

                    ParsedArg arg = new ParsedArg();
                    arg.setTid(tid);
                    arg.setTagTypeName(tagTypeName);
                    arg.setArgTypeName(name.substring(0, name.length() - MaeStrings.ARG_IDCOL_SUF.length()));
                    arg.setArgTid(value);
                    args.add(arg);
                } else if (name.endsWith(MaeStrings.ARG_TEXTCOL_SUF)) {
                } else {
                    parseAttribute(tagTypeName, tid, name, value);
                }
            }
            tags.add(tag);

        }

        private void parseAttribute(String tagTypeName, String tid, String name, String value) {
            if (value.length() > 0) {
                ParsedAtt att = new ParsedAtt();
                att.setTid(tid);
                att.setTagTypeName(tagTypeName);
                att.setAttTypeName(name);
                att.setAttValue(value);
                atts.add(att);
            }
        }


        @Override
        public void characters(char[] ch, int start, int length) {
            if (hasTextElem) {
                setPrimaryText(new String(ch, start, length));
                hasTextElem = false;
            }
        }

        public List<ParsedAtt> getParsedAtts() {
            return atts;
        }

        public List<ParsedArg> getParsedArgs() {
            return args;
        }

        public List<ParsedTag> getParsedTags() {
            return tags;
        }

        public String getTaskName() {
            return taskName;
        }

        public void setTaskName(String taskName) {
            this.taskName = taskName;
        }

        public String getPrimaryText(){
            return primaryText;
        }

        public void setPrimaryText(String primaryText) {
            this.primaryText = primaryText;
        }

    }
    public class MaeSAXSimpleHandler extends MaeSAXHandler {

        public MaeSAXSimpleHandler() {
        }

        @Override
        public void startElement(String nsURI, String localName, String qName,
                                 Attributes attributes) throws SAXException {

            if (!super.hasRootElem) {
                if (qName.equalsIgnoreCase("text") || attributes.getLength() > 0) {
                    throw new SAXException("Root node should be the task name");
                } else {
                    setTaskName(qName);
                    super.hasRootElem = true;
                }
            }

            if (qName.equalsIgnoreCase("text")) {
                super.hasTextElem = true;
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            if (super.hasTextElem) {
                setPrimaryText(new String(ch, start, length));
                super.hasTextElem = false;
            }
        }

    }

}
