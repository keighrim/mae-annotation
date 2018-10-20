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
import edu.brandeis.llc.mae.model.Tag;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Deletes one or more tags. Deleting an extent tag will also delete all link tags
 * using that extent tag as their arguments. Thus users are warned before performing
 * any deletions.
 */
public class DeleteTag extends MaeActionI {

    public DeleteTag(String text, ImageIcon icon, KeyStroke hotkey, Integer mnemonic, MaeMainController controller) {
        super(text, icon, hotkey, mnemonic, controller);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (getMainController().showBatchDeletionWarning()) {
            String[] tids = event.getActionCommand().split(MaeStrings.SEP);
            for (String tid : tids) {
                deleteTag(tid);
            }
        }
    }

    void deleteTag(String tid) {
        Tag tag = getMainController().getTagByTid(tid);
        getMainController().deleteTag(tag);
    }
}


