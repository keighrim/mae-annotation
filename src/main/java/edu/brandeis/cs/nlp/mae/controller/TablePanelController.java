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

package edu.brandeis.cs.nlp.mae.controller;

import edu.brandeis.cs.nlp.mae.MaeException;
import edu.brandeis.cs.nlp.mae.MaeStrings;
import edu.brandeis.cs.nlp.mae.database.MaeDBException;
import edu.brandeis.cs.nlp.mae.model.*;
import edu.brandeis.cs.nlp.mae.util.ColorHandler;
import edu.brandeis.cs.nlp.mae.util.SpanHandler;
import edu.brandeis.cs.nlp.mae.view.TablePanelView;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;

/**
 * Created by krim on 12/31/2015.
 */
class TablePanelController extends MaeControllerI {

    // column number of some fixed attributes
    public static final int SRC_COL = 0;
    public static final int ID_COL = 1;
    public static final int SPANS_COL = 2;
    public static final int TEXT_COL = 3;

    TablePanelView view;
    private TagType dummyForAllTagTab;
    private Set<TagType> activeLinkTags;
    private Set<TagType> activeExtentTags;
    private List<TagType> tabOrder;
    private Map<String, JTable> tableMap;

    TablePanelController(MaeMainController mainController) throws MaeControlException, MaeDBException {
        super(mainController);
        dummyForAllTagTab = new TagType(MaeStrings.ALL_TABLE_TAB_BACK_NAME, MaeStrings.ALL_TABLE_TAB_PREFIX, false);
        view = new TablePanelView();
        emptyTagTables();

    }


    Set<TagType> getActiveTags() {
        Set<TagType> types = new HashSet<>();
        types.addAll(activeLinkTags);
        types.addAll(activeExtentTags);
        return types;
    }

    Set<TagType> getActiveExtentTags() {
        return activeExtentTags;
    }

    Set<TagType> getActiveLinkTags() {
        return activeLinkTags;
    }

    @Override
    protected TablePanelView getView() {
        return view;
    }


    void emptyTagTables() {
        getView().getTabs().removeAll();
        activeExtentTags = new HashSet<>();
        activeLinkTags = new HashSet<>();
        tabOrder = new ArrayList<>();
        tableMap = new TreeMap<>();

    }

    void makeAllTables() throws MaeDBException, MaeControlException {
        if (!getMainController().isTaskLoaded()) {
            throw new MaeControlException("Cannot make tables without a task definition!");
        }
        emptyTagTables();
        List<TagType> types = getDriver().getAllTagTypes();
        logger.debug(String.format("start creating tables for %d tag types", types.size()));

        if (types.size() > 20) {
            getView().getTabs().setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        } else {
            getView().getTabs().setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
        }

        // create a tab for all extents and place it at first
        getView().addTab(MaeStrings.ALL_TABLE_TAB_BACK_NAME, new TablePanelView.TogglingTabTitle(dummyForAllTagTab), makeAllTagTable());
        // then create tabs for each element in the annotation task
        for (TagType type : types) {
            String name = type.getName();
            TablePanelView.TogglingTabTitle title;
            if (type.isExtent()) {
                title = new TablePanelView.TogglingTabTitle(type, getMainController().getFGColor(type));
            } else {
                title = new TablePanelView.TogglingTabTitle(type);
            }
            getView().addTab(name, title, makeTagTable(type));
        }

        getActiveLinkTags().clear();
        getActiveExtentTags().clear();
        addToggleListeners();
    }

    void wipeAllTables() {
        for (String tagTypeName : tableMap.keySet()) {
            TagTableModel model = (TagTableModel) tableMap.get(tagTypeName).getModel();
            if (!tagTypeName.equals(MaeStrings.ALL_TABLE_TAB_BACK_NAME)) {
                for (TableModelListener listener : model.getTableModelListeners()) {
                    if (listener instanceof TagTableModel) {
                        model.removeTableModelListener(listener);
                    }
                }

            }
            int stored = model.getRowCount();
            for (int row = stored - 1; row >= 0; row--) {
                model.removeRow(row);
            }
        }

    }

