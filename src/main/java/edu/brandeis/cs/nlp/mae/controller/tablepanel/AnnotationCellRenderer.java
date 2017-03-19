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

import edu.brandeis.cs.nlp.mae.util.FileHandler;
import edu.brandeis.cs.nlp.mae.util.FontHandler;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.JTextComponent;
import java.awt.*;

/**
 * Created by krim on 1/11/2017.
 */
class AnnotationCellRenderer extends DefaultTableCellRenderer {

    @Override
    public JTextComponent getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
        JTextComponent renderer;

        if (((TagTableModel) table.getModel()).isTextColumn(col)) {
            renderer = new JTextPane();
            int fontSize = c.getFont().getSize();
            ((JTextPane) renderer).setContentType("text/plain; charset=UTF-8");
            ((JTextPane) renderer).setStyledDocument(
                    FontHandler.stringToSimpleStyledDocument(
                            (String) value, "dialog", fontSize, getCellForeground(isSelected)));
        } else {
            renderer = new JTextArea((String) value);
            renderer.setFont(c.getFont());
        }

        if (col == TablePanelController.SRC_COL) {
            renderer.setText(FileHandler.getFileBaseName(getText()));
        }

        renderer.setMargin(new Insets(0, 2, 0, 2));
        renderer.setOpaque(true);
        renderer.setForeground(getCellForeground(isSelected));
        renderer.setToolTipText(value == null ? " " : (String) value);
        renderer.setBackground(c.getBackground());
        renderer.setBorder(hasFocus ?
                UIManager.getBorder("Table.focusCellHighlightBorder")
                : BorderFactory.createEmptyBorder(1, 1, 1, 1));

        return renderer;
    }

    Color getCellForeground(boolean isSelected) {
        if (isSelected) {
            return UIManager.getColor("Table.selectionForeground");
        } else {
            return UIManager.getColor("Table.foreground");
        }

    }

}
