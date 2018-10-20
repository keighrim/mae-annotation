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

package edu.brandeis.llc.mae.agreement.io;

import edu.brandeis.llc.mae.io.MaeIOException;

import java.io.File;
import java.util.LinkedList;

import static edu.brandeis.llc.mae.util.FileHandler.*;

/**
 * Index annotation files from annotated dataset. The code expects the dataset is
 * passed as an array of directories, each of which has data from each annotator
 * and only them. All annotation files must be XML and their names formatted as
 * `docId_annotatorID.xml`. All non-xml files as well as xml files that does not
 * match the naming rule are ignored. That is, all annotation files must contains
 * the annotator ID, and they must be stored in a directory named after the annotator
 */
public class AnnotationDirsIndexer extends AbstractAnnotationIndexer {

    @Override
    public void indexAnnotations(File[] dataset) throws MaeIOException {
        if (annotatorMap == null) {
            annotatorMap = new LinkedList<>();
        }
        for (int i = 0; i < dataset.length; i++) {
            File annotatorDir = dataset[i];
            String annotatorID = annotatorDir.getName();
            annotatorMap.add(annotatorID);

            // filter only files that have annotator ID as suffix and the "xml" extension
            File[] annotationFiles = annotatorDir.listFiles(
                    file -> file.getName().matches(".+_" + annotatorID + "\\.[Xx][Mm][Ll]$"));
            if (annotationFiles == null || annotationFiles.length == 0) {
                 throw new MaeIOException("Found an empty subset: " + annotatorDir.getName());
            }
            for (File annotation : annotationFiles) {
                String docId = splitAnnotationAnnotator(
                        getFileNameWithoutExtension(
                        getFileBaseName(annotation).trim()))
                        [0];
                if (!documentFileMap.containsKey(docId)) {
                    documentFileMap.put(docId, new String[dataset.length]);
                }
                documentFileMap.get(docId)[i] = annotation.getAbsolutePath();
            }
        }
    }
}
