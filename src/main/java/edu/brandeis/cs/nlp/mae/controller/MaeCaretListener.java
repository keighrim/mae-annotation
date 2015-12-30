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

package edu.brandeis.cs.nlp.mae.controller;

import edu.brandeis.cs.nlp.mae.controller.Colors;
import edu.brandeis.cs.nlp.mae.controller.MaeMainUI;

import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.Highlighter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * AnnCaretListener keeps track of what extents have been selected so that other
 * methods can use that information in the display and links.
 */
public class MaeCaretListener implements CaretListener {
    private MaeMainUI maeMainUI;

    public MaeCaretListener(MaeMainUI maeMainUI) {
        this.maeMainUI = maeMainUI;
    }

    @Override
    public void caretUpdate(CaretEvent e) {
        Highlighter hl = maeMainUI.getTextPane().getHighlighter();
        //when the caret is moved, remove the any link highlights
        hl.removeAllHighlights();

        int dot = e.getDot();
        int mark = e.getMark();

        /*
        Not just set start and end field to caret selection,
        but clear the spans set first, then fill it with caret selection span.
        Consequently the array get one int[] in it.
        */

        // in normal mode, reset span for every mouse event
        if (maeMainUI.getMode() == MaeMainUI.M_NORMAL) {
            maeMainUI.resetSpans();
        }

        // before selecting a text span, clear default (-1, -1) pair in mSpan
        if (maeMainUI.isSpansEmpty()) {
            maeMainUI.getSpans().clear();
        }
        int start, end;

        // mouse is dragged
        if (dot != mark) {
            maeMainUI.setTextSelected();
            if (dot < mark) {
                start = dot;
                end = mark;
            } else {
                start = mark;
                end = dot;
            }
            if (maeMainUI.getMode() == MaeMainUI.M_MULTI_SPAN || maeMainUI.getMode() == MaeMainUI.M_ARG_SEL) {
                maeMainUI.getPrevSpans().add(new ArrayList<int[]>(maeMainUI.getSpans()));
                maeMainUI.getLastSelection().add(new int[]{start, end});
            }
            maeMainUI.getSpans().add(new int[]{start, end});
            if (maeMainUI.getMode() == MaeMainUI.M_ARG_SEL) {
                maeMainUI.updateArgList();
            }
        }

        // highlight corresponding row of table
        maeMainUI.findHighlightRows();

        // krim: need to update current selection and status bar
        if (!maeMainUI.isSpansEmpty()) {
            maeMainUI.highlightTextSpans(hl, maeMainUI.getSpans(), Colors.getDefaultHighliter());
            maeMainUI.setSpans(removeOverlapping(maeMainUI.getSpans()));
        }
        maeMainUI.updateStatusBar();

    }

    /**
     * go over current spans and remove overlaps
     */
    ArrayList<int[]> removeOverlapping(ArrayList<int[]> spans) {
        // sorting is necessary for linear looping below
        Collections.sort(spans, new Comparator<int[]>() {
            @Override
            public int compare(int[] o1, int[] o2) {
                if (o1[0] == o2[0]) {
                    return o1[1] - o2[1];
                } else {
                    return o1[0] - o2[0];
                }
            }
        });
        ArrayList<int[]> stack = new ArrayList<int[]>();
        stack.add(spans.get(0));
        spans.remove(0);
        for (int[] span : spans) {
            // linear loop, YEAH!
            int[] top = stack.remove(stack.size() - 1);
            if (top[0] <= span[0] && span[0] < top[1]) {
                // note that inequity in second part does not include equal sign
                // because one might one adjacent discontinuous spans (?)
                // e.g.> tagging over morphological affixes separately
                top[1] = Math.max(span[1], top[1]);
                stack.add(top);
            } else {
                stack.add(top);
                stack.add(span);
            }
        }
        return stack;
    }
}
