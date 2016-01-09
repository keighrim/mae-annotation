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

import edu.brandeis.cs.nlp.mae.MaeStrings;

import javax.swing.*;
import java.io.File;

/**
 * All popups and supplement sub windows are managed within this class
 *
 * Created by krim on 1/1/2016.
 */
public class DialogController {
    JFrame parent;
    MaeMainController mainController;
    JFrame dialog;
    JFileChooser fileChooser;

    public DialogController(MaeMainController mainController) {
        this.mainController = mainController;
        this.parent = getMainController().getMainWindow();

        this.fileChooser = new JFileChooser(".");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

    }

    public JFileChooser getFileChooser() {
        return fileChooser;
    }

    public JFrame getParent() {
        return parent;
    }

    public MaeMainController getMainController() {
        return mainController;
    }

    public int showWarning(String warnMessage) {
        return JOptionPane.showConfirmDialog(getParent(), warnMessage, MaeStrings.WARN_POPUP_TITLE, JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
    }

    public void showError(String errorMessage) {
        // TODO: 1/1/2016 maybe can implement "send error log to dev" button
        JOptionPane.showMessageDialog(getParent(), errorMessage, MaeStrings.ERROR_POPUP_TITLE, JOptionPane.WARNING_MESSAGE);

    }

    public File showFileChooseDialogAndSelect() {
        JFileChooser chooser = new JFileChooser(".");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        // TODO: 1/1/2016 implement multi selection for multi file support

        if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        }
        return null;

    }

    public void showAboutDialog() {
        // TODO: 1/1/2016 write this

    }

    public void showLinkCreator() {

        // TODO: 1/1/2016 write this
    }

    public void showArgumentSetter() {
        // TODO: 1/1/2016 write this

    }

}
