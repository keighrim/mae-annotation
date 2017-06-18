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

import edu.brandeis.cs.nlp.mae.agreement.io.AbstractAnnotationIndexer;
import edu.brandeis.cs.nlp.mae.agreement.io.AnnotationDirsIndexer;
import edu.brandeis.cs.nlp.mae.agreement.io.AnnotationFilesIndexer;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Created by krim on 4/13/16.
 */
public class AnnotationFilesIndexerTest {
    AbstractAnnotationIndexer indexer;

    @Test
    public void testGetAnnotationMatrixFromDirs() throws Exception {
        indexer = new AnnotationDirsIndexer();
        URL exmapleFileUrl = Thread.currentThread().getContextClassLoader().getResource("iaa_example_dirs");
        File exampleDir = new File(exmapleFileUrl.getPath());
        List<File> subsets = new LinkedList<>();
        for (File subset : exampleDir.listFiles()) {
            if (subset.isDirectory()) {
                subsets.add(subset);
            }
        }
        File[] targets = new File[subsets.size()];
        targets = subsets.toArray(targets);
        indexer.indexAnnotations(targets);
        testIndexer();
    }

    @Test
    public void testGetAnnotationMatrixFromFiles() throws Exception {
        indexer = new AnnotationFilesIndexer();
        URL exmapleFileUrl = Thread.currentThread().getContextClassLoader().getResource("iaa_example");
        File[] exampleDir = new File[]{new File(exmapleFileUrl.getPath())};
        indexer.indexAnnotations(exampleDir);
        testIndexer();
    }

    private void testIndexer() {
        assertEquals("Expected 5 annotators, found " + indexer.getAnnotators().size(),
                5, indexer.getAnnotators().size());
        Map<String, String[]> map = (indexer.getDocumentFileMap());
        assertEquals("Expected 4 documents, found " + map.keySet().size(),
                4, map.keySet().size());
        String[] doc1anns = map.get("doc1");
        int doc1nulls = countNull(doc1anns);
        assertEquals("Expected doc1 has 5 annotations, found " + (5 - doc1nulls),
                0, doc1nulls);
        String[] doc2anns = map.get("doc2");
        int doc2nulls = countNull(doc2anns);
        assertEquals("Expected doc2 has 4 annotations, found " + (5 - doc2nulls),
                1, doc2nulls);
    }

    private int countNull(Object[] array) {
        int count = 0;
        for (Object obj : array) {
            if (obj == null) {
                count++;
            }
        }
        return count;
    }
}