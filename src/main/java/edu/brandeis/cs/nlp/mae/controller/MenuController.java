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
import edu.brandeis.cs.nlp.mae.controller.action.*;
import edu.brandeis.cs.nlp.mae.controller.action.MaeActionI;
import edu.brandeis.cs.nlp.mae.database.MaeDBException;
import edu.brandeis.cs.nlp.mae.model.ExtentTag;
import edu.brandeis.cs.nlp.mae.model.Tag;
import edu.brandeis.cs.nlp.mae.model.TagType;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;

import java.util.LinkedList;
import java.util.List;
import java.awt.*;

import static edu.brandeis.cs.nlp.mae.MaeHotKeys.*;
import static edu.brandeis.cs.nlp.mae.MaeStrings.*;

/**
 * Created by krim on 1/2/2016.
 */
public class MenuController extends MaeControllerI {

    // this controller is responsible for all these menus
    JMenu fileMenu;
    JMenu displayMenu;
    JMenu helpMenu;
    JMenu modeMenu;
    JMenu preferenceMenu;


    // and this view for top main menu
    private JMenuBar menubar;

    public MenuController(MaeMainController mainController) {
        super(mainController);
        view = new JPanel(new BorderLayout());
        menubar = new JMenuBar();
        reset();
        view.add(menubar, BorderLayout.CENTER);

    }

    @Override
    void reset() {

        menubar.removeAll();

        fileMenu = createFileMenu();
        menubar.add(fileMenu);

        view.updateUI();

    }

    @Override
    void addListeners() throws MaeException {

    }

    public JPopupMenu createTextContextMenu() throws MaeDBException {

        JPopupMenu contextMenu = new JPopupMenu();

        if (getMainController().getSelectedTextSpans().length > 0) {
            contextMenu.add(createMakeTagMenu(false));
        }
        contextMenu.add(createMakeTagMenu(true));

        List<ExtentTag> tags = getMainController().getExtentTagsInSelectedSpans();

        return contextMenu;

    }

    JMenu createMakeTagMenu(boolean nc) throws MaeDBException {
        JMenu makeTagMenu = new JMenu("Create an Extent tag with selected text");
        int i = 0;
        List<TagType> types = getDriver().getExtentTagTypes();

        if (nc) {
            makeTagMenu = new JMenu("Create an NC Extent tag with no text associated");
            List<TagType> ncTypes = new ArrayList<>();
            for (TagType type : types) {
                if (type.isNonConsuming()) ncTypes.add(type);
            }
            types = ncTypes;
        }

        for (TagType type : types) {
            JMenuItem makeTagItem;
            String makeTagItemLabel;
            MaeActionI makeTagAction;
            if (i < 10) {
                makeTagItemLabel = String.format("(%d) %s", i+1, type.getName());
                if (nc) {
                    makeTagAction = new MakeNCTag(makeTagItemLabel, null, null, numKeys[i], getMainController());
                } else {
                    makeTagAction = new MakeTag(makeTagItemLabel, null, null, numKeys[i], getMainController());
                }
                i++;
            } else {
                makeTagItemLabel = String.format("    %s", type.getName());
                if (nc) {
                    makeTagAction = new MakeNCTag(makeTagItemLabel, null, null, null, getMainController());
                } else {
                    makeTagAction = new MakeTag(makeTagItemLabel, null, null, null, getMainController());
                }
            }
            makeTagItem = new JMenuItem(makeTagAction);
            makeTagItem.setActionCommand(type.getName());
            makeTagMenu.add(makeTagItem);
        }
        return makeTagMenu;
    }


    public JPopupMenu createTableContextMenu(JTable table) throws MaeDBException {


        String rowS = selected == 1 ? "row" : "rows";
        JPopupMenu contextMenu = new JPopupMenu(String.format("%d %s selected", selected, rowS));
        TablePanelController.TagTableModel model = (TablePanelController.TagTableModel) table.getModel();

        if (selected == 1) {
            contextMenu.add(createSingleDeleteMenu(table.getSelectedRow(), model));
        } else {
            contextMenu.add(createPluralDeleteMenu(table.getSelectedRows(), model));
        }

        if (model.getAssociatedTagType().isExtent()) {
//             TODO: 2016-01-17 20:40:03EST set-as-argument menu, etc
//            contextMenu.add(createSingleArgumentSetMenu(table, model));
        }
        return contextMenu;

    }

