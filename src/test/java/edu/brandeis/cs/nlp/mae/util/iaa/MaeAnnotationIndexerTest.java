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

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Created by krim on 4/13/16.
 */
public class MaeAnnotationIndexerTest {
    MaeAnnotationIndexer calc;
    List<File> files;

    @Before
    public void setUp() throws Exception {
        calc = new MaeAnnotationIndexer();
        files = new ArrayList<>();
        String[] docs = new String[]{"doc1", "doc2", "doc3", "doc4", "doc5"};
        String[] anns = new String[]{"a1", "a2", "a3", "a4", "a5"};
        for (String doc: docs) {
            for (String ann : anns) {
                files.add(new File(calc.generateAnnotationFileName(doc, ann)));
            }
        }
    }

    @Test
    public void testGetAnnotationMatrixFromFiles() throws Exception {
        files.remove(new File("doc2_a2.xml"));
        calc.getAnnotationMatrixFromFiles(files);
        assertEquals("Expected 5 annotators, found " + calc.getAnnotators().size(),
                5, calc.getAnnotators().size());
        Map<String, String[]> map = (calc.getDocumentFileMap());
        assertEquals("Expected 5 documents, found " + map.keySet().size(),
                5, map.keySet().size());
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