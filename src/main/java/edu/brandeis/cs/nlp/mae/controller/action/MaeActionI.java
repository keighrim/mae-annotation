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

/**
 * Created by krim on 12/30/2015.
 * Provide interface for actions associated to menu items.
 */
public abstract class MaeActionI extends AbstractAction {

    protected MaeMainController mainController;

    public MaeActionI(String text, ImageIcon icon, KeyStroke hotkey, Integer mnemonic, MaeMainController mainController) {
        super(text, icon);
        if (hotkey != null) {
            putValue(ACCELERATOR_KEY, hotkey);
        }
        if (mnemonic != null) {
            putValue(MNEMONIC_KEY, mnemonic);
        }
        this.mainController = mainController;
    }

    protected MaeMainController getMainController() {
        return mainController;
    }

    protected void catchException(Exception e) {
        getMainController().showError(e);
    }
}
