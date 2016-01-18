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

package edu.brandeis.cs.nlp.mae.controller.action;

import edu.brandeis.cs.nlp.mae.controller.MaeMainController;
import edu.brandeis.cs.nlp.mae.controller.MenuController;
import edu.brandeis.cs.nlp.mae.controller.TablePanelController;
import edu.brandeis.cs.nlp.mae.database.MaeDBException;
import edu.brandeis.cs.nlp.mae.model.Tag;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Called when the user selects the option to delete the highlighted rows from
 * the table in view.  Rows are removed both from the database and the table.
 */
public class RemoveTableRows extends MenuActionI {

    JTable table;

    public RemoveTableRows(String text, ImageIcon icon, KeyStroke hotkey, Integer mnemonic, MaeMainController controller) {
        super(text, icon, hotkey, mnemonic, controller);
    }

    public void setTable(JTable table) {
        this.table = table;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (getMainController().showBatchDeletionWarning()) {
            for (int row : table.getSelectedRows()) {
                String tid = (String) table.getModel().getValueAt(row, TablePanelController.ID_COL);
                Tag tag = null;
                try {
                    tag = getMainController().getDriver().getTagByTid(tid);
                } catch (MaeDBException e) {
                    getMainController().showError(e);
                }
                getMainController().removeTag(tag);
            }

        }
    }
}

