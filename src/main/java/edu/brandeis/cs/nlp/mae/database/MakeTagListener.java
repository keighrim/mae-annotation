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

package edu.brandeis.cs.nlp.mae.database;

import edu.brandeis.cs.nlp.mae.MaeHotKeys;
import edu.brandeis.cs.nlp.mae.MaeStrings;
import edu.brandeis.cs.nlp.mae.model.*;
import edu.brandeis.cs.nlp.mae.controller.MaeMainUI;
import edu.brandeis.cs.nlp.mae.util.SpanHandler;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * This is the class that's called when creating an extent tag (by either popup
 * menu or NC menu) * LinkListner listens to menu items that creating a link tag
 * with arguments from selected text or table rows Creating a link tag from
 * multiple arguments are done by an additional popup window, mLinkPopupFrame
 */
public class MakeTagListener implements ActionListener {
    private MaeMainUI maeMainUI;
    private boolean isLink;
    private boolean isArgLink;
    private String newName;
    private String newId;
    private Elem newElem;

    public MakeTagListener(MaeMainUI maeMainUI) {
        this.maeMainUI = maeMainUI;
    }

    public void actionPerformed(ActionEvent actionEvent) {
        String command = actionEvent.getActionCommand();

        maeMainUI.clearTableSelections();

        // first get tag type; is it a link? is it a NC tag?
        isLink = false;
        if (command.startsWith(MaeStrings.ADD_LINK_COMMAND)) {
            newName = command.
                    substring(MaeStrings.ADD_LINK_COMMAND.length());
            isLink = true;
        } else if (command.startsWith(MaeStrings.ADD_LINK_WITH_ARGS_COMMAND)) {
            newName = command.
                    substring(MaeStrings.ADD_LINK_WITH_ARGS_COMMAND.length());
            isArgLink = true;
        } else if (command.startsWith(MaeStrings.ADD_NC_COMMAND)) {
            // if the tag being added is non-consuming, make sure
            // it's added with (-1, -1) span
            maeMainUI.resetSpans();
            newName = command.substring(MaeStrings.ADD_NC_COMMAND.length());
        } else {
            newName = command;
        }

        // get a new ID
        newElem = maeMainUI.getTask().getTagTypeByName(newName);
        newId = maeMainUI.getTask().getNextID(newName);

        // first add a new tag to table
        // it's needed because link arguments need pre-defined placeholders
        insertToTable();

        // then add to DB
        if (isLink) {
            processLink();
            succeed();
        } else if (isArgLink) {
            processLinkWithArgs();
        } else {
            maeMainUI.addExtTagToDb(newName, newId);
            succeed();
        }
    }

    /**
     * update interface and post to user
     */
    void succeed() {
        // assign colors if necessary
        if (!maeMainUI.isSpansEmpty()) {
            maeMainUI.assignTextColor(maeMainUI.getSpans());
        }
        // return if user prefer
        if (maeMainUI.isOptionExitOnCreation() && (maeMainUI.getMode() != MaeMainUI.M_NORMAL)) {
            maeMainUI.returnToNormalMode(false);
        }
        maeMainUI.resetSpans();
        maeMainUI.getStatusBar().setText(String.format("%s is created!", newId));
        maeMainUI.delayedUpdateStatusBar(3000);
        maeMainUI.setTaskChanged(true);
        maeMainUI.updateTitle();
    }

    private void insertToTable() {
        // bring up corresponding panel from the bottom table
        JTable tab = maeMainUI.getElementTables().get(newName);
        DefaultTableModel tableModel = (DefaultTableModel) tab.getModel();

        //  create a dummy data set and insert to table
        String[] newEmptyData = createEmptyRowData(newName, newId);
        tableModel.addRow(newEmptyData);

        // when adding an extent tag, also insert to all_table
        if (!isLink && !isArgLink) {
            DefaultTableModel allTableModel
                    = (DefaultTableModel) maeMainUI.getElementTables().get(
                    MaeStrings.ALL_TABLE_TAB_BACK_NAME).getModel();
            // all_extent tab takes only obligatory columns
            String[] newdataForAll
                    = Arrays.copyOfRange(newEmptyData, 0, maeMainUI.LAST_ESSE_COL);
            allTableModel.addRow(newdataForAll);
        }

        // move cursor and set focus to newly added tag
        maeMainUI.getBottomTable().setSelectedIndex(maeMainUI.getBottomTable().indexOfTab(newName));
        tab.clearSelection();
        tab.setRowSelectionInterval(
                tableModel.getRowCount() - 1, tableModel.getRowCount() - 1);
        Rectangle rect = tab.getCellRect(
                tableModel.getRowCount() - 1, 0, true);
        tab.scrollRectToVisible(rect);
    }

    private void processLink() {
        // get number of arguments associated with this link tag
        ElemLink target = (ElemLink) newElem;
        int argNum = target.getArgNum();

        // initiate lists with empty strings as dummy arguments and comm with DB
        String[] argIds = new String[argNum], argTypes = new String[argNum];
        Arrays.fill(argIds, "");
        Arrays.fill(argTypes, "");
        maeMainUI.addLinkTagToDb(newName, newId,
                Arrays.asList(argIds), Arrays.asList(argTypes));

        // add id of the new tag to underspecified set for further lookup
        maeMainUI.getUnderspecified().add(newId);
    }

