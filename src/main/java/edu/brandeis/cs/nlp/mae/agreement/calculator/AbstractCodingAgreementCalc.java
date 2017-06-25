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

package edu.brandeis.cs.nlp.mae.agreement.calculator;

import edu.brandeis.cs.nlp.mae.MaeException;
import edu.brandeis.cs.nlp.mae.agreement.io.AbstractAnnotationIndexer;
import edu.brandeis.cs.nlp.mae.agreement.io.XMLParseCache;
import edu.brandeis.cs.nlp.mae.io.MaeXMLParser;
import edu.brandeis.cs.nlp.mae.io.ParsedAtt;
import edu.brandeis.cs.nlp.mae.io.ParsedTag;
import edu.brandeis.cs.nlp.mae.util.MappedSet;
import edu.brandeis.cs.nlp.mae.util.SortedIntArrayComparator;
import org.dkpro.statistics.agreement.coding.CodingAnnotationStudy;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static edu.brandeis.cs.nlp.mae.agreement.MaeAgreementStrings.*;

/**
 * Abstract superclass for AgreementCalc classes for coding (labeling) task.
 * Holds common helper methods.
 */
public abstract class AbstractCodingAgreementCalc extends AbstractMaeAgreementCalc {

    public AbstractCodingAgreementCalc(AbstractAnnotationIndexer fileIdx, XMLParseCache parseCache) {
        super(fileIdx, parseCache);
    }

    /**
     * Given an array of XML parses and a single tag name, return a set of
     * spans of relevant tags from the XML parses. Note that the final list does
     * not know about different documents in the parses. All instances of tags from
     * all documents are collapsed.
     * @param parses parsed annotation XML files
     * @param tagTypeName the name of the extent tag of interest
     * @return a set of spans of relevant tags
     */
    private Set<int[]> getSpansOfTagType(MaeXMLParser[] parses, String tagTypeName) {
        TreeSet<int[]> spans = new TreeSet<>(new SortedIntArrayComparator());

        Arrays.stream(parses).filter(
                parse -> parse != null
        ).forEach(
                parse -> parse.getParsedTags().stream().filter(
                        tag -> tagTypeName.equals(tag.getTagTypeName())
                ).forEach(
                        tag -> spans.add(tag.getSpans())
                ));
        return spans;
    }

    /**
     * Given an array of XML parses and a set of tag names, return a set of
     * spans of relevant tags from the XML parses. Note that the final list does
     * not know about different documents in the parses. All instances of tags from
     * all documents are collapsed.
     * @param parses parsed annotation XML files
     * @param tagTypeNames all target extent tag names
     * @return a set of spans of relevant tags
     */
    private Set<int[]> getSpansOfTagTypes(MaeXMLParser[] parses, Set<String> tagTypeNames) {

        TreeSet<int[]> spans = new TreeSet<>(new SortedIntArrayComparator());

        Arrays.stream(parses).filter(
                parse -> parse != null
        ).forEach(
                parse -> parse.getParsedTags().stream().filter(
                        tag -> tagTypeNames.contains(tag.getTagTypeName())
                ).forEach(
                        tag -> spans.add(tag.getSpans())
                ));
        return spans;
    }

