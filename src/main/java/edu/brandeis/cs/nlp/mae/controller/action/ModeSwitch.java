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

package edu.brandeis.cs.nlp.mae.controller.action;

import edu.brandeis.cs.nlp.mae.controller.MaeMainController;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Switches between annotation modes. Simply calls main controller's switch methods.
 */
public class ModeSwitch extends MaeActionI {

    public ModeSwitch(String text, ImageIcon icon, KeyStroke hotkey, Integer mnemonic, MaeMainController controller) {
        super(text, icon, hotkey, mnemonic, controller);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        int mode = Integer.parseInt(event.getActionCommand());
        // TODO: 12/12/2016 will be more efficient to capsulate these in main controller?
        switch (mode) {
            case MaeMainController.MODE_ARG_SEL:
                getMainController().switchToArgSelMode();
                break;
            case MaeMainController.MODE_MULTI_SPAN:
                getMainController().switchToMSpanMode();
                break;
            case MaeMainController.MODE_NORMAL:
                getMainController().switchToNormalMode();
                break;
            case MaeMainController.START_ADJUD:
                getMainController().switchToAdjudMode();
                break;
        }
    }

}


