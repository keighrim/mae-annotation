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
 * Created by krim on 4/24/2016.
 */
public abstract class AbstractCodingAgreementCalc extends AbstractMaeAgreementCalc {

    public AbstractCodingAgreementCalc(AbstractAnnotationIndexer fileIdx, XMLParseCache parseCache) {
        super(fileIdx, parseCache);
    }

    Set<int[]> getSegmentSpansOfTagType(MaeXMLParser[] parses, String tagTypeName) {
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

    Set<int[]> getSegmentSpansOfTagTypes(MaeXMLParser[] parses, Set<String> tagTypeNames) {

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

    Object[] prepareNullCodings() {
        Object[] nullArray = new Object[numAnnotators];
        Arrays.fill(nullArray, UNMARKED_CAT);
        return nullArray;
    }

    public Map<String, CodingAnnotationStudy> prepareLocalCodingStudies(MappedSet<String, String> targetTagsAndAtts) throws IOException, SAXException, MaeException {

        Map<String, String> attFullNameMap = new HashMap<>();
        Map<String, CodingAnnotationStudy> studyPerAtt = new LinkedHashMap<>();
        for (String tagTypeName : targetTagsAndAtts.keyList()) {
            List<String> attTypeNames = targetTagsAndAtts.getAsList(tagTypeName);
            attTypeNames.add(0, SPAN_ATT);
            for (String attTypeName : attTypeNames) {
                String attFullName = tagTypeName + TAG_ATT_DELIM + attTypeName;
                studyPerAtt.put(attFullName, new CodingAnnotationStudy(numAnnotators));
                attFullNameMap.put(attTypeName, attFullName);
            }
            List<String> documents = fileIdx.getDocumentNames();
            for (String document : documents) {
                MaeXMLParser[] parses = getParses(document);
                Set<int[]> relevantSpans = getSegmentSpansOfTagType(parses, tagTypeName);

                for (int[] relevantSpan : relevantSpans) {
                    Map<String, String[]> attAnnotationsMap = prepareAttAnnotationMap(attTypeNames);

                    for (int j = 0; j < parses.length; j++) {
                        MaeXMLParser parse = parses[j];
                        if (parse == null) {
                            fillUnmarkednessOfAnnotator(attAnnotationsMap, j);
                        } else {
                            List<ParsedTag> relevantTags
                                    = getTagsOfTagTypesAndSpans(relevantSpan, Collections.singletonList(tagTypeName), parse);
                            switch (relevantTags.size()) {
                                case 0:
                                    fillUnmarkednessOfAnnotator(attAnnotationsMap, j);
                                    break;
                                case 1:
                                    attAnnotationsMap.get(SPAN_ATT)[j] = Boolean.toString(true);
                                    String tid = relevantTags.get(0).getTid();
                                    fillAllAttValueOfTid(parse, j, tid, attAnnotationsMap);
                                    break;
                                default:
                                    throw new MaeException(
                                            String.format("Error occurred while calculating local labeling agreement:" +
                                                    " an annotator marked the same range with two or labels - \"%s\", \"%s\", \"%d\"",
                                                    document, fileIdx.getAnnotators().get(j), relevantSpan[0]));
                            }
                        }
                    }
                    for (String attTypeName : attAnnotationsMap.keySet()) {
                        String attFullName = attFullNameMap.get(attTypeName);
                        studyPerAtt.get(attFullName).addItemAsArray(attAnnotationsMap.get(attTypeName));
                    }
                }
            }
        }
        return studyPerAtt;
    }

    void fillUnmarkednessOfAnnotator(Map<String, String[]> attAnnotationMap, int annotatorIdx) {
        for (String attTypeName : attAnnotationMap.keySet()) {
            String[] markups = attAnnotationMap.get(attTypeName);
            markups[annotatorIdx] = attTypeName.equals(SPAN_ATT)? Boolean.toString(false) : null;
        }
    }

    Map<String, String[]> prepareAttAnnotationMap(List<String> attTypeNames) {
        Map<String, String[]> attMarkupMap = new HashMap<>();
        attMarkupMap.put(SPAN_ATT, new String[numAnnotators]);
        for (String attTypeName : attTypeNames) {
            attMarkupMap.put(attTypeName, new String[numAnnotators]);
        }
        return attMarkupMap;
    }

    void fillAllAttValueOfTid(MaeXMLParser annotation, int annotatorIdx, String tid, Map<String, String[]> attAnnotationsMap) {
        for (ParsedAtt att : annotation.getParsedAtts()) {
            if (att.getTid().equals(tid) && attAnnotationsMap.containsKey(att.getAttTypeName())) {
                String attTypeName = att.getAttTypeName();
                if (att.getAttValue() != null && att.getAttValue().length() > 0) {
                    attAnnotationsMap.get(attTypeName)[annotatorIdx] = att.getAttValue();
                } else {
                    // TODO: 2016-04-25 15:27:30EDT  unmarked vs empty marked ??
                    attAnnotationsMap.get(attTypeName)[annotatorIdx] = UNMARKED_CAT;
                }
            }
        }
    }

    public CodingAnnotationStudy prepareGlobalCodingStudy(MappedSet<String, String> targetTagsAndAtts) throws IOException, SAXException, MaeException {

        CodingAnnotationStudy study = new CodingAnnotationStudy(numAnnotators);
        List<String> documents = fileIdx.getDocumentNames();
        Set<String> targetTags = targetTagsAndAtts.keySet();

        for (String document : documents) {
            MaeXMLParser[] parses = parseCache.getParses(document);
            Set<int[]> relevantSpans = getSegmentSpansOfTagTypes(parses, targetTags);
            if (relevantSpans.size() == 0) {
                study.addItem(prepareNullCodings());
            } else {
                for (int[] span : relevantSpans) {
                    Object[] annotations = new String[numAnnotators];
                    for (int i = 0; i < parses.length; i++) {
                        MaeXMLParser parse = parses[i];
                        List<ParsedTag> relevantTags = getTagsOfTagTypesAndSpans(span, targetTags, parse);
                        switch (relevantTags.size()) {
                            case 0:
                                annotations[i] = UNMARKED_CAT;
                                break;
                            case 1:
                                annotations[i] = relevantTags.get(0).getTagTypeName();
                                break;
                            default:
                                throw new MaeException(
                                        String.format("Error occurred while calculating global labeling agreement:" +
                                                        " an annotator marked the same range with two or labels - \"%s\", \"%s\", \"%d\"",
                                                document, fileIdx.getAnnotators().get(i), span[0]));
                        }
                    }
                    study.addItem(annotations);
                }
            }
        }
        return study;
    }
}

