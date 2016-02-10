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

import edu.brandeis.cs.nlp.mae.MaeException;
import edu.brandeis.cs.nlp.mae.MaeStrings;
import edu.brandeis.cs.nlp.mae.controller.MaeMainController;
import edu.brandeis.cs.nlp.mae.model.ExtentTag;
import edu.brandeis.cs.nlp.mae.model.TagType;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by krim on 1/23/2016.
 */
public class MakeLinkFromTable extends MakeLink {
     public MakeLinkFromTable(String text, ImageIcon icon, KeyStroke hotkey, Integer mnemonic, MaeMainController controller) {
        super(text, icon, hotkey, mnemonic, controller);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        try {
            String[] commands = event.getActionCommand().split(MaeStrings.SEP);
            TagType linkType = null;
            List<String> tids = new LinkedList<>();
            for (int i = 0; i < commands.length; i++ ) {
                if (i == 0) {
                    linkType = getMainController().getDriver().getTagTypeByName(commands[i]);
                } else {
                    tids.add(commands[i]);
                }
            }

            List<ExtentTag> tags = new LinkedList<>();
            for (String tid : tids) {
                tags.add((ExtentTag) getMainController().getTagByTid(tid));
            }
            getMainController().createLinkFromDialog(linkType, tags);
        } catch (MaeException e) {
            catchException(e);
        }

    }
}
