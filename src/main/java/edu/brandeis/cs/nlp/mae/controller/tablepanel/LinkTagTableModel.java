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

import edu.brandeis.cs.nlp.mae.MaeStrings;
import edu.brandeis.cs.nlp.mae.database.MaeDBException;
import edu.brandeis.cs.nlp.mae.model.ExtentTag;
import edu.brandeis.cs.nlp.mae.model.LinkTag;
import edu.brandeis.cs.nlp.mae.model.TagType;
import edu.brandeis.cs.nlp.mae.util.ColorHandler;

import javax.swing.event.TableModelEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Creates a table model for tables showing link tags in annotation mode.
 * Unlike the model for extent tag table, link tag table model disables user edits
 * on link id, argument id and argument text columns.
 */
class LinkTagTableModel extends TagTableModel {
    private Set<Integer> argumentTextColumns;

    LinkTagTableModel(TablePanelController tablePanelController, TagType tagType) {
        super(tablePanelController, tagType);
    }

    @Override
    protected void init() {
        argumentTextColumns = new HashSet<>();
    }

    @Override
    public Set<Integer> getTextColumns() {
        return argumentTextColumns;

    }

    void addArgumentTextColumn(int col) {
        argumentTextColumns.add(col);
    }

    boolean isArgumentTextColumn(int col) {
        return argumentTextColumns.contains(col);
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return col != TablePanelController.ID_COL && col != TablePanelController.SRC_COL && !isArgumentTextColumn(col);
    }

    @Override
    void propagateChange(TableModelEvent event, String tid, String newValue, List<Integer> oldSpans) {
        if (argumentTextColumns.contains(event.getColumn() + 1)) {
            // update adjacent text column
            if (newValue.length() == 0) {
                setValueAt("", event.getFirstRow(), event.getColumn() + 1);
            } else {
                ExtentTag newArg = (ExtentTag) tablePanelController.getMainController().getTagByTid(newValue);
                String newText = newArg.getText();
                setValueAt(newText, event.getFirstRow(), event.getColumn() + 1);
            }
            tablePanelController.getMainController().removeAllBGColors();
            try {
                List<Integer> newSpans = tablePanelController.getDriver().getAnchorLocationsByTid(tid);
                tablePanelController.getMainController().assignTextColorsOver(oldSpans);
                tablePanelController.getMainController().assignTextColorsOver(newSpans);
                tablePanelController.getMainController().removeAllBGColors();
                tablePanelController.getMainController().addBGColorOver(newSpans, ColorHandler.getVividHighliter());
            } catch (MaeDBException e) {
                tablePanelController.getMainController().showError(e);
            }

        }
    }

    @Override
    void revertChange(int row, int col) {
        String tid = (String) getValueAt(row, TablePanelController.ID_COL);
        LinkTag tag = (LinkTag) tablePanelController.getMainController().getTagByTid(tid);
        String oldVal;
        if (argumentTextColumns.contains(col + 1)) {
            String argType = getColumnName(col);
            argType = argType.substring(0, argType.length() - MaeStrings.ARG_IDCOL_SUF.length());
            oldVal = tag.getArgumentTidsWithNames().get(argType);
        } else {
            String attType = getColumnName(col);
            oldVal = tag.getAttributesWithNames().get(attType);
        }
        setValueAt(oldVal, row, col);
    }

}
