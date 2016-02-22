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

import java.util.*;

/**
 * Collection of static helper methods used within DB schema (model package)
 * Created by krim on 12/13/2015.
 */
public class ModelHelpers {

    public static Map<String, String> getArgumentTextsWithNames(Collection<Argument> arguments) {
        HashMap<String, String> argumentTextsWithNames = new HashMap<>();
        for (Argument argument : arguments) {
            argumentTextsWithNames.put(argument.getName(), argument.getArgument().getText());
        }
        return argumentTextsWithNames;

    }

    public static Map<String, String> getArgumentTidsWithNames(Collection<Argument> arguments) {
        HashMap<String, String> argumentsWithNames = new HashMap<>();
        for (Argument argument : arguments) {
            argumentsWithNames.put(argument.getName(), argument.getArgument().getTid());
        }
        return argumentsWithNames;

    }

}
