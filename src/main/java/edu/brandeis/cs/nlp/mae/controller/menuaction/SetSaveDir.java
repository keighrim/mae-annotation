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

package edu.brandeis.cs.nlp.mae.controller.menuaction;

import edu.brandeis.cs.nlp.mae.controller.MaeMainController;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * The user sets the directory to store annotation works. File Choose is provided.
 */
public class SetSaveDir extends MaeActionI {

    public SetSaveDir(String text, ImageIcon icon, KeyStroke hotkey, Integer mnemonic, MaeMainController mainController) {
        super(text, icon, hotkey, mnemonic, mainController);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        String curDir = getMainController().getSaveDirectory();
        if (curDir == null) curDir = "";

        int response = JOptionPane.showConfirmDialog(null,
                "Choose a directory for saving XML files.\n" +
                        "When saving an annotation work, this directory will be offered by default.\n\n" +
                        "Current Location:\n" + (curDir.length() > 0 ? curDir : "Not set") + "\n\n" +
                        "Would you like to change?"
                , "Default Save Location",
                JOptionPane.YES_NO_OPTION);
        if (response == JOptionPane.YES_OPTION) {
            JFileChooser fileChooser;
            if (curDir.length() < 1) {
                fileChooser = new JFileChooser(getMainController().getLastWorkingDirectory());
            } else {
                fileChooser = new JFileChooser(curDir);
            }
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                curDir = fileChooser.getSelectedFile().getAbsolutePath();
            }
        }
        getMainController().setSaveDirectory(curDir);
    }
}
