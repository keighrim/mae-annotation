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
import java.util.List;

/**
 * Created by krim on 12/31/2015.
 */
public class TablePanelController extends MaeControllerI {

    // TODO: 2016-01-08 23:37:57EST need a listener to update backend models listens to any changes in tables
    // column number of some fixed attributes
    public static final int SRC_COL = 0;
    public static final int ID_COL = 0;
    public static final int SPANS_COL = 1;
    public static final int TEXT_COL = 2;
    TablePanelView view;
    private Set<TagType> activeLinkTags;
    private Set<TagType> activeExtentTags;
    private TagType dummyForAllTagTab;
    private List<TagType> tabOrder;
    private Map<String, JTable> tableMap;

    public TablePanelController(MaeMainController mainController) throws MaeControlException, MaeDBException {
        super(mainController);
        view = new TablePanelView();
        activeExtentTags = new HashSet<>();
        activeLinkTags = new HashSet<>();
        tabOrder = new ArrayList<>();
        tableMap = new TreeMap<>();
        dummyForAllTagTab = new TagType(MaeStrings.ALL_TABLE_TAB_BACK_NAME, MaeStrings.ALL_TABLE_TAB_PREFIX, false);
        reset();

    }

    public TablePanelView.TogglingTabTitle getTagTabTitle(int tabIndex) {
        return (TablePanelView.TogglingTabTitle) getView().getTabs().getTabComponentAt(tabIndex);
    }

    public Set<TagType> getActiveLinkTags() {
        return activeLinkTags;
    }

    public void setActiveLinkTags(Set<TagType> types) {
        getActiveLinkTags().clear();
        for (TagType type : types) {
            activateLinkTag(type);
        }
    }

    public void updateTag(Tag tag) throws MaeDBException, MaeControlException {
        TagTableModel tableModel = (TagTableModel) tableMap.get(tag.getTagtype().getName()).getModel();
        int newRowNum = tableModel.searchForRowByTid(tag.getId());
        insertRowData(tableModel, newRowNum, getTagRowData(tag, tableModel));
        if (tag.getTagtype().isExtent()) {
            insertRowToAllTagsTable(tag);
        }
    }

    private void insertRowData(TagTableModel tableModel, int insertAt, String[] newRowData) throws MaeControlException {

        if (insertAt == tableModel.getRowCount()) {
            tableModel.addRow(newRowData);
        } else if (insertAt < tableModel.getRowCount()) {
            tableModel.updateRow(insertAt, newRowData);
        } else {
            // TODO: 2016-01-08 19:50:19EST sophisticate here
            throw (new MaeControlException("cannot add a row!"));
        }
    }

