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
import edu.brandeis.cs.nlp.mae.MaeStrings;

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
        return this.setSpans(parseSpansString(spansString));
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
        return spansToString(parseCharIndices(this.getSpansAsList()));
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    /**
     * Takes a string representing possibly multiple spans of an extent tag Return
     * array of integer pairs
     *
     * @param spansString - string of spans
     * @return a ArrayList of int[]
     */
    public static ArrayList<int[]> parseSpansString(String spansString) {
        ArrayList<int[]> spans = new ArrayList<>();
        if (spansString == null || spansString.equals("") || spansString.equals("-1~-1")) {
            spans.add(new int[]{-1, -1});
            return spans;
        }

        // split each span
        String[] pairs = spansString.split(MaeStrings.SPANSEPARATOR);
        for (String pair : pairs) {
            int[] span = new int[2];

            // parse start and end points
            span[0] = Integer.parseInt(pair.split(MaeStrings.SPANDELIMITER)[0]);
            span[1] = Integer.parseInt(pair.split(MaeStrings.SPANDELIMITER)[1]);

            spans.add(span);
        }
        return spans;
    }

    /**
     * Takes an array of integer pairs, then merge it into a string. Each span
     * separated by SPANSEPARATOR start and end point of each span joined with
     * SPANDELIMITER
     *
     * @param spans - an sorted set of integer pairs
     * @return a formatted string of spans of a tag
     */
    public static String spansToString(ArrayList<int[]> spans) {
        String spanString = "";
        Iterator<int[]> iter = spans.iterator();
        while (iter.hasNext()) {
            int[] span = iter.next();
            if (iter.hasNext()) {
                spanString += span[0] + MaeStrings.SPANDELIMITER + span[1]
                        + MaeStrings.SPANSEPARATOR;
            } else {
                spanString += span[0] + MaeStrings.SPANDELIMITER + span[1];
            }
        }
        return spanString;
    }

    /**
     * Takes an array of CharIndex, make it into an array of int pairs,
     * which can be used in spansToString()
     *
     * @param spans - an sorted set of integer pairs
     * @return a ArrayList of int[]
     */
    public static ArrayList<int[]> parseCharIndices(List<CharIndex> spans) {
        ArrayList<int[]> spansList = new ArrayList<>();
        if (spans == null || spans.size() ==0) {
            spansList.add(new int[]{-1, -1});
            return spansList;
        }

        int[] locations = new int[spans.size()];
        for (int i = 0; i < spans.size(); i++) {
            locations[i] = (spans.get(i).getLocation());
        }
        Arrays.sort(locations);

        int start = locations[0];
        int prev = locations[0];
        for (int i = 1; i < locations.length; i++) {
            if (i == locations.length - 1) {
                spansList.add(new int[]{start, locations[i] + 1});
            } else if (prev + 1 < locations[i]) {
                spansList.add(new int[]{start, prev + 1});
                prev = locations[i];
                start = locations[i];
            } else {
                prev = locations[i];
            }
        }
        return spansList;

    }


}
