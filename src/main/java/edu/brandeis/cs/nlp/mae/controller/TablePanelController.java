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
import edu.brandeis.cs.nlp.mae.util.FileHandler;
import edu.brandeis.cs.nlp.mae.util.FontHandler;
import edu.brandeis.cs.nlp.mae.util.SpanHandler;
import edu.brandeis.cs.nlp.mae.view.TablePanelView;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.*;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
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
class TablePanelController extends MaeControllerI {

    // column number of some fixed attributes
    public static final int SRC_COL = 0;
    public static final int ID_COL = 1;
    public static final int SPANS_COL = 2;
    public static final int TEXT_COL = 3;

    TablePanelView view;
    private TagType dummyForAllTagsTab;
    private Set<TagType> activeLinkTags;
    private Set<TagType> activeExtentTags;
    private List<TagType> tabOrder;
    private Map<String, JTable> tableMap;

    TablePanelController(MaeMainController mainController) throws MaeControlException, MaeDBException {
        super(mainController);
        dummyForAllTagsTab = new TagType(MaeStrings.ALL_TABLE_TAB_BACK_NAME, MaeStrings.ALL_TABLE_TAB_PREFIX, false);
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
        JTabbedPane tabs = getView().getTabs();
        for (ChangeListener listen : tabs.getChangeListeners()) {
            if (listen instanceof AdjudicationTabSwitchListener) {
                tabs.removeChangeListener(listen);
            }
        }
        tabs.removeAll();
        activeExtentTags = new HashSet<>();
        activeLinkTags = new HashSet<>();
        tabOrder = new ArrayList<>();
        tableMap = new TreeMap<>();

    }

    void prepareAllTables() throws MaeDBException, MaeControlException {
        if (!getMainController().isTaskLoaded()) {
            throw new MaeControlException("Cannot make tables without a task definition!");
        }

        emptyTagTables();
        getActiveLinkTags().clear();
        getActiveExtentTags().clear();

        if (getMainController().isAdjudicating()) {
            prepareAdjudicationTables();
        } else {
            prepareAnnotationTables();

        }

    }

    private void prepareAdjudicationTables() throws MaeDBException {
        List<TagType> types = getDriver().getAllTagTypes();
        getView().getTabs().setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        for (TagType type : types) {
            String name = type.getName();
            JLabel title = new JLabel(name);
            getView().addTab(name, title, makeAdjudicationArea(type));
        }
        getView().getTabs().addChangeListener(new AdjudicationTabSwitchListener());
    }

    private void prepareAnnotationTables() throws MaeDBException {
        List<TagType> types = getDriver().getAllTagTypes();
        logger.debug(String.format("start creating tables for %d tag types", types.size()));

        if (types.size() > 20) {
            getView().getTabs().setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        } else {
            getView().getTabs().setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
        }

        // create a tab for all extents and place it at first
        TablePanelView.TogglingTabTitle allTagsTabTitle = new TablePanelView.TogglingTabTitle(dummyForAllTagsTab);
        getView().addTab(MaeStrings.ALL_TABLE_TAB_BACK_NAME, allTagsTabTitle, makeAllExtentTagsArea());
        // then create tabs for each element in the annotation task
        for (TagType type : types) {
            String name = type.getName();
            TablePanelView.TogglingTabTitle title = createTogglingTabTitle(type);
            getView().addTab(name, title, makeAnnotationArea(type));
            HighlightToggleListener toggleListener = new HighlightToggleListener(title.getTagType(), false, title);
            title.addToggleListener(toggleListener);
            if (type.isExtent()) {
                title.addMouseListener(toggleListener);
            }

        }

        allTagsTabTitle.addToggleListener(new HighlightToggleListener(dummyForAllTagsTab, true, allTagsTabTitle));
        // this will turn on each extent tag title
        allTagsTabTitle.setHighlighted(true);
    }