    void insertAllTags() throws MaeControlException, MaeDBException {
        if (!getMainController().isTaskLoaded() || !getMainController().isDocumentOpen()) {
            throw new MaeControlException("Cannot populate tables without a document open!");
        }
        for (TagType type : tabOrder) {
            if (type.equals(dummyForAllTagTab)) {
            } else if (type.isExtent()) {
                for (ExtentTag tag : getDriver().getAllExtentTagsOfType(type)) {
                    insertTagIntoTable(tag);
                }
            } else {
                for (LinkTag tag : getDriver().getAllLinkTagsOfType(type)) {
                    insertTagIntoTable(tag);
                }
            }
        }
        addMouseListeners();
        addTableModelListeners();

    }

    int getTabIndexOfTagType(TagType type) {
        return tabOrder.indexOf(type);
    }

    TablePanelView.TogglingTabTitle getTagTabTitle(int tabIndex) {
        return (TablePanelView.TogglingTabTitle) getView().getTabs().getTabComponentAt(tabIndex);
    }

    void insertValueIntoCell(Tag tag, String colName, String value) {
        TagTableModel model = (TagTableModel) tableMap.get(tag.getTagtype().getName()).getModel();
        int row = model.searchForRowByTid(tag.getTid());
        int col = model.searchForColumnByColName(colName);
        model.setValueAt(value, row, col);
    }

    void insertTagIntoTable(Tag tag) throws MaeDBException, MaeControlException {
        TagTableModel tableModel = (TagTableModel) tableMap.get(tag.getTagTypeName()).getModel();
        int newRowNum = tableModel.searchForRowByTid(tag.getId());
        insertRowData(tableModel, newRowNum, convertTagIntoRow(tag, tableModel));
        if (tag.getTagtype().isExtent()) {
            insertTagToAllTagsTable(tag);
        }
    }

    private void insertRowData(TagTableModel tableModel, int insertAt, String[] newRowData) throws MaeControlException {

        if (insertAt == tableModel.getRowCount()) {
            tableModel.addRow(newRowData);
            logger.debug(String.format("inserting a new row, %s, to \"%s\" table, now has %d rows", Arrays.toString(newRowData), tableModel.getAssociatedTagTypeName(), tableModel.getRowCount()));
        } else if (insertAt < tableModel.getRowCount()) {
            tableModel.updateRow(insertAt, newRowData);
            logger.debug(String.format("updating a row, %s, to \"%s\" table at %d", Arrays.toString(newRowData), tableModel.getAssociatedTagTypeName(), insertAt));
        } else {
            throw (new MaeControlException("cannot add a row!"));
        }
    }

