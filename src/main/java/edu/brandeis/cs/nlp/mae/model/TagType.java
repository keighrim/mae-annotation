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
import java.util.LinkedList;
import java.util.List;

/**
 * Created by krim on 11/19/15.
 */

@DatabaseTable(tableName = DBSchema.TAB_TT)
public class TagType implements IModel, Comparable<TagType> {

    @DatabaseField(id = true, columnName = DBSchema.TAB_TT_COL_NAME)
    private String name;

    @DatabaseField(unique = true, canBeNull = false, columnName = DBSchema.TAB_TT_COL_PREFIX)
    private String prefix;

    @DatabaseField(canBeNull = false, columnName = DBSchema.TAB_TT_COL_ISLINK)
    private boolean isLink;

    @DatabaseField
    private boolean isNonConsuming;

    @ForeignCollectionField(eager = true)
    private ForeignCollection<AttributeType> attributeTypes;

    @ForeignCollectionField(eager = true)
    private ForeignCollection<ArgumentType> argumentTypes;

    @ForeignCollectionField(eager = true)
    private ForeignCollection<ExtentTag> extentTags;

    @ForeignCollectionField(eager = true)
    private ForeignCollection<LinkTag> linkTags;

    public TagType() {

    }

    public TagType(String name, String prefix, boolean isLink) {
        this.setName(name);
        this.setPrefix(prefix);
        // next two setters should be called in order (setting non-consuming makes the type to be extent)
        this.setNonConsuming(false);
        this.setLink(isLink);
        // TODO 151209 color is not responsible for this model class, it's an UI component
//        this.setColor(Colors.getNextColor());

    }

    public String getId() {
        return this.getName();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isExtent() {
//        return isNonConsuming() || getArgumentTypes() == null || getArgumentTypes().size() == 0;
        return !isLink;
    }

    public void setLink(boolean isLink) {
        this.isLink = isLink;
    }

    public boolean isLink() {
        return isLink;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public boolean isConsuming() {
        return !isNonConsuming;
    }

    public boolean isNonConsuming() {
        return isNonConsuming;
    }

    public void setNonConsuming(boolean nonConsuming) {
        isNonConsuming = nonConsuming;
        setLink(false);
    }

    public ForeignCollection<AttributeType> getAttributeTypes() {
        return attributeTypes;
    }

    public ForeignCollection<ArgumentType> getArgumentTypes() {
        return argumentTypes;
    }

    public ForeignCollection<ExtentTag> getExtentTags() {
        return this.extentTags;
    }

    public List<ExtentTag> getExtentTagsAsList(boolean consumingOnly) {
        ArrayList<ExtentTag> tags = new ArrayList<>();
        for (ExtentTag tag : getExtentTags()) {
            if (tag.isConsuming() || !consumingOnly) {
                tags.add(tag);
            }
        }
        return tags;
    }

    public ForeignCollection<LinkTag> getLinkTags() {
        return this.linkTags;
    }

    public ForeignCollection<? extends Tag> getTags() {
        if (isExtent()) {
            return getExtentTags();
        } else {
            return getLinkTags();
        }
    }

    public int getNumInstances() {
        return getTags().size();
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    public List<String> getAttributeTypesAsString() {
        LinkedList<String> attTypes = new LinkedList<>();
        for (AttributeType attType : this.getAttributeTypes()) {
            attTypes.add(attType.getName());
        }
        return attTypes;
    }

    public List<String> getArgumentTypesAsString() {
        LinkedList<String> argTypes = new LinkedList<>();
        for (ArgumentType argType : this.getArgumentTypes()) {
            argTypes.add(argType.getName());
        }
        return argTypes;
    }

    public String toString() {
        return String.format("%s - att: %s, arg: %s", getName(), getAttributeTypesAsString(), getArgumentTypesAsString());
    }

    @Override
    public boolean equals(Object tagType) {
        return tagType instanceof TagType && getName().equals(((TagType) tagType).getName());

    }

    @Override
    public int compareTo(TagType tagType) {
        return getPrefix().compareTo(tagType.getPrefix());
    }
}
