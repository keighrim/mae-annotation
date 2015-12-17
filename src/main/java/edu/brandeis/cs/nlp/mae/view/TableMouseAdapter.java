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

import edu.brandeis.cs.nlp.mae.model.Elem;
import edu.brandeis.cs.nlp.mae.model.ElemExtent;
import edu.brandeis.cs.nlp.mae.model.ElemLink;
import edu.brandeis.cs.nlp.mae.ui.Colors;
import edu.brandeis.cs.nlp.mae.ui.MaeMainUI;
import edu.brandeis.cs.nlp.mae.util.SpanHandler;

import javax.swing.*;
import javax.swing.table.TableModel;
import javax.swing.text.Highlighter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.TreeSet;

/**
 * JTableListener determines if the ID of a tag has been double-clicked, and if
 * it has it highlights the appropriate text extent/extents.
 */
public class TableMouseAdapter extends MouseAdapter {

    private MaeMainUI maeMainUI;

    public TableMouseAdapter(MaeMainUI maeMainUI) {
        this.maeMainUI = maeMainUI;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        maybeShowTablePopup(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        maybeShowTablePopup(e);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            String elemName
                    = maeMainUI.getBottomTable().getTitleAt(maeMainUI.getBottomTable().getSelectedIndex());
            JTable table = maeMainUI.getElementTables().get(elemName);
            int selectedRow = table.getSelectedRow();
            TableModel tableModel = table.getModel();
            String elemId = (String) tableModel.getValueAt(selectedRow, maeMainUI.ID_COL);
            Elem el = maeMainUI.getTask().getElemByName(maeMainUI.getTask().getTagTypeByTid(elemId));
            Highlighter hl = maeMainUI.getTextPane().getHighlighter();
            hl.removeAllHighlights();

            if (el instanceof ElemExtent) {
                // use table column[1] to get spanString then parse it
                ArrayList<int[]> spansSelect = SpanHandler.convertStringToPairs(
                        (String) tableModel.getValueAt(selectedRow, maeMainUI.SPANS_COL));
                maeMainUI.highlightTextSpans(hl, spansSelect, Colors.getVividHighliter());
            } //end if ElemExtent

            // krim: below is used to highlight linked extents
            if (el instanceof ElemLink) {

                // get relevant argument columns
                TreeSet<Integer> argColumns = maeMainUI.getArgColIndices(elemName);

                int j = 0;
                for (Integer i : argColumns) {
                    String argId = (String) tableModel.getValueAt(selectedRow, i);
                    // argId can be empty (not all arguments required)
                    if (!argId.equals("")) {
                        ArrayList<int[]> argSpans = maeMainUI.getTask().getSpansByTid(argId);
                        maeMainUI.highlightTextSpans(hl, argSpans, Colors.getHighlighters()[j]);
                    }
                    j++;
                }
            }//end if ElemLink
        }
    }

    //if the user right-clicks on the table
    private void maybeShowTablePopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            maeMainUI.resetSpans();
            maeMainUI.setTablePopup(maeMainUI.createTableContextMenu(e));
            maeMainUI.getTablePopup().show(e.getComponent(),
                    e.getX(), e.getY());
        }
    }
}
