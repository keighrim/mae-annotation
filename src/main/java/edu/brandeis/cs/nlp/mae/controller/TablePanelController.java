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

import edu.brandeis.cs.nlp.mae.MaeStrings;
import edu.brandeis.cs.nlp.mae.database.MaeDBException;
import edu.brandeis.cs.nlp.mae.model.*;
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
public class TablePanelController extends MaeControllerI {

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

    public TablePanelController(MaeMainController mainController) throws MaeControlException, MaeDBException {
        super(mainController);
        dummyForAllTagTab = new TagType(MaeStrings.ALL_TABLE_TAB_BACK_NAME, MaeStrings.ALL_TABLE_TAB_PREFIX, false);
        view = new TablePanelView();
        reset();

    }
    public Set<TagType> getActiveExtentTags() {
        return activeExtentTags;
    }

    public void setActiveExtentTags(Set<TagType> types) {
        getActiveExtentTags().clear();
        for (TagType type : types) {
            if (!type.isLink()) {
                getActiveExtentTags().add(type);
            }
        }
    }


    public Set<TagType> getActiveLinkTags() {
        return activeLinkTags;
    }

    public void setActiveLinkTags(Set<TagType> types) {
        getActiveLinkTags().clear();
        for (TagType type : types) {
            if (type.isLink()) {
                getActiveLinkTags().add(type);
            }
        }
    }

    @Override
    protected TablePanelView getView() {
        return view;
    }


    @Override
    void reset() {
        getView().getTabs().removeAll();
        activeExtentTags = new HashSet<>();
        activeLinkTags = new HashSet<>();
        tabOrder = new ArrayList<>();
        tableMap = new TreeMap<>();

    }

