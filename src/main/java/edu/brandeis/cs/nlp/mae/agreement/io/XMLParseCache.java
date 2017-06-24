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

import edu.brandeis.cs.nlp.mae.database.MaeDBException;
import edu.brandeis.cs.nlp.mae.database.MaeDriverI;
import edu.brandeis.cs.nlp.mae.io.MaeXMLParser;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by krim on 4/23/2016.
 */
public class XMLParseCache {
    private Map<String, MaeXMLParser[]> parseCache;
    private MaeDriverI driver;
    private AbstractAnnotationIndexer fileIdx;
    private Map<String, String> parseWarnings;

    public XMLParseCache(MaeDriverI driver, AbstractAnnotationIndexer fileIdx) {
        this.driver = driver;
        this.fileIdx = fileIdx;
        this.parseCache = new HashMap<>();
        this.parseWarnings = new HashMap<>();
    }

    public MaeXMLParser[] getParses(String docName) throws IOException, SAXException, MaeDBException {
        return getOrCacheXMLParse(docName);
    }

    private MaeXMLParser[] getOrCacheXMLParse(String docName) throws MaeDBException, IOException, SAXException {
        if (!this.parseCache.containsKey(docName)) {
            parseCache.put(docName, cacheXMLParse(docName));
        }
        return parseCache.get(docName);
    }

    private MaeXMLParser[] cacheXMLParse(String docName) throws MaeDBException, IOException, SAXException {
        // this var would only hold xml files of approved annotators, and so would the cache
        String[] xmlFilesToCache = fileIdx.getAnnotationsOfDocument(docName);
        MaeXMLParser[] parses = new MaeXMLParser[xmlFilesToCache.length];
        for (int i = 0; i < xmlFilesToCache.length; i++) {
            String fileName = xmlFilesToCache[i];
            if (fileName != null) {
                MaeXMLParser parser = new MaeXMLParser(driver);
                parser.readAnnotationFile(new File(fileName));
                parses[i] = parser;
                if (parser.getParseWarnings().length() > 0) {
                    this.parseWarnings.put(fileName, parser.getParseWarnings());
                }
            }
        }
        return parses;
    }

    public Map<String, String> getParseWarnings() {
        return this.parseWarnings;
    }
}