    private JMenuItem createSingleDeleteMenu(int selectedRow, TablePanelController.TagTableModel model) throws MaeDBException {
        String tid = (String) model.getValueAt(selectedRow, TablePanelController.ID_COL);
        Tag tag = getDriver().getTagByTid(tid);
        return createDeleteMenuItem(tag, ksDELETE);
    }

    private JMenuItem createDeleteMenuItem(Tag tag, KeyStroke hotKey) {
        MaeActionI deleteTagAction = new DeleteTag(MENUITEM_DELETE_TAG_SINGLE + tag.toString(), null, hotKey, null, getMainController());
        JMenuItem deleteTagItem = new JMenuItem(deleteTagAction);
        deleteTagItem.setActionCommand(tag.getId());
        return deleteTagItem;
    }


    private JMenu createPluralDeleteMenu(int[] selectedRows, TablePanelController.TagTableModel model) throws MaeDBException {

        JMenu deleteMenu = new JMenu(MENU_TBPOP_ITEM_DELETE);
        deleteMenu.setMnemonic(cmnDELETE);

        List<Tag> tags = new LinkedList<>();
        for (int row : selectedRows) {
            tags.add(getDriver().getTagByTid((String) model.getValueAt(row, TablePanelController.ID_COL)));
        }

        deleteMenu.add(createDeleteAllMenuItem(tags, ksN0));

        // this will assign hotkey
        int i = 0;
        for (Tag tag : tags) {
            if (i < 9) {
                deleteMenu.add(createDeleteMenuItem(tag, noneNums[i]));
                i++;
            } else {
                deleteMenu.add(createDeleteMenuItem(tag, null));
            }
        }

        return deleteMenu;
    }

    private JMenuItem createDeleteAllMenuItem(List<Tag> tags, KeyStroke hotKey) {
        List<String> tids = new LinkedList<>();
        for (Tag tag : tags) {
            tids.add(tag.getId());
        }
        MaeActionI deleteTagAction = new DeleteTag(String.format(MENUITEM_DELETE_TAG_PLURAL, tags.size(), tids.toString()), null, hotKey, null, getMainController());
        JMenuItem deleteTagItem = new JMenuItem(deleteTagAction);
        deleteTagItem.setActionCommand(StringUtils.join(tids, SEP));
        return deleteTagItem;
    }


    private JMenu createFileMenu() {
        MaeActionI loadTaskAction = new LoadTask(MENUITEM_LOADTASK, null, ksLOADTASK, null, getMainController());
        MaeActionI openFileAction = new OpenFile(MENUITEM_OPENFILE, null, ksOPENFILE, null, getMainController());
        MaeActionI saveXMLAction = new SaveXML(MENUITEM_SAVEXML, null, ksSAVEXML, null, getMainController());
//        MaeActionI saveRTFAction = new SaveRTF(MENU_FILE_ITEM_SAVERTF, null, ksSAVERTF, null, getMainController());
//        MaeActionI closeFileAction = new LoadTask(MENU_FILE_ITEM_CLOSEFILE, null, ksCLOSEFILE, null, getMainController());
        // TODO: 2016-01-10 16:45:38EST add menu item to load gold standard

        JMenu menu = new JMenu(MENU_FILE);

        JMenuItem loadTask = new JMenuItem(loadTaskAction);
        JMenuItem openFile = new JMenuItem(openFileAction);
        JMenuItem saveXML = new JMenuItem(saveXMLAction);
//        JMenuItem saveRTF = new JMenuItem(saveRTFAction);
//        JMenuItem closeFile = new JMenuItem(closeFileAction);
        boolean taskLoaded = getMainController().isTaskLoaded();
        boolean fileLoaded = getMainController().isDocumentOpen();
        openFile.setEnabled(taskLoaded);
        saveXML.setEnabled(fileLoaded);
//        saveRTF.setEnabled(fileLoaded);
//        closeFile.setEnabled(fileLoaded);

        menu.add(loadTask);
        menu.add(openFile);
        menu.addSeparator();
        menu.add(saveXML);
//        menu.add(saveRTF);
        menu.addSeparator();
//        menu.add(closeFile);
        logger.info("file menu is created: " + menu.getItemCount());
        return menu;
    }


}
