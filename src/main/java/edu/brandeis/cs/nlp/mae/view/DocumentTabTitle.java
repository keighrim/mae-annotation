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

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by krim on 1/21/17.
 */
public class DocumentTabTitle extends JPanel {
    JTabbedPane parentPane;
    JButton closeButton;
    JLabel documentLabel;
    JLabel changeState;

    public DocumentTabTitle(String label, JTabbedPane parentPane) {
        super(new GridBagLayout());
        setOpaque(false);
        this.parentPane = parentPane;
        documentLabel = new JLabel(label);
        documentLabel.setFont(new Font("dialog", Font.BOLD, 12));
        changeState = new JLabel(" ");
        changeState.setFont(documentLabel.getFont().deriveFont(Font.BOLD));
        createCloseButton(label);

        add(changeState);
        add(documentLabel);
        add(closeButton);
    }

    private void createCloseButton(String label) {
        closeButton = new JButton("x");
        closeButton.setFont(new Font("sanserif", Font.PLAIN, 9));
        closeButton.setFocusable(false);
        closeButton.setBorderPainted(false);
        closeButton.setUI(new BasicButtonUI());
        closeButton.setContentAreaFilled(false);
        closeButton.setRolloverEnabled(true);
        closeButton.setToolTipText(String.format("Close %s", label));
        closeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                Component component = e.getComponent();
                if (component instanceof AbstractButton) {
                    AbstractButton button = (AbstractButton) component;
                    button.setForeground(isEnabled() ? Color.RED : Color.LIGHT_GRAY);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                Component component = e.getComponent();
                if (component instanceof AbstractButton) {
                    AbstractButton button = (AbstractButton) component;
                    button.setForeground(Color.BLACK);
                }
            }
        });

    }

    public String getLabel() {
        return documentLabel.getText();
    }

    public void setLabel(String label) {
        documentLabel.setText(label);
        revalidate();

    }

    public int getTabIndex() {
        return parentPane.indexOfTabComponent(DocumentTabTitle.this);
    }

    public void addCloseListener(ActionListener listener) {
        closeButton.addActionListener(listener);
    }

    public void setChanged(boolean changed) {
        if (changed) {
            changeState.setText(MaeStrings.UNSAVED_INDICATOR);
        } else {
            changeState.setText(" ");
        }
    }

    public void setLabelColor(Color color) {
        documentLabel.setForeground(color);
    }
}
