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

import com.j256.ormlite.field.DatabaseField;

/**
 * Created by krim on 12/26/2015.
 */
public class TagProperty implements ModelI {

    @DatabaseField(generatedId = true, columnName = DBSchema.TAB_AT_COL_ID)
    private int id;

    @DatabaseField(foreign = true, canBeNull = false, columnName = DBSchema.TAB_AT_FCOL_TT)
    private TagType tagType;

    @DatabaseField(canBeNull = false, columnName = DBSchema.TAB_AT_COL_NAME)
    private String name;

    @DatabaseField(canBeNull = false, columnName = DBSchema.TAB_AT_COL_REQ)
    private boolean isRequired;

    @DatabaseField(canBeNull = false, columnName = DBSchema.TAB_AT_COL_IDREF)
    private boolean isIdRef;

    public String getId() {
        return Integer.toString(id);
    }

    public TagType getTagType() {
        return tagType;
    }

    public void setTagType(TagType tagType) {
        this.tagType = tagType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isIdRef() {
        return isIdRef;
    }

    public void setIdRef(boolean idRef) {
        isIdRef = idRef;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public void setRequired(boolean required) {
        isRequired = required;
    }

    public void setId(int id) {
        this.id = id;
    }
}
