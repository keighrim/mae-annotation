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

package edu.brandeis.cs.nlp.mae.agreement;

import edu.brandeis.cs.nlp.mae.MaeException;
import edu.brandeis.cs.nlp.mae.agreement.calculator.*;
import edu.brandeis.cs.nlp.mae.agreement.io.AbstractAnnotationIndexer;
import edu.brandeis.cs.nlp.mae.agreement.io.AnnotationDirsIndexer;
import edu.brandeis.cs.nlp.mae.agreement.io.AnnotationFilesIndexer;
import edu.brandeis.cs.nlp.mae.agreement.io.XMLParseCache;
import edu.brandeis.cs.nlp.mae.database.MaeDBException;
import edu.brandeis.cs.nlp.mae.database.MaeDriverI;
import edu.brandeis.cs.nlp.mae.io.MaeIOException;
import edu.brandeis.cs.nlp.mae.io.MaeIOXMLException;
import edu.brandeis.cs.nlp.mae.io.MaeXMLParser;
import edu.brandeis.cs.nlp.mae.util.FileHandler;
import edu.brandeis.cs.nlp.mae.util.MappedSet;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static edu.brandeis.cs.nlp.mae.agreement.MaeAgreementStrings.*;

/**
 * Created by krim on 4/14/2016.
 */
public class MaeAgreementMain {

    private final String SUCCESS = "%!$@#%!$%!";

    private AbstractAnnotationIndexer fileIdx;
    private MaeDriverI driver;
    private XMLParseCache parseCache;
    private int[] documentLength;

    public MaeAgreementMain(MaeDriverI driver) {
        this.driver = driver;
    }

    public void indexDataset(File datasetDir) throws MaeIOException {
         if (!FileHandler.containsDirsOnly(datasetDir)) {
            fileIdx = new AnnotationFilesIndexer();
            fileIdx.indexAnnotations(new File[]{datasetDir});
        } else {
            fileIdx = new AnnotationDirsIndexer();
            fileIdx.indexAnnotations(datasetDir.listFiles());
        }
    }

    public List<String> getAnnotators() {
        return fileIdx.getAnnotators();
    }

    public void ignoreAnnotator(String annotatorId) {
        fileIdx.ignoreAnnotator(annotatorId);
    }

    public void approveAnnotator(String annotatorId) {
        fileIdx.approveAnnotator(annotatorId);
    }

    public void loadXmlFiles() throws MaeIOException, IOException, SAXException, MaeDBException {

        String invalidTaskNameFile = validateTaskNames(driver.getTaskName());
        String invalidPrimaryTextFile = validateTextSharing();
        if (!invalidTaskNameFile.equals(SUCCESS)) {
            throw new MaeIOException("XML annotated with different DTD name: " + invalidTaskNameFile);
        }
        if (!invalidPrimaryTextFile.equals(SUCCESS)) {
            throw new MaeIOException("XML file has different primary text: " + invalidPrimaryTextFile);
        }
        parseCache = new XMLParseCache(driver, fileIdx);
    }

    public Map<String, String> getParseWarnings() {
        return parseCache.getParseWarnings();
    }

    String validateTaskNames(String taskName) throws IOException, SAXException, MaeIOXMLException {
        MaeXMLParser parser = new MaeXMLParser();
        for (String docName : fileIdx.getDocumentNames()) {
            for (String fileName : fileIdx.getAnnotationsOfDocument(docName)) {
                try {
                    if (fileName != null && !parser.isTaskNameMatching(new File(fileName), taskName)) {
                        return fileName;
                    }
                } catch (SAXParseException e) {
                    throw new MaeIOXMLException(String.format("Invalid XML string (%s): %s", e.getMessage(), fileName));
                }
            }
        }
        return SUCCESS;
    }

    String validateTextSharing() throws IOException, SAXException, MaeIOXMLException {
        MaeXMLParser parser = new MaeXMLParser();
        documentLength = new int[fileIdx.getDocumentNames().size()];
        int curDoc = 0;
        for (String docName : fileIdx.getDocumentNames()) {
            String[] fileNames = fileIdx.getAnnotationsOfDocument(docName);
            int seen = getFirstNonNullIndex(fileNames);
            parser.readAnnotationPreamble(new File(fileNames[seen++]));
            String primaryText = parser.getParsedPrimaryText();
            documentLength[curDoc++] = primaryText.length();
            for (int i = seen; i < fileNames.length; i++) {
                try {
                    if (fileNames[i] != null && !parser.isPrimaryTextMatching(new File(fileNames[i]), primaryText)) {
                        return fileNames[i];
                    }
                } catch (SAXParseException e) {
                    throw new MaeIOXMLException(String.format("Invalid XML string (%s): %s", e.getMessage(), fileNames[i]));
                }
            }
        }
        return SUCCESS;
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
        StringBuilder results = new StringBuilder(String.format("== %s ==\n\n", agreementType));
        for (String agreementKey : agreements.keySet()) {
            results.append(agreementToString(agreementType, agreementKey, agreements.get(agreementKey)));
        }
        results.append("\n");
        return results.toString();
    }

