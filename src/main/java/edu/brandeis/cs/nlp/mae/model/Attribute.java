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

/**
 * Created by krim on 11/19/15.
 */
public class Attribute {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(foreign = true, canBeNull = false, foreignAutoRefresh = true)
    private AttributeType attributeType;

    @DatabaseField(canBeNull = false)
    private String tid;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private ExtentTag extentTag;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private LinkTag linkTag;

    @DatabaseField(canBeNull = false)
    private String value;

    public Attribute() {

    }

    public Attribute(Tag tag, AttributeType attributeType) {
        this(tag, attributeType, attributeType.getDefaultValue());
    }

    public Attribute(Tag tag, AttributeType attributeType, String value) {
        this.setAttributeType(attributeType);
        this.setTag(tag);
        this.setValue(value);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public void setValue(String value) {
        // TODO 151212 check if the value is valid (using valid valueset from aType)
        this.value = value;
    }

    public String getName() {
        return this.attributeType.getName();
    }
}
