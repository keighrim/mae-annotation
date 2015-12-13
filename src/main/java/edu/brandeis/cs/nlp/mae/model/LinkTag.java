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

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by krim on 11/19/15.
 */

@DatabaseTable(tableName = ModelStrings.TAB_LTAG)
public class LinkTag extends Tag implements IModel {

    @ForeignCollectionField
    protected ForeignCollection<Attribute> attributes;

    @ForeignCollectionField
    private ForeignCollection<Argument> arguments;

    public LinkTag() {

    }

    public LinkTag(String tid, TagType tagType) {
        super(tid, tagType);
    }

    public ForeignCollection<Argument> getArguments() {
        return arguments;
    }

    @Override
    boolean isComplete() throws SQLException {
        checkRequiredAtts();
        if (isComplete) {
            checkRequiredArgs();
        }
        return isComplete;
    }

    @Override
    public ForeignCollection<Attribute> getAttributes() {
        return attributes;
    }

    private void checkRequiredArgs() throws SQLException {
        setComplete(true);
        ArrayList<String> curArgNames = new ArrayList<String>();
        for (Argument arg : getArguments()) {
            // this for-each loop always goes through all items,
            // making sure DAO connection is closed after iteration.
            if (arg.isComplete()) {
                curArgNames.add(arg.getName());
            }
        }
        // however, next loop can be terminated in the middle of iteration.
        // so we use an iterator to close the connection when it breaks
        CloseableIterator<ArgumentType> iterArgType
                = getTagtype().getArgumentTypes().closeableIterator();
        try {
            while (iterArgType.hasNext()) {
                ArgumentType argType = iterArgType.next();
                if (argType.isRequired() && !curArgNames.contains(argType.getName())) {
                    setComplete(false);
                    break;
                }
            }
        } finally {
            iterArgType.close();
        }
    }

    public Map<String, String> getArgumentTidsWithNames() {
        return ModelHelpers.getArgumentsWithNames(this.getArguments());
    }

    @Override
    public Map<String, String> getAttbutesWithNames() {
        return ModelHelpers.getAttbutesWithNames(this.getAttributes());
    }
}