    private TablePanelView.TogglingTabTitle createTogglingTabTitle(TagType type) {
        if (type.isExtent()) {
            return new TablePanelView.TogglingTabTitle(type, getMainController().getFGColor(type));
        } else {
            return new TablePanelView.TogglingTabTitle(type);
        }
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
            String tagTypeName = type.getName();
            TagTableModel tagTypeTableModel = (TagTableModel) tableMap.get(tagTypeName).getModel();
            if (!type.equals(dummyForAllTagsTab) && type.isExtent()) {
                // ignore insertions occurred on all-tags table

                // old tag-by-tag conversion was fastest
                List<String[]> tagsToRows = new LinkedList<>();
                List<String[]> tagsToSimpleRows = new LinkedList<>();
                Collection<ExtentTag> tags = getDriver().getAllExtentTagsOfType(type);
                for (ExtentTag tag : tags) {
                    String[] rowRepresentation = convertTagIntoTableRow(tag,
                            tagTypeTableModel, false);
                    tagsToRows.add(rowRepresentation);
                    tagsToSimpleRows.add(Arrays.copyOfRange(rowRepresentation, SRC_COL, TEXT_COL));
                }
                TableBatchInsertSwingWorker swingWorker
                        = new TableBatchInsertSwingWorker(tagTypeTableModel, tagsToRows);
                swingWorker.execute();
                swingWorker
                        = new TableBatchInsertSwingWorker(
                        (TagTableModel) tableMap.get(MaeStrings.ALL_TABLE_TAB_BACK_NAME).getModel(),
                        tagsToSimpleRows);
                swingWorker.execute();
            } else if (type.isLink()) {

                // old tag-by-tag conversion
                List<String[]> tagsToRows = new LinkedList<>();
                for (LinkTag tag : getDriver().getAllLinkTagsOfType(type)) {
                    String[] rowRepresentation = convertTagIntoTableRow(tag,
                            tagTypeTableModel, true);
                    tagsToRows.add(rowRepresentation);
                }

//                String[][] tagsToRows = convertAllTagsOfTagTypeIntoTableRows(type, tagTypeTableModel);
                TableBatchInsertSwingWorker swingWorker
                        = new TableBatchInsertSwingWorker(tagTypeTableModel, tagsToRows);
                swingWorker.execute();
            }
        }
        addMouseListeners();
        addTableModelListeners();

    }

    void bigFontSize() {
        SimpleAttributeSet attributeSet = new SimpleAttributeSet();
        StyleConstants.setFontSize(attributeSet, 36);

        JTabbedPane tabs = getView().getTabs();
        for (int i = 0; i < tabs.getTabCount(); i++) {
            JScrollPane sp = (JScrollPane) tabs.getComponentAt(i);
            JTable table = (JTable) sp.getViewport().getView();
            table.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 24));
            table.setRowHeight(36);
            table.updateUI();
        }

    }

    int getTabIndexOfTagType(TagType type) {
        return tabOrder.indexOf(type);
    }

    TablePanelView.TogglingTabTitle getTagTabTitle(TagType type) {
        return getTagTabTitle(getTabIndexOfTagType(type));
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

    void insertTagIntoTable(Tag tag, TagType type) throws MaeDBException, MaeControlException {
        TagTableModel tableModel = (TagTableModel) tableMap.get(type.getName()).getModel();
        int newRowNum = tableModel.searchForRowByTid(tag.getId());
        insertRowData(tableModel, newRowNum, convertTagIntoTableRow(tag, tableModel, type.isLink()));
        if (tag.getTagtype().isExtent() && !getMainController().isAdjudicating()) {
            insertTagToAllTagsTable(tag);
        }
    }

    void insertNewTagIntoTable(Tag tag, TagType type) throws MaeDBException, MaeControlException {
        TagTableModel tableModel = (TagTableModel) tableMap.get(type.getName()).getModel();
        insertRowData(tableModel, tableModel.getRowCount(), convertTagIntoTableRow(tag, tableModel, type.isLink()));
        if (tag.getTagtype().isExtent() && !getMainController().isAdjudicating()) {
            insertNewTagToAllTagsTable(tag);
        }

    }

    void insertTagIntoAdjudicationTable(Tag tag) throws MaeDBException {
        AdjudicationTableModelI tableModel = (AdjudicationTableModelI) getView().getTable().getModel();
        tableModel.populateTable(tag);

    }

    void clearAdjudicationTable() {
        ((AdjudicationTableModelI) getView().getTable().getModel()).clearTable();
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

    private String[][] convertAllTagsOfTagTypeIntoTableRows(
            TagType tagType, TagTableModel tableModel) throws MaeDBException {
        Map<Tag, Map<String, String>> attsByTags = getDriver().getAttributeMapsOfTagType(tagType);
        String[][] tableRows = new String[attsByTags.size()][tableModel.getColumnCount()];
        int row = 0;
        for (Tag tag : attsByTags.keySet()) {
            tableRows[row++] = convertAttMapToTableRow(tag, tableModel, tagType.isLink(), attsByTags.get(tag));

        }
        return tableRows;
    }

    private String[] convertTagIntoTableRow(Tag tag, TagTableModel tableModel, boolean isLink) throws MaeDBException {
        Map<String, String> attMap = tag.getAttributesWithNamesWithoutChecking();
//        Map<String, String> attMap = getDriver().getAttributeMapOfTag(tag);

        String[] newRow = convertAttMapToTableRow(tag, tableModel, isLink, attMap);
        return newRow;
    }

    private String[] convertAttMapToTableRow(Tag tag, TagTableModel tableModel, boolean isLink, Map<String, String> attMap) {
        String[] newRow = new String[tableModel.getColumnCount()];
        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            switch (i) {
                // source and id columns are always there
                case SRC_COL:
                    newRow[i] = tag.getFilename();
                    break;
                case ID_COL:
                    newRow[i] = tag.getId();
                    break;
                case SPANS_COL:
                    if (!isLink) {
                        newRow[i] = ((ExtentTag) tag).getSpansAsString();
                        break;
                    }
                case TEXT_COL:
                    if (!isLink) {
                        newRow[i] = ((ExtentTag) tag).getText();
                        break;
                    }
                default:
                    String colName = tableModel.getColumnName(i);
                    newRow[i] = attMap.get(colName);

            }
        }
        return newRow;
    }

    private void insertTagToAllTagsTable(Tag tag) throws MaeControlException, MaeDBException {
        UneditableTableModel tableModel = (UneditableTableModel) tableMap.get(MaeStrings.ALL_TABLE_TAB_BACK_NAME).getModel();
        insertRowData(tableModel, tableModel.searchForRowByTid(tag.getId()), convertTagIntoSimplifiedRow((ExtentTag) tag));
    }

    private void insertNewTagToAllTagsTable(Tag tag) throws MaeControlException, MaeDBException {
        UneditableTableModel tableModel = (UneditableTableModel) tableMap.get(MaeStrings.ALL_TABLE_TAB_BACK_NAME).getModel();
        insertRowData(tableModel, tableModel.getRowCount(), convertTagIntoSimplifiedRow((ExtentTag) tag));
    }

    private String[] convertTagIntoSimplifiedRow(ExtentTag tag) throws MaeDBException {
        return new String[]{getDriver().getAnnotationFileName(), tag.getId(), tag.getSpansAsString(), tag.getText()};
    }

    void selectTabOf(TagType type) {
        getView().getTabs().setSelectedIndex(tabOrder.indexOf(type));
    }

    TagType getCurrentTagType() {
        return tabOrder.get(getView().getTabs().getSelectedIndex());
    }

    void selectTagFromTable(Tag tag) throws MaeDBException {
        JTable table = tableMap.get(tag.getTagTypeName());
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
        getMainController().deleteTagFromDB(tag);

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
    }

    private void setColumnRenders(JTable table, TableCellRenderer renderer) {
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

    }

    private JComponent makeAllExtentTagsArea() {
        UneditableTableModel model = new UneditableTableModel(dummyForAllTagsTab);
        JTable table = createMinimumTable(model, true);
        tabOrder.add(dummyForAllTagsTab);
        tableMap.put(MaeStrings.ALL_TABLE_TAB_BACK_NAME, table);
        AnnotationCellRenderer renderer = new AnnotationCellRenderer();
        setColumnRenders(table, renderer);

        return new JScrollPane(table);
    }

    private JComponent makeAnnotationArea(TagType type) {
        TagTableModel model = type.isExtent()? new TagTableModel(type) : new LinkTagTableModel(type);
        JTable table = makeTagTable(type, model);
        AnnotationCellRenderer renderer = new AnnotationCellRenderer();
        setColumnRenders(table, renderer);
        logger.debug("successfully created a table for: " + type.getName());
        return new JScrollPane(table);
    }

    private JComponent makeAdjudicationArea(TagType type) {
        TagTableModel model = type.isExtent()? new AdjudicationTableModel(type) : new AdjudicationLinkTableModel(type);
        model.addTableModelListener(model);
        JTable table = makeTagTable(type, model);
        AdjudicationCellRenderer renderer = new AdjudicationCellRenderer(model);
        setColumnRenders(table, renderer);
        table.addMouseListener(new AdjudicationTablePanelMouseListener());
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        logger.debug("successfully created an adjudication table for: " + type.getName());
        return new JScrollPane(table);
    }

    private void indexTagTable(TagType type, JTable table) {
        tabOrder.add(type);
        tableMap.put(type.getName(), table);
    }

    private JTable makeTagTable(TagType type, TagTableModel model) {
        JTable table = createMinimumTable(model, type.isExtent());
        table.getTableHeader().setReorderingAllowed(false);
        indexTagTable(type, table);
        addAdditionalColumns(type, table);
        return table;
    }

    private JTable createMinimumTable(final TagTableModel model, boolean isExtent) {
        model.addColumn(MaeStrings.SRC_COL_NAME);
        model.addColumn(MaeStrings.ID_COL_NAME);

        if (isExtent) {
            // for extent tags, add text and spans columns
            model.addColumn(MaeStrings.SPANS_COL_NAME);
            model.addColumn(MaeStrings.TEXT_COL_NAME);
        }

        JTable table;
        if (!getMainController().isAdjudicating()) {
            table = new JTable(model);
        } else {
            final AdjudicationTableModelI adjudModel = (AdjudicationTableModelI) model;
            table = new JTable(adjudModel);
        }

        for (int i = 0; i < model.getColumnCount(); i++) {
            TableColumn column = table.getColumnModel().getColumn(i);
            setBoldColumnHeader(column);

        }

        table.getTableHeader().setReorderingAllowed(false);
        table.setAutoCreateRowSorter(true);
        table.setAutoCreateColumnsFromModel(false);
        if (!getMainController().isAdjudicating()) {
            table.removeColumn(table.getColumnModel().getColumn(SRC_COL));
        }

        return table;
    }

    private void addAdditionalColumns(TagType type, JTable minimumTable) {
        if (type.isLink()) {
            addArgumentColumns(type, minimumTable);
        }
        addAttributeColumns(type, minimumTable);
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
            if (argType.isRequired()) {
                setBoldColumnHeader(column);
            }
            table.addColumn(column);

            model.addColumn(argType.getName() + "Text");
            column = new TableColumn(model.getColumnCount()-1);
            if (argType.isRequired()) {
                setBoldColumnHeader(column);
            }
            table.addColumn(column);
            model.addArgumentTextColumn(model.getColumnCount() - 1);
        }
    }

    private void setBoldColumnHeader(TableColumn column) {
        column.setHeaderRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                super.getTableCellRendererComponent(t, v, s, f, r, c);
                JTableHeader tableHeader = t.getTableHeader();
                if (tableHeader != null) {
                    setForeground(tableHeader.getForeground());
                    setBackground(tableHeader.getBackground());
                    setFont(tableHeader.getFont().deriveFont(Font.BOLD));
                }
                Icon sortIcon = null;
                if (t.getRowSorter().getSortKeys().size() > 0) {
                    RowSorter.SortKey sortKey = t.getRowSorter().getSortKeys().get(0);
                    if (sortKey != null && t.convertColumnIndexToView(sortKey.getColumn()) == c) {
                        String iconKey = sortKey.getSortOrder() == SortOrder.ASCENDING ?
                                "Table.ascendingSortIcon"
                                : "Table.descendingSortIcon";
                        sortIcon = UIManager.getIcon(iconKey);
                    }
                }
                setIcon(sortIcon);
                setBorder(UIManager.getBorder("TableHeader.cellBorder"));
                setHorizontalAlignment(CENTER);
                setHorizontalTextPosition(LEFT);
                return this;
            }
        });
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
            if (attType.isRequired()) {
                setBoldColumnHeader(column);
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
                throw new MaeControlException(String.format("the data for a new row does not fit to \"%s\" table.", getAssociatedTagTypeName()));
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

        public Set<Integer> getTextColumns() {
            Set<Integer> textCol = new HashSet<>();
            textCol.add(TEXT_COL);
            return textCol;

        }

        public boolean isTextColum(int col) {
            return getTextColumns().contains(col);
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
                // propagated deletion should be called right before the deletion of a row happens
                // that is, in DeleteTag action, not here, after deletion
                String tid = (String) getValueAt(event.getFirstRow(), ID_COL);
                List<Integer> oldSpans = Collections.emptyList();
                try {
                    oldSpans = getDriver().getAnchorsByTid(tid);
                } catch (MaeDBException e) {
                    getMainController().showError(e);
                }
                String colName = getColumnName(event.getColumn());
                String value = (String) getValueAt(event.getFirstRow(), event.getColumn());
                // this will return false if update fails
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
                    String newText = propagateToCurrentTable(event, newValue, oldSpans);
                    propagateToAssociatedTables(tid, newValue, newText);
                } catch (MaeException ignored) {
                    // this spanstring is already validated within getMain().updateDB() method
                }
            }
        }

        String propagateToCurrentTable(TableModelEvent event, String newValue, List<Integer> oldSpans) throws MaeException {
            String newText = updateTextColumnFromSpansChange(event.getFirstRow(), newValue);
            getMainController().assignTextColorsOver(oldSpans);
            List<Integer> newSpans = SpanHandler.convertIntegerarrayToIntegerlist(SpanHandler.convertStringToArray(newValue));
            getMainController().assignTextColorsOver(newSpans);
            getMainController().removeAllBGColors();
            getMainController().addBGColorOver(newSpans, ColorHandler.getVividHighliter());
            return newText;
        }

        String updateTextColumnFromSpansChange(int rowToUpdate, String value) throws MaeException {
            int[] newSpans = SpanHandler.convertStringToArray(value);
            String newText = getMainController().getTextIn(newSpans);
            setValueAt(newText, rowToUpdate, TEXT_COL);
            return newText;
        }

        private void propagateToAssociatedTables(String tid, String newValue, String newText) throws MaeDBException {
            updateAllTagsTableRow(tid, newValue, newText);
            updateAssociatedLinkTagRows(tid, newText);
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
            // ID_COL and TEXT_COL are not editable at the first place
            // and except for SPANS_COL, everything else are attribute columns
            String tid = (String) getValueAt(row, ID_COL);
            ExtentTag tag = (ExtentTag) getMainController().getTagByTid(tid);
            String oldVal;
            if (col == SPANS_COL) {
                oldVal = tag.getSpansAsString();
            } else {
                String attType = getColumnName(col);
                oldVal = tag.getAttributesWithNames().get(attType);
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
            return col != ID_COL && col != SRC_COL && !isArgumentTextColumn(col);
        }

        @Override
        void propagateChange(TableModelEvent event, String tid, String newValue, List<Integer> oldSpans) {
            if (argumentTextColumns.contains(event.getColumn() + 1)) {
                // update adjacent text column
                if (newValue.length() == 0) {
                    setValueAt("", event.getFirstRow(), event.getColumn() + 1);
                } else {
                    ExtentTag newArg = (ExtentTag) getMainController().getTagByTid(newValue);
                    String newText = newArg.getText();
                    setValueAt(newText, event.getFirstRow(), event.getColumn() + 1);
                }
                getMainController().removeAllBGColors();
                try {
                    List<Integer> newSpans = getDriver().getAnchorsByTid(tid);
                    getMainController().assignTextColorsOver(oldSpans);
                    getMainController().assignTextColorsOver(newSpans);
                    getMainController().removeAllBGColors();
                    getMainController().addBGColorOver(newSpans, ColorHandler.getVividHighliter());
                } catch (MaeDBException e) {
                    getMainController().showError(e);
                }

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

    interface AdjudicationTableModelI  extends TableModel {

        void setRowAsGoldTag(int row);

        boolean isGoldTagRow(int row);

        int getNonGoldRowCount();

        void clearTable();

        void populateTable(Tag tag) throws MaeDBException;

    }

    class AdjudicationTableModel extends TagTableModel implements AdjudicationTableModelI {

        private Set<Integer> goldTagRows;

        AdjudicationTableModel(TagType tagType) {
            super(tagType);
            goldTagRows = new HashSet<>();
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return goldTagRows.contains(row) && super.isCellEditable(row, col);
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
            String annotationFileName = getDriver().getAnnotationFileName();
            if (!annotationFileName.equals(tag.getFilename())) {
                addRow(convertTagIntoTableRow(tag, this, this.getAssociatedTagType().isLink()));
            } else {
                setRowAsGoldTag(getRowCount());
                addRow(convertTagIntoTableRow(tag, this, this.getAssociatedTagType().isLink()));
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
            if (event.getColumn() == SPANS_COL) {
                try {
                    propagateToCurrentTable(event, newValue, oldSpans);
                } catch (MaeException ignored) {
                    // this spanstring is already validated within getMain().updateDB() method
                }
            }
        }
    }

    class AdjudicationLinkTableModel extends LinkTagTableModel implements AdjudicationTableModelI {
        private Set<Integer> goldTagRows;

        AdjudicationLinkTableModel(TagType tagType) {
            super(tagType);
            goldTagRows = new HashSet<>();
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return isGoldTagRow(row) && col != ID_COL && col != SRC_COL && !isArgumentTextColumn(col);
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
            String annotationFileName = getDriver().getAnnotationFileName();
            if (annotationFileName.equals(tag.getFilename())) {
                setRowAsGoldTag(getRowCount());
                addRow(convertTagIntoTableRow(tag, this, this.getAssociatedTagType().isLink()));
            } else {
                addRow(convertTagIntoTableRow(tag, this, this.getAssociatedTagType().isLink()));

            }
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

    private class AnnotationCellRenderer extends DefaultTableCellRenderer {

        protected Color nonGoldRowBackground = Color.LIGHT_GRAY;
        protected Color adjudicationSelectionForeground = Color.BLUE;

        @Override
        public JTextComponent getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            JTextComponent renderer;

            if (((TagTableModel) table.getModel()).isTextColum(col)) {
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

            if (col == SRC_COL) {
                renderer.setText(FileHandler.getFileBaseName(getText()));
            }

            renderer.setMargin(new Insets(0,2,0,2));
            renderer.setOpaque(true);
            renderer.setForeground(getCellForeground(isSelected));
            renderer.setToolTipText(value == null ? " " : (String) value);
            renderer.setBackground(c.getBackground());
            renderer.setBorder(hasFocus ?
                    UIManager.getBorder("Table.focusCellHighlightBorder")
                    : BorderFactory.createEmptyBorder(1, 1, 1, 1));

            return renderer;
        }

        private Color getCellForeground(boolean isSelected) {
            if (getMainController().isAdjudicating() && isSelected) {
                return adjudicationSelectionForeground;
            } else if (isSelected) {
                return UIManager.getColor("Table.selectionForeground");
            } else {
                return UIManager.getColor("Table.foreground");
            }

        }

    }

    private class AdjudicationCellRenderer extends AnnotationCellRenderer {

        AdjudicationTableModelI dataModel;
        TagType associatedType;
        final int indicatorThickness = 1;
        final Border absMinorityIndicator = BorderFactory.createLineBorder(Color.RED, indicatorThickness);
        final Border minorityIndicator = BorderFactory.createLineBorder(Color.ORANGE, indicatorThickness);
        final Border majorityIndicator = BorderFactory.createLineBorder(Color.YELLOW, indicatorThickness);
        final Border absMajorityIndicator = BorderFactory.createLineBorder(Color.GREEN, indicatorThickness);
        // this needs to match to what's returned from getValueDistribution() in terms of order
        final Border[] borders = new Border[]{absMinorityIndicator, minorityIndicator, majorityIndicator, absMajorityIndicator};


        public AdjudicationCellRenderer(TagTableModel model) {
            associatedType = model.getAssociatedTagType();
            dataModel = (AdjudicationTableModelI) model;
        }

        @Override
        public JTextComponent getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {

            JTextComponent renderer = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

            renderer.setBackground(dataModel.isGoldTagRow(table.convertRowIndexToModel(row)) ? getBackground() : nonGoldRowBackground);
            int lastMinimalColumn = associatedType.isExtent() ? TEXT_COL : Collections.max(((TagTableModel) dataModel).getTextColumns());

            if (!dataModel.isGoldTagRow(table.convertRowIndexToModel(row))) {
                if (col == SRC_COL) {
                    renderer.setForeground(getMainController().getDocumentColor((String) value));
                    renderer.setBorder(null);
                } else if (((TagTableModel) dataModel).isTextColum(col) || col > lastMinimalColumn) {
                    List[] distribution = getValueDistribution(dataModel, col);
                    if (distribution != null) {
                        renderer.setBorder(getAgreementIndicator(distribution, (String) value));
                    }
                } else {
                    renderer.setBorder(null);
                }
            } else {
                renderer.setBackground(UIManager.getColor("Table.background")); // do not change background on selection
            }

            return renderer;
        }

        private Border getAgreementIndicator(List[] distribution, String value) {
            for (int i = 0; i < distribution.length; i++) {
                if (distribution[i].contains(value)) return borders[i];
            }
            return null;
        }

        public List[] getValueDistribution(AdjudicationTableModelI model, int col) {
            // will return array of lists of values
            // [0] -> the absolute majority (over 0.5) (green)
            // [1] -> majority (yellow)
            // [2] -> minority (not the least, not major) (orange)
            // [3] -> the absolute minority (red)
            // if values are equally distributed, all are considered as minor
            // if values are binomialy distributed, the more gets the abs majority, the less gets the abs minority
            // if values are diverged over three or more values
            //   only when the most frequent one is occurred more than 50%, it gets the abs majority, otherwise gets simple majority
            //   the least frequent one and only one always gets the abs minority
            //   the rest gets minorities
            List<String> values = new ArrayList<>();
            for (int i = 0; i < model.getRowCount(); i++) {
                if (!model.isGoldTagRow(i)) {
                    values.add((String) model.getValueAt(i, col));
                }
            }
            if (values.size() <= 1) { // single or no line
                logger.debug("att value agreement: single or no tag selected.");
                return null;
            }
            List<String> absMinor = new ArrayList<>();
            List<String> minor = new ArrayList<>();
            List<String> major = new ArrayList<>();
            List<String> absMajor = new ArrayList<>();
            List[] distribution = new List[]{absMinor, minor, major, absMajor};

            Map<String, Integer> valueCounts = new HashMap<>();

            for (String value : values) {
                Integer count = valueCounts.get(value);
                if (count == null) count = 0;
                count++;
                valueCounts.put(value, count);
            }

            Set<String> valueSet = valueCounts.keySet();
            Set<Integer> counts = new HashSet<>(valueCounts.values());
            if (valueSet.size() == 1 && counts.size() == 1) { // unanimity
                logger.debug("att value agreement: unanimity to " + valueSet.iterator().next());
                absMajor.addAll(valueCounts.keySet());
                return distribution;
            } else if (valueSet.size() > 1 && counts.size() == 1) { // uniform dist
                logger.debug("att value agreement: uniform distribution of " + valueSet);
                minor.addAll(valueCounts.keySet());
                return distribution;
            } else {
                Map<Integer, List<String>> countsToValues = new HashMap<>();
                for (Map.Entry<String, Integer> entry : valueCounts.entrySet()) {
                    List<String> valuesOccued = countsToValues.get(entry.getValue());
                    if (valuesOccued == null) valuesOccued = new ArrayList<>();
                    valuesOccued.add(entry.getKey());
                    countsToValues.put(entry.getValue(), valuesOccued);
                }
                List<Integer> frequencyDist = new ArrayList<>(countsToValues.keySet());
                Collections.sort(frequencyDist);  // ascending sort

                if (valueSet.size() == 2 && counts.size() == 2) { // binomial dist
                    logger.debug("att value agreement: binomial distribution of " + valueSet);
                    absMajor.addAll(countsToValues.get(frequencyDist.get(1)));
                    absMinor.addAll(countsToValues.get(frequencyDist.get(0)));
                    return distribution;
                }

                int majorExist = frequencyDist.size() - 1;
                int theMostOccurenece = frequencyDist.get(majorExist);
                List<String> theMost = countsToValues.get(theMostOccurenece);
                if (theMost.size() == 1) {
                    logger.debug("att value agreement: diverging, and found majority: " + theMost.get(0));
                    if (theMostOccurenece > (values.size() / 2)) {
                        absMajor.addAll(theMost);
                        logger.debug("att value agreement: it was the absolute majority: " + theMost.get(0));
                    } else {
                        major.addAll(theMost);
                        logger.debug("att value agreement: it did not absolutely majored: " + theMost.get(0));
                    }
                    majorExist--;
                }

                int minorExist = 0;
                int theLeastOccurenece = frequencyDist.get(minorExist);
                List<String> theLeast = countsToValues.get(theLeastOccurenece);
                if (theLeast.size() == 1) {
                    absMinor.addAll(theLeast);
                    logger.debug("att value agreement: diverging, and found the absolute minority: " + theLeast.get(0));
                    minorExist++;
                }

                for (int i = majorExist; i <= minorExist; i++) {
                    minor.addAll(countsToValues.get(frequencyDist.get(i)));
                }
                return distribution;
            }

        }
    }

    private class HighlightToggleListener extends MouseAdapter implements ItemListener {

        private TagType tagType;
        private boolean forAllTagsTable;
        private TablePanelView.TogglingTabTitle toggle;

        HighlightToggleListener(TagType tagType, boolean forAllTagsTable, TablePanelView.TogglingTabTitle toggle) {
            this.tagType = tagType;
            this.forAllTagsTable = forAllTagsTable;
            this.toggle = toggle;

        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger() && tagType != dummyForAllTagsTab) {
                callColorSetter();
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (e.isPopupTrigger() && tagType != dummyForAllTagsTab) {
                callColorSetter();
            }
        }

        void callColorSetter() {
            Color newColor = JColorChooser.showDialog(null, "Choose a Color", toggle.getColor());
            if (newColor != null) {
                toggle.setColor(newColor);
                getMainController().setFGColor(tagType, newColor);
            }
        }

        TagType getTagType() {
            return this.tagType;
        }

        @Override
        public void itemStateChanged(ItemEvent e) {

            try {
                getMainController().sendWaitMessage();

                if (forAllTagsTable) {
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
                getMainController().updateNotificationArea();

            } catch (MaeDBException ex) {
                getMainController().showError(ex);
            }

        }

        private List<Integer> getRelevantAnchors() throws MaeDBException {
            return getDriver().getAllAnchorsOfTagType(tagType);

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
                String tid = (String) tableModel.getValueAt(
                        table.convertRowIndexToModel(table.getSelectedRow()), ID_COL);
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

    private class AdjudicationTablePanelMouseListener extends TablePanelMouseListener {
        @Override
        public void mouseReleased(MouseEvent e) {

            if (e.isPopupTrigger()) {
                createAndShowContextMenu(e);
            } else if (e.getClickCount() == 2) {
                // TODO: 2016-02-07 14:53:35EST copy to gold
            }
        }

    }

    private class AdjudicationTabSwitchListener implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            try {
                getMainController().switchAdjudicationTag();
            } catch (MaeDBException ex) {
                getMainController().showError(ex);
            }

        }
    }

    public class TableBatchInsertSwingWorker extends SwingWorker<TagTableModel, String[]> {

        private final TagTableModel tableModel;
        private final List<String[]> tagsToInsert;

        public TableBatchInsertSwingWorker(TagTableModel tableModel, List<String[]> tagsToInsert) {
            this.tableModel = tableModel;
            this.tagsToInsert = tagsToInsert;

        }

        public TableBatchInsertSwingWorker(TagTableModel tableModel, String[][] tagsToInsert) {
            this(tableModel, Arrays.asList(tagsToInsert));
        }

        @Override
        protected TagTableModel doInBackground() {

            // This is a deliberate pause to allow the UI time to render
//            Thread.sleep(500);

            for (String[] tag : tagsToInsert) {
                publish(tag);
                Thread.yield();
            }
            return tableModel;
        }

        @Override
        protected void process(List<String[]> chunks) {
            for (String[] row : chunks) {
                tableModel.addRow(row);
            }
        }
    }

}
