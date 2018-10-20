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

import edu.brandeis.llc.mae.MaeStrings;
import edu.brandeis.llc.mae.controller.MaeControlException;
import edu.brandeis.llc.mae.controller.MaeControllerI;
import edu.brandeis.llc.mae.controller.MaeMainController;
import edu.brandeis.llc.mae.database.MaeDBException;
import edu.brandeis.llc.mae.database.MaeDriverI;
import edu.brandeis.llc.mae.view.TablePanelView;
import edu.brandeis.llc.mae.model.*;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by krim on 12/31/2015.
 */
public class TablePanelController extends MaeControllerI {

    // column number of some fixed attributes
    public static final int SRC_COL = 0;
    public static final int ID_COL = 1;
    public static final int SPANS_COL = 2;
    public static final int TEXT_COL = 3;
    public static final TagType dummyForAllTagsTab = new TagType(MaeStrings.ALL_TABLE_TAB_BACK_NAME, MaeStrings.ALL_TABLE_TAB_PREFIX, false);

    TablePanelView view;
    Set<TagType> activeLinkTags;
    Set<TagType> activeExtentTags;
    List<TagType> tabOrder;
    private Map<String, JTable> tableMap;

    public TablePanelController(MaeMainController mainController) throws MaeControlException, MaeDBException {
        super(mainController);
        view = new TablePanelView();
        emptyTagTables();

    }


    public Set<TagType> getActiveTags() {
        Set<TagType> types = new HashSet<>();
        types.addAll(activeLinkTags);
        types.addAll(activeExtentTags);
        return types;
    }

    public Set<TagType> getActiveExtentTags() {
        return activeExtentTags;
    }

    public Set<TagType> getActiveLinkTags() {
        return activeLinkTags;
    }

    @Override
    public TablePanelView getView() {
        return view;
    }

    public void emptyTagTables() throws MaeDBException {
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
        MaeDriverI driver = getMainController().getDriver();
        if (getMainController().isAdjudicating() ||
                (driver != null && driver.getAllTagTypes().size() > 20)) {
            getView().getTabs().setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        } else {
            getView().getTabs().setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
        }
    }

    public void prepareAllTables() throws MaeDBException, MaeControlException {
        if (!getMainController().isTaskLoaded()) {
            throw new MaeControlException("Cannot make tables without a task definition!");
        }

        emptyTagTables();

        if (getMainController().isAdjudicating()) {
            prepareAdjudicationTables();
        } else {
            prepareAnnotationTables();

        }

    }

    private void prepareAdjudicationTables() throws MaeDBException {
        List<TagType> types = getDriver().getAllTagTypes();
        for (TagType type : types) {
            String name = type.getName();
            JLabel title = new JLabel(name);
            getView().addTab(name, title, makeAdjudicationArea(type));
        }
        getView().getTabs().addChangeListener(new AdjudicationTabSwitchListener(this));
    }

