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

package edu.brandeis.llc.mae.agreement.calculator;

import edu.brandeis.llc.mae.agreement.io.AbstractAnnotationIndexer;
import edu.brandeis.llc.mae.agreement.io.XMLParseCache;
import edu.brandeis.llc.mae.database.MaeDBException;
import edu.brandeis.llc.mae.io.MaeXMLParser;
import edu.brandeis.llc.mae.io.ParsedAtt;
import edu.brandeis.llc.mae.io.ParsedTag;
import edu.brandeis.llc.mae.util.MappedSet;
import edu.brandeis.llc.mae.util.SpanHandler;
import org.dkpro.statistics.agreement.unitizing.UnitizingAnnotationStudy;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * Created by krim on 4/23/2016.
 */

public abstract class AbstractUnitizationAgreementCalc extends AbstractMaeAgreementCalc {

    int totalDocumentsLength;
    int[] documentLength;

    public AbstractUnitizationAgreementCalc(AbstractAnnotationIndexer fileIdx, XMLParseCache parseCache, int[] documentLength) {
        super(fileIdx, parseCache);
        this.documentLength = documentLength;
        this.totalDocumentsLength = IntStream.of(documentLength).reduce( 0,(a, b) -> a + b);

    }

    void addTagAsUnits(String tagTypeName, MaeXMLParser[] annotations, int textOffset, UnitizingAnnotationStudy study) {

        int annotator = 0;
        for (MaeXMLParser parse : annotations) {
            if (parse == null) continue;
            for (ParsedTag tag : parse.getParsedTags()) {
                if (tag.getTagTypeName().equals(tagTypeName) && tag.getSpans().length > 0) {
                    for (int[] pair : SpanHandler.convertArrayToPairs(tag.getSpans())) {
                        study.addUnit(pair[0] + textOffset, pair[1] - pair[0], annotator, tagTypeName);
                    }
                }
            }
            annotator++;
        }

    }

    void addAttAsUnits(String tagTypeName, String attTypeName, MaeXMLParser[] annotations, int textOffset, UnitizingAnnotationStudy study) {

        int annotator = 0;
        for (MaeXMLParser parse : annotations) {
            if (parse != null) {
                for (ParsedTag tag : parse.getParsedTags()) {
                    if (tag.getTagTypeName().equalsIgnoreCase(tagTypeName) && tag.getSpans().length > 0) {
                        for (ParsedAtt att : parse.getParsedAtts()) {
                            if (att.getTagTypeName().equalsIgnoreCase(tag.getTagTypeName()) &&
                                    att.getAttTypeName().equalsIgnoreCase(attTypeName)) {
                                for (int[] pair : SpanHandler.convertArrayToPairs(tag.getSpans())) {
                                    study.addUnit(pair[0] + textOffset, pair[1] - pair[0], annotator, att.getAttValue());
                                }
                            }
                        }
                    }
                }
            }
            annotator++;
        }
    }


    @Override
    abstract public Map<String, Double> calculateAgreement(
            MappedSet<String, String> targetTagsAndAtts,
            boolean allowMultiTagging)
            throws IOException, SAXException, MaeDBException;
}
