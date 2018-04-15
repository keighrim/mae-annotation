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

import static edu.brandeis.cs.nlp.mae.util.FileHandler.ANNOTATOR_SUFFIX_DELIM;
import static edu.brandeis.cs.nlp.mae.util.FileHandler.XML_EXT;


/**
 * Abstract class for file indexer, providing public methods to index a annotation
 * dataset as well as getters for file names of specific documents or annotators.
 */
public abstract class AbstractAnnotationIndexer {

    // indexed list of annotator IDs
    List<String> annotatorMap;
    // map from document ID to annotation file names
    Map<String, String[]> documentFileMap;
    Set<Integer> ignored;

    public AbstractAnnotationIndexer() {
        annotatorMap = new ArrayList<>();
        documentFileMap = new TreeMap<>();
        ignored = new TreeSet<>();
    }

    public void ignoreAnnotator(String annotatorID) {
        int annotatorIndex = getAnnotatorIndex(annotatorID);
        if (!ignored.contains(annotatorIndex))
            ignored.add(annotatorIndex);
    }

    public void approveAnnotator(String annotatorID) {
        int annotatorIndex = getAnnotatorIndex(annotatorID);
        if (ignored.contains(annotatorIndex))
            ignored.remove((annotatorIndex));
    }

    public List<String> getApprovedAnnotators() {
        List<String> annotators = new LinkedList<>();
        for (int i = 0; i < annotatorMap.size(); i++) {
            if (!ignored.contains(i)) {
                annotators.add(annotatorMap.get(i));
            }
        }
        return annotators;
    }

    public List<String> getAnnotators() {
        return annotatorMap;
    }

    public Map<String, String[]> getDocumentFileMap() {
        return documentFileMap;
    }

    public int getAnnotatorIndex(String annotatorSymbol) {
        return annotatorMap.indexOf(annotatorSymbol);
    }

    public List<String> getDocumentNames() {
        return new ArrayList<>(documentFileMap.keySet());
    }

    public String[] getAnnotationsOfDocument(String docName) {
        if (ignored.size() == 0) {
            return documentFileMap.get(docName);
        }
        String[] docs =  new String[annotatorMap.size() - ignored.size()];
        int j = 0;
        for (int i = 0 ; i < annotatorMap.size(); i++ ) {
            if (!ignored.contains(i)) {
                docs[j++] = documentFileMap.get(docName)[i];
            }
        }
        return docs;
    }

    public String generateAnnotationFileName(String document, String annotator) {
        return String.format("%s%s%s%s",
                document, ANNOTATOR_SUFFIX_DELIM,
                annotator, XML_EXT);
    }

    public abstract void indexAnnotations(File[] dataset) throws MaeIOException;

    public int getDocumentCount() {
        return documentFileMap.keySet().size();
    }

    public int getApprovedAnnotatorCount() {
        return annotatorMap.size() - ignored.size();
    }

    public int getAllAnnotatorCount() {
        return annotatorMap.size();
    }


}
