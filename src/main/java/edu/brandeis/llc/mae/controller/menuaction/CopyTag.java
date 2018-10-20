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

import edu.brandeis.llc.mae.MaeStrings;
import edu.brandeis.llc.mae.controller.MaeMainController;
import edu.brandeis.llc.mae.database.MaeDBException;
import edu.brandeis.llc.mae.model.Tag;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Copies a selected tag from a raw annotation to gold standard.
 * Can be an extent tag or a link.
 */
public class CopyTag extends MaeActionI {

    public CopyTag(String text, ImageIcon icon, KeyStroke hotkey, Integer mnemonic, MaeMainController controller) {
        super(text, icon, hotkey, mnemonic, controller);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        try {
            String[] srcAndTid = event.getActionCommand().split(MaeStrings.SEP);
            if (getMainController().getDriver().getAnnotationFileName().endsWith(srcAndTid[0])) {
                getMainController().showError("Cannot copy a tag from itself!");
                return;
            }
            Tag original = getMainController().getTagBySourceAndTid(srcAndTid[0], srcAndTid[1]);
            getMainController().copyTag(original);
        } catch (MaeDBException e) {
            catchException(e);
        }
    }

}

