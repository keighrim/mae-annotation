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

import edu.brandeis.cs.nlp.mae.MaeException;
import edu.brandeis.cs.nlp.mae.MaeStrings;
import edu.brandeis.cs.nlp.mae.controller.action.*;
import edu.brandeis.cs.nlp.mae.database.MaeDBException;
import edu.brandeis.cs.nlp.mae.model.ExtentTag;
import edu.brandeis.cs.nlp.mae.model.Tag;
import edu.brandeis.cs.nlp.mae.model.TagType;

import javax.swing.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static edu.brandeis.cs.nlp.mae.MaeHotKeys.*;
import static edu.brandeis.cs.nlp.mae.MaeStrings.*;

/**
 * Created by krim on 1/2/2016.
 */
public class ContextMenuController extends MaeControllerI {

    static final int ETAG = 0;
    static final int NCTAG = 1;
    static final int EMPTY_LTAG = 2;
    static final int LTAG = 3;
    static final int LTAG_FROM_TABLE = 4;
    static final int DELETE_MENU = 0;
    static final int SETARG_MENU = 1;

    public ContextMenuController(MaeMainController mainController) {
        super(mainController);

    }

    @Override
    void reset() {
        // nothing to reset

    }

    @Override
    void addListeners() throws MaeException {
        // nothing to listen

    }

    // TODO: 2016-01-23 16:20:59EST continue from here, add more menu items that's mode specific
    public JPopupMenu createTextContextMenu() throws MaeDBException {

        List<ExtentTag> tags = getMainController().getExtentTagsInSelectedSpans();
        JPopupMenu contextMenu = new JPopupMenu();

        // TODO: 2016-01-23 01:38:46EST add more mode specific items as go on
        if (getMainController().isTextSelected()) {
            if (getMainController().getMode() != MaeMainController.MODE_ARG_SEL) {
                contextMenu.add(createMakeTagMenu(ETAG));
            } else if (getMainController().getSelectedArguments() != null
                    && getMainController().getSelectedArguments().size() > 0) {
                contextMenu.add(createMakeTagMenu(LTAG));
            }
            contextMenu.addSeparator();
        }
        contextMenu.add(createMakeTagMenu(NCTAG));
        contextMenu.add(createMakeTagMenu(EMPTY_LTAG));
        switch (tags.size()) {
            case 0:
                break;
            case 1:
                contextMenu.addSeparator();
                contextMenu.add(createSingleDeleteMenu(tags.get(0)));
                contextMenu.add(createSingleSetArgMenu(tags.get(0)));
                break;
            default:
                contextMenu.addSeparator();
                contextMenu.add(createPluralDeleteMenu(tags));
                contextMenu.add(createPluralSetArgMenu(tags));

        }
        if (getMainController().getMode() != MaeMainController.MODE_NORMAL) {
            contextMenu.addSeparator();
            // this will not work unless figure out how to prevent right click from firing caret update
            contextMenu.add(createUndoLastSelectionMenu());
            contextMenu.add(createStartOverMenu());
        }

        return contextMenu;

    }

    private JMenuItem createUndoLastSelectionMenu() {
        return new JMenuItem(new UndoLastSelection(MENUITEM_UNDOSELECTION, null, ksUNDO, cmnUNDO, getMainController()));
    }

    private JMenuItem createStartOverMenu() {
        return new JMenuItem(new ResetSelection(MENUITEM_STARTOVER, null, null, cmnSTARTOVER, getMainController()));
    }

    JMenu createMakeTagMenu(int category) throws MaeDBException {
        JMenu makeTagMenu = new JMenu(getMakeTagMenuLabel(category));
        makeTagMenu.setMnemonic(getMenuMnemonic(category));
        List<TagType> types = getTagTypes(category);

        int typeCount = 0;
        for (TagType type : types) {
            JMenuItem makeTagItem = new JMenuItem(getMakeTagAction(category, typeCount++, type));
            makeTagItem.setActionCommand(type.getName());
            makeTagMenu.add(makeTagItem);
        }
        return makeTagMenu;
    }

    private int getMenuMnemonic(int category) {
        switch (category) {
            case NCTAG:
                return cmnCREATENC;
            case EMPTY_LTAG:
                return cmnCREATELINK;
            default:
                return cmnCREATE;
        }
    }