    private void prepareAnnotationTables() throws MaeDBException {
        List<TagType> types = getDriver().getAllTagTypes();
        logger.debug(String.format("start creating tables for %d tag types", types.size()));

        // create a tab for all extents and place it at first
        TablePanelView.TogglingTabTitle allTagsTabTitle = new TablePanelView.TogglingTabTitle(dummyForAllTagsTab);
        getView().addTab(MaeStrings.ALL_TABLE_TAB_BACK_NAME, allTagsTabTitle, makeAllExtentTagsArea());
        // then create tabs for each element in the annotation task
        for (TagType type : types) {
            String name = type.getName();
            TablePanelView.TogglingTabTitle title = createTogglingTabTitle(type);
            getView().addTab(name, title, makeAnnotationArea(type));
            HighlightToggleListener toggleListener = new HighlightToggleListener(this, false, title);
            title.addToggleListener(toggleListener);
            if (type.isExtent()) {
                title.addMouseListener(toggleListener);
            }

        }

        allTagsTabTitle.addToggleListener(new HighlightToggleListener(this, true, allTagsTabTitle));
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

    public void wipeAllTables() {
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

    public void insertAllTags() throws MaeControlException, MaeDBException {
        if (!getMainController().isTaskLoaded() || !getMainController().isDocumentOpen()) {
            throw new MaeControlException("Cannot populate tables without a document open!");
        }
        for (TagType type : tabOrder) {
            TagTableModel tagTypeTableModel = (TagTableModel) tableMap.get(type.getName()).getModel();
            new TableBatchInsertSwingWorker(tagTypeTableModel, getDriver().getAllExtentTagsOfType(type)).execute();
        }
        addMouseListeners();
        addTableModelListeners();

    }

    public void bigFontSize() {
        SimpleAttributeSet attributeSet = new SimpleAttributeSet();
        StyleConstants.setFontSize(attributeSet, 36);

        JTabbedPane tabs = getView().getTabs();
        for (int i = 0; i < tabs.getTabCount(); i++) {
            JScrollPane sp = (JScrollPane) tabs.getComponentAt(i);
            JTable table = (JTable) sp.getViewport().getView();
            table.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 24));
            table.setRowHeight(36);
            table.revalidate();
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

    public void insertValueIntoCell(Tag tag, String colName, String value) {
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

    public void insertNewTagIntoTable(Tag tag, TagType type) throws MaeDBException, MaeControlException {
        TagTableModel tableModel = (TagTableModel) tableMap.get(type.getName()).getModel();
        insertRowData(tableModel, tableModel.getRowCount(), convertTagIntoTableRow(tag, tableModel, type.isLink()));
        if (tag.getTagtype().isExtent() && !getMainController().isAdjudicating()) {
            insertNewTagToAllTagsTable(tag);
        }

    }

    public void insertTagIntoAdjudicationTable(Tag tag) throws MaeDBException {
        AdjudicationTableModelI tableModel = (AdjudicationTableModelI) getView().getTable().getModel();
        tableModel.populateTable(tag);

    }

    public void clearAdjudicationTable() {
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

    protected String[] convertTagIntoTableRow(Tag tag, TagTableModel tableModel, boolean isLink) throws MaeDBException {
        Map<String, String> attMap = tag.getAttributesWithNames();
//        Map<String, String> attMap = tag.getAttributesWithNamesWithoutChecking();

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

    public void selectTabOf(TagType type) {
        getView().getTabs().setSelectedIndex(tabOrder.indexOf(type));
    }

    public TagType getCurrentTagType() {
        return tabOrder.get(getView().getTabs().getSelectedIndex());
    }

    public void selectTagFromTable(Tag tag) throws MaeDBException {
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

    public void removeTagFromTable(Tag tag) throws MaeDBException {
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
    protected void addListeners() throws MaeDBException {
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
            table.addMouseListener(new TablePanelMouseListener(getMainController()));
        }
    }

    void addToggleListeners() {
    }

    private void setColumnRenders(JTable table, TableCellRenderer renderer) {
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

    }

    public JScrollPane makeAllExtentTagsArea() {
        UneditableTableModel model = new UneditableTableModel(this, dummyForAllTagsTab);
        JTable table = createMinimumTable(model, true);
        tabOrder.add(dummyForAllTagsTab);
        tableMap.put(MaeStrings.ALL_TABLE_TAB_BACK_NAME, table);
        AnnotationCellRenderer renderer = new AnnotationCellRenderer();
        setColumnRenders(table, renderer);

        return new JScrollPane(table);
    }

    public JScrollPane makeAnnotationArea(TagType type) {
        TagTableModel model = type.isExtent()? new TagTableModel(this, type) : new LinkTagTableModel(this, type);
        JTable table = makeTagTable(type, model);
        if (table.getColumnCount() > 8) {
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        }
        AnnotationCellRenderer renderer = new AnnotationCellRenderer();
        setColumnRenders(table, renderer);
        logger.debug("successfully created a table for: " + type.getName());
        return new JScrollPane(table);
    }

    public JScrollPane makeAdjudicationArea(TagType type) {
        TagTableModel model = type.isExtent()? new AdjudicationTableModel(this, type) : new AdjudicationLinkTableModel(this, type);
        model.addTableModelListener(model);
        JTable table = makeTagTable(type, model);
        if (table.getColumnCount() > 8) {
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        }
        AdjudicationCellRenderer renderer = new AdjudicationCellRenderer(this, model);
        setColumnRenders(table, renderer);
        table.addMouseListener(new AdjudicationTablePanelMouseListener(getMainController()));
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


    public class TableBatchInsertSwingWorker extends SwingWorker<Void, Object[]> {

        private final TagTableModel tableModel;
        private final Collection<? extends Tag> tagsToInsert;

        TableBatchInsertSwingWorker(TagTableModel tableModel, Collection<? extends Tag> tagsToInsert) {
            this.tableModel = tableModel;
            this.tagsToInsert = tagsToInsert;
        }

        @Override
        protected Void doInBackground() {
            TagType type = tableModel.getAssociatedTagType();
            if (type.isExtent()) {
                TagTableModel allTagsModel = (TagTableModel)
                        tableMap.get(MaeStrings.ALL_TABLE_TAB_BACK_NAME).getModel();
                for (Tag tag : tagsToInsert) {
                    try {
                        String[] rowString
                                = convertTagIntoTableRow(tag, tableModel, false);
                        publish(new Object[]{tableModel, rowString});
                        publish(new Object[]{allTagsModel,
                                Arrays.copyOfRange(rowString, SRC_COL, TEXT_COL+1)});
                    } catch (MaeDBException ignored) {
                    }
                }
            } else {
                for (Tag tag : tagsToInsert) {
                    try {
                        publish(new Object[]{tableModel, convertTagIntoTableRow(tag, tableModel, true)});
                    } catch (MaeDBException ignored){
                    }
                }
            }
            return null;
        }

        @Override
        protected void process(List<Object[]> modelAndRows) {
            for (Object[] modelAndRow : modelAndRows) {
                TagTableModel model = (TagTableModel) modelAndRow[0];
                String[] row = (String[]) modelAndRow[1];
                model.addRow(row);
            }
        }
    }

}
