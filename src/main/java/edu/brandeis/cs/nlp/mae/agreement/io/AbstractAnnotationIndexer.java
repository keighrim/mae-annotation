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

import java.io.File;
import java.util.*;

import static edu.brandeis.cs.nlp.mae.util.FileHandler.*;


/**
 * Created by krim on 4/13/16.
 */
public abstract class AbstractAnnotationIndexer {

    Map<String, Integer> annotatorMap;
    Map<String, String[]> documentFileMap;

    public AbstractAnnotationIndexer() {
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

    public List<String> getDocumentNames() {
        return new ArrayList<>(documentFileMap.keySet());
    }

    public String[] getAnnotationsOfDocument(String docName) {
        return documentFileMap.get(docName);
    }

    public String generateAnnotationFileName(String document, String annotator) {
        return String.format("%s%s%s%s",
                document, ANNOTATOR_SUFFIX_DELIM,
                annotator, XML_EXT);
    }

    public abstract void indexAnnotations(File datasetDirectory) throws MaeIOException;

    public abstract int listupAnnotators(File datasetDirectory) throws MaeIOException;

    public void getAnnotationMatrixFromDirectories(List<File> annotationDirs) throws MaeIOException {
        // TODO: 2016-04-13 20:41:43EDT implement this to take a set of dir names and treat each of them as an annotator

    }



}
