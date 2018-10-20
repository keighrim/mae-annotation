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

package edu.brandeis.llc.mae.controller.tablepanel;

import edu.brandeis.llc.mae.controller.MaeMainController;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Listens to mouse event inside the table panel.
 */
class TablePanelMouseListener extends MouseAdapter {
    // note that mousePressed() and mouseReleased() are both required for os-independence

    private MaeMainController mainController;

    TablePanelMouseListener(MaeMainController mainController) {
        this.mainController = mainController;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
            createAndShowContextMenu(e);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {

        if (e.isPopupTrigger()) {

            createAndShowContextMenu(e);

        } else if (e.getClickCount() == 2) {
            JTable table = (JTable) e.getSource();
            TableModel tableModel = table.getModel();
            String tid = (String) tableModel.getValueAt(
                    table.convertRowIndexToModel(table.getSelectedRow()), TablePanelController.ID_COL);
            mainController.propagateSelectionFromTablePanel(tid);
        }
    }

    void createAndShowContextMenu(MouseEvent e) {
        // get tab and count selected rows
        JTable table = (JTable) e.getSource();

        int clickedRow = table.rowAtPoint(e.getPoint());
        // switch selection to clicked row only if one or zero row is selected before
        if (table.getSelectedRowCount() <= 1) {
            table.setRowSelectionInterval(clickedRow, clickedRow);
        }

        mainController.createTableContextMenu(table).show(e.getComponent(), e.getX(), e.getY());
    }

}
