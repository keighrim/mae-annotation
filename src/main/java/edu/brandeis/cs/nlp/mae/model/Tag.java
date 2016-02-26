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

package edu.brandeis.cs.nlp.mae.model;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * Created by krim on 11/19/15.
 */

public abstract class Tag implements ModelI, Comparable<Tag> {

    @DatabaseField(id = true, columnName = DBSchema.TAB_TAG_COL_TID)
    protected String tid;

    @DatabaseField(foreign = true, canBeNull = false, foreignAutoRefresh = true, columnName = DBSchema.TAB_TAG_FCOL_TT)
    protected TagType tagtype;

    @DatabaseField(columnName = DBSchema.TAB_TAG_COL_FN)
    protected String filename;

    public Tag() {

    }

    public Tag(String tid, TagType tagType,String filename) {
        this.setTid(tid);
        this.setTagtype(tagType);
        this.setFilename(filename);

    }

    public Map<String, String> getAttributesWithNames() {
        Map<String, String> attributesWithNames = new LinkedHashMap<>();
        if (getAttributes() != null && getAttributes().size() > 0) {
            for (Attribute attribute : getAttributes()) {
                attributesWithNames.put(attribute.getName(), attribute.getValue());
            }
        }
        return attributesWithNames;

    }

    public String getId() {
        return this.getTid();
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

    public String getTagTypeName() {
        return tagtype.getName();

    }

    public void setTagtype(TagType tagtype) {
        this.tagtype = tagtype;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public boolean isComplete() {
        return getUnderspec().size() == 0;
    }

    public abstract Set<String> getUnderspec();

    protected Set<String> checkRequiredAtts() {
        Set<String> underspec = new TreeSet<>();
        ArrayList<AttributeType> existingAtts = new ArrayList<>();
        for (Attribute att : getAttributes()) {
            // this for each loop always goes through all items,
            // making sure DAO connection is closed after iteration.
            if (att.getValue() != null && att.getValue().length() > 0) {
                existingAtts.add(att.getAttributeType());
            }
        }
        for (AttributeType attType : getTagtype().getAttributeTypes()) {
            if (attType.isRequired() && !existingAtts.contains(attType)) {
                underspec.add(attType.getName());
            }
        }
        return underspec;
    }

    public abstract ForeignCollection<Attribute> getAttributes();

    @Override
    public int hashCode() {
        return this.tid.hashCode();
    }

    @Override
    public boolean equals(Object tag) {
        return tag instanceof Tag
                && getId().equals(((Tag) tag).getId())
                && getFilename().equals(((Tag) tag).getFilename());
    }

    @Override
    public int compareTo(Tag tag) {
        return getId().compareTo(tag.getId());
    }

    public abstract String toJsonString();

    public abstract String toXmlString();

    protected String getAttributesXmlString() {
        Map<String, String> attMap = getAttributesWithNames();
        List<String> attList = new ArrayList<>();
        for (String attName : attMap.keySet()) {

            attList.add(String.format("%s=\"%s\"", attName, attMap.get(attName)));
        }
        return StringUtils.join(attList, " ");
    }
}