    public void makeAllTables() throws MaeDBException, MaeControlException {
        if (!getMainController().isTaskLoaded()) {
            throw new MaeControlException("Cannot make tables without a task definition!");
        }
        List<TagType> types = getDriver().getAllTagTypes();

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

    public void insertAllTags() throws MaeControlException, MaeDBException {
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

    public int getTabIndexOfTagType(TagType type) {
        return tabOrder.indexOf(type);
    }

    public TablePanelView.TogglingTabTitle getTagTabTitle(int tabIndex) {
        return (TablePanelView.TogglingTabTitle) getView().getTabs().getTabComponentAt(tabIndex);
    }

    public void insertTagIntoTable(Tag tag) throws MaeDBException, MaeControlException {
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
            logger.info(String.format("inserting a new row, %s, to %s table", Arrays.toString(newRowData), tableModel.getAssociatedTagTypeName()));
        } else if (insertAt < tableModel.getRowCount()) {
            tableModel.updateRow(insertAt, newRowData);
            logger.info(String.format("updating a row, %s, to %s table at %d", Arrays.toString(newRowData), tableModel.getAssociatedTagTypeName(), insertAt));
        } else {
            // TODO: 2016-01-08 19:50:19EST this is for error checking, make sure this works as intended
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

    public void selectTagFromTable(Tag tag) throws MaeDBException {
        JTable table = tableMap.get(tag.getTagTypeName());
        TagTableModel tableModel = (TagTableModel) table.getModel();
        int viewIndex = table.convertRowIndexToView(tableModel.searchForRowByTid(tag.getId()));
        table.addRowSelectionInterval(viewIndex, viewIndex);
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
    }

    public void removeTagFromTable(Tag tag) throws MaeDBException {
        TagTableModel tableModel = (TagTableModel) tableMap.get(tag.getTagTypeName()).getModel();
        tableModel.removeRow(tableModel.searchForRowByTid(tag.getId()));
        if (tag.getTagtype().isExtent()) {
            removeTagFromAllTagsTable(tag.getId());
            for (LinkTag link : getDriver().getLinksHasArgumentTag((ExtentTag) tag)) {
                removeTagFromTable(link);
            }
        }

    }

    private void removeTagFromAllTagsTable(String tid) {
        UneditableTableModel tableModel = (UneditableTableModel) tableMap.get(MaeStrings.ALL_TABLE_TAB_BACK_NAME).getModel();
        tableModel.removeRow(tableModel.searchForRowByTid(tid));
    }

    public void clearTableSelections() {
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
    public void addListeners() throws MaeDBException {
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

        table.removeColumn(table.getColumnModel().getColumn(SRC_COL));
        return new JScrollPane(table);
    }

    private JComponent makeTagTable(TagType type) {

        TagTableModel model = type.isExtent()? new TagTableModel(type) : new LinkTagTableModel(type);
        JTable table = makeTagTableFromEmptyModel(model, type.isExtent());
        tabOrder.add(type);
        tableMap.put(type.getName(), table);

        if (type.isLink()) {
            addArgumentColumns(type, table);
        }
        addAttributeColumns(type, table);

        // TODO: 2016-01-12 17:32:17EST 4MAII remove source coloumn olny when not adjudicating
        table.getColumnModel().removeColumn(table.getColumn(MaeStrings.SRC_COL_NAME));
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
        table.setAutoCreateColumnsFromModel( false );
        return table;
    }

    private void addArgumentColumns(TagType type, JTable table) {
        LinkTagTableModel model = (LinkTagTableModel) table.getModel();
        List<ArgumentType> arguments = new ArrayList<>(type.getArgumentTypes());
        for (ArgumentType argType : arguments) {
            logger.info(String.format("adding columns for '%s' argument to '%s' link tag table.", argType.getName(), type.getName()));
            // TODO: 2016-01-07 22:21:08EST this column should be id_ref
            model.addColumn(argType.getName() + "ID");
            model.addColumn(argType.getName() + "Text");
            model.addArgumentTextColumn(model.getColumnCount() - 1);
        }
    }

    private void addAttributeColumns(TagType type, JTable table) {
        List<AttributeType> attributes = new ArrayList<>(type.getAttributeTypes());
        TagTableModel model = (TagTableModel) table.getModel();
        for (AttributeType attType : attributes) {
            logger.info(String.format("adding '%s' attribute column to '%s' tag table.", attType.getName(), type.getName()));
            model.addColumn(attType.getName());
            TableColumn column = new TableColumn(table.getColumnCount());
            if (attType.isFiniteValueset()) {
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
    private class TagTableModel extends DefaultTableModel implements TableModelListener {
        TagType tagType;

        public TagTableModel(TagType tagType) {
            this.tagType = tagType;
        }

        public String getAssociatedTagTypeName() {
            return tagType.getName();
        }

        public void updateRow(int row, String[] rowData) throws MaeControlException {
            if (this.getColumnCount() != rowData.length) {
                throw new MaeControlException("the data for a new row does not fit in the table.");
            }
            for (int col = 0; col < rowData.length; col++) {
                setValueAt(rowData[col], row, col);
            }
        }

        public int searchForRowByTid(String tid) {
            for (int row = 0; row < getRowCount(); row++) {
                if (getValueAt(row, ID_COL).equals(tid)) {
                    return row;
                }
            }
            return getRowCount();

        }

        @Override
        public void tableChanged(TableModelEvent event) {
            if (event.getFirstRow() == -1 || event.getColumn() == -1) {
                // ignore changes happened out of table (when initially setting up tables)
                return;
            }
            switch (event.getType()) {
                case TableModelEvent.INSERT:
                    Map<String, String> insertedRow = new HashMap<>();
                    int row = event.getFirstRow();
                    for (int col = 0; col < getColumnCount(); col++) {
                        insertedRow.put(getColumnName(col), (String) getValueAt(row, col));
                    }
                    getMainController().createTagFromTableInsertion(tagType, insertedRow);
                    break;
                case TableModelEvent.DELETE:
                    getMainController().deleteTagFromTableDeletion((String) getValueAt(event.getFirstRow(), ID_COL));
                    break;
                case TableModelEvent.UPDATE:
                    String tid = (String) getValueAt(event.getFirstRow(), ID_COL);
                    String colName = getColumnName(event.getColumn());
                    String value = (String) getValueAt(event.getFirstRow(), event.getColumn());
                    if (colName.equals(MaeStrings.SPANS_COL_NAME)) {
                        // TODO: 2016-01-15 22:41:23EST  validate spans string: below works, but very messy
                        try {
                            SpanHandler.convertStringToArray(value);
                        } catch (Exception e) {
                            TagTableModel model = (TagTableModel) event.getSource();
                            try {
                                String oldSpans = ((ExtentTag) getDriver().getTagByTid(tid)).getSpansAsString();
                                model.setValueAt(oldSpans, event.getFirstRow(), SPANS_COL);

                            } catch (MaeDBException e1) {
                                e1.printStackTrace();
                            }
//                            model.editCellAt(-1, -1);
                            // TODO: 2016-01-15 22:27:26EST add more educational message embedded in a proper exception
                            getMainController().showError("The value for tag spans is not well-formed: ");


                        }
                    }
                    // TODO: 2016-01-15 22:41:50EST update TEXT_COL according to changes in SPANS_COL 
//                    if (colName.equals(MaeStrings.SPANS_COL_NAME)) {
//                        try {
//                            int[] newSpans = SpanHandler.convertStringToArray(value);
//                            String newText = getMainController().getTextPanel().getTextIn(newSpans, false);
//                            ((JTable) event.getSource()).setValueAt(newText, event.getFirstRow(), TEXT_COL);
//                        } catch (MaeControlException e) {
//                            getMainController().showError(e);
//                        }
//                    }
                    getMainController().updateTagFromTableUpdate(tid, colName, value);
                    break;
            }
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return (col != ID_COL) && (col != SRC_COL) && (col != TEXT_COL);
        }

    }

    public class LinkTagTableModel extends TagTableModel {

        private Set<Integer> argumentTextColumns;

        public LinkTagTableModel(TagType tagType) {
            super(tagType);
            argumentTextColumns = new HashSet<>();

        }

        public void addArgumentTextColumn(int col) {
            argumentTextColumns.add(col);
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return col != ID_COL && col != SRC_COL && !argumentTextColumns.contains(col);
        }

    }

    /**
     * UneditableTableModel creates a TableModel that is not editable at all.
     * This is only used to create the all extents tab
     */
    private class UneditableTableModel extends TagTableModel {

        public UneditableTableModel(TagType tagType) {
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

        public HighlightToggleListener(TagType tagType, int tabIndex) {
            this.tagType = tagType;
            this.tabIndex = tabIndex;

        }

        public TagType getTagType() {
            return this.tagType;
        }

        @Override
        public void itemStateChanged(ItemEvent e) {

            try {
                getMainController().sendWaitMessage();

                if (tabIndex == 0) {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        logger.info(String.format("activated FG colors of all %d/%d tags", getActiveExtentTags().size(), getMainController().colorableTagTypes()));
                        for (int tabIndex = 1; tabIndex < tabOrder.size();tabIndex++) {
                            // ignore 0th tab (all tags)
                            TablePanelView.TogglingTabTitle tabTitle = getTagTabTitle(tabIndex);
                            if (!tabTitle.isHighlighted() && tabTitle.getTagType().isExtent()) {
                                tabTitle.setHighlighted(true);
                            }
                        }
                    } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                        logger.info(String.format("deactivated FG colors of all %d/%d tags", getActiveExtentTags().size(), getMainController().colorableTagTypes()));
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
                getMainController().resetNotificationMessageIn(1000);

            } catch (MaeDBException ex) {
                // TODO: 2016-01-10 20:29:47EST make sure this is a safe way
                getMainController().showError(ex);
            }

        }

        private List<Integer> getRelevantAnchors() throws MaeDBException {
            return getDriver().getAllAnchorsOfTagType(tagType, Collections.<TagType>emptyList());

        }

        private void checkAllTab() throws MaeDBException {
            TablePanelView.TogglingTabTitle allTab = getTagTabTitle(0);
            if (getActiveExtentTags().size() == getMainController().colorableTagTypes()) {
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
                logger.info(String.format("activated: %s, now %d/%d types are activated", tagType.getName(), activeExtentTags.size(), getMainController().colorableTagTypes()));
            }
        }

        private void deactivateTag() {
            if (tagType.isLink()) {
                getActiveLinkTags().remove(tagType);
            } else {
                getActiveExtentTags().remove(tagType);
                logger.info(String.format("deactivated: %s, now %d/%d types are activated", tagType.getName(), activeExtentTags.size(), getMainController().colorableTagTypes()));
            }

        }

    }

    private class TablePanelMouseListener extends MouseAdapter {

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()) {
                getMainController().createTableContextMenu().show(e.getComponent(), e.getX(), e.getY());
            } else if (e.getClickCount() == 2) {
                // TODO: 2016-01-08 16:29:13EST test if this getSource() will get the right one
                JTable table = (JTable) e.getSource();
                TableModel tableModel = table.getModel();
                String tid = (String) tableModel.getValueAt(table.getSelectedRow(), ID_COL);
                getMainController().propagateSelectionFromTablePanel(tid);
            }
        }

    }

}
