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

package edu.brandeis.llc.mae.controller.tablepanel;

import edu.brandeis.llc.mae.database.MaeDBException;
import edu.brandeis.llc.mae.model.Tag;
import edu.brandeis.llc.mae.model.TagType;

import java.util.HashSet;
import java.util.Set;

/**
 * Creates a table model for table showing link tags during adjudication.
 * Most implementation of methods from the adjud-table-model interface are duplicates
 */
class AdjudicationLinkTableModel extends LinkTagTableModel implements AdjudicationTableModelI {
    private Set<Integer> goldTagRows;

    AdjudicationLinkTableModel(TablePanelController tablePanelController, TagType tagType) {
        super(tablePanelController, tagType);
    }

    @Override
    protected void init() {
        super.init();
        goldTagRows = new HashSet<>();
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return isGoldTagRow(row) && col != TablePanelController.ID_COL && col != TablePanelController.SRC_COL && !isArgumentTextColumn(col);
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
        if (annotationFileName.equals(tag.getFilename())) {
            setRowAsGoldTag(getRowCount());
            addRow(tablePanelController.convertTagIntoTableRow(tag, this, this.getAssociatedTagType().isLink()));
        } else {
            addRow(tablePanelController.convertTagIntoTableRow(tag, this, this.getAssociatedTagType().isLink()));

        }
    }

}
