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

package edu.brandeis.cs.nlp.mae.controller;

import edu.brandeis.cs.nlp.mae.view.TabTitle;

import javax.swing.text.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * This is the class to toggle hightlight color for a specific tagnamw
 */
public class ToggleHighlightListener implements ItemListener {

    private MaeMainUI maeMainUI;
    private String elemName;
    private boolean isLink;

    public ToggleHighlightListener(MaeMainUI maeMainUI, String elemName) {
        this.maeMainUI = maeMainUI;
        this.elemName = elemName;
        this.isLink = maeMainUI.getTask().getLinkTagTypes().contains(elemName);
    }

    public String getElemName() {
        return this.elemName;
    }

    @Override
    public void itemStateChanged(ItemEvent itemEvent) {
        maeMainUI.getStatusBar().setText("Processing...");
        int index = maeMainUI.getBottomTable().indexOfTab(this.getElemName());

        TabTitle tab = (TabTitle) maeMainUI.getBottomTable().getTabComponentAt(index);
        // checking 0 might be a little bit hardcoding
        // toggle all extent elements
        if (index == 0) {
            if (tab.isHighlighted()) {
                maeMainUI.assignAllColors();
            } else {
                maeMainUI.unassignAllColors();
            }
        }
        // toggle a single link element
        else if (index > 0 && isLink) {
            if (tab.isHighlighted()) {
                maeMainUI.getActiveLinks().add(this.elemName);
                this.turnOnLink();
            } else {
                maeMainUI.getActiveLinks().remove(this.elemName);
                this.turnOffLink();
            }
        }
        // toggle a single extent element
        else {
            if (tab.isHighlighted()) {
                maeMainUI.getActiveExts().add(this.elemName);

                // when all single tabs are turned on, turn all_extents tab on
                if (maeMainUI.getActiveExts().size() == maeMainUI.getTask().getExtentTagTypes().size()) {
                    // since allTab is created after all single tabs are created
                    // getTabComponentAt() will return null while loading up
                    // a new DTD file, and will cause a nullpointer exception
                    TabTitle allTab = (TabTitle) maeMainUI.getBottomTable().getTabComponentAt(0);
                    if (allTab != null) {
                        allTab.setHighlighted(true);
                    }
                }
            } else {
                maeMainUI.getActiveExts().remove(this.elemName);

                // when all single tabs are turned off, turn all_extents tab off
                if (maeMainUI.getActiveExts().size() == 0) {
                    TabTitle allTab = (TabTitle) maeMainUI.getBottomTable().getTabComponentAt(0);
                    allTab.setHighlighted(false);
                }
            }
            this.updateElemColor();
        }
        maeMainUI.delayedUpdateStatusBar(1000);
    }

    private void turnOnLink() {
        DefaultStyledDocument styleDoc =
                (DefaultStyledDocument) maeMainUI.getTextPane().getStyledDocument();
        //get list of locations associated with the selected link
        Hashtable<Integer, String> locs
                = maeMainUI.getTask().getAllLocationsOfTagType(elemName);

        // TODO this for loop is redundant in this one and turnOff one
        for (Enumeration<Integer> e = locs.keys(); e.hasMoreElements(); ) {
            Integer i = e.nextElement();
            Element el = styleDoc.getCharacterElement(i);
            AttributeSet as = el.getAttributes();
            SimpleAttributeSet sas = new SimpleAttributeSet(as);
            StyleConstants.setItalic(sas, true);
            StyleConstants.setBold(sas, true);
            styleDoc.setCharacterAttributes(i, 1, sas, false);
        }
    }

    private void turnOffLink() {
        DefaultStyledDocument styleDoc =
                (DefaultStyledDocument) maeMainUI.getTextPane().getStyledDocument();
        //if boldness is being removed, have to make sure it doesn't
        //take away boldness of other tags that are selected
        //get list of active displays
        ArrayList<String> active = new ArrayList<String>();
        for (String linkName : maeMainUI.getActiveLinks()) {
            active.add(linkName);
        }
        active.remove(elemName);
        Hashtable<Integer, String> locs =
                maeMainUI.getTask().getAllLocationsOfTagType(elemName, active);

        for (Enumeration<Integer> e = locs.keys(); e.hasMoreElements(); ) {
            Integer i = e.nextElement();
            Element el = styleDoc.getCharacterElement(i);
            AttributeSet as = el.getAttributes();
            SimpleAttributeSet sas = new SimpleAttributeSet(as);
            StyleConstants.setItalic(sas, false);
            StyleConstants.setBold(sas, false);
            styleDoc.setCharacterAttributes(i, 1, sas, false);
        }
    }

    private void updateElemColor() {
        for (String id : maeMainUI.getTask().getAllExtentTagsOfType(this.elemName)) {
            maeMainUI.assignTextColor(maeMainUI.getTask().getSpansByTid(id));
        }
    }

}
