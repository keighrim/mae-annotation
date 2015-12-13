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

package edu.brandeis.cs.nlp.mae.view;

import edu.brandeis.cs.nlp.mae.ui.MaeMainUI;

import javax.swing.table.DefaultTableModel;

/**
 * AnnTableModel creates a TableModel that allows the ID column to be
 * uneditable.  This helps prevent user-created database conflicts by ensuring
 * the IDs being generated will not be changed, and makes it so that users can
 * double-click on the ID in order to see where that tag appears in the text.
 */
public class MaeTableModel extends DefaultTableModel {
    static final long serialVersionUID = 552012L;

    private MaeMainUI maeMainUI;

    public MaeTableModel(MaeMainUI maeMainUI) {
        this.maeMainUI = maeMainUI;
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return (col != maeMainUI.ID_COL) && (col != maeMainUI.SRC_COL);
    }
}
