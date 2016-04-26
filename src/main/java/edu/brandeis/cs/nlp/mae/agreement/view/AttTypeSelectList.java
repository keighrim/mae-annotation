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

package edu.brandeis.cs.nlp.mae.agreement.view;

import edu.brandeis.cs.nlp.mae.util.SpanHandler;

import javax.swing.*;
import java.util.List;

/**
 * Created by krim on 4/24/2016.
 */
class AttTypeSelectList extends JList<String> {

    private String tagTypeName;

    AttTypeSelectList(String tagTypeName, String[] attTypeNames) {
        super(attTypeNames);
        this.tagTypeName = tagTypeName;
        this.setSelectedIndices(SpanHandler.range(0, attTypeNames.length));
    }

    public String getTagTypeName() {
        return tagTypeName;
    }

    List<String> getSelectedAttTypes() {
        return this.getSelectedValuesList();
    }
}