    public String agreementToString(String agrType, String agrKey, Double agr) {
        return String.format("% .4f (%s) %s\n", agr, agrType, agrKey );
    }

    Map<String, Double> calculateLocalAlphaU(MappedSet<String, String> targetTagsAndAtts) throws IOException, SAXException, MaeDBException {
        LocalAlphaUCalc calc = new LocalAlphaUCalc(fileIdx, parseCache, documentLength);
        return calc.calculateAgreement(targetTagsAndAtts);
    }

    Map<String, Double> calculateGlobalAlphaU(MappedSet<String, String> targetTagsAndAtts) throws IOException, SAXException, MaeDBException {
        GlobalAlphaUCalc calc = new GlobalAlphaUCalc(fileIdx, parseCache, documentLength);
        return calc.calculateAgreement(targetTagsAndAtts);
    }

    Map<String, Double> calculateLocalMultiPi(MappedSet<String, String> targetTagsAndAtts) throws IOException, SAXException, MaeException {
        LocalMultiPiCalc calc = new LocalMultiPiCalc(fileIdx, parseCache);
        return calc.calculateAgreement(targetTagsAndAtts);
    }

    Map<String, Double> calculateGlobalMultiPi(MappedSet<String, String> targetTagsAndAtts) throws IOException, SAXException, MaeException {
        GlobalMultiPiCalc calc = new GlobalMultiPiCalc(fileIdx, parseCache);
        return calc.calculateAgreement(targetTagsAndAtts);
    }

     Map<String, Double> calculateLocalMultiKappa(MappedSet<String, String> targetTagsAndAtts) throws IOException, SAXException, MaeException {
        LocalMultiKappaCalc calc = new LocalMultiKappaCalc(fileIdx, parseCache);
        return calc.calculateAgreement(targetTagsAndAtts);
    }

    Map<String, Double> calculateGlobalMultiKappa(MappedSet<String, String> targetTagsAndAtts) throws IOException, SAXException, MaeException {
        GlobalMultiKappaCalc calc = new GlobalMultiKappaCalc(fileIdx, parseCache);
        return calc.calculateAgreement(targetTagsAndAtts);
    }

    public String calcGlobalAgreementToString(Map<String, MappedSet<String, String>> metricToTargetsMap) throws MaeException, SAXException, IOException {
        StringBuilder result = new StringBuilder();
        for (String metricType : metricToTargetsMap.keySet()) {
            MappedSet<String, String> targetTagsAndAtts = metricToTargetsMap.get(metricType);
            if (targetTagsAndAtts.size() == 0) {
                continue;
            }
            String agrTitle = String.format("<%s> %s  %s", SCOPE_CROSSTAG_STRING, metricType, targetTagsAndAtts.keyList());
            switch (metricType) {
                case ALPHAU_CALC_STRING:
                    result.append(agreementsToString(agrTitle, calculateGlobalAlphaU(targetTagsAndAtts)));
                    break;
                case ALPHA_CALC_STRING:
                    break;
                case MULTIKAPPA_CALC_STRING:
                    result.append(agreementsToString(agrTitle, calculateGlobalMultiKappa(targetTagsAndAtts)));
                    break;
                case MULTIPI_CALC_STRING:
                    result.append(agreementsToString(agrTitle, calculateGlobalMultiPi(targetTagsAndAtts)));
                    break;
            }

        }
        return result.toString();
    }

    public String calcLocalAgreementToString(Map<String, MappedSet<String, String>> metricToTargetsMap) throws MaeException, SAXException, IOException {
        StringBuilder result = new StringBuilder();
        for (String metricType : metricToTargetsMap.keySet()) {
            MappedSet<String, String> targetTagsAndAtts = metricToTargetsMap.get(metricType);
            if (targetTagsAndAtts.size() == 0) {
                continue;
            }
            String agrTitle = String.format("<%s> %s", SCOPE_LOCAL_STRING, metricType);
            switch (metricType) {
                case ALPHAU_CALC_STRING:
                    result.append(agreementsToString(agrTitle, calculateLocalAlphaU(targetTagsAndAtts)));
                    break;
                case ALPHA_CALC_STRING:
                    break;
                case MULTIKAPPA_CALC_STRING:
                    result.append(agreementsToString(agrTitle, calculateLocalMultiKappa(targetTagsAndAtts)));
                    break;
                case MULTIPI_CALC_STRING:
                    result.append(agreementsToString(agrTitle, calculateLocalMultiPi(targetTagsAndAtts)));
                    break;
            }

        }
        return result.toString();
    }
}
