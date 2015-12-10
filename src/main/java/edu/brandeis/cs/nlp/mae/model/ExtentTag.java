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

import java.util.ArrayList;

/**
 * Created by krim on 11/19/15.
 */

@DatabaseTable(tableName = "extenttag")
public class ExtentTag extends Tag {

    @DatabaseField
    private String text;

    @ForeignCollectionField
    private ForeignCollection<CharIndex> spans;

    public ExtentTag() {

    }

    public ExtentTag(String tid, TagType tagType) {
        super(tid, tagType);
        this.spans = null;
        this.text = null;

    }

    public ExtentTag(String tid, TagType tagType, ArrayList<int[]> spans, String text) {
        super(tid, tagType);
        this.setSpans(spans);
        this.setText(text);
    }

    private void setSpans(ArrayList<int[]> spans) {
        for (int[] span : spans) {
            for (int i=span[0]; i<span[1]; i++) {
                new CharIndex(i, this);
            }
        }
    }

    @Override
    public boolean isFulfilled() {
        checkRequiredAtts();
        return isFulfilled;
    }

    public boolean isConsuming() {
        return getSpans() == null || getSpans().size() == 0;
    }

    public ForeignCollection<CharIndex> getSpans() {
        return spans;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}