    private String[] convertTagIntoRow(Tag tag, TagTableModel tableModel) throws MaeDBException {
        String[] newRow = new String[tableModel.getColumnCount()];
        Map<String, String> attMap = tag.getAttributesWithNames();
        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            String colName = tableModel.getColumnName(i);
            switch (colName) {
                case MaeStrings.SRC_COL_NAME:
                    newRow[i] = getDriver().getAnnotationFileBaseName();
                    break;
                case MaeStrings.ID_COL_NAME:
                    newRow[i] = tag.getId();
                    break;
                case MaeStrings.SPANS_COL_NAME:
                    newRow[i] = ((ExtentTag) tag).getSpansAsString();
                    break;
                case MaeStrings.TEXT_COL_NAME:
                    newRow[i] = ((ExtentTag) tag).getText();
                    break;
                default:
                    newRow[i] = attMap.get(colName);
                    break;
            }
        }
        return newRow;
    }

    private void insertTagToAllTagsTable(Tag tag) throws MaeControlException, MaeDBException {
        UneditableTableModel tableModel = (UneditableTableModel) tableMap.get(MaeStrings.ALL_TABLE_TAB_BACK_NAME).getModel();
        insertRowData(tableModel, tableModel.searchForRowByTid(tag.getId()), convertTagIntoSimplifiedRow((ExtentTag) tag));
    }

    private String[] convertTagIntoSimplifiedRow(ExtentTag tag) throws MaeDBException {
        return new String[]{getDriver().getAnnotationFileBaseName(), tag.getId(), tag.getSpansAsString(), tag.getText()};
    }

    void selectTabOf(TagType type) {
        getView().getTabs().setSelectedIndex(tabOrder.indexOf(type));
    }

    void selectTagFromTable(Tag tag) throws MaeDBException {
        JTable table = tableMap.get(tag.getTagTypeName());
        table.clearSelection();
        TagTableModel tableModel = (TagTableModel) table.getModel();
        int viewIndex = table.convertRowIndexToView(tableModel.searchForRowByTid(tag.getId()));
        table.addRowSelectionInterval(viewIndex, viewIndex);
        table.scrollRectToVisible(table.getCellRect(viewIndex, 0, true));
        if (tag.getTagtype().isExtent()) {
            selectTagFromAllTagsTable(tag.getId());
            for (LinkTag link : getDriver().getLinksHasArgumentTag((ExtentTag) tag)) {
                selectTagFromTable(link);
            }
        }
    }

    private void selectTagFromAllTagsTable(String tid) {
        JTable table = tableMap.get(MaeStrings.ALL_TABLE_TAB_BACK_NAME);
        UneditableTableModel tableModel = (UneditableTableModel) table.getModel();
        int viewIndex = table.convertRowIndexToView(tableModel.searchForRowByTid(tid));
        table.addRowSelectionInterval(viewIndex, viewIndex);
        table.scrollRectToVisible(table.getCellRect(viewIndex, 0, true));
    }

    void removeTagFromTable(Tag tag) throws MaeDBException {
        TagTableModel tableModel = (TagTableModel) tableMap.get(tag.getTagTypeName()).getModel();
        logger.debug(String.format("removing a row, %s from \"%s\" table, current has %d rows", tag.toString(), tableModel.getAssociatedTagTypeName(), tableModel.getRowCount()));
        if (tag.getTagtype().isExtent()) {
            logger.debug(String.format("however, first, removing the mirrored row from all tags table, %s", tag.toString()));
            removeTagFromAllTagsTable(tag.getId());
            logger.debug(String.format("next, removing link tags associated to %s", tag.toString()));
            for (LinkTag link : getDriver().getLinksHasArgumentTag((ExtentTag) tag)) {
                removeTagFromTable(link);
            }
            logger.debug("finally, removing the original extent tag");
        }
        tableModel.removeRow(tableModel.searchForRowByTid(tag.getId()));
        getMainController().deleteTagFromTableDeletion(tag);

    }

    private void removeTagFromAllTagsTable(String tid) {
        UneditableTableModel tableModel = (UneditableTableModel) tableMap.get(MaeStrings.ALL_TABLE_TAB_BACK_NAME).getModel();
        tableModel.removeRow(tableModel.searchForRowByTid(tid));
    }

    void clearTableSelections() {
        for (JTable table : tableMap.values()) {
            TagTableModel tableModel = (TagTableModel) table.getModel();
            if (tableModel.getRowCount() > 0) {
                table.removeRowSelectionInterval(0, tableModel.getRowCount() - 1);
            }
        }
    }

    @Override
    /**
     * This methods is not actually used, nut exists for documentation purpose. Will be removed later
     */
    void addListeners() throws MaeDBException {
        addToggleListeners();
        addMouseListeners();
        addTableModelListeners();

    }

    private void addTableModelListeners() {
        // do not add change listener to the all tags table
        for (String tagTypeName : tableMap.keySet()) {
            if (!tagTypeName.equals(MaeStrings.ALL_TABLE_TAB_BACK_NAME)) {
                TagTableModel model = (TagTableModel) tableMap.get(tagTypeName).getModel();
                model.addTableModelListener(model);
            }
        }
    }

    void addMouseListeners() {
        for (JTable table : tableMap.values()) {
            table.addMouseListener(new TablePanelMouseListener());
        }
    }

    void addToggleListeners() {
        TablePanelView.TogglingTabTitle allTagTab = getTagTabTitle(0);
        allTagTab.addToggleListener(new HighlightToggleListener(dummyForAllTagTab, 0));
        for (int i = 1; i < getView().getTabs().getTabCount(); i++) {
            TablePanelView.TogglingTabTitle title = getTagTabTitle(i);
            title.addToggleListener(new HighlightToggleListener(title.getTagType(), i));
        }
        // this will turn on each extent tag title
        allTagTab.setHighlighted(true);
    }

    private JComponent makeAllTagTable() {

        UneditableTableModel model = new UneditableTableModel(dummyForAllTagTab);
        JTable table = makeTagTableFromEmptyModel(model, true);
        tabOrder.add(dummyForAllTagTab);
        tableMap.put(MaeStrings.ALL_TABLE_TAB_BACK_NAME, table);

        return new JScrollPane(table);
    }

    private JComponent makeTagTable(TagType type) {

        TagTableModel model = type.isExtent()? new TagTableModel(type) : new LinkTagTableModel(type);
        JTable table = makeTagTableFromEmptyModel(model, type.isExtent());
        tabOrder.add(type);
        tableMap.put(type.getName(), table);
        logger.debug("successfully created a table for: " + type.getName());

        if (type.isLink()) {
            addArgumentColumns(type, table);
        }
        addAttributeColumns(type, table);

        return new JScrollPane(table);
    }

    private JTable makeTagTableFromEmptyModel(TagTableModel model, boolean isExtent) {
        model.addColumn(MaeStrings.SRC_COL_NAME);
        model.addColumn(MaeStrings.ID_COL_NAME);

        if (isExtent) {
            // for extent tags, add text and spans columns
            model.addColumn(MaeStrings.SPANS_COL_NAME);
            model.addColumn(MaeStrings.TEXT_COL_NAME);
        }

        JTable table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        table.setAutoCreateColumnsFromModel(false);

        // TODO: 2016-01-12 17:32:17EST 4MAII remove source coloumn only when not adjudicating
        table.removeColumn(table.getColumnModel().getColumn(SRC_COL));

        return table;
    }

    private void addArgumentColumns(TagType type, JTable table) {
        LinkTagTableModel model = (LinkTagTableModel) table.getModel();
        List<ArgumentType> arguments = new ArrayList<>(type.getArgumentTypes());
        for (ArgumentType argType : arguments) {
            logger.debug(String.format("adding columns for '%s' argument to '%s' link tag table.", argType.getName(), type.getName()));

            model.addColumn(argType.getName() + "ID");
            TableColumn column = new TableColumn(model.getColumnCount()-1);
            // TODO: 2016-01-07 22:21:08EST this column should be id_ref
            column.setCellEditor(new DefaultCellEditor(new JTextField()));
            table.addColumn(column);

            model.addColumn(argType.getName() + "Text");
            column = new TableColumn(model.getColumnCount()-1);
            table.addColumn(column);
            model.addArgumentTextColumn(model.getColumnCount() - 1);
        }
    }

    private void addAttributeColumns(TagType type, JTable table) {
        List<AttributeType> attributes = new ArrayList<>(type.getAttributeTypes());
        TagTableModel model = (TagTableModel) table.getModel();
        for (AttributeType attType : attributes) {
            logger.debug(String.format("adding '%s' attribute column to '%s' tag table.", attType.getName(), type.getName()));
            model.addColumn(attType.getName());
            TableColumn column = new TableColumn(model.getColumnCount()-1);
            if (attType.isFiniteValueset()) {
                logger.debug(String.format("... and it has predefined value set: %s", attType.getValueset()));
                JComboBox valueset = makeValidValuesComboBox(attType);
                column.setCellEditor(new DefaultCellEditor(valueset));
            } else if (attType.isIdRef()) {
                // TODO: 2016-01-07 22:25:00EST add idref here
                // maybe adding a button to pop up to select an argument?
                column.setCellEditor(new DefaultCellEditor(new JTextField()));
            } else {
                column.setCellEditor(new DefaultCellEditor(new JTextField()));
            }
            table.addColumn(column);
        }
    }

    private JComboBox makeValidValuesComboBox(AttributeType att) {
        JComboBox<String> options = new JComboBox<>();
        options.addItem("");
        for (String value : att.getValuesetAsList()) {
            options.addItem(value);
        }
        return options;
    }

    /**
     * AnnotationTableModel creates a TableModel that user can't mess with id and source
     * // TODO: 2016-01-07 22:04:15EST 4MAII split annTableModel and adjTableModel, then SRC_COL will not be needed here
     */
    class TagTableModel extends DefaultTableModel implements TableModelListener {
        private TagType tagType;

        TagTableModel(TagType tagType) {
            this.tagType = tagType;
        }

        TagType getAssociatedTagType() {
            return tagType;
        }

        String getAssociatedTagTypeName() {
            return tagType.getName();
        }

        void updateRow(int row, String[] rowData) throws MaeControlException {
            if (this.getColumnCount() != rowData.length) {
                throw new MaeControlException("the data for a new row does not fit in the table.");
            }
            for (int col = 0; col < rowData.length; col++) {
                setValueAt(rowData[col], row, col);
            }
        }

        int searchForRowByTid(String tid) {
            for (int row = 0; row < getRowCount(); row++) {
                if (getValueAt(row, ID_COL).equals(tid)) {
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

        @Override
        public boolean isCellEditable(int row, int col) {
            return (col != ID_COL) && (col != SRC_COL) && (col != TEXT_COL);
        }

        @Override
        public void tableChanged(TableModelEvent event) {
            logger.debug(String.format("\"%s\" table changed: %d at %d, %d (INS=1, UPD=0, DEL=-1)", getAssociatedTagTypeName(), event.getType(), event.getFirstRow(), event.getColumn()));

            if (event.getFirstRow() == -1 && event.getColumn() == -1) {
                // ignore changes happened out of table (when initially setting up tables)
                return;
            }
            if (event.getType() == TableModelEvent.UPDATE) {
                // INSERT: listen to insertion is unnecessary, since adding a new tag never happens through table
                // DELETE: since we cannot recover what's already deleted anyway,
                // propagated deletion should be called right before the deletion of a row happens (not here, after deletion)
                String tid = (String) getValueAt(event.getFirstRow(), ID_COL);
                List<Integer> oldSpans = Collections.emptyList();
                try {
                    oldSpans = getDriver().getAnchorsByTid(tid);
                } catch (MaeDBException e) {
                    getMainController().showError(e);
                }
                String colName = getColumnName(event.getColumn());
                String value = (String) getValueAt(event.getFirstRow(), event.getColumn());
                boolean updated = getMainController().updateDBFromTableUpdate(tid, colName, value);
                if (!updated) {
                    revertChange(event.getFirstRow(), event.getColumn());
                } else {
                    propagateChange(event, tid, value, oldSpans);
                }

            }
        }

        void propagateChange(TableModelEvent event, String tid, String newValue, List<Integer> oldSpans) {
            if (event.getColumn() == SPANS_COL) {
                try {
                    // update adjacent text column
                    String newText = updateTextColumnFromSpasChange(event.getFirstRow(), newValue);
                    updateAllTagsTableRow(tid, newValue, newText);
                    updateAssociatedLinkTagRows(tid, newText);
                    getMainController().assignTextColorsOver(oldSpans);
                    List<Integer> newSpans = SpanHandler.convertIntegerarrayToIntegerlist(SpanHandler.convertStringToArray(newValue));
                    getMainController().assignTextColorsOver(newSpans);
                    getMainController().removeAllBGColors();
                    getMainController().addBGColorOver(newSpans, ColorHandler.getVividHighliter());
                } catch (MaeException ignored) {
                    // this spanstring is already validated within getMain().updateDB() method
                }
            }
        }

        String updateTextColumnFromSpasChange(int rowToUpdate, String value) throws MaeException {
            int[] newSpans = SpanHandler.convertStringToArray(value);
            String newText = getMainController().getTextIn(newSpans);
            setValueAt(newText, rowToUpdate, TEXT_COL);
            return newText;
        }

        void updateAllTagsTableRow(String tid, String newSpans, String newText) {
            JTable allTagsTable = tableMap.get(MaeStrings.ALL_TABLE_TAB_BACK_NAME);
            int rowAtAllTable = ((TagTableModel) allTagsTable.getModel()).searchForRowByTid(tid);
            allTagsTable.getModel().setValueAt(newSpans, rowAtAllTable, SPANS_COL);
            allTagsTable.getModel().setValueAt(newText, rowAtAllTable, TEXT_COL);
        }

        void updateAssociatedLinkTagRows(String tid, String newText) throws MaeDBException {
            // update text column of link tags associated
            ExtentTag argTag = (ExtentTag) getMainController().getTagByTid(tid);
            Set<LinkTag> linkers = getDriver().getLinksHasArgumentTag(argTag);
            for (LinkTag linker : linkers) {
                TagTableModel model = (TagTableModel) tableMap.get(linker.getTagtype().getName()).getModel();
                int row = model.searchForRowByTid(linker.getId());
                Map<String, String> argNameToTid = linker.getArgumentTidsWithNames();
                for (String argName : argNameToTid.keySet()) {
                    if (argNameToTid.get(argName).equals(tid)) {
                        String colName = argName + MaeStrings.ARG_TEXTCOL_SUF;
                        int col = model.searchForColumnByColName(colName);
                        model.setValueAt(newText, row, col);
                    }
                }
            }
        }

        void revertChange(int row, int col) {
            // ID_COL and TEXT_COL are not editable at the first place: except for SPANS_COL, everything else are attribute columns
            String tid = (String) getValueAt(row, ID_COL);
            ExtentTag tag = (ExtentTag) getMainController().getTagByTid(tid);
            String oldVal;
            if (col == SPANS_COL) {
                oldVal = tag.getSpansAsString();
            } else {
                String attType = getColumnName(col);
                oldVal = tag.getAttributesWithNames().get(attType);
                // TODO: 2016-02-01 17:48:48EST bug here, reverting goes into inf loop
            }
            setValueAt(oldVal, row, col);
        }
    }

    class LinkTagTableModel extends TagTableModel {

        private Set<Integer> argumentTextColumns;

        LinkTagTableModel(TagType tagType) {
            super(tagType);
            argumentTextColumns = new HashSet<>();

        }

        void addArgumentTextColumn(int col) {
            argumentTextColumns.add(col);
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return col != ID_COL && col != SRC_COL && !argumentTextColumns.contains(col);
        }

        @Override
        void propagateChange(TableModelEvent event, String tid, String newValue, List<Integer> oldSpans) {
            if (argumentTextColumns.contains(event.getColumn() + 1)) {
                // update adjacent text column
                ExtentTag newArg = (ExtentTag) getMainController().getTagByTid(newValue);
                String newText = newArg.getText();
                setValueAt(newText, event.getFirstRow(), event.getColumn() + 1);
                getMainController().assignTextColorsOver(oldSpans);
                getMainController().assignTextColorsOver(newArg.getSpansAsList());

            }
        }

        @Override
        void revertChange(int row, int col) {
            String tid = (String) getValueAt(row, ID_COL);
            LinkTag tag = (LinkTag) getMainController().getTagByTid(tid);
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

    /**
     * UneditableTableModel creates a TableModel that is not editable at all.
     * This is only used to create the all extents tab
     */
    private class UneditableTableModel extends TagTableModel {

        UneditableTableModel(TagType tagType) {
            super(tagType);
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    }

    private class HighlightToggleListener implements ItemListener {

        private TagType tagType;
        private int tabIndex;

        HighlightToggleListener(TagType tagType, int tabIndex) {
            this.tagType = tagType;
            this.tabIndex = tabIndex;

        }

        TagType getTagType() {
            return this.tagType;
        }

        @Override
        public void itemStateChanged(ItemEvent e) {

            try {
                getMainController().sendWaitMessage();

                if (tabIndex == 0) {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        logger.debug(String.format("activated FG colors of all %d/%d tags", getActiveExtentTags().size(), getMainController().paintableTagTypes()));
                        for (int tabIndex = 1; tabIndex < tabOrder.size();tabIndex++) {
                            // ignore 0th tab (all tags)
                            TablePanelView.TogglingTabTitle tabTitle = getTagTabTitle(tabIndex);
                            if (!tabTitle.isHighlighted() && tabTitle.getTagType().isExtent()) {
                                tabTitle.setHighlighted(true);
                            }
                        }
                    } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                        logger.debug(String.format("deactivated FG colors of all %d/%d tags", getActiveExtentTags().size(), getMainController().paintableTagTypes()));
                        for (int tabIndex = 1; tabIndex < tabOrder.size();tabIndex++) {
                            // ignore 0th tab (all tags)
                            TablePanelView.TogglingTabTitle tabTitle = getTagTabTitle(tabIndex);
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
                    getMainController().assignTextColorsOver(getRelevantAnchors());

                }
                getMainController().updateNotificationAreaIn(1000);

            } catch (MaeDBException ex) {
                getMainController().showError(ex);
            }

        }

        private List<Integer> getRelevantAnchors() throws MaeDBException {
            return getDriver().getAllAnchorsOfTagType(tagType, Collections.<TagType>emptyList());

        }

        private void checkAllTab() throws MaeDBException {
            TablePanelView.TogglingTabTitle allTab = getTagTabTitle(0);
            if (getActiveExtentTags().size() == getMainController().paintableTagTypes()) {
                allTab.setHighlighted(true);
            }
            if (getActiveExtentTags().size() == 0) {
                allTab.setHighlighted(false);
            }
        }

        private void activateTag() {
            if (tagType.isLink()) {
                getActiveLinkTags().add(tagType);
            } else {
                getActiveExtentTags().add(tagType);
                logger.debug(String.format("activated: %s, now %d/%d types are activated", tagType.getName(), activeExtentTags.size(), getMainController().paintableTagTypes()));
            }
        }

        private void deactivateTag() {
            if (tagType.isLink()) {
                getActiveLinkTags().remove(tagType);
            } else {
                getActiveExtentTags().remove(tagType);
                logger.debug(String.format("deactivated: %s, now %d/%d types are activated", tagType.getName(), activeExtentTags.size(), getMainController().paintableTagTypes()));
            }

        }

    }

    private class TablePanelMouseListener extends MouseAdapter {
        // note that mousePressed() and mouseReleased() are both required for os-independence


        @Override
        public void mousePressed(MouseEvent e) {
            if (e.isPopupTrigger()) {
                createAndShowContextMenu(e);
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {

            if (e.isPopupTrigger()) {

                createAndShowContextMenu(e);

            } else if (e.getClickCount() == 2) {
                JTable table = (JTable) e.getSource();
                TableModel tableModel = table.getModel();
                String tid = (String) tableModel.getValueAt(table.getSelectedRow(), ID_COL);
                getMainController().propagateSelectionFromTablePanel(tid);
            }
        }

        void createAndShowContextMenu(MouseEvent e) {
            // get tab and count selected rows
            JTable table = (JTable) e.getSource();

            int clickedRow = table.rowAtPoint(e.getPoint());
            // switch selection to clicked row only if one or zero row is selected before
            if (table.getSelectedRowCount() <= 1) {
                table.setRowSelectionInterval(clickedRow, clickedRow);
            }

            getMainController().createTableContextMenu(table).show(e.getComponent(), e.getX(), e.getY());
        }

    }

}
