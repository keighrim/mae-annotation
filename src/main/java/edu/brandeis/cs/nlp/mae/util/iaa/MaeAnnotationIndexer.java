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

import edu.brandeis.cs.nlp.mae.io.MaeIOException;

import java.io.File;
import java.util.*;

import static edu.brandeis.cs.nlp.mae.util.FileHandler.*;


/**
 * Created by krim on 4/13/16.
 */
public class MaeAnnotationIndexer {

    private Map<String, Integer> annotatorMap;
    private Map<String, String[]> documentFileMap;

    public MaeAnnotationIndexer() {
        annotatorMap = new TreeMap<>();
        documentFileMap = new TreeMap<>();
    }

    public List<String> getAnnotators() {
        return new ArrayList<>(annotatorMap.keySet());
    }

    public Map<String, String[]> getDocumentFileMap() {
        return documentFileMap;
    }

    public int getAnnotatorIndex(String annotatorSymbol) {
        return annotatorMap.get(annotatorSymbol);
    }

    public List<String> getDocuments() {
        return new ArrayList<>(documentFileMap.keySet());
    }

    public String[] getAnnotationsOfDocument(String docName) {
        return documentFileMap.get(docName);
    }


    public void getAnnotationMatrixFromFiles(List<File> annotationFiles) throws MaeIOException {
        // takes a list of all relevant files (assume all files are .xml)
        // files need to end with annotator suffix, affixed with underscore('_')
        // also they should share the rest of their names

        listupAnnotators(annotationFiles);
        while (annotationFiles.size() > 0) {
            File annotationFile = annotationFiles.remove(0);
            String[] split = splitAnnotationAnnotator(
                    getFileNameWithoutExtension(
                            getFileBaseName(annotationFile).trim()));
            String annotationName = split[0];
            String annotatorName  = split[1];
            String[] indexedFileNames = new String[annotatorMap.size()];
            // fill current file's slot
            indexedFileNames[annotatorMap.get(annotatorName)] = annotationFile.getAbsolutePath();

            // then iterate the rest of files, to find associated files
            for (String symbol : annotatorMap.keySet()) {
                if (symbol.equals(annotatorName)) {
                    continue;
                }
                String secondaryAnnotationBaseName = generateAnnotationFileName(annotationName, symbol);

                int i = 0;
                while (i < annotationFiles.size()) {
                    File rest = annotationFiles.get(i);
                    if (rest.getName().endsWith(secondaryAnnotationBaseName)) {
                        indexedFileNames[annotatorMap.get(symbol)] = rest.getAbsolutePath();
                        annotationFiles.remove(i);
                        break;
                    } else {
                        i++;
                    }
                }
            }
            documentFileMap.put(annotationName, indexedFileNames);
        }
    }

    public String generateAnnotationFileName(String document, String annotator) {
        return String.format("%s%s%s%s",
                document, ANNOTATOR_SUFFIX_DELIM,
                annotator, XML_EXT);
    }


    private int listupAnnotators(List<File> annotationFiles) throws MaeIOException {
        annotatorMap = new TreeMap<>();
        int countSeen = 0;

        for (File annotationFile : annotationFiles) {
            String annotationBaseName = getFileBaseName(annotationFile).trim();
            if (!annotationFile.getName().endsWith(XML_EXT)) {
                throw new MaeIOException("An annotation should be an XML file: " + annotationFile.getName());
            }

            String annotationShortName = getFileNameWithoutExtension(annotationBaseName);
            String annotatorSymbol = splitAnnotationAnnotator(annotationShortName)[1];
            if (!annotatorMap.containsKey(annotatorSymbol)) {
                annotatorMap.put(annotatorSymbol, countSeen++);
            }
        }
        return annotatorMap.size();
    }

    public void getAnnotationMatrixFromDirectories(List<File> annotationDirs) throws MaeIOException {
        // TODO: 2016-04-13 20:41:43EDT implement this to take a set of dir names and treat each of them as an annotator

    }



}
