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

package edu.brandeis.cs.nlp.mae.view;

import edu.brandeis.cs.nlp.mae.MaeStrings;
import edu.brandeis.cs.nlp.mae.model.TagType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemListener;

/**
 * Created by krim on 1/2/2016.
 */
public class TablePanelView extends JPanel {

    private JTabbedPane tagTabs;

    public TablePanelView() {
        super(new BorderLayout());
        tagTabs = new JTabbedPane();
        clear();
    }

    public JTabbedPane getTabs() {
        return this.tagTabs;
    }

    public void addTab(String titleText, JComponent titleComponent, JComponent tableComponent) {
        getTabs().addTab(titleText, tableComponent);
        getTabs().setTabComponentAt(getTabs().getTabCount() - 1, titleComponent);
    }

    public JTable getTable() {
        JScrollPane sp = (JScrollPane) getTabs().getSelectedComponent();
        JTable table = (JTable) sp.getViewport().getView();
        return table;
    }

    private void clear() {

        getTabs().removeAll();
        String text = "";
        JPanel panel = new JPanel(false);
        JLabel filler = new JLabel(text);
        filler.setHorizontalAlignment(JLabel.CENTER);
        panel.setLayout(new GridLayout(1, 1));
        panel.add(filler);
        add(getTabs(), BorderLayout.CENTER);

    }

    /**
     * TabTitle is a renderer class to render tabs in the bottom table with an
     * element name and a highlight-toggling checkbox.
     */
    public static class TogglingTabTitle extends JPanel {
        private TagType tagType;
        private JCheckBox toggle;
        private JLabel titleLabel;

        /**
         * constructor for a link element tab note that this constructor accepts
         * only name since a link tag doesn't have a color assigned to highlight
         *
         * @param tagType name of the element
         */
        public TogglingTabTitle(TagType tagType) {
            this.tagType = tagType;
            this.toggle = new JCheckBox();
            this.init();
        }

        /**
         * constructor for a extent element tab
         *
         * @param tagType  name of the element
         * @param color color assigned to highlight the element
         */
        public TogglingTabTitle(TagType tagType, Color color) {
            this.tagType = tagType;
            Icon unselected = new BorderRect(color, 13);
            Icon selected = new ColorRect(color, 13);
            this.toggle = new JCheckBox(unselected);
            this.toggle.setSelectedIcon(selected);
            this.init();
        }

        /**
         * common constructor
         */
        private void init() {
            // set layout and transparency
            setLayout(new GridBagLayout());
            toggle.setOpaque(false);
            setOpaque(false);

            // make components to be set on titleLabel and place them
            if (tagType.getName().equals(MaeStrings.ALL_TABLE_TAB_BACK_NAME)) {
                titleLabel = new JLabel(MaeStrings.ALL_TABLE_TAB_FRONT_NAME);
            } else {
                titleLabel = new JLabel(tagType.getName());
            }
            add(toggle);
            add(titleLabel);
        }

        public void addToggleListener(ItemListener listener) {
            toggle.addItemListener(listener);
        }

        /**
         * returns the name of the tab of current tab *
         *
         * @return element name
         */
        public TagType getTagType() {
            return tagType;
        }

        /**
         * set the highlighting status of current tab
         *
         * @param b whether turn on or off highlighting
         */
        public void setHighlighted(Boolean b) {
            toggle.setSelected(b);
        }

        /**
         * returns where current tab is highlighted or not
         *
         * @return true if current tab is highlighted
         */
        public boolean isHighlighted() {
            return toggle.isSelected();
        }

        /**
         * Rectangle icon class to be used in tab titles as toggle buttons
         */
        public static class BorderRect implements Icon {
            private int size;
            private Color color;

            public BorderRect(Color c, int size) {
                this.color = c;
                this.size = size;
            }

            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                g.setColor(color);
                g.drawRect(x, y, size, size);
            }

            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }
        }

        /**
         * filled color icon class to be used in tab titles as toggle buttons
         */
        public static class ColorRect implements Icon {
            private int size;
            private Color color;

            public ColorRect(Color c, int size) {
                this.color = c;
                this.size = size;
            }

            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                g.setColor(color);
                g.fillRect(x, y, size, size);
            }

            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }
        }
    }


}
