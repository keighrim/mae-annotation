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
import org.dkpro.statistics.agreement.coding.CodingAnnotationStudy;
import org.dkpro.statistics.agreement.coding.HubertKappaAgreement;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by krim on 4/24/2016.
 */
public class GlobalMultiKappaCalc extends AbstractCodingAgreementCalc {

    public GlobalMultiKappaCalc(AbstractAnnotationIndexer fileIdx, XMLParseCache parseCache) {
        super(fileIdx, parseCache);
    }

    @Override
    public Map<String, Double> calculateAgreement(MappedSet<String, String> targetTagsAndAtts) throws IOException, SAXException, MaeException {
        Map<String, Double> globalMultiKappa = new TreeMap<>();
        CodingAnnotationStudy study = prepareGlobalCodingStudy(targetTagsAndAtts);
        double agreement = (new HubertKappaAgreement(study)).calculateAgreement();
        globalMultiKappa.put("cross-tag_multi_kappa", agreement);
        return globalMultiKappa;
    }
}


