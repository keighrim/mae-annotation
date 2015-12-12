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

import edu.brandeis.cs.nlp.mae.HashCollection;
import edu.brandeis.cs.nlp.mae.MaeStrings;
import edu.brandeis.cs.nlp.mae.model.Elem;
import edu.brandeis.cs.nlp.mae.model.ElemExtent;
import edu.brandeis.cs.nlp.mae.model.ExtentTag;
import edu.brandeis.cs.nlp.mae.ui.MaeMainUI;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Called when the user selects the option to delete the highlighted rows from
 * the table in view.  Rows are removed both from the database and the table.
 */
public class RemoveSelectedTableRows implements ActionListener {
    private MaeMainUI maeMainUI;

    public RemoveSelectedTableRows(MaeMainUI maeMainUI) {
        this.maeMainUI = maeMainUI;
    }

    public void actionPerformed(ActionEvent actionEvent) {
        if (maeMainUI.showDeleteWarning()) {
            // command is concatenated ids from selected rows, so first split it
            String[] ids = actionEvent.getActionCommand().split(MaeStrings.SEP);
            for (String id : ids) {
                // load corresponding table and its back-end model
                String elemName = maeMainUI.getTask().getElemNameById(id);
                Elem elem = maeMainUI.getTask().getElemByName(elemName);
                JTable table = maeMainUI.getElementTables().get(elemName);
                DefaultTableModel tableModel
                        = (DefaultTableModel) table.getModel();

                // search in the table model for matching id, remove that row
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    if (tableModel.getValueAt(i, maeMainUI.ID_COL).equals(id)) {
                        // if removing an extent tag, re-assign highlighting
                        if (elem instanceof ElemExtent) {
                            maeMainUI.getTask().removeExtentByID(id);
                            maeMainUI.assignTextColor(ExtentTag.parseSpansString(
                                    (String) tableModel.getValueAt(i, maeMainUI.SPANS_COL)));
                            //remove links that use the tag being removed
                            HashCollection<String, String> links
                                    = maeMainUI.getTask().getLinksByExtentID(elemName, id);
                            maeMainUI.removeLinkTableRows(links);
                            // also remove item from all extents tab
                            maeMainUI.removeAllTableRow(id);
                        } else {
                            maeMainUI.getTask().removeLinkByID(id);
                        }
                        tableModel.removeRow(i);
                        break;
                    }

                }
            }
        }
        maeMainUI.setTaskChanged(true);
        maeMainUI.updateTitle();
    }
}