    List<ParsedTag> getTagsOfTagTypesAndSpans(int[] spans, Collection<String> tagTypeNames, MaeXMLParser parse) {
        if (parse != null) {
            return parse.getParsedTags().stream().filter(
                    tag -> tagTypeNames.contains(tag.getTagTypeName()) && Arrays.equals(tag.getSpans(), spans)
            ).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    /**
     *
     * @param targetTagsAndAtts a map of [name of a selected tag --> its selected attribute names]
     * @return
     * @throws IOException
     * @throws SAXException
     * @throws MaeException
     */
    public Map<String, CodingAnnotationStudy> prepareLocalCodingStudies(MappedSet<String, String> targetTagsAndAtts) throws IOException, SAXException, MaeException {

        // These two are reused at every tag, thus attribute name conflicts won't happen.
        // maps [name of attribute name --> its "full" name (tag name + att name concatenated)]
        Map<String, String> attFullNameMap;
        // maps [bare attrib name --> its value]
        Map<String, String[]> attValueMap;

        // maps [att "full" name --> behind-the-hood data structure for IAA]
        Map<String, CodingAnnotationStudy> attToStudyMap = new LinkedHashMap<>();

        for (String tagType : targetTagsAndAtts.keyList()) {
            List<String> attTypes = targetTagsAndAtts.getAsList(tagType);
            attTypes.add(0, SPAN_ATT);

            attFullNameMap = new HashMap<>();
            // attTypes already has SPAN_ATT item at this point
            attValueMap = new HashMap<>();

            // add dummy item representing the tag itself
            // convert each att names to their "full" names and initiate bookkeepers
            for (String attType : attTypes) {
                String attFull = tagType + TAG_ATT_DELIM + attType;
                attToStudyMap.put(attFull, new CodingAnnotationStudy(numAnnotators));
                attFullNameMap.put(attType, attFull);
                attValueMap.put(attType, new String[numAnnotators]);
            }

            for (String document : fileIdx.getDocumentNames()) {

                MaeXMLParser[] parses = parseCache.getParses(document);
                Set<int[]> relevantSpans = getSpansOfTagType(parses, tagType);

                // will treat each span of a tag type that we found from the data set as a single annotation item
                for (int[] span : relevantSpans) {

                    // for each span, this will populate the att-value array
                    for (int i = 0; i < parses.length; i++) {
                        MaeXMLParser parse = parses[i];
                        List<ParsedTag> relevantTags
                                = getTagsOfTagTypesAndSpans(span, Collections.singletonList(tagType), parse);
                        // when no tags are found or parse is null
                        if (relevantTags.size() == 0) {
                            for (String attName : attValueMap.keySet()) {
                                // Why do we use "null" for not found attributes?
                                // (Note that not all coding measures can handle null annotation.
                                // e.g. FleissKapps can't, and ignore all null values - resulting in higher agreement)
                                // The reason for using null value is to ensure the use can
                                // see the agreements between annotators that actually tagged at this span.
                                attValueMap.get(attName)[i] = attName.equals(SPAN_ATT) ? Boolean.toString(false) : null;
                            }
                        } else if (relevantTags.size() == 1){
                            attValueMap.get(SPAN_ATT)[i] = Boolean.toString(true);
                            String tid = relevantTags.get(0).getTid();
                            fillAllAttValueOfTid(parse, i, tid, attValueMap);

                        } else {
                            int errorLocation = span.length == 0 ? -1 : span[0];
                            StringBuilder errorBuilder = new StringBuilder("Error: an annotator marked the same range with two or labels - ");
                            errorBuilder.append(String.format("Document: \"%s\", Annotator: \"%s\", Offset: \"%d\"", document, fileIdx.getApprovedAnnotators().get(i), errorLocation));
                            relevantTags.forEach(tag -> errorBuilder.append(String.format("<%s> ", tag.getTagTypeName())));
                            throw new MaeException(errorBuilder.toString());
                        }
                    }

                    for (String attTypeName : attValueMap.keySet()) {
                        String attFullName = attFullNameMap.get(attTypeName);
                        attToStudyMap.get(attFullName).addItemAsArray(attValueMap.get(attTypeName));
                    }
                }
            }
        }
        // make sure all studies have enough labels used.
        for (String attFullName : attToStudyMap.keySet()) {
            CodingAnnotationStudy study = attToStudyMap.get(attFullName);
            if (study.getCategoryCount() < 2) {
                StringBuilder errorBuilder = new StringBuilder();
                errorBuilder.append(String.format("Error: \"%s\" has too few categories: ", attFullName));
                study.getCategories().forEach(cat -> errorBuilder.append(String.format("<%s> ", cat)));
                errorBuilder.append("\n100% agreement can cause this error, as the program couldn't find any other category.");
                throw new MaeException(errorBuilder.toString());
            }
        }
        return attToStudyMap;
    }

    void fillAllAttValueOfTid(MaeXMLParser annotation, int annotatorIdx, String tid, Map<String, String[]> attAnnotationsMap) {
        for (ParsedAtt att : annotation.getParsedAtts()) {
            if (att.getTid().equals(tid) && attAnnotationsMap.containsKey(att.getAttTypeName())) {
                String attTypeName = att.getAttTypeName();
                if (att.getAttValue() != null && att.getAttValue().length() > 0) {
                    attAnnotationsMap.get(attTypeName)[annotatorIdx] = att.getAttValue();
                } else {
                    // using UNMARKED value ensures this attribute to be included in the calculation, as opposed to null (e.g. FleissKapps will ignore null values, which results in higher agreement)
                    attAnnotationsMap.get(attTypeName)[annotatorIdx] = UNMARKED_CAT;
                }
            }
        }
    }

    public CodingAnnotationStudy prepareGlobalCodingStudy(MappedSet<String, String> targetTagsAndAtts) throws IOException, SAXException, MaeException {

        // Note that in global (cross-tag) level calculation, all tag names (keys
        // of tTAA var) are used as a set of labels and no attributes are included
        // (values of tTAA var is completely irrelevant).
        // so we only need one "study" for all tags.
        Set<String> targetTags = targetTagsAndAtts.keySet();
        CodingAnnotationStudy study = new CodingAnnotationStudy(numAnnotators);

        for (String document : fileIdx.getDocumentNames()) {
            MaeXMLParser[] parses = parseCache.getParses(document);
            Set<int[]> relevantSpans = getSpansOfTagTypes(parses, targetTags);
            if (relevantSpans.size() == 0) {
                Object[] unmarkedArray = new Object[numAnnotators];
                Arrays.fill(unmarkedArray, UNMARKED_CAT);
                study.addItem(unmarkedArray);
                continue;
            }
            for (int[] span : relevantSpans) {
                Object[] annotations = new String[numAnnotators];
                for (int i = 0; i < parses.length; i++) {
                    // we pass targetTags as a whole set since all tags on
                    // the global level are treated as a set of labels
                    List<ParsedTag> relevantTags = getTagsOfTagTypesAndSpans(span, targetTags, parses[i]);
                    if (relevantTags.size() == 0) {
                        annotations[i] = UNMARKED_CAT;
                    } else if (relevantTags.size() == 1) {
                        annotations[i] = relevantTags.get(0).getTagTypeName();
                    } else {
                        int errorLocation = span.length == 0 ? -1 : span[0];
                        StringBuilder errorBuilder = new StringBuilder("Error: an annotator marked the same range with two or labels: \n");
                        errorBuilder.append(String.format("Document: \"%s\", Annotator: \"%s\", Offset: \"%d\"", document, fileIdx.getApprovedAnnotators().get(i), errorLocation));
                        relevantTags.forEach(tag -> errorBuilder.append(String.format("<%s> ", tag.getTagTypeName())));
                        throw new MaeException(errorBuilder.toString());
                    }
                }
                study.addItem(annotations);
            }
        }
        // do not worry about the study having only one category, as we forced
        // null annotations to be "UNMARKED_CAT"
        return study;
    }
}

