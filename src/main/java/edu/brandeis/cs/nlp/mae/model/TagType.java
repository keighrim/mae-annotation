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

import java.util.LinkedList;
import java.util.List;

/**
 * Created by krim on 11/19/15.
 */

@DatabaseTable(tableName = ModelStrings.TAB_TT)
public class TagType implements IModel {

    @DatabaseField(id = true, columnName = ModelStrings.TAB_TT_COL_NAME)
    private String name;

    @DatabaseField(canBeNull = false, columnName = ModelStrings.TAB_TT_COL_COLOR)
    private int color;

    @DatabaseField(unique = true, canBeNull = false,
            columnName = ModelStrings.TAB_TT_COL_PREFIX)
    private String prefix;

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

    public TagType(String name, String prefix) {
        // TODO 151209 prefix should be given from DTD loader
        this.setName(name);
        // TODO 151209 write this method
//        this.setColor(Colors.getNextColor());
        this.setPrefix(prefix);

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
        return this.getArgumentTypes() == null || this.getArgumentTypes().size() == 0;
    }

    public boolean isLink() {
        return !this.isExtent();
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
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

}
