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
import edu.brandeis.cs.nlp.mae.util.MappedList;
import edu.brandeis.cs.nlp.mae.view.TablePanelView;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.*;
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
    public static final int ID_COL = 1;
    public static final int SPANS_COL = 2;
    public static final int TEXT_COL = 3;
    TablePanelView view;
    private Set<TagType> activeLinkTags;
    private Set<TagType> activeExtentTags;
    private TagType dummyForAllTagTab;
    private List<TagType> tabOrder;
    private Map<String, JTable> tableMap;

    public TablePanelController(Container mainController) {
        super(mainController);
        view = new TablePanelView();
        activeExtentTags = new HashSet<>();
        activeLinkTags = new HashSet<>();
        tabOrder = new ArrayList<>();
        tableMap = new TreeMap<>();
        dummyForAllTagTab = new TagType(MaeStrings.ALL_TABLE_TAB_BACK_NAME, MaeStrings.ALL_TABLE_TAB_PREFIX, false);

    }

    public TablePanelView.TogglingTabTitle getTagTabTitle(int tabIndex) {
        return (TablePanelView.TogglingTabTitle) getView().getTabs().getComponentAt(tabIndex);
    }

    public Set<TagType> getActiveLinkTags() {
        return activeLinkTags;
    }

    public void setActiveLinkTags(Set<TagType> types) {
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
                    newRow[i] = getMainController().getDriver().getAnnotationFileName();
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
        return new String[]{getMainController().getDriver().getAnnotationFileName(), tag.getId(), tag.getSpansAsString(), tag.getText()};
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
            for (LinkTag link : getMainController().getDriver().getLinksHasArgumentTag((ExtentTag) tag)) {
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
            for (LinkTag link : getMainController().getDriver().getLinksHasArgumentTag((ExtentTag) tag)) {
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
        for (TagType type : types) {
            activateExtentTag(type);
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

            getMainController().sendWaitMessage();

            try {
                // checking 0 might be a little bit hardcoding
                // toggle all extent elements
                if (tabIndex == 0) {
                    toggleAll();
                } else {
                    toggleColor();
                }
            } catch (MaeDBException ex) {
                // TODO: 2016-01-07 20:42:59EST  figure out how to throw error popup from here
                ex.printStackTrace();
            }
            // TODO: 2016-01-07 20:50:26EST think of a better way
            getMainController().sendTemporaryNotification(MaeStrings.WAIT_MESSAGE, 1000);
        }

        private void toggleAll() throws MaeDBException {
            if (getTagTabTitle(tabIndex).isHighlighted()) {
                setActiveExtentTags(new HashSet<>(getMainController().getDriver().getAllTagTypes()));
            } else {
                setActiveExtentTags(Collections.<TagType>emptySet());
            }
            getMainController().assignColors();
        }

        private void toggleColor() throws MaeDBException {
            if (getTagTabTitle(tabIndex).isHighlighted()) {
                activateTag();
            } else {
                deactivateTag();
            }
            assignTextColor();
            if (tagType.isExtent()) {
                checkAllTab();
            }
        }

        private void assignTextColor() throws MaeDBException {
            List<Integer> anchorsList = getMainController().getDriver().getAllAnchorsOfTagType(tagType, Collections.<TagType>emptyList());
            int[] anchors = new int[anchorsList.size()];
            for (int i = 0; i < anchorsList.size(); i++) {
                anchors[i] = anchorsList.get(i);
            }

            getMainController().getTextPanel().assignTextColorOver(anchors);

        }

        private void checkAllTab() throws MaeDBException {
            if (getActiveExtentTags().size() == getMainController().getDriver().getExtentTagTypes().size()) {
                // since allTab is created after all single tabs are created
                // getTabComponentAt() will return null while loading up a new DTD file,
                // hence we need to check if it's null
                // TODO: 2016-01-07 20:55:00EST think of a better way
                TablePanelView.TogglingTabTitle allTab = getTagTabTitle(0);
                if (allTab != null) {
                    allTab.setHighlighted(true);
                }
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
            }
        }

        private void deactivateTag() {
            if (tagType.isLink()) {
                getActiveLinkTags().remove(tagType);
            } else {
                getActiveExtentTags().remove(tagType);
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

    @Override
    protected TablePanelView getView() {
        return view;
    }

    /**
     * UneditableTableModel creates a TableModel that is not editable at all.
     * This is only used to create the all extents tab
     */
    public class UneditableTableModel extends TagTableModel {

        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    }

    /**
     * AnnotationTableModel creates a TableModel that user can't mess with id and source
     * // TODO: 2016-01-07 22:04:15EST split annTableModel and adjTableModel, then SRC_COL will not be needed here
     */
    public class TagTableModel extends DefaultTableModel {
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
        public boolean isCellEditable(int row, int col) {
            return (col != ID_COL) && (col != SRC_COL);
        }
    }

    @Override
    void reset() throws MaeControlException, MaeDBException {
        getView().getTabs().removeAll();
        if (getMainController().isTaskLoaded()) {
            List<TagType> types = getMainController().getDriver().getAllTagTypes();

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
        } else {
            view = new TablePanelView();
        }
    }

    @Override
    public void addListeners() throws MaeDBException {
        addToggleListeners();
        addMouseListeners();

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
            if (title.getTagType().isLink()) {
                title.setHighlighted(false);
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

        UneditableTableModel model = new UneditableTableModel();
        JTable table = makeTagTableFromEmptyModel(model, true);

        tabOrder.add(dummyForAllTagTab);
        tableMap.put(dummyForAllTagTab.getName(), table);

        JScrollPane scrollPane = new JScrollPane(table);
        return scrollPane;
    }

    private JComponent makeTagTable(TagType type) {

        TagTableModel model = new TagTableModel();
        JTable table = makeTagTableFromEmptyModel(model, type.isExtent());

        indexTagTab(type, table);

        if (type.isLink()) {
            // when adding a link tag, add argument columns right after id attributes
            List<ArgumentType> arguments = new ArrayList<>(type.getArgumentTypes());
            for (ArgumentType argType : arguments) {
                // TODO: 2016-01-08 13:19:19EST test this way of adding column works as intended.
                // TODO: 2016-01-07 22:21:08EST this column should be id_ref
                model.addColumn(argType.getName() + "ID");
                model.addColumn(argType.getName() + "Text");
            }
        }
        //go through element attributes and add columns
        List<AttributeType> attributes = new ArrayList<>(type.getAttributeTypes());
        // NOTE from old version: for some reason, it's necessary to add the columns first, then go back and add the cell renderers.
        for (AttributeType att : attributes) {
            // TODO: 2016-01-08 19:23:51EST  also test this way
            TableColumn column = new TableColumn(model.getColumnCount()); // put it at the end of current table
            column.setIdentifier(att.getName());
            if (att.isFiniteValueset()) {
                JComboBox valueset = makeValidValuesComboBox(att);
                column.setCellEditor(new DefaultCellEditor(valueset));
            } else if (att.isIdRef()) {
                // TODO: 2016-01-07 22:25:00EST add idref here
                // maybe adding a button to pop up to select an argument?
            }
            model.addColumn(column);
        }

        return new JScrollPane(table);
    }

    private void indexTagTab(TagType type, JTable table) {
        tabOrder.add(type);
        tableMap.put(type.getName(), table);
    }

    private JTable makeTagTableFromEmptyModel(DefaultTableModel model, boolean isExtent) {
        model.addColumn(MaeStrings.SRC_COL_NAME);
        model.addColumn(MaeStrings.ID_COL_NAME);

        if (isExtent) {
            // for extent tags, add text and spans columns
            model.addColumn(MaeStrings.SPANS_COL_NAME);
            model.addColumn(MaeStrings.TEXT_COL_NAME);
        }

        JTable table = new JTable(model);
        table.setAutoCreateRowSorter(true);

        // if not in adjudication, suppress SRC column to be invisible
        // TODO: 2016-01-07 21:40:06EST fix this for mai integration
        if (getMainController().getMode() != MaeMainController.MODE_ADJUD) {
            table.getColumnModel().removeColumn(
                    table.getColumnModel().getColumn(SRC_COL));
        }

        return table;
    }

}
