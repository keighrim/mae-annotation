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

package edu.brandeis.cs.nlp.mae.agreement.calculator;

import edu.brandeis.cs.nlp.mae.MaeException;
import edu.brandeis.cs.nlp.mae.agreement.io.AbstractAnnotationIndexer;
import edu.brandeis.cs.nlp.mae.agreement.io.XMLParseCache;
import edu.brandeis.cs.nlp.mae.util.MappedSet;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Map;

/**
 * Abstract superclass for all AgreementCalc classes to hold common instance fields.
 */
public abstract class AbstractMaeAgreementCalc {

    int numAnnotators;
    AbstractAnnotationIndexer fileIdx;
    XMLParseCache parseCache;

    public AbstractMaeAgreementCalc(AbstractAnnotationIndexer fileIdx, XMLParseCache parseCache) {
        this.fileIdx = fileIdx;
        this.parseCache = parseCache;
        this.numAnnotators = fileIdx.getApprovedAnnotatorCount();
    }

    public abstract Map<String, Double> calculateAgreement(
            MappedSet<String, String> targetTagsAndAtts,
            boolean allowMultiTagging)
            throws IOException, SAXException, MaeException;
}
