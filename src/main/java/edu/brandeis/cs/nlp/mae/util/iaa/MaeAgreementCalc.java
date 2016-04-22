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
import java.util.*;
import java.util.stream.IntStream;

/**
 * Created by krim on 4/14/2016.
 */
public class MaeAgreementCalc {

    private String SPAN_ATT = "#@!#!";
    private String TAG_ATT_DELIM = "::";

    private int KAPPA_COEFFICIENT = 0;
    private int ALPHA_COEFFICIENT = 1;
    private int ALPHAU_COEFFICIENT = 2;

    private MaeAnnotationIndexer fileIdx;
    private MaeDriverI driver;
    private Map<String, MaeXMLParser[]> parseCache;
    private int[] documentLength;
    private int totalDocumentsLength;
    private int numAnnotators;

    public MaeAgreementCalc(MaeDriverI driver) {
        this.fileIdx = new MaeAnnotationIndexer();
        this.driver = driver;
        this.parseCache = new HashMap<>();
    }

    public void loadAnnotationFiles(File singleDir) throws MaeIOException, IOException, SAXException, MaeDBException {
        fileIdx.getAnnotationMatrixFromFiles(FileHandler.getAllFileIn(singleDir));
        validateTaskNames(driver.getTaskName());
        validateTextSharing();

        numAnnotators = fileIdx.getAnnotators().size();
        totalDocumentsLength = IntStream.of(documentLength).reduce( 0,(a, b) -> a + b);


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
        documentLength = new int[fileIdx.getDocuments().size()];
        int curDoc = 0;
        for (String docName : fileIdx.getDocuments()) {
            String[] fileNames = fileIdx.getAnnotationsOfDocument(docName);
            int seen = getFirstNonNullIndex(fileNames);
            parser.readAnnotationPreamble(new File(fileNames[seen++]));
            String primaryText = parser.getParsedPrimaryText();
            documentLength[curDoc++] = primaryText.length();
            for (int i = seen; i < fileNames.length; i++) {
                if (fileNames[i] != null && !parser.isPrimaryTextMatching(new File(fileNames[i]), primaryText)) {
                    return false;
                }
            }
        }
        return true;
    }

    private MaeXMLParser[] getOrCacheXMLParse(String docName) throws MaeDBException, IOException, SAXException {
        if (!this.parseCache.containsKey(docName)) {
            parseCache.put(docName, cacheXMLParse(docName));
        }
        return parseCache.get(docName);
    }

