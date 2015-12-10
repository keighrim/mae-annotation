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
public class Argument {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(foreign = true)
    private LinkTag linker;

    @DatabaseField(foreign = true)
    private ExtentTag argument;

    @DatabaseField(canBeNull = false)
    private ArgumentType argumentType;

    private boolean isFulfilled;


    public Argument() {
    }

    public Argument(ArgumentType argumentType) {
        this.setArgumentType(argumentType);
        this.setFulfilled(false);

    }

    public Argument(LinkTag linker, ExtentTag argument, ArgumentType argumentType) {
        this.linker = linker;
        this.argument = argument;
        this.argumentType = argumentType;
        this.setFulfilled(true);
    }

    public LinkTag getLinker() {
        return linker;
    }

    public void setLinker(LinkTag linker) {
        this.linker = linker;
        if (!this.isFulfilled && this.getArgument() != null) {
            this.setFulfilled(true);
        }
    }

    public ExtentTag getArgument() {
        return argument;
    }

    public void setArgument(ExtentTag argument) {
        this.argument = argument;
        if (!this.isFulfilled && this.getLinker() != null) {
            this.setFulfilled(true);
        }
    }

    public ArgumentType getArgumentType() {
        return argumentType;
    }

    public void setArgumentType(ArgumentType argumentType) {
        this.argumentType = argumentType;
    }

    public boolean isFulfilled() {
        return isFulfilled;
    }

    public void setFulfilled(boolean fulfilled) {
        isFulfilled = fulfilled;
    }

    public String getName() {
        return this.getArgumentType().getName();
    }
}