    private String[] getTagRowData(Tag tag, TagTableModel tableModel) throws MaeDBException {
        String[] newRow = new String[tableModel.getColumnCount()];
        Map<String, String> attMap = tag.getAttributesWithNames();
        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            String colName = tableModel.getColumnName(i);
            switch (colName) {
                case MaeStrings.SRC_COL_NAME:
                    newRow[i] = getDriver().getAnnotationFileName();
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

    private void insertRowToAllTagsTable(Tag tag) throws MaeControlException, MaeDBException {
        UneditableTableModel tableModel = (UneditableTableModel) tableMap.get(MaeStrings.ALL_TABLE_TAB_BACK_NAME).getModel();
        insertRowData(tableModel, tableModel.searchForRowByTid(tag.getId()), getSimpleExtentTagRowData((ExtentTag) tag));
    }

    private String[] getSimpleExtentTagRowData(ExtentTag tag) throws MaeDBException {
        // TODO: 2016-01-08 22:27:35EST '4' here is too hard coded, make a way to encapsulate it
        return new String[]{getDriver().getAnnotationFileName(), tag.getId(), tag.getSpansAsString(), tag.getText()};
    }

    private void selectAllTagsTableRow(String tid) {
        JTable table = tableMap.get(MaeStrings.ALL_TABLE_TAB_BACK_NAME);
        UneditableTableModel tableModel = (UneditableTableModel) table.getModel();
        int viewIndex = table.convertRowIndexToView(tableModel.searchForRowByTid(tid));
        table.addRowSelectionInterval(viewIndex, viewIndex);
    }

    /**
     * Update a link tag in the bottom table with a single specific argument
     *
     * @param linkName name of link element being updated
     * @param linkId   id of element being updated
     * @param argName  name of an argument extent tag being added
     * @param argId    id of an argument extent tag being added
     * @param argText  text of an argument extent tag being added
     */
    @Deprecated
    public void setArgumentInTable(String linkName, String linkId, String argName, String argId, String argText) {

    }

    private void activateExtentTag(TagType type) {
        if (!type.isLink()) {
            getActiveExtentTags().add(type);
        }
    }

    private void removeRowFromAllTagsTable(String tid) {
        UneditableTableModel tableModel = (UneditableTableModel) tableMap.get(MaeStrings.ALL_TABLE_TAB_BACK_NAME).getModel();
        tableModel.removeRow(tableModel.searchForRowByTid(tid));
    }

    public void removeTableRows(Tag tag) throws MaeDBException {
        TagTableModel tableModel = (TagTableModel) tableMap.get(tag.getTagtype().getName()).getModel();
        tableModel.removeRow(tableModel.searchForRowByTid(tag.getId()));
        if (tag.getTagtype().isExtent()) {
            removeRowFromAllTagsTable(tag.getId());
            for (LinkTag link : getDriver().getLinksHasArgumentTag((ExtentTag) tag)) {
                removeTableRows(link);
            }
        }

    }

    private void activateLinkTag(TagType type) {
        if (type.isLink()) {
            getActiveLinkTags().add(type);
        }
    }

    public void selectTableRows(Tag tag) throws MaeDBException {
        clearTableSelections();
        JTable table = tableMap.get(tag.getTagtype().getName());
        TagTableModel tableModel = (TagTableModel) table.getModel();
        int viewIndex = table.convertRowIndexToView(tableModel.searchForRowByTid(tag.getId()));
        table.addRowSelectionInterval(viewIndex, viewIndex);
        if (tag.getTagtype().isExtent()) {
            selectAllTagsTableRow(tag.getId());
            for (LinkTag link : getDriver().getLinksHasArgumentTag((ExtentTag) tag)) {
                selectTableRows(link);
            }
        }
    }

    public Set<TagType> getActiveExtentTags() {
        return activeExtentTags;
    }

    public void clearTableSelections() {
        for (JTable table : tableMap.values()) {
            TagTableModel tableModel = (TagTableModel) table.getModel();
            if (tableModel.getRowCount() > 0) {
                table.removeRowSelectionInterval(0, tableModel.getRowCount() - 1);
            }
        }
    }

    public int getTabIndexOfTagType(TagType type) {
        return tabOrder.indexOf(type);
    }

    public void setActiveExtentTags(Set<TagType> types) {
        getActiveExtentTags().clear();
        for (TagType type : types) {
            activateExtentTag(type);
        }
    }
   @Override
    protected TablePanelView getView() {
        return view;
    }

  @Override
    void reset() throws MaeControlException, MaeDBException {
        getView().getTabs().removeAll();
        if (getMainController().isTaskLoaded()) {
            List<TagType> types = getDriver().getAllTagTypes();

            // TODO: 12/31/2015 is 20 safe?
            if (types.size() > 20) {
                getView().getTabs().setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
            } else {
                getView().getTabs().setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
            }

            // create a tab for all extents and place it at first
            getView().addTab(MaeStrings.ALL_TABLE_TAB_BACK_NAME, new TablePanelView.TogglingTabTitle(dummyForAllTagTab), makeAllTagTable());
            //create a tab for each element in the annotation task
            for (TagType type : types) {
                String name = type.getName();
                TablePanelView.TogglingTabTitle title;
                if (type.isExtent()) {
                    title = new TablePanelView.TogglingTabTitle(type, getMainController().getHighlightColor(type));
                } else {
                    title = new TablePanelView.TogglingTabTitle(type);
                }
                getView().addTab(name, title, makeTagTable(type));
            }

            getActiveLinkTags().clear();
            getActiveExtentTags().clear();
            addListeners();
        } else {
            view = new TablePanelView();
        }
    }

    @Override
    public void addListeners() throws MaeDBException {
        addToggleListeners();
        addMouseListeners();
        addTableModelListeners();

    }

    private void addTableModelListeners() {
        for (JTable table : tableMap.values()) {
            TagTableModel model = (TagTableModel) table.getModel();
            model.addTableModelListener(model);
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
            if (title.getTagType().isExtent()) {
                title.setHighlighted(true);
            }
        }
        allTagTab.setHighlighted(true);
    }

    private JComboBox makeValidValuesComboBox(AttributeType att) {
        JComboBox<String> options = new JComboBox<>();
        options.addItem("");
        for (String value : att.getValuesetAsList()) {
            options.addItem(value);
        }
        return options;
    }

    private JComponent makeAllTagTable() {

        UneditableTableModel model = new UneditableTableModel(dummyForAllTagTab);
        JTable table = makeTagTableFromEmptyModel(model, true);

        tabOrder.add(dummyForAllTagTab);
        tableMap.put(dummyForAllTagTab.getName(), table);

        JScrollPane scrollPane = new JScrollPane(table);
        return scrollPane;
    }

    private JComponent makeTagTable(TagType type) {

        TagTableModel model = new TagTableModel(type);
        JTable table = makeTagTableFromEmptyModel(model, type.isExtent());

        indexTagTab(type, table);

        if (type.isLink()) {
            // when adding a link tag, add argument columns right after id attributes
            List<ArgumentType> arguments = new ArrayList<>(type.getArgumentTypes());
            for (ArgumentType argType : arguments) {
                // TODO: 2016-01-07 22:21:08EST this column should be id_ref
                model.addColumn(argType.getName() + "ID");
                model.addColumn(argType.getName() + "Text");
            }
        }
        // then go through element attributes and add columns
        List<AttributeType> attributes = new ArrayList<>(type.getAttributeTypes());
        for (AttributeType att : attributes) {
            logger.info(String.format("adding '%s' attribute column to '%s' tag table.", att.getName(), type.getName()));
            model.addColumn(att.getName());
            TableColumn column = table.getColumnModel().getColumn(model.getColumnCount() - 1);
            if (att.isFiniteValueset()) {
                JComboBox valueset = makeValidValuesComboBox(att);
                column.setCellEditor(new DefaultCellEditor(valueset));
            } else if (att.isIdRef()) {
                // TODO: 2016-01-07 22:25:00EST add idref here
                // maybe adding a button to pop up to select an argument?
            }
        }

        return new JScrollPane(table);
    }

    private void indexTagTab(TagType type, JTable table) {
        tabOrder.add(type);
        tableMap.put(type.getName(), table);
    }

    private JTable makeTagTableFromEmptyModel(TagTableModel model, boolean isExtent) {
//        model.addColumn(MaeStrings.SRC_COL_NAME);
        model.addColumn(MaeStrings.ID_COL_NAME);

        if (isExtent) {
            // for extent tags, add text and spans columns
            model.addColumn(MaeStrings.SPANS_COL_NAME);
            model.addColumn(MaeStrings.TEXT_COL_NAME);
        }
        model.addTableModelListener(model);

        JTable table = new JTable(model);
        table.setAutoCreateRowSorter(true);

        // if not in adjudication, suppress SRC column to be invisible
        // TODO: 2016-01-07 21:40:06EST fix this for mai integration
//        table.getColumnModel().removeColumn(table.getColumnModel().getColumn(SRC_COL));

        return table;
    }
    /**
     * AnnotationTableModel creates a TableModel that user can't mess with id and source
     * // TODO: 2016-01-07 22:04:15EST split annTableModel and adjTableModel, then SRC_COL will not be needed here
     */
    private class TagTableModel extends DefaultTableModel implements TableModelListener {
        TagType tagType;

        public TagTableModel(TagType tagType) {
            this.tagType = tagType;
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
                    getMainController().updateTagFromTableUpdate(tid, colName, value);
                    break;
            }
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return (col != ID_COL) && (col != SRC_COL);
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

                // checking 0 might be a little bit hardcoding
                // toggle all extent elements
                if (tabIndex == 0) {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        setActiveExtentTags(new HashSet<>(getDriver().getAllTagTypes()));
                        logger.info(String.format("activated colors of all %d/%d tags", getActiveExtentTags().size(), getMainController().colorableTagTypes()));
                        getMainController().assignAllTextColors();
                    } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                        setActiveExtentTags(Collections.<TagType>emptySet());
                        logger.info(String.format("deactivated colors of all %d/%d tags", getActiveExtentTags().size(), getMainController().colorableTagTypes()));
                        getMainController().unassignAllTextColors();
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
            if (getActiveExtentTags().size() == getMainController().colorableTagTypes()) {
                // since allTab is created after all single tabs are created
                // getTabComponentAt() will return null while loading up a new DTD file,
                // hence we need to check if it's null
                // TODO: 2016-01-07 20:55:00EST think of a better way
                TablePanelView.TogglingTabTitle allTab = getTagTabTitle(0);
//                if (allTab != null) {
                    allTab.setHighlighted(true);
//                }
            }
            if (getActiveExtentTags().size() == 0) {
                TablePanelView.TogglingTabTitle allTab = getTagTabTitle(0);
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
