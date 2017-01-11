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
import edu.brandeis.cs.nlp.mae.model.Tag;
import edu.brandeis.cs.nlp.mae.model.TagType;

import javax.swing.event.TableModelEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Creates a table model for tables showing extent tags while in adjudication mode.
 * Tables in adjudication mode works quite differently from tables for annotation,
 * in that they shows all tags anchored on selected text span across all annotation
 * with their degree of agreement.
 * Adjudicators can easily find and copy the most agreeable annotations, and then
 * perform additional edit manually.
 */
class AdjudicationTableModel extends TagTableModel implements AdjudicationTableModelI {
    private Set<Integer> goldTagRows;

    AdjudicationTableModel(TablePanelController tablePanelController, TagType tagType) {
        super(tablePanelController, tagType);
    }


    @Override
    protected void init() {
        goldTagRows = new HashSet<>();
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return isGoldTagRow(row) && super.isCellEditable(row, col);
    }

    @Override
    public void setRowAsGoldTag(int row) {
        goldTagRows.add(row);
    }

    @Override
    public boolean isGoldTagRow(int row) {
        return goldTagRows.contains(row);
    }

    @Override
    public int getNonGoldRowCount() {
        return getRowCount() - goldTagRows.size();

    }

    @Override
    public void clearTable() {
        goldTagRows.clear();
        for (int row = getRowCount() - 1; row >= 0; row--) {
            removeRow(row);
        }
    }

    @Override
    public void populateTable(Tag tag) throws MaeDBException {
        String annotationFileName = tablePanelController.getDriver().getAnnotationFileName();
        if (!annotationFileName.equals(tag.getFilename())) {
            addRow(tablePanelController.convertTagIntoTableRow(tag, this, this.getAssociatedTagType().isLink()));
        } else {
            setRowAsGoldTag(getRowCount());
            addRow(tablePanelController.convertTagIntoTableRow(tag, this, this.getAssociatedTagType().isLink()));
        }
    }

    @Override
    void updateRow(int row, String[] rowData) throws MaeControlException {
        // do nothing
    }

    @Override
    int searchForRowByTid(String tid) {
        // do nothing
        return -1;
    }

    @Override
    void propagateChange(TableModelEvent event, String tid, String newValue, List<Integer> oldSpans) {
        if (event.getColumn() == TablePanelController.SPANS_COL) {
            try {
                propagateToCurrentTableAndGetNewText(event, newValue, oldSpans);
            } catch (MaeException ignored) {
                // this spanstring is already validated within getMain().updateDB() method
            }
        }
    }
}
