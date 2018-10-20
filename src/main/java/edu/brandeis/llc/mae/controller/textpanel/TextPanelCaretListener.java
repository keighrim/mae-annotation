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

package edu.brandeis.llc.mae.controller.textpanel;

import edu.brandeis.llc.mae.controller.MaeMainController;
import edu.brandeis.llc.mae.database.MaeDBException;

import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

/**
 * Created by krim on 1/9/2017.
 */
class TextPanelCaretListener implements CaretListener {

    private TextPanelController textPanelController;
    private MaeMainController mainController;

    TextPanelCaretListener(TextPanelController textPanelController,
                           MaeMainController mainController) {
        this.textPanelController = textPanelController;
        this.mainController = mainController;
    }

    boolean acceptingSingleClick() {
        return mainController.getMode() == MaeMainController.MODE_ARG_SEL;

    }

    @Override
    public void caretUpdate(CaretEvent e) {

        try {
            if (e.getDot() != e.getMark()) { // that is, mouse is dragged and text is selected
                addDraggedSelection(e.getDot(), e.getMark());
            } else if (mainController.getMode() == MaeMainController.MODE_MULTI_SPAN) {
                // MSPAN mode always ignore single click
            } else {
                if (mainController.getMode() == MaeMainController.MODE_NORMAL) {
                    textPanelController.clearSelection(); // single click will clear out prev selection
                }
                if (acceptingSingleClick()) {
                    textPanelController.addSelection(new int[]{e.getDot(), e.getDot() + 1});
                }
            }
        } catch (MaeDBException ex) {
            mainController.showError(ex);
        }
        textPanelController.repaintBGColor();
        mainController.propagateSelectionFromTextPanel();
    }

    void addDraggedSelection(int dot, int mark) throws MaeDBException {
        int start = Math.min(dot, mark);
        int end = Math.max(dot, mark);
        if (mainController.getMode() == MaeMainController.MODE_NORMAL) {
            // in normal mode, clear selection before adding a new selection
            textPanelController.clearSelection();
        }
        textPanelController.addSelection(new int[]{start, end});
    }
}
