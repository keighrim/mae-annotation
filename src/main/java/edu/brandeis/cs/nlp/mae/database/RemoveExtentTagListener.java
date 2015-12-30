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

package edu.brandeis.cs.nlp.mae.database;

import edu.brandeis.cs.nlp.mae.util.HashedList;
import edu.brandeis.cs.nlp.mae.MaeStrings;
import edu.brandeis.cs.nlp.mae.model.Elem;
import edu.brandeis.cs.nlp.mae.controller.MaeMainUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Listen to remove commmand in the text context menu
 */
public class RemoveExtentTagListener implements ActionListener {
    private MaeMainUI maeMainUI;

    public RemoveExtentTagListener(MaeMainUI maeMainUI) {
        this.maeMainUI = maeMainUI;
    }

    public void actionPerformed(ActionEvent actionEvent) {
        boolean check = maeMainUI.showDeleteWarning();
        if (check) {
            String command = actionEvent.getActionCommand();
            // command looks like this: elemName + SEP + elemId
            String elemName = command.split(MaeStrings.SEP)[0];
            String elemId = command.split(MaeStrings.SEP)[1];
            Elem elem = maeMainUI.getTask().getTagTypeByName(elemName);

            // removes extent tags and related link tags from DB
            maeMainUI.getTask().removeExtentByID(elemId);

            //remove extent tags and recolors text area
            maeMainUI.removeTableRows(elem, elemId);
            maeMainUI.removeAllTableRow(elemId);

            //remove links that use the tag being removed
            HashedList<String, String> links
                    = maeMainUI.getTask().getLinksHasArgumentOf(elemName, elemId);
            maeMainUI.removeLinkTableRows(links);
            for (String link : links.keyList()) {
                for (String linkId : links.get(link)) {
                    maeMainUI.getTask().removeLinkByID(linkId);
                }
            }

            // mark as a change
            maeMainUI.setTaskChanged(true);
            maeMainUI.updateTitle();
        }
    }
}
