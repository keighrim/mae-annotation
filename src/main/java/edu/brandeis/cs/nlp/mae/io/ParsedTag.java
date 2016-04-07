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

package edu.brandeis.cs.nlp.mae.io;

import edu.brandeis.cs.nlp.mae.MaeException;
import edu.brandeis.cs.nlp.mae.util.SpanHandler;

/**
 * Created by krim on 4/6/16.
 */
public class ParsedTag {
    private boolean isLink;
    private String tid;
    private String tagTypeName;
    private String text;
    private int[] spans;

    public boolean isLink() {
        return isLink;
    }

    public void setLink(boolean link) {
        isLink = link;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public String getTagTypeName() {
        return tagTypeName;
    }

    public void setTagTypeName(String tagTypeName) {
        this.tagTypeName = tagTypeName;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int[] getSpans() {
        return spans;
    }

    public void setSpans(String spansString) throws MaeException {
        this.spans = SpanHandler.convertStringToArray(spansString);
    }

    public void setSpans(String start, String end) {
        int s = Integer.parseInt(start);
        int e = Integer.parseInt(end);
        if (s == -1 && e == -1) {
            this.spans = new int[0];
        } else {
            this.spans = SpanHandler.range(s, e);
        }
    }

    public void setSpans(int[] spans) {
        this.spans = spans;
    }

    public String toString() {
        if (isLink) {
            return String.format("LINK: %s-%s", tid, tagTypeName);
        } else {
            return String.format("EXT: %s (%s)", tid, text);
        }
    }
}
