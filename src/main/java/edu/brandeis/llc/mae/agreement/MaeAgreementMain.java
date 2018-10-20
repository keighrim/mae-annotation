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

package edu.brandeis.llc.mae.agreement;

import edu.brandeis.llc.mae.MaeException;
import edu.brandeis.llc.mae.agreement.io.AbstractAnnotationIndexer;
import edu.brandeis.llc.mae.agreement.io.AnnotationDirsIndexer;
import edu.brandeis.llc.mae.agreement.io.AnnotationFilesIndexer;
import edu.brandeis.llc.mae.agreement.io.XMLParseCache;
import edu.brandeis.llc.mae.database.MaeDBException;
import edu.brandeis.llc.mae.database.MaeDriverI;
import edu.brandeis.llc.mae.io.MaeIOException;
import edu.brandeis.llc.mae.io.MaeIOXMLException;
import edu.brandeis.llc.mae.io.MaeXMLParser;
import edu.brandeis.llc.mae.util.FileHandler;
import edu.brandeis.llc.mae.util.MappedSet;
import edu.brandeis.llc.mae.agreement.calculator.*;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Main controller for IAA calculator.
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
            fileIdx.indexAnnotations(datasetDir.listFiles(file -> !file.isHidden()));
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
            if (seen == -1) {
                continue;
            }
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

    /**
     * Returns the index of the first non-null element of the given array.
     * If the array is full of null items, return -1.
     * @param array an array
     * @return the index of non-null item, or -1
     */
    private static int getFirstNonNullIndex(Object[] array) {
        int seen = 0;
        while (seen < array.length && array[seen] == null) {
            seen++;
        }
        if (seen >= array.length) {
            return -1;
        } else {
            return seen;
        }
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

    public String calcGlobalAgreementToString(
            Map<String, MappedSet<String, String>> metricToTargetsMap,
            boolean allowMultiTagging)
            throws MaeException, SAXException, IOException {
        StringBuilder result = new StringBuilder();
        for (String metricType : metricToTargetsMap.keySet()) {
            MappedSet<String, String> targetTagsAndAtts = metricToTargetsMap.get(metricType);
            if (targetTagsAndAtts.size() == 0) {
                continue;
            }
            String agrTitle = String.format("<%s> %s  %s", MaeAgreementStrings.SCOPE_CROSSTAG_STRING, metricType, targetTagsAndAtts.keyList());
            AbstractMaeAgreementCalc calc = null;
            switch (metricType) {
                case MaeAgreementStrings.ALPHAU_CALC_STRING:
                    calc = new GlobalAlphaUCalc(fileIdx, parseCache, documentLength);
                    break;
                case MaeAgreementStrings.ALPHA_CALC_STRING:
                    break;
                case MaeAgreementStrings.MULTIKAPPA_CALC_STRING:
                    calc = new GlobalMultiKappaCalc(fileIdx, parseCache);
                    break;
                case MaeAgreementStrings.MULTIPI_CALC_STRING:
                    calc = new GlobalMultiPiCalc(fileIdx, parseCache);
                    break;
            }
            if (calc == null) {
                result.append("metric not defined: ").append(metricType);
            } else {
                result.append(agreementsToString(agrTitle,
                        calc.calculateAgreement(targetTagsAndAtts, allowMultiTagging)));
            }
        }
        return result.toString();
    }

    public String calcLocalAgreementToString(
            Map<String, MappedSet<String, String>> metricToTargetsMap,
            boolean allowMultiTagging)
            throws MaeException, SAXException, IOException {
        StringBuilder result = new StringBuilder();
        for (String metricType : metricToTargetsMap.keySet()) {
            MappedSet<String, String> targetTagsAndAtts = metricToTargetsMap.get(metricType);
            if (targetTagsAndAtts.size() == 0) {
                continue;
            }
            String agrTitle = String.format("<%s> %s", MaeAgreementStrings.SCOPE_LOCAL_STRING, metricType);
            AbstractMaeAgreementCalc calc = null;
            switch (metricType) {
                case MaeAgreementStrings.ALPHAU_CALC_STRING:
                    calc = new LocalAlphaUCalc(fileIdx, parseCache, documentLength);
                    break;
                case MaeAgreementStrings.ALPHA_CALC_STRING:
                    break;
                case MaeAgreementStrings.MULTIKAPPA_CALC_STRING:
                    calc = new LocalMultiKappaCalc(fileIdx, parseCache);
                    break;
                case MaeAgreementStrings.MULTIPI_CALC_STRING:
                    calc = new LocalMultiPiCalc(fileIdx, parseCache);
                    break;
            }
            if (calc == null) {
                result.append("metric not defined: ").append(metricType);
            } else {
                result.append(agreementsToString(agrTitle,
                        calc.calculateAgreement(targetTagsAndAtts, allowMultiTagging)));
            }
        }
        return result.toString();
    }
}
