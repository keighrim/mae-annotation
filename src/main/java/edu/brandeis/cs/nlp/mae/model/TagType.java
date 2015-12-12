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

/**
 * Created by krim on 11/19/15.
 */

@DatabaseTable(tableName = "tagtype")
public class TagType {

    @DatabaseField(id = true)
    private String name;

    @DatabaseField(canBeNull = false)
    private int color;

    @DatabaseField(unique = true, canBeNull = false)
    private String prefix;

    @ForeignCollectionField
    private ForeignCollection<AttributeType> attributeTypes;

    @ForeignCollectionField
    private ForeignCollection<ArgumentType> argumentTypes;

    @ForeignCollectionField
    private ForeignCollection<Tag> tags;

    public TagType() {

    }

    public TagType(String name, String prefix) {
        // TODO 151209 prefix should be given from DTD loader
        this.setName(name);
        // TODO 151209 write this method
//        this.setColor(Colors.getNextColor());
        this.setPrefix(prefix);

    }

    public boolean isExtent() {
        return this.getArgumentTypes() == null || this.getArgumentTypes().size() == 0;
    }

    public boolean isLink() {
        return !this.isExtent();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public ForeignCollection<Tag> getTags() {
        return tags;
    }

    public int getNumInstances() {
        return getTags().size();
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

}