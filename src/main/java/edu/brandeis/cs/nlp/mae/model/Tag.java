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

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by krim on 11/19/15.
 */

public abstract class Tag {

    @DatabaseField(id = true)
    protected String tid;

    @DatabaseField(foreign = true, canBeNull = false)
    protected TagType tagtype;

    protected boolean isFulfilled;

    @ForeignCollectionField
    protected ForeignCollection<Attribute> attributes;

    public Tag() {

    }

    public Tag(String tid, TagType tagType) {
        // TODO 151209 need a method to get a proper next id, maybe in DAO?
        this.setTid(tid);
        this.setTagtype(tagType);
        this.setFulfilled(false);

    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public TagType getTagtype() {
        return tagtype;
    }

    public void setTagtype(TagType tagtype) {
        this.tagtype = tagtype;
    }

    protected void setFulfilled(boolean fulfilled) {
        isFulfilled = fulfilled;
    }

    abstract boolean isFulfilled() throws SQLException;

    protected void checkRequiredAtts() {
        setFulfilled(true);
        ArrayList<String> curAttNames = new ArrayList<String>();
        for (Attribute att : getAttributes()) {
            // this for each loop always goes through all items,
            // making sure DAO connection is closed after iteration.
            if (att.getValue() != null && att.getValue().length() > 0) {
                curAttNames.add(att.getName());
            }
        }
        for (AttributeType attType : getTagtype().getAttributeTypes()) {
            if (attType.isRequired() && !curAttNames.contains(attType.getName())) {
                setFulfilled(false);
                break;
            }
        }
    }

    public ForeignCollection<Attribute> getAttributes() {
        return attributes;
    }

    @Override
    public int hashCode() {
        return this.tid.hashCode();
    }

}
