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
import java.util.ArrayList;
import java.util.List;

import static edu.brandeis.cs.nlp.mae.util.FileHandler.*;

/**
 * Index annotation files from a single directory. The code expects all annotation
 * files are XML and their names are formatted in `docId_annotatorID.xml`. All
 * non-xml files and subdirectories would be ignored. The main public method to
 * index files expects an singleton array of the dataset directory.
 */
public class AnnotationFilesIndexer extends AbstractAnnotationIndexer {

    private void listUpAnnotators(List<File> annotationFiles) throws MaeIOException {

        if (annotatorMap == null) {
            annotatorMap = new ArrayList<>();
        }
        if (annotatorMap.size() == 0) {
            for (File annotationFile : annotationFiles) {
                String annotationBaseName = getFileBaseName(annotationFile).trim();
                if (!annotationFile.getName().endsWith(XML_EXT)) {
                    throw new MaeIOException("An annotation should be an XML file: " + annotationFile.getName());
                }

                String annotationShortName = getFileNameWithoutExtension(annotationBaseName);
                String annotatorSymbol = splitAnnotationAnnotator(annotationShortName)[1];
                if (annotatorMap.indexOf(annotatorSymbol) < 0) {
                    annotatorMap.add(annotatorSymbol);
                }
            }
        }
    }

    @Override
    public void indexAnnotations(File[] dataset) throws MaeIOException {
        indexAnnotationFiles(FileHandler.getAllXMLFilesIn(dataset[0]));
    }

    private void indexAnnotationFiles(List<File> annotationFiles) throws MaeIOException {
        // takes a list of all relevant files (assume all files are .xml)
        // files need to end with annotator suffix, affixed with underscore('_')
        // also they should share the rest of their names

        listUpAnnotators(annotationFiles);
        while (annotationFiles.size() > 0) {
            File annotationFile = annotationFiles.remove(0);
            String[] split = splitAnnotationAnnotator(getFileNameWithoutExtension(
                    getFileBaseName(annotationFile).trim()));
            String annotationName = split[0];
            String annotatorName  = split[1];
            String[] indexedFileNames = new String[annotatorMap.size()];
            // fill current file's slot
            indexedFileNames[annotatorMap.indexOf(annotatorName)] = annotationFile.getAbsolutePath();

            // then iterate the rest of files, to find associated files
            for (String symbol : annotatorMap) {
                if (symbol.equals(annotatorName)) {
                    continue;
                }
                String secondaryAnnotationBaseName = generateAnnotationFileName(annotationName, symbol);

                int i = 0;
                while (i < annotationFiles.size()) {
                    File rest = annotationFiles.get(i);
                    if (rest.getName().endsWith(secondaryAnnotationBaseName)) {
                        indexedFileNames[annotatorMap.indexOf(symbol)] = rest.getAbsolutePath();
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
