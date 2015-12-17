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
import org.apache.commons.lang3.StringUtils;
import edu.brandeis.cs.nlp.mae.MaeStrings;

import java.util.Arrays;
import java.util.List;

/**
 * Created by krim on 11/19/15.
 */

@DatabaseTable(tableName = DBSchema.TAB_AT)
public class AttributeType implements IModel {

    @DatabaseField(generatedId = true, columnName = DBSchema.TAB_AT_COL_ID)
    private int id;

    @DatabaseField(foreign = true, canBeNull = false, columnName = DBSchema.TAB_AT_FCOL_TT)
    private TagType tagType;

    @DatabaseField(canBeNull = false, columnName = DBSchema.TAB_AT_COL_NAME)
    private String name;

    @DatabaseField(columnName = DBSchema.TAB_AT_COL_VALUESET)
    private String valueset;

    @DatabaseField(canBeNull = false, columnName = DBSchema.TAB_AT_COL_DEFVALUE)
    private String defaultValue;

    @DatabaseField(canBeNull = false, columnName = DBSchema.TAB_AT_COL_IDREF)
    private boolean isIdRef;

    @DatabaseField(canBeNull = false, columnName = DBSchema.TAB_AT_COL_REQ)
    private boolean isRequired;

    @ForeignCollectionField(eager = true)
    private ForeignCollection<Attribute> attributes;


    public AttributeType() {

    }

    public AttributeType(TagType tagType, String name) {
        this.setTagType(tagType);
        this.setName(name);
        this.setRequired(false);
        this.setIdRef(false);
        this.defaultValue = "";
        this.valueset = null;
    }

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

    public String getValueset() {
        return valueset;
    }

    public List<String> getValuesetAsList() {
        return Arrays.asList(this.getValueset().split(MaeStrings.ATT_VALUESET_SEPARATOR));
    }

    public void setValuesetFromList(List<String> valueset) {
        this.valueset = StringUtils.join(valueset, MaeStrings.ATT_VALUESET_SEPARATOR);
    }

    public boolean isFreeText() {
        return this.valueset == null;
    }

    public boolean isFiniteValueset() {
        return !this.isFreeText();
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
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

    public ForeignCollection<Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(ForeignCollection<Attribute> attributes) {
        this.attributes = attributes;
    }

    public void setId(int id) {
        this.id = id;
    }
}
