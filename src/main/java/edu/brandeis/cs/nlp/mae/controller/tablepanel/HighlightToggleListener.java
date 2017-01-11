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

package edu.brandeis.cs.nlp.mae.controller.tablepanel;

import edu.brandeis.cs.nlp.mae.database.MaeDBException;
import edu.brandeis.cs.nlp.mae.model.TagType;
import edu.brandeis.cs.nlp.mae.view.TablePanelView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by krim on 1/11/2017.
 */
class HighlightToggleListener extends MouseAdapter implements ItemListener {

    private TablePanelController tablePanelController;
    private TagType tagType;
    private boolean forAllTagsTable;
    private TablePanelView.TogglingTabTitle toggle;

    HighlightToggleListener(TablePanelController tablePanelController, TagType tagType, boolean forAllTagsTable, TablePanelView.TogglingTabTitle toggle) {
        this.tablePanelController = tablePanelController;
        this.tagType = tagType;
        this.forAllTagsTable = forAllTagsTable;
        this.toggle = toggle;

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger() && tagType != tablePanelController.dummyForAllTagsTab) {
            callColorSetter();
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger() && tagType != tablePanelController.dummyForAllTagsTab) {
            callColorSetter();
        }
    }

    void callColorSetter() {
        Color newColor = JColorChooser.showDialog(null, "Choose a Color", toggle.getColor());
        if (newColor != null) {
            toggle.setColor(newColor);
            tablePanelController.getMainController().setFGColor(tagType, newColor);
        }
    }

    TagType getTagType() {
        return this.tagType;
    }

    @Override
    public void itemStateChanged(ItemEvent e) {

        try {
            tablePanelController.getMainController().sendWaitMessage();

            if (forAllTagsTable) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    tablePanelController.logger.debug(String.format("activated FG colors of all %d/%d tags", tablePanelController.getActiveExtentTags().size(), tablePanelController.getMainController().paintableTagTypes()));
                    for (int tabIndex = 1; tabIndex < tablePanelController.tabOrder.size(); tabIndex++) {
                        // ignore 0th tab (all tags)
                        TablePanelView.TogglingTabTitle tabTitle = tablePanelController.getTagTabTitle(tabIndex);
                        if (!tabTitle.isHighlighted() && tabTitle.getTagType().isExtent()) {
                            tabTitle.setHighlighted(true);
                        }
                    }
                } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                    tablePanelController.logger.debug(String.format("deactivated FG colors of all %d/%d tags", tablePanelController.getActiveExtentTags().size(), tablePanelController.getMainController().paintableTagTypes()));
                    for (int tabIndex = 1; tabIndex < tablePanelController.tabOrder.size(); tabIndex++) {
                        // ignore 0th tab (all tags)
                        TablePanelView.TogglingTabTitle tabTitle = tablePanelController.getTagTabTitle(tabIndex);
                        if (tabTitle.isHighlighted() && tabTitle.getTagType().isExtent()) {
                            tabTitle.setHighlighted(false);
                        }
                    }
                }
            } else {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    activateTag();
                } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                    deactivateTag();
                }
                checkAllTab();
                tablePanelController.getMainController().assignTextColorsOver(getRelevantAnchors());

            }
            tablePanelController.getMainController().updateNotificationArea();

        } catch (MaeDBException ex) {
            tablePanelController.getMainController().showError(ex);
        }

    }

    private java.util.List<Integer> getRelevantAnchors() throws MaeDBException {
        return tablePanelController.getDriver().getAllAnchorsOfTagType(tagType);

    }

    private void checkAllTab() throws MaeDBException {
        TablePanelView.TogglingTabTitle allTab = tablePanelController.getTagTabTitle(0);
        if (tablePanelController.getActiveExtentTags().size() == tablePanelController.getMainController().paintableTagTypes()) {
            allTab.setHighlighted(true);
        }
        if (tablePanelController.getActiveExtentTags().size() == 0) {
            allTab.setHighlighted(false);
        }
    }

    void activateTag() {
        if (tagType.isLink()) {
            tablePanelController.getActiveLinkTags().add(tagType);
        } else {
            tablePanelController.getActiveExtentTags().add(tagType);
            tablePanelController.logger.debug(String.format("activated: %s, now %d/%d types are activated", tagType.getName(), tablePanelController.activeExtentTags.size(), tablePanelController.getMainController().paintableTagTypes()));
        }
    }

    void deactivateTag() {
        if (tagType.isLink()) {
            tablePanelController.getActiveLinkTags().remove(tagType);
        } else {
            tablePanelController.getActiveExtentTags().remove(tagType);
            tablePanelController.logger.debug(String.format("deactivated: %s, now %d/%d types are activated", tagType.getName(), tablePanelController.activeExtentTags.size(), tablePanelController.getMainController().paintableTagTypes()));
        }

    }

}
