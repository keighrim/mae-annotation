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

package edu.brandeis.llc.mae.model;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import edu.brandeis.llc.mae.MaeStrings;
import edu.brandeis.llc.mae.database.ExtentTagDao;
import edu.brandeis.llc.mae.util.SpanHandler;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by krim on 11/19/15.
 */

@DatabaseTable(tableName = DBSchema.TAB_ETAG, daoClass = ExtentTagDao.class)
public class ExtentTag extends Tag implements ModelI {

    @DatabaseField(columnName = DBSchema.TAB_ETAG_COL_TEXT)
    private String text;

    @ForeignCollectionField(eager = true)
    private ForeignCollection<CharIndex> spans;

    public ExtentTag() {

    }

    public ExtentTag(String tid, TagType tagType, String filename) {
        super(tid, tagType, filename);
        this.spans = null;
        this.text = null;

    }

    public List<CharIndex> setSpans(int...locations) {
        List<CharIndex> indices = new LinkedList<>();
        if (locations != null) {
            for (int location : locations) {
                indices.add(new CharIndex(location, this));

            }
        }
        // cannot call DAO inside ETag class, so we return list to save these afterwards
        return indices;
    }

    public List<CharIndex> setSpans(ArrayList<int[]> spans) {
        return this.setSpans(SpanHandler.convertPairsToArray(spans));
    }

    public List<CharIndex> setSpans(String spansString) {
        return this.setSpans(SpanHandler.convertStringToPairs(spansString));
    }

    @Override
    public Set<String> getUnderspec() {
        return checkRequiredAtts();
    }

    public boolean isConsuming() {
        return getSpans() != null && getSpans().size() > 0;
    }

    public ForeignCollection<CharIndex> getSpans() {
        return spans;
    }

    public List<Integer> getSpansAsList() {
        List<Integer> spans = new ArrayList<>();
        for (CharIndex anchor : getSpans()) {
            spans.add(anchor.getLocation());
        }
        return spans;

    }

    public int[] getSpansAsArray() {
        int[] spans = new int[this.getSpans().size()];
        int i = 0;
        for (CharIndex ci : getSpans()) {
            spans[i++] = ci.getLocation();
        }
        return spans;
    }

    public String getSpansAsString() {
        return SpanHandler.convertArrayToString(this.getSpansAsArray());
    }

    public String getText() {
        if (isConsuming()) {
            return text;
        } else {
            return MaeStrings.NC_TEXT;
        }
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toJsonString() {
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public String toXmlString() {
        String tagTypeName = getTagTypeName();
        String idAtt = String.format("id=\"%s\"", tid);
        String spansAtt = String.format("spans=\"%s\"", getSpansAsString());
        String textAtt = String.format("text=\"%s\"", escapeXmlString(text));
        String attributes = getAttributesXmlString();
        return String.format("<%s />", StringUtils.join(new String[]{tagTypeName, idAtt, spansAtt, textAtt, attributes}, " "));
    }

    @Override
    public String toString() {
        String tagText;
        if (isConsuming()) {
            tagText = getText();
        } else {
            tagText = "=NC=";
        }
        return String.format("%s (%s)", getId(), tagText);
    }

}
