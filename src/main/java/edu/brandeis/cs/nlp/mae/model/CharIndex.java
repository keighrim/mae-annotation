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

/**
 * Created by krim on 11/19/15.
 */

@DatabaseTable(tableName = "offset")
public class CharIndex {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(index = true, canBeNull = false)
    private int location;

    @DatabaseField(foreign = true, canBeNull = false, foreignAutoRefresh = true)
    private ExtentTag tag;

    public CharIndex() {

    }

    public CharIndex(int location, ExtentTag tag) {
        this.setLocation(location);
        this.setTag(tag);
    }

    public int getLocation() {
        return location;
    }

    public void setLocation(int location) {
        this.location = location;
    }

    public ExtentTag getTag() {
        return tag;
    }

    public void setTag(ExtentTag tag) {
        this.tag = tag;
    }

}