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

package edu.brandeis.cs.nlp.mae.controller.deprecated;

import edu.brandeis.cs.nlp.mae.controller.MaeMainUI;
import edu.brandeis.cs.nlp.mae.util.ColorHandler;

import javax.swing.text.Highlighter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * Remove last selected text span from spans list
 * Used only in multi-span mode or n-ary argument selection mode
 */
public class UndoSelectListener implements ActionListener {
    private MaeMainUI maeMainUI;

    public UndoSelectListener(MaeMainUI maeMainUI) {
        this.maeMainUI = maeMainUI;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        String command = actionEvent.getActionCommand();
        if (command.equals("Undo")) {
            // by calling menu with right clicking,
            // PrevSpans and LastSel get update with a duplicate span
            maeMainUI.getPrevSpans().remove(maeMainUI.getPrevSpans().size() - 1);
            maeMainUI.getLastSelection().remove(maeMainUI.getLastSelection().size() - 1);

            maeMainUI.setSpans(maeMainUI.getPrevSpans().remove(maeMainUI.getPrevSpans().size() - 1));
            ArrayList<int[]> tmp = new ArrayList<int[]>();
            int[] lastSpan = maeMainUI.getLastSelection().remove(maeMainUI.getLastSelection().size() - 1);
            tmp.add(lastSpan);

            Highlighter hl = maeMainUI.getTextPanel().getHighlighter();
            hl.removeAllHighlights();
            maeMainUI.highlightTextSpans(hl, maeMainUI.getSpans(), ColorHandler.getDefaultHighlighter());
            maeMainUI.highlightTextSpans(hl, tmp, ColorHandler.getFadingHighlighter());

            maeMainUI.getStatusBar().setText(String.format(
                    "Removed '%s' from selection!" +
                            " Click anywhere to continue."
                    , maeMainUI.getTextBetween(lastSpan[0], lastSpan[1])));
        } else if (command.equals("Over")) {
            maeMainUI.resetSpans();
            maeMainUI.getStatusBar().setText(
                    "No text selected! Click anywhere to continue.");

        }
        maeMainUI.delayedUpdateStatusBar(1000);
        if (maeMainUI.getMode() == MaeMainUI.M_ARG_SEL) {
            maeMainUI.getPotentialArgsInSelectedOrder();
        }

    }
}