    private MaeXMLParser[] cacheXMLParse(String docName) throws MaeDBException, IOException, SAXException {
        String[] xmlFileNames = fileIdx.getAnnotationsOfDocument(docName);
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

    public String agreementsToString(String agreementType, Map<String, Double> agreements) {
        String results = agreementType + "\n\n";
        for (String agreementKey : agreements.keySet()) {
            results += agreementToString(agreementType, agreementKey, agreements.get(agreementKey));
        }
        results += "\n";
        return results;
    }

    public String agreementToString(String agrType, String agrKey, Double agr) {
        return String.format("% .4f (%s) %s\n", agr, agrType, agrKey );
    }

    public Map<String, Double> calculateLocalAlphaU(MappedSet<String, String> targetTagsAndAtts) throws IOException, SAXException, MaeDBException {
        Map<String, Double> alphaUs = new TreeMap<>();

        Map<String, String> attFullNameMap = new HashMap<>();
        for (String tagTypeName : targetTagsAndAtts.keyList()) {
            Map<String, UnitizingAnnotationStudy> studyPerAtt = new LinkedHashMap<>();
            List<String> attTypeNames = targetTagsAndAtts.getAsList(tagTypeName);
            attTypeNames.add(0, SPAN_ATT);
            for (String attTypeName : attTypeNames) {
                String attFullName = tagTypeName + TAG_ATT_DELIM + attTypeName;
                studyPerAtt.put(attFullName, new UnitizingAnnotationStudy(numAnnotators, totalDocumentsLength));
                attFullNameMap.put(attTypeName, attFullName);
            }
            int curDocLength = 0;
            List<String> documents = fileIdx.getDocuments();
            for (int i = 0; i < documents.size(); i++) {
                String document = documents.get(i);
                MaeXMLParser[] parses = getOrCacheXMLParse(document);

                addTagAsUnits(tagTypeName, parses, curDocLength, studyPerAtt.get(attFullNameMap.get(SPAN_ATT)));
                for (int j = 1; j < attTypeNames.size(); j++) {
                    String attTypeName = attTypeNames.get(j);
                    addAttAsUnits(tagTypeName, attTypeName, parses, curDocLength, studyPerAtt.get(attFullNameMap.get(attTypeName)));
                }
                curDocLength += documentLength[i];
            }
            for (String attTypeName : attTypeNames) {
                double agree = (new KrippendorffAlphaUnitizingAgreement(studyPerAtt.get(attFullNameMap.get(attTypeName)))).calculateAgreement();
                alphaUs.put(attFullNameMap.get(attTypeName), agree);
            }
        }
        return alphaUs;
    }

    public Map<String, Double> calculateGlobalAlphaU(List<String> targetTags) throws IOException, SAXException, MaeDBException {
        Map<String, Double> globalAlphaU = new TreeMap<>();

        UnitizingAnnotationStudy study = new UnitizingAnnotationStudy(numAnnotators, totalDocumentsLength);
        int curDocLength = 0;
        List<String> documents = fileIdx.getDocuments();
        for (int i = 0; i < documents.size(); i++) {
            String document = documents.get(i);
            MaeXMLParser[] parses = getOrCacheXMLParse(document);
            for (String tagTypeName : targetTags) {
                addTagAsUnits(tagTypeName, parses, curDocLength, study);
            }
            curDocLength += documentLength[i];
        }
        double agree = (new KrippendorffAlphaUnitizingAgreement(study)).calculateAgreement();
        globalAlphaU.put("global_alpha_u", agree);
        return globalAlphaU;


    }

    private void addTagAsUnits(String tagTypeName, MaeXMLParser[] annotations, int textOffset, UnitizingAnnotationStudy study) {

        int annotator = 0;
        for (MaeXMLParser parse : annotations) {
            if (parse != null) {
                for (ParsedTag tag : parse.getParsedTags()) {
                    if (tag.getTagTypeName().equals(tagTypeName) && tag.getSpans().length > 0) {
                        for (int[] pair : SpanHandler.convertArrayToPairs(tag.getSpans())) {
                            study.addUnit(pair[0] + textOffset, pair[1] - pair[0], annotator, tagTypeName);
                        }
                    }
                }
            }
            annotator++;
        }

    }

    private void addAttAsUnits(String tagTypeName, String attTypeName, MaeXMLParser[] annotations, int textOffset, UnitizingAnnotationStudy study) {

        int annotator = 0;
        for (MaeXMLParser parse : annotations) {
            if (parse != null) {
                for (ParsedTag tag : parse.getParsedTags()) {
                    if (tag.getTagTypeName().equalsIgnoreCase(tagTypeName) && tag.getSpans().length > 0) {
                        for (ParsedAtt att : parse.getParsedAtts()) {
                            if (att.getTagTypeName().equalsIgnoreCase(tag.getTagTypeName()) &&
                                    att.getAttTypeName().equalsIgnoreCase(attTypeName)) {
                                for (int[] pair : SpanHandler.convertArrayToPairs(tag.getSpans())) {
                                    study.addUnit(pair[0] + textOffset, pair[1] - pair[0], annotator, att.getAttValue());
                                }
                            }
                        }
                    }
                }
            }
            annotator++;
        }
    }

    public boolean isIgnoreMissing() {
        // TODO: 2016-04-20 00:06:05EDT implement this as an option, also amend code searchable by 'annotator++'
        return false;
    }
}
