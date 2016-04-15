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

package edu.brandeis.cs.nlp.mae.util.iaa;

import edu.brandeis.cs.nlp.mae.database.MaeDBException;
import edu.brandeis.cs.nlp.mae.database.MaeDriverI;
import edu.brandeis.cs.nlp.mae.io.MaeIOException;
import edu.brandeis.cs.nlp.mae.io.MaeXMLParser;
import edu.brandeis.cs.nlp.mae.io.ParsedAtt;
import edu.brandeis.cs.nlp.mae.io.ParsedTag;
import edu.brandeis.cs.nlp.mae.util.FileHandler;
import edu.brandeis.cs.nlp.mae.util.MappedSet;
import edu.brandeis.cs.nlp.mae.util.SpanHandler;
import org.dkpro.statistics.agreement.unitizing.KrippendorffAlphaUnitizingAgreement;
import org.dkpro.statistics.agreement.unitizing.UnitizingAnnotationStudy;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by krim on 4/14/2016.
 */
public class MaeAgreementCalc {

    private static String SPAN_ATT = "#@!#!";

    private MaeAnnotationIndexer fileIdx;
    private MaeDriverI driver;

    public MaeAgreementCalc(MaeDriverI driver) {
        this.fileIdx = new MaeAnnotationIndexer();
        this.driver = driver;
    }

    public void loadAnnotationFiles(File singleDir) throws MaeIOException {
        fileIdx.getAnnotationMatrixFromFiles(FileHandler.getAllFileIn(singleDir));
    }

    public boolean validateTaskNames(String taskName) throws IOException, SAXException {
        MaeXMLParser parser = new MaeXMLParser();
        for (String docName : fileIdx.getDocuments()) {
            for (String fileName : fileIdx.getAnnotationsOfDocument(docName)) {
                if (fileName != null && !parser.isTaskNameMatching(new File(fileName), taskName)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean validateTextSharing() throws IOException, SAXException {
        MaeXMLParser parser = new MaeXMLParser();
        for (String docName : fileIdx.getDocuments()) {
            String[] fileNames = fileIdx.getAnnotationsOfDocument(docName);
            int seen = 0;
            while (seen < fileNames.length && fileNames[seen] == null) {
                seen++;
            }
            parser.readAnnotationPreamble(new File(fileNames[seen++]));
            String primaryText = parser.getParsedPrimaryText();
            for (int i = seen; i < fileNames.length; i++) {
                if (fileNames[i] != null && !parser.isPrimaryTextMatching(new File(fileNames[i]), primaryText)) {
                    return false;
                }
            }
        }
        return true;
    }

    private MaeXMLParser[] cacheXMLParse(String[] xmlFileNames) throws MaeDBException, IOException, SAXException {
        MaeXMLParser[] parses = new MaeXMLParser[xmlFileNames.length];
        for (int i = 0; i < xmlFileNames.length; i++) {
            String fileName = xmlFileNames[i];
            if (fileName != null) {
                MaeXMLParser parser = new MaeXMLParser(driver);
                parser.readAnnotationFile(new File(fileName));
                parses[i] = parser;
            }
        }
        return parses;
    }

    private static int countNonNull(Object[] array) {
        int countNonNull = 0;
        for (Object obj : array) {
            if (obj != null) {
                countNonNull++;
            }
        }
        return countNonNull;
    }

    private static int getFirstNonNullIndex(Object[] array) {
        int seen = 0;
        while (seen < array.length && array[seen] == null) {
            seen++;
        }
        return seen;
    }

    public Map<String, Map<String, Map<String, Double>>> calcTagSpanAgreement(MappedSet<String, String> targetTagsAndAtts) throws IOException, SAXException, MaeDBException {
        Map<String, Map<String, Map<String, Double>>> agrPerDoc = new LinkedHashMap<>();
        for (String fileName : fileIdx.getDocuments()) {

            Map<String, Map<String, Double>> agrPerTag = new LinkedHashMap<>();

            MaeXMLParser[] parses = cacheXMLParse(fileIdx.getAnnotationsOfDocument(fileName));
            String primaryText = parses[getFirstNonNullIndex(parses)].getParsedPrimaryText();
            int countAnnotators = isIgnoreMissing() ? countNonNull(parses) : parses.length;

            List<String> tagsTypeNames = targetTagsAndAtts.keyList();
            for (String tagTypeName : tagsTypeNames) {
                Map<String, Double> agrPerAtt = new LinkedHashMap<>();
                List<String> attTypeNames = targetTagsAndAtts.getAsList(tagTypeName);

                attTypeNames.add(0, SPAN_ATT);

                double[] iaas = new double[attTypeNames.size()];
                agrPerAtt.put(SPAN_ATT, computeSpanAgreement(tagTypeName, parses, new UnitizingAnnotationStudy(countAnnotators, primaryText.length())));
                for (int i = 1; i < iaas.length; i++) {
                    String attTypeName = attTypeNames.get(i);
                    UnitizingAnnotationStudy study = new UnitizingAnnotationStudy(countAnnotators, primaryText.length());
                    agrPerAtt.put(attTypeName, computeAttAgreement(tagTypeName, attTypeName, parses, study));
                }
                agrPerTag.put(tagTypeName, agrPerAtt);
            }
            agrPerDoc.put(fileName, agrPerTag);
        }
        return agrPerDoc;
    }

    private double computeSpanAgreement(String tagTypeName, MaeXMLParser[] annotations, UnitizingAnnotationStudy study) {

        int annotator = 0;
        for (int i = 0; i < annotations.length; i++) {
            MaeXMLParser parse = annotations[i];
            if (parse == null) {
                continue;
            }
            for (ParsedTag tag : parse.getParsedTags()) {
                if (tag.getTagTypeName().equals(tagTypeName) && tag.getSpans().length > 0) {
                    for (int[] pair : SpanHandler.convertArrayToPairs(tag.getSpans())) {
                        study.addUnit(pair[0], pair[1] - pair[0], annotator, tagTypeName);
                    }
                }
            }
            annotator++;
        }
        return (new KrippendorffAlphaUnitizingAgreement(study)).calculateAgreement();
    }

    private double computeAttAgreement(String tagTypeName, String attTypeName, MaeXMLParser[] annotations, UnitizingAnnotationStudy study) {

        int annotator = 0;
        for (int i = 0; i < annotations.length; i++) {
            MaeXMLParser parse = annotations[i];
            if (parse == null) {
                continue;
            }
            for (ParsedTag tag : parse.getParsedTags()) {
                if (tag.getTagTypeName().equalsIgnoreCase(tagTypeName) && tag.getSpans().length > 0) {
                    for (ParsedAtt att : parse.getParsedAtts()) {
                        if (att.getTagTypeName().equalsIgnoreCase(tag.getTagTypeName()) &&
                                att.getAttTypeName().equalsIgnoreCase(attTypeName)) {
                            for (int[] pair : SpanHandler.convertArrayToPairs(tag.getSpans())) {
                                study.addUnit(pair[0], pair[1] - pair[0], annotator, att.getAttValue());
                            }
                        }
                    }
                }
            }
            annotator++;
        }
        return (new KrippendorffAlphaUnitizingAgreement(study)).calculateAgreement();
    }


    public boolean isIgnoreMissing() {
        return true;
    }
}