    private void processLinkWithArgs() {
        final ElemLink target = (ElemLink) newElem;
        maeMainUI.setLinkPopupFrame(new JFrame());

        JPanel boxPane = new JPanel(new GridLayout(target.getArgNum() + 1, 2));

        // information for creating a link tag
        final String[] argIds = new String[target.getArgNum()];
        final String[] argTypes = new String[target.getArgNum()];

        // OK button
        JButton okay = new JButton("OK");
        okay.setMnemonic(MaeHotKeys.mnOK_BUTTON);
        okay.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                maeMainUI.addLinkTagToDb(newName, newId,
                        Arrays.asList(argIds), Arrays.asList(argTypes));
                for (int i = 0; i < argIds.length; i++) {
                    maeMainUI.setArgumentInTable(
                            // name, id, argType, argId, argText
                            newName, newId,
                            target.getArguments().get(i), argIds[i],
                            maeMainUI.getTextByID(argTypes[i], argIds[i], true));
                }
                maeMainUI.getLinkPopupFrame().setVisible(false);
                maeMainUI.getLinkPopupFrame().dispose();
                // return if user prefer
                if (maeMainUI.isOptionExitOnCreation()) {
                    maeMainUI.returnToNormalMode(false);
                }
                succeed();
            }
        });

        // cancel button
        JButton cancel = new JButton("Cancel");
        cancel.setMnemonic(MaeHotKeys.mnCANCEL_BUTTON);
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                maeMainUI.getLinkPopupFrame().setVisible(false);
                maeMainUI.getLinkPopupFrame().dispose();
                maeMainUI.removeTableRows(newElem, newId);
                maeMainUI.getStatusBar().setText("Canceled");
                maeMainUI.delayedUpdateStatusBar(3000);
            }
        });

        // comboboxes to select arguments
        for (int i = 0; i < target.getArgNum(); i++) {
            JComboBox<String> candidates = new JComboBox<String>();
            for (String item : maeMainUI.getComboItems(maeMainUI.getPossibleArgIds(), false)) {
                candidates.addItem(item);
            }
            // front of mPossibleArgIds is sorted by selection order
            candidates.setSelectedIndex(i % candidates.getItemCount());
            // set initial argid and argtype from seleted item
            String selected = (String) candidates.getSelectedItem();
            argTypes[i] = selected.split(MaeStrings.COMBO_DELIMITER)[0];
            argIds[i] = selected.split(MaeStrings.COMBO_DELIMITER)[1];

            // action command is simply index of current argument
            candidates.setActionCommand(String.valueOf(i));
            // action listener for changing selection of an item
            candidates.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int command = Integer.parseInt(e.getActionCommand());
                    JComboBox box = (JComboBox) e.getSource();
                    String item = (String) box.getSelectedItem();
                    argTypes[command]
                            = item.split(MaeStrings.COMBO_DELIMITER)[0];
                    argIds[command]
                            = item.split(MaeStrings.COMBO_DELIMITER)[1];

                }
            });
            boxPane.add(new JLabel(target.getArguments().get(i), JLabel.CENTER));
            boxPane.add(candidates);

        }
        boxPane.add(okay);
        boxPane.add(cancel);
        okay.requestFocus();
        JFrame popup = maeMainUI.getLinkPopupFrame();
        popup.add(boxPane);
        popup.pack();
        popup.setTitle(String.format("Creating %s - %s", newName, newId));
        popup.setLocation(300, 200);
        popup.setVisible(true);
        popup.setAlwaysOnTop(true);
        popup.requestFocus();

    }

    private String[] createEmptyRowData(String elemName, String newId) {
        // get the target element and a list of its attrib
        Elem e = maeMainUI.getTask().getTagTypeByName(elemName);
        ArrayList<Attrib> attributes = e.getAttributes();
        String[] newData = new String[attributes.size() + 1];

        // first column is for source file
        newData[0] = maeMainUI.getWorkingFileName();
        int curCol = 1;
        // go through the list of attributes, fill newdata array with proper values
        for (Attrib att : attributes) {
            // get ID number. This isn't as hard-coded as it looks:
            // the columns for the table are created from the Attributes array list
            if (att instanceof AttID) {
                newData[curCol] = newId;
            }
            // since link tags never have spans and text, below is safe
            else if (att.getName().equals(MaeStrings.SPANS_COL_NAME)) {
                newData[curCol] = SpanHandler.convertPairsToString(maeMainUI.getSpans());
            } else if (att.getName().equals(MaeStrings.TEXT_COL_NAME)
                    && !maeMainUI.isSpansEmpty()) {
                newData[curCol] = maeMainUI.getTextIn(maeMainUI.getSpans());
            }
            // for the rest slots of newdata, make sure it's not staying as null value
            else {
                if (att.hasDefaultValue()) {
                    newData[curCol] = att.getDefaultValue();
                } else {
                    newData[curCol] = "";
                }
            }
            curCol++;
        }
        return newData;
    }
}
