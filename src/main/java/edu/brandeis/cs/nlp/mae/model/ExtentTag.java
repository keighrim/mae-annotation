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
 * For feedback, reporting bugs, use the project repo on github
 * @see <a href="https://github.com/keighrim/mae-annotation">https://github.com/keighrim/mae-annotation</a>
 */

package edu.brandeis.cs.nlp.mae.model;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.*;

/**
 * Created by krim on 11/19/15.
 */

@DatabaseTable(tableName = ModelStrings.TAB_ETAG)
public class ExtentTag extends Tag {

    @DatabaseField(columnName = ModelStrings.TAB_ETAG_COL_TEXT)
    private String text;

    @ForeignCollectionField
    protected ForeignCollection<Attribute> attributes;

    @ForeignCollectionField(eager = true)
    private ForeignCollection<CharIndex> spans;

    public ExtentTag() {

    }

    public ExtentTag(String tid, TagType tagType) {
        super(tid, tagType);
        this.spans = null;
        this.text = null;

    }

    @Override
    public ForeignCollection<Attribute> getAttributes() {
        return attributes;
    }

    public List<CharIndex> setSpans(int...locations) {
        List<CharIndex> indices = new LinkedList<>();
        for (int location : locations) {
            indices.add(new CharIndex(location, this));

        }
        // cannot call DAO inside ETag class, so we return list to save these afterwards
        return indices;
    }

    public List<CharIndex> setSpans(ArrayList<int[]> spans) {
        // maybe legacy support?
        List<CharIndex> indices = new LinkedList<>();
        if (spans.size() == 1 && Arrays.equals(spans.get(0), new int[]{-1, -1})) {
            return indices;
        }
        for (int[] span : spans) {
            for (int i=span[0]; i<span[1]; i++) {
                indices.add(new CharIndex(i, this));
            }
        }
        return indices;
    }

    public List<CharIndex> setSpans(String spansString) {
        return this.setSpans(ModelHelpers.parseSpansString(spansString));
    }

    @Override
    public boolean isComplete() {
        checkRequiredAtts();
        return isComplete;
    }

    public boolean isConsuming() {
        return getSpans() == null || getSpans().size() == 0;
    }

    public ForeignCollection<CharIndex> getSpans() {
        return spans;
    }

    public List<CharIndex> getSpansAsList() {
        return new ArrayList<>(this.getSpans());
    }

    public String getSpansAsString() {
        return ModelHelpers.spansToString(ModelHelpers.parseCharIndices(this.getSpansAsList()));
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public Map<String, String> getAttbutesWithNames() {
        return ModelHelpers.getAttbutesWithNames(this.getAttributes());
    }

}
