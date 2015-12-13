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

package edu.brandeis.cs.nlp.mae.ui;

import edu.brandeis.cs.nlp.mae.MaeStrings;

import javax.swing.*;
import java.awt.*;

/**
 * TabTitle is a renderer class to render tabs in the bottom table with an
 * element name and a highlight-toggling checkbox.
 */
public class TabTitle extends JPanel {
    private MaeMainUI maeMainUI;
    private String elemName;
    private Color elemColor;
    private JCheckBox toggle;
    private JLabel title;

    /**
     * constructor for a link element tab note that this constructor accepts
     * only name since a link tag doesn't have a color assigned to highlight
     *
     * @param elemName name of the element
     */
    public TabTitle(MaeMainUI maeMainUI, String elemName) {
        this.maeMainUI = maeMainUI;
        this.elemName = elemName;
        this.elemColor = Color.white;
        this.toggle = new JCheckBox();
        this.init();
    }

    /**
     * constructor for a extent element tab
     *
     * @param elemName  name of the element
     * @param elemColor color assigned to highlight the element
     */
    public TabTitle(MaeMainUI maeMainUI, String elemName, Color elemColor) {
        this.maeMainUI = maeMainUI;
        this.elemName = elemName;
        this.elemColor = elemColor;
        Icon unselected = new BorderRect(this.elemColor, 13);
        Icon selected = new ColorRect(this.elemColor, 13);
        this.toggle = new JCheckBox(unselected);
        this.toggle.setSelectedIcon(selected);
        this.init();
    }

    /**
     * common constructor
     */
    private void init() {
        // set layout and transparency
        this.setLayout(new GridBagLayout());
        this.setOpaque(false);

        // make components to be set on title and place them
        if (this.elemName.equals(MaeStrings.ALL_TABLE_TAB_BACK_NAME)) {
            this.title = new JLabel(MaeStrings.ALL_TABLE_TAB_FRONT_NAME);
        } else {
            this.title = new JLabel(this.elemName);
        }
        this.toggle.addItemListener(new ToggleHighlightListener(this.maeMainUI, elemName));
        this.add(this.toggle);
        this.add(this.title);
    }

    /**
     * returns the name of the tab of current tab *
     *
     * @return element name
     */
    String getTitleName() {
        return this.elemName;
    }

    /**
     * set the highlighting status of current tab
     *
     * @param b whether turn on or off highlighting
     */
    void setHighlighted(Boolean b) {
        this.toggle.setSelected(b);
    }

    /**
     * returns where current tab is highlighted or not
     *
     * @return true if current tab is highlighted
     */
    boolean isHighlighted() {
        return this.toggle.isSelected();
    }
}
