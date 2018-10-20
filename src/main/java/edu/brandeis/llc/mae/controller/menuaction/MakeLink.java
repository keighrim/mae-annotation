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
import edu.brandeis.llc.mae.database.MaeDBException;
import edu.brandeis.llc.mae.model.TagType;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Creates a new link tag with selected arguments. Selected arguments are not passed
 * as arguments, but provided from main controller. That being said, this action is
 * called from text pane context menu in arg-select mode.
 */
public class MakeLink extends MaeActionI {

    public MakeLink(String text, ImageIcon icon, KeyStroke hotkey, Integer mnemonic, MaeMainController controller) {
        super(text, icon, hotkey, mnemonic, controller);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        try {
            TagType linkType = getMainController().getDriver().getTagTypeByName(event.getActionCommand());
            getMainController().createLinkFromDialog(linkType, getMainController().getSelectedArguments());
        } catch (MaeDBException e) {
            catchException(e);
        }

    }

}

