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

package edu.brandeis.cs.nlp.mae.controller.textpanel;

import edu.brandeis.cs.nlp.mae.controller.MaeMainController;
import edu.brandeis.cs.nlp.mae.view.TextPanelView;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by krim on 1/9/2017.
 */
class DocumentCloseListener implements ActionListener {
    private MaeMainController mainController;

    DocumentCloseListener(MaeMainController mainController) {
        this.mainController = mainController;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        TextPanelView.DocumentTabTitle title = getProperParent((Component) e.getSource());
        if (title.isEnabled()) {
            int tabIdx = title.getTabIndex();
            if (mainController.showIncompleteTagsWarningAt(tabIdx, false) &&
                    mainController.showUnsavedChangeWarningAt(tabIdx)) {
                mainController.closeDocumentAt(title.getTabIndex());
            }
        }

    }

    TextPanelView.DocumentTabTitle getProperParent(Component component) {
        if (component instanceof TextPanelView.DocumentTabTitle) {
            return (TextPanelView.DocumentTabTitle) component;
        } else {
            return getProperParent(component.getParent());
        }
    }
}