    private MaeActionI getMakeTagAction(int category, int mnemonicNum, TagType type) {
        String makeTagItemLabel;
        Integer mnemonic;
        if (mnemonicNum < 10) {
            makeTagItemLabel = String.format("(%d) %s", mnemonicNum + 1, type.getName());
            mnemonic = numKeys[mnemonicNum];
        } else {
            makeTagItemLabel = String.format("    %s", type.getName());
            mnemonic = null;
        }
        switch (category) {
            case LTAG:
                return new MakeLink(makeTagItemLabel, null, null, mnemonic, getMainController());
            case LTAG_FROM_TABLE:
                return new MakeLinkFromTable(makeTagItemLabel, null, null, mnemonic, getMainController());
            default:
                return new MakeTag(makeTagItemLabel, null, null, mnemonic, getMainController());
        }
    }

    private String getMakeTagMenuLabel(int category) {
        switch (category) {
            case ETAG:
                return MENUITEM_CREATE_ETAG;
            case NCTAG:
                return MENUITEM_CREATE_NCTAG;
            case EMPTY_LTAG:
                return MENUITEM_CREATE_LTAG_EMPTY;
            case LTAG:
                return MENUITEM_CREATE_LTAG_FROM_SEL;
        }
        return null;
    }

    private List<TagType> getTagTypes(int category) throws MaeDBException {
        switch (category) {
            case ETAG:
                return getDriver().getExtentTagTypes();
            case NCTAG:
                List<TagType> ncTypes = new ArrayList<>();
                for (TagType type : getDriver().getExtentTagTypes()) {
                    if (type.isNonConsuming()) ncTypes.add(type);
                }
                return ncTypes;
            default:
                return getDriver().getLinkTagTypes();
        }
    }

    JMenuItem createSingleDeleteMenu(Tag tag) throws MaeDBException {
        return createDeleteMenuItem(tag, String.format(MENUITEM_DELETE_TAG_SINGLE, tag.toString()), cmnDELETE);
    }

    JMenu createPluralDeleteMenu(List<? extends Tag> tags) throws MaeDBException {
        JMenu deleteMenu = new JMenu(MENU_DELETE_TAG);
        deleteMenu.setMnemonic(cmnDELETE);
        deleteMenu.add(createWholeDeleteMenuItem(tags, "(0) " + String.format(MENUITEM_DELETE_TAG_PLURAL, tags.size(), tags.toString()), n0));

        // this will assign hotkey
        createMenuItemsWithNumberMnemonics(tags, MENUITEM_DELETE_TAG_SINGLE, deleteMenu, 9, DELETE_MENU);

        return deleteMenu;
    }

    private JMenuItem createDeleteMenuItem(Tag tag, String label, int mnemonic) {
        MaeActionI deleteTagAction = getDeleteTagAction(label, mnemonic);
        JMenuItem deleteTagItem = new JMenuItem(deleteTagAction);
        deleteTagItem.setActionCommand(tag.getId());
        return deleteTagItem;
    }

    private JMenuItem createWholeDeleteMenuItem(List<? extends Tag> tags, String label, int mnemonic) {
        String tids = "";
        for (Tag tag : tags) {
            tids += tag.getId() + SEP;
        }
        MaeActionI deleteTagAction = new DeleteTag(label, null, null, mnemonic, getMainController());
        JMenuItem deleteTagItem = new JMenuItem(deleteTagAction);
        deleteTagItem.setActionCommand(tids);
        return deleteTagItem;
    }

    private MaeActionI getDeleteTagAction(String deleteTagLabel, int mnemonic) {
        return new DeleteTag(deleteTagLabel, null, null, mnemonic, getMainController());

    }

    JMenuItem createSingleSetArgMenu(ExtentTag tag) {
        return createSetArgMenuItem(tag, String.format(MENUITEM_SETARG_SINGLE, tag.toString()), cmnSETARG);

    }

    private JMenuItem createSetArgMenuItem(Tag tag, String label, int mnemonic) {
        MaeActionI setArgAction = getSetArgTagAction(label, mnemonic);
        JMenuItem setArgItem = new JMenuItem(setArgAction);
        setArgItem.setActionCommand(tag.getId());
        return setArgItem;
    }

    private MaeActionI getSetArgTagAction(String label, int mnemonic) {
        return new SetArgument(label, null, null, mnemonic, getMainController());

    }

