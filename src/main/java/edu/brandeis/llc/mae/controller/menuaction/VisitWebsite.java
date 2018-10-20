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

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Opens a new web browser tab of the project website. The address is kept in string
 * registry class.
 */
public class VisitWebsite extends MaeActionI {

    public VisitWebsite(String text, ImageIcon icon, KeyStroke hotkey, Integer mnemonic, MaeMainController controller) {
        super(text, icon, hotkey, mnemonic, controller);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        visitWebsite();
    }

    static void visitWebsite() {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(
                        new URI(MaeStrings.PROJECT_WEBPAGE));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (URISyntaxException ignored) {
            }
        }
    }

}


