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

package edu.brandeis.llc.mae.controller.menuaction;

import edu.brandeis.llc.mae.controller.MaeMainController;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Sets the suffix from the user input, which is used at saving annotation works.
 */
public class SetSaveSuffix extends MaeActionI{

    public SetSaveSuffix(String text, ImageIcon icon, KeyStroke hotkey, Integer mnemonic, MaeMainController mainController) {
        super(text, icon, hotkey, mnemonic, mainController);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        String suffix = (String)
                JOptionPane.showInputDialog(null, "Set a file name suffix.\n" +
                                "When saving an annotation work, this suffix will be offered automatically (with an \"_\"). ",
                        "XML Suffix",
                        JOptionPane.QUESTION_MESSAGE,
                        null, null, getMainController().getSaveSuffix());
        // get null when the user hit cancel
        if (suffix != null) getMainController().setSaveSuffix(suffix);
    }
}