    JMenu createPluralSetArgMenu(List<? extends Tag> tags) throws MaeDBException {
        JMenu setArgMenu = new JMenu(MENU_SETARG);
        setArgMenu.setMnemonic(cmnSETARG);
        createMenuItemsWithNumberMnemonics(tags, MENUITEM_SETARG_SINGLE, setArgMenu, 10, SETARG_MENU);
        return setArgMenu;
    }

    private void createMenuItemsWithNumberMnemonics(List<? extends Tag> tags, String labelTemplate, JMenu menu, int endPoint, int menuType) {
        String label;
        Integer mnemonic;
        int i = 0;
        for (Tag tag : tags) {
            String labelWithoutMnemonic = String.format(labelTemplate, tag.toString());
            if (i < endPoint) {
                label = String.format("(%d) %s", i + 1, labelWithoutMnemonic);
                mnemonic = numKeys[i++];
            } else {
                label = String.format("    %s", labelWithoutMnemonic);
                mnemonic = null;
            }
            switch (menuType) {
                case DELETE_MENU:
                    menu.add(createDeleteMenuItem(tag, label, mnemonic));
                    break;
                case SETARG_MENU:
                    menu.add(createSetArgMenuItem(tag, label, mnemonic));
                    break;
            }
        }
    }

    public JPopupMenu createTableContextMenu(JTable table) throws MaeDBException {

        int selected = table.getSelectedRowCount();

        String rowS = selected == 1 ? "row" : "rows";
        JPopupMenu contextMenu = new JPopupMenu(String.format("%d %s selected", selected, rowS));
        TablePanelController.TagTableModel model = (TablePanelController.TagTableModel) table.getModel();

        if (selected == 1) {
            contextMenu.add(createSingleDeleteMenu(table.getSelectedRow(), model));
            contextMenu.add(createSingleSetArgMenu(table.getSelectedRow(), model));
        } else {
            contextMenu.add(createPluralDeleteMenu(table.getSelectedRows(), model));
            if (model.getAssociatedTagType().isExtent()) {
                contextMenu.add(createMakeLinkFromTableMenu(table.getSelectedRows(), model));
            }
        }

        if (model.getAssociatedTagType().isExtent()) {
//             TODO: 2016-01-17 20:40:03EST set-as-argument menu, etc
        }
        return contextMenu;

    }

    private JMenuItem createSingleDeleteMenu(int selectedRow, TablePanelController.TagTableModel model) throws MaeDBException {
        String tid = (String) model.getValueAt(selectedRow, TablePanelController.ID_COL);
        Tag tag = getDriver().getTagByTid(tid);
        return createSingleDeleteMenu(tag);
    }

    private JMenu createPluralDeleteMenu(int[] selectedRows, TablePanelController.TagTableModel model) throws MaeDBException {
        List<Tag> tags = new LinkedList<>();
        for (int row : selectedRows) {
            tags.add(getDriver().getTagByTid((String) model.getValueAt(row, TablePanelController.ID_COL)));
        }
        return createPluralDeleteMenu(tags);
    }

    JMenu createMakeLinkFromTableMenu(int[] selectedRows, TablePanelController.TagTableModel model) throws MaeDBException {
        JMenu makeLinkFromTableMenu = new JMenu(MENUITEM_CREATE_LTAG_FROM_SEL);
        String tids = MaeStrings.SEP;
        for (int row : selectedRows) {
            tids += (String) model.getValueAt(row, TablePanelController.ID_COL) + MaeStrings.SEP;
        }
        int typeCount = 0;
        for (TagType linkType : getTagTypes(LTAG)) {
            JMenuItem makeLinkFromTableItem = new JMenuItem(getMakeTagAction(LTAG_FROM_TABLE, typeCount, linkType));
            makeLinkFromTableItem.setActionCommand(linkType.getName() + tids);
            makeLinkFromTableMenu.add(makeLinkFromTableItem);
        }
        return makeLinkFromTableMenu;

    }

    private JMenuItem createSingleSetArgMenu(int selectedRow, TablePanelController.TagTableModel model) throws MaeDBException {
        String tid = (String) model.getValueAt(selectedRow, TablePanelController.ID_COL);
        Tag tag = getDriver().getTagByTid(tid);
        return createSingleSetArgMenu((ExtentTag) tag);
    }

}

