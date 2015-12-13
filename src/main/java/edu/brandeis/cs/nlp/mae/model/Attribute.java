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

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.List;

/**
 * Created by krim on 11/19/15.
 */

@DatabaseTable(tableName = ModelStrings.TAB_ATT)
public class Attribute {

    @DatabaseField(generatedId = true, columnName = ModelStrings.TAB_ATT_COL_ID)
    private int id;

    @DatabaseField(foreign = true, canBeNull = false, foreignAutoRefresh = true,
            columnName = ModelStrings.TAB_ATT_FCOL_AT)
    private AttributeType attributeType;

    @DatabaseField(canBeNull = false, columnName = ModelStrings.TAB_ATT_COL_TID)
    private String tid;

    @DatabaseField(foreign = true, foreignAutoRefresh = true,
            columnName = ModelStrings.TAB_ATT_FCOL_ETAG)
    private ExtentTag extentTag;

    @DatabaseField(foreign = true, foreignAutoRefresh = true,
            columnName = ModelStrings.TAB_ATT_FCOL_LTAG)
    private LinkTag linkTag;

    @DatabaseField(canBeNull = false, columnName = ModelStrings.TAB_ATT_COL_VALUE)
    private String value;

    public Attribute() {

    }

    public Attribute(Tag tag, AttributeType attributeType) throws ModelException {
        this(tag, attributeType, attributeType.getDefaultValue());
    }

    public Attribute(Tag tag, AttributeType attributeType, String value)
            throws ModelException {
        this.setAttributeType(attributeType);
        this.setTag(tag);
        try {
            this.setValue(value);
        } catch (ModelException ex) {
            throw ex;
        }
    }

    public int getId() {
        return id;
    }

    public ExtentTag getExtentTag() {
        return extentTag;
    }

    public void setExtentTag(ExtentTag extentTag) {
        this.extentTag = extentTag;
    }

    public LinkTag getLinkTag() {
        return linkTag;
    }

    public void setLinkTag(LinkTag linkTag) {
        this.linkTag = linkTag;
    }

    public AttributeType getAttributeType() {
        return attributeType;
    }

    public void setAttributeType(AttributeType attributeType) {
        this.attributeType = attributeType;
    }

    public String getTid() {
        return this.tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public void setTag(Tag tag) {
        if (tag instanceof ExtentTag) {
            this.setExtentTag((ExtentTag) tag);
        } else if (tag instanceof LinkTag) {
            this.setLinkTag((LinkTag) tag);
        }
        this.setTid(tag.getTid());
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) throws ModelException {
        if (this.getAttributeType().getValueset() == null) {
            this.value = value;
        } else {
            List<String> validValues = this.getAttributeType().getValuesetAsList();
            if (validValues.contains(value)) {
                this.value = value;
            } else {
                throw new ModelException(String.format(
                        "%s: \"%s\" is not a valid value for %s",
                        this.getClass().getSimpleName(), value, this.getName()));
            }
        }
    }

    public String getName() {
        return this.attributeType.getName();
    }
}
