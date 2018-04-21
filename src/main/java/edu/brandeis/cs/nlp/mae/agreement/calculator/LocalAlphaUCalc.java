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

import edu.brandeis.cs.nlp.mae.agreement.io.AbstractAnnotationIndexer;
import edu.brandeis.cs.nlp.mae.agreement.io.XMLParseCache;
import edu.brandeis.cs.nlp.mae.database.MaeDBException;
import edu.brandeis.cs.nlp.mae.io.MaeXMLParser;
import edu.brandeis.cs.nlp.mae.util.MappedSet;
import org.dkpro.statistics.agreement.unitizing.KrippendorffAlphaUnitizingAgreement;
import org.dkpro.statistics.agreement.unitizing.UnitizingAnnotationStudy;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.*;

import static edu.brandeis.cs.nlp.mae.agreement.MaeAgreementStrings.SPAN_ATT;
import static edu.brandeis.cs.nlp.mae.agreement.MaeAgreementStrings.TAG_ATT_DELIM;

/**
 * Created by krim on 4/23/2016.
 */
public class LocalAlphaUCalc extends AbstractUnitizationAgreementCalc {


    public LocalAlphaUCalc(AbstractAnnotationIndexer fileIdx, XMLParseCache parseCache, int[] documentLength) {
        super(fileIdx, parseCache, documentLength);
    }

    @Override
    public Map<String, Double> calculateAgreement(MappedSet<String, String> targetTagsAndAtts, boolean allowMultiTagging) throws IOException, SAXException, MaeDBException {
        Map<String, Double> alphaUs = new TreeMap<>();

        Map<String, String> attFullNameMap = new HashMap<>();
        for (String tagTypeName : targetTagsAndAtts.keyList()) {
            Map<String, UnitizingAnnotationStudy> studyPerAtt = new LinkedHashMap<>();
            List<String> attTypeNames = targetTagsAndAtts.getAsList(tagTypeName);
            attTypeNames.add(0, SPAN_ATT);
            for (String attTypeName : attTypeNames) {
                String attFullName = tagTypeName + TAG_ATT_DELIM + attTypeName;
                studyPerAtt.put(attFullName, new UnitizingAnnotationStudy(numAnnotators, totalDocumentsLength));
                attFullNameMap.put(attTypeName, attFullName);
            }
            int curDocLength = 0;
            List<String> documents = fileIdx.getDocumentNames();
            for (int i = 0; i < documents.size(); i++) {
                String document = documents.get(i);
                MaeXMLParser[] parses = parseCache.getParses(document);

                addTagAsUnits(tagTypeName, parses, curDocLength, studyPerAtt.get(attFullNameMap.get(SPAN_ATT)));
                for (int j = 1; j < attTypeNames.size(); j++) {
                    String attTypeName = attTypeNames.get(j);
                    addAttAsUnits(tagTypeName, attTypeName, parses, curDocLength, studyPerAtt.get(attFullNameMap.get(attTypeName)));
                }
                curDocLength += documentLength[i];
            }
            for (String attTypeName : attTypeNames) {
                double agree = (new KrippendorffAlphaUnitizingAgreement(studyPerAtt.get(attFullNameMap.get(attTypeName)))).calculateAgreement();
                alphaUs.put(attFullNameMap.get(attTypeName), agree);
            }
        }
        return alphaUs;
    }

}
