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

package edu.brandeis.cs.nlp.mae.view;

import edu.brandeis.cs.nlp.mae.MaeStrings;
import edu.brandeis.cs.nlp.mae.ui.MaeMainUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This listener is associated with 'set as argument of...' menu items
 * callable from context menus from either bottom table or main text pane
 */
public class SetAsArgListener implements ActionListener {
    private MaeMainUI maeMainUI;

    public SetAsArgListener(MaeMainUI maeMainUI) {
        this.maeMainUI = maeMainUI;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        // command looks like this:
        // linkType(0), linkId(1), argName(2), argId(3), argText(4)
        String[] command = actionEvent.getActionCommand().split(MaeStrings.SEP);
        maeMainUI.setArgumentInTable(command[0], command[1], command[2], command[3], command[4]);
        int argNum = maeMainUI.getTask().getArguments(command[0]).indexOf(command[2]);
        String argType = maeMainUI.getTask().getElemNameById(command[3]);
        maeMainUI.getTask().addArgument(command[1], argNum, command[3], argType);
    }
}
