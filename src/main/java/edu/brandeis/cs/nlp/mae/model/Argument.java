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
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by krim on 11/19/15.
 */

@DatabaseTable(tableName = DBSchema.TAB_ARG)
public class Argument implements ModelI {

    @DatabaseField(generatedId = true, columnName = DBSchema.TAB_ARG_COL_ID)
    private int id;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = DBSchema.TAB_ARG_FCOL_LTAG)
    private LinkTag linker;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = DBSchema.TAB_ARG_FCOL_ETAG)
    private ExtentTag argument;

    @DatabaseField(foreign = true, canBeNull = false, foreignAutoRefresh = true, columnName = DBSchema.TAB_ARG_FCOL_ART)
    private ArgumentType argumentType;

    public Argument() {
    }

    public Argument(ArgumentType argumentType) {
        this.setArgumentType(argumentType);

    }

    public Argument(LinkTag linker, ArgumentType argumentType, ExtentTag argument) {
        this.linker = linker;
        this.argument = argument;
        this.argumentType = argumentType;
    }

    public String getId() {
        return Integer.toString(id);
    }

    public LinkTag getLinker() {
        return linker;
    }

    public void setLinker(LinkTag linker) {
        this.linker = linker;
    }

    public String getArgumentText() {
        return getArgument().getText();
    }

    public String getArgumentId() {
        return getArgument().getId();
    }

    public ExtentTag getArgument() {
        return argument;
    }

    public void setArgument(ExtentTag argument) {
        this.argument = argument;
    }

    public ArgumentType getArgumentType() {
        return argumentType;
    }

    public void setArgumentType(ArgumentType argumentType) {
        this.argumentType = argumentType;
    }

    public boolean isComplete() {
        return getLinker() != null && getArgument() != null;
    }

    public String getName() {
        return this.getArgumentType().getName();
    }
}
