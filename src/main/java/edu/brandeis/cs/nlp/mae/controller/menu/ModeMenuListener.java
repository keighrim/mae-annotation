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

package edu.brandeis.cs.nlp.mae.controller.menu;

import edu.brandeis.cs.nlp.mae.controller.MaeMainUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Listener to select special modes
 */
public class ModeMenuListener implements ActionListener {
    private MaeMainUI maeMainUI;

    public ModeMenuListener(MaeMainUI maeMainUI) {
        this.maeMainUI = maeMainUI;
    }

    // TODO add adjud mode
    public void actionPerformed(ActionEvent actionEvent) {
        int action = Integer.parseInt(actionEvent.getActionCommand());

        switch (action) {
            // return to normal mode
            case MaeMainUI.M_NORMAL:
                maeMainUI.returnToNormalMode(true);
                break;
            case MaeMainUI.M_MULTI_SPAN:
                maeMainUI.setMode(MaeMainUI.M_MULTI_SPAN);
                maeMainUI.getStatusBar().setText(
                        "Multi-span mode! Click anywhere to continue.");
                break;
            case MaeMainUI.M_ARG_SEL:
                maeMainUI.setMode(MaeMainUI.M_ARG_SEL);
                maeMainUI.getStatusBar().setText(
                        "Argument select mode! Click anywhere to continue.");
                break;
        }
        maeMainUI.updateMenus();
        maeMainUI.resetSpans();
        maeMainUI.delayedUpdateStatusBar(3000);
    }
}
