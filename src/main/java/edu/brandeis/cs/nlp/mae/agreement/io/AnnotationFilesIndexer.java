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

package edu.brandeis.cs.nlp.mae.agreement.io;

import edu.brandeis.cs.nlp.mae.io.MaeIOException;
import edu.brandeis.cs.nlp.mae.util.FileHandler;

import java.io.File;
import java.util.List;
import java.util.TreeMap;

import static edu.brandeis.cs.nlp.mae.util.FileHandler.*;

/**
 * Created by krim on 4/23/2016.
 */
public class AnnotationFilesIndexer extends AbstractAnnotationIndexer {

    private List<File> annotationFiles;

    @Override
    public int listupAnnotators(File datasetDirectory) throws MaeIOException {
        if (annotationFiles == null) {
            annotationFiles = FileHandler.getAllXMLFilesIn(datasetDirectory);
        }
        return listupAnnotators(annotationFiles);
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

    @Override
    public void indexAnnotations(File datasetDirectory) throws MaeIOException {
        if (annotationFiles == null) {
            annotationFiles = FileHandler.getAllXMLFilesIn(datasetDirectory);
        }
        indexAnnotationFiles(annotationFiles);
    }

    private void indexAnnotationFiles(List<File> annotationFiles) throws MaeIOException {
        // takes a list of all relevant files (assume all files are .xml)
        // files need to end with annotator suffix, affixed with underscore('_')
        // also they should share the rest of their names

        listupAnnotators(annotationFiles);
        while (annotationFiles.size() > 0) {
            File annotationFile = annotationFiles.remove(0);
            String[] split = splitAnnotationAnnotator(getFileNameWithoutExtension(
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
}
