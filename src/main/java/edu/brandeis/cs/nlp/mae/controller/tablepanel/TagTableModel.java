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

import edu.brandeis.cs.nlp.mae.MaeException;
import edu.brandeis.cs.nlp.mae.controller.MaeControlException;
import edu.brandeis.cs.nlp.mae.database.MaeDBException;
import edu.brandeis.cs.nlp.mae.model.ExtentTag;
import edu.brandeis.cs.nlp.mae.model.TagType;
import edu.brandeis.cs.nlp.mae.util.ColorHandler;
import edu.brandeis.cs.nlp.mae.util.SpanHandler;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Creates a basic table model for table panel. This basic model is used for
 * tables showing extent tags during annotation. Link tag tables and adjudication
 * tables are inherits this.
 * In this basic model, user cannot edit id column and text column.
 * Also providing a listener method to listen to any changes in table values
 * caused by user edit.
 */
public class TagTableModel extends DefaultTableModel implements TableModelListener {
    protected TablePanelController tablePanelController;
    protected TagType tagType;

    TagTableModel(TablePanelController tablePanelController, TagType tagType) {
        this.tablePanelController = tablePanelController;
        this.tagType = tagType;
        init();
    }

    protected void init() {

    }

    public TagType getAssociatedTagType() {
        return tagType;
    }

    String getAssociatedTagTypeName() {
        return tagType.getName();
    }

    void updateRow(int row, String[] rowData) throws MaeControlException {
        if (this.getColumnCount() != rowData.length) {
            throw new MaeControlException(String.format("the data for a new row does not fit to \"%s\" table.", getAssociatedTagTypeName()));
        }
        for (int col = 0; col < rowData.length; col++) {
            setValueAt(rowData[col], row, col);
        }
    }

    int searchForRowByTid(String tid) {
        for (int row = 0; row < getRowCount(); row++) {
            if (getValueAt(row, TablePanelController.ID_COL).equals(tid)) {
                return row;
            }
        }
        return getRowCount();

    }

    int searchForColumnByColName(String colName) {
        for (int col = 0; col < getColumnCount(); col++) {
            if (getColumnName(col).equals(colName)) {
                return col;
            }
        }
        return -1;

    }

    Set<Integer> getTextColumns() {
        Set<Integer> textCol = new HashSet<>();
        textCol.add(TablePanelController.TEXT_COL);
        return textCol;

    }

    boolean isTextColumn(int col) {
        return getTextColumns().contains(col);
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return (col != TablePanelController.ID_COL) && (col != TablePanelController.SRC_COL) && (col != TablePanelController.TEXT_COL);
    }

    @Override
    public void tableChanged(TableModelEvent event) {
        tablePanelController.logger.debug(String.format("\"%s\" table changed: %d at %d, %d (INS=1, UPD=0, DEL=-1)", getAssociatedTagTypeName(), event.getType(), event.getFirstRow(), event.getColumn()));

        if (event.getFirstRow() == -1 && event.getColumn() == -1) {
            // ignore changes happened out of table (when initially setting up tables)
            return;
        }
        if (event.getType() == TableModelEvent.UPDATE) {
            // INSERT: listen to insertion is unnecessary, since adding a new tag never happens through table
            // DELETE: since we cannot recover what's already deleted anyway,
            // propagated deletion should be called right before the deletion of a row happens
            // that is, in DeleteTag action, not here, after deletion
            String tid = (String) getValueAt(event.getFirstRow(), TablePanelController.ID_COL);
            List<Integer> oldSpans = Collections.emptyList();
            try {
                oldSpans = tablePanelController.getDriver().getAnchorLocationsByTid(tid);
            } catch (MaeDBException e) {
                tablePanelController.getMainController().showError(e);
            }
            String colName = getColumnName(event.getColumn());
            String value = (String) getValueAt(event.getFirstRow(), event.getColumn());
            // this will return false if update fails
            boolean updated = tablePanelController.getMainController().updateDBFromTableUpdate(tid, colName, value);
            if (!updated) {
                revertChange(event.getFirstRow(), event.getColumn());
            } else {
                propagateChange(event, tid, value, oldSpans);
            }

        }
    }

    void propagateChange(TableModelEvent event, String tid, String newValue, List<Integer> oldSpans) {
        if (event.getColumn() == TablePanelController.SPANS_COL) {
            try {
                // update adjacent text column
                String newText = propagateToCurrentTableAndGetNewText(event, newValue, oldSpans);
                propagateToAssociatedTables(tid, newValue, newText);
            } catch (MaeException ignored) {
                // this spanstring is already validated within getMain().updateDB() method
            }
        }
    }

    String propagateToCurrentTableAndGetNewText(TableModelEvent event, String newValue, List<Integer> oldSpans) throws MaeException {
        String newText = updateTextColumnFromSpansChange(event.getFirstRow(), newValue);
        tablePanelController.getMainController().assignTextColorsOver(oldSpans);
        List<Integer> newSpans = SpanHandler.convertIntegerarrayToIntegerlist(SpanHandler.convertStringToArray(newValue));
        tablePanelController.getMainController().assignTextColorsOver(newSpans);
        tablePanelController.getMainController().removeAllBGColors();
        tablePanelController.getMainController().addBGColorOver(newSpans, ColorHandler.getVividHighliter());
        return newText;
    }

    private String updateTextColumnFromSpansChange(int rowToUpdate, String value) throws MaeException {
        int[] newSpans = SpanHandler.convertStringToArray(value);
        String newText = tablePanelController.getMainController().getTextIn(newSpans);
        setValueAt(newText, rowToUpdate, TablePanelController.TEXT_COL);
        return newText;
    }

    private void propagateToAssociatedTables(String tid, String newValue, String newText) throws MaeDBException {
        tablePanelController.updateAllTagsTableRow(tid, newValue, newText);
        tablePanelController.updateAssociatedLinkTagRows(tid, newText);
    }

    void revertChange(int row, int col) {
        // ID_COL and TEXT_COL are not editable at the first place
        // and except for SPANS_COL, everything else are attribute columns
        String tid = (String) getValueAt(row, TablePanelController.ID_COL);
        ExtentTag tag = (ExtentTag) tablePanelController.getMainController().getTagByTid(tid);
        String oldVal;
        if (col == TablePanelController.SPANS_COL) {
            oldVal = tag.getSpansAsString();
        } else {
            String attType = getColumnName(col);
            oldVal = tag.getAttributesWithNames().get(attType);
        }
        setValueAt(oldVal, row, col);
    }
}
