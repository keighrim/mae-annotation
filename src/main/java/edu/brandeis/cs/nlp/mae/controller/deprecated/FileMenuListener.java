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

package edu.brandeis.cs.nlp.mae.controller.deprecated;

import edu.brandeis.cs.nlp.mae.util.MappedList;
import edu.brandeis.cs.nlp.mae.database.DTD;
import edu.brandeis.cs.nlp.mae.io.DTDLoader;
import edu.brandeis.cs.nlp.mae.io.FileOperations;
import edu.brandeis.cs.nlp.mae.io.XMLFileLoader;
import edu.brandeis.cs.nlp.mae.controller.MaeMainUI;

import javax.swing.*;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Hashtable;

/**
 * Listener for the File menu; determines what action to take for loading/saving
 * documents.
 */
public class FileMenuListener extends AbstractAction implements ActionListener {
    private MaeMainUI maeMainUI;

    public FileMenuListener(MaeMainUI maeMainUI) {
        this.maeMainUI = maeMainUI;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        String command = actionEvent.getActionCommand();

        // TODO: 12/31/2015 each if-branch to separate action class
        if (command.equals("Load DTD")) { // separation done
            if (maeMainUI.isFileOpen() && maeMainUI.isTaskChanged()) {
                maeMainUI.showSaveWarning();
            }
            loadDtd();
        } else if (command.equals("Add File")) {
            addFile();
        } else if (command.equals("Load File")) {
            if (maeMainUI.isFileOpen() && maeMainUI.isTaskChanged()) {
                maeMainUI.showSaveWarning();
            }
            loadFile();
        } else if (command.equals("Save RTF")) {
            saveRtf();
        } else if (command.equals("Save XML")) {
            saveXml();
        } else if (command.equals("Close File")) {
            if (maeMainUI.isFileOpen() && maeMainUI.isTaskChanged()) {
                maeMainUI.showSaveWarning();
            }
            closeFile();
        }
        // reset status bar after 3 secs
        maeMainUI.delayedUpdateStatusBar(3000);
    }

    void loadDtd() {
        if (maeMainUI.getLoadFC().showOpenDialog(maeMainUI) == JFileChooser.APPROVE_OPTION) {
            File file = maeMainUI.getLoadFC().getSelectedFile();
            try {
                maeMainUI.getTextPanel().setStyledDocument(new DefaultStyledDocument());
                DTDLoader dtdl = new DTDLoader(file);
                maeMainUI.getTask().resetDb();
                DTD d = dtdl.getDTD();
                maeMainUI.getTask().setDtd(d);
                maeMainUI.getActiveLinks().clear();
                maeMainUI.getActiveExts().clear();
                maeMainUI.assignColors();
                maeMainUI.resetTablePanel();

                // refresh interfaces
                maeMainUI.updateMenus();
                maeMainUI.updateTitle();
                maeMainUI.resetSpans();
                maeMainUI.returnToNormalMode(false);
                maeMainUI.getStatusBar().setText("DTD load succeed! Click anywhere to continue.");

                if (maeMainUI.getTask().getAllTagTypes().size() > 20) {
                    maeMainUI.getBottomTable().setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
                } else {
                    maeMainUI.getBottomTable().setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
                }

                maeMainUI.setFileOpen(false);
            } catch (Exception ex) {
                System.err.println("Error loading DTD");
                ex.printStackTrace();

                // print out the error message on the status bar
                maeMainUI.getStatusBar().setText("Error loading DTD: " + ex.toString());
            }
        }
    }

    void addFile() {
        // TODO re-write this part
        boolean succeed = true;
        String status = "";
        if (maeMainUI.getLoadFC().showOpenDialog(maeMainUI) == JFileChooser.APPROVE_OPTION) {
            File file = maeMainUI.getLoadFC().getSelectedFile();
            maeMainUI.setWorkingFileName(file.getName());
            try {
                maeMainUI.updateTitle();
                maeMainUI.setFileOpen(true);
                maeMainUI.getTask().resetDb();
                maeMainUI.getTask().resetIdTracker();
                maeMainUI.getTask().setWorkingFile(maeMainUI.getWorkingFileName());

                // refresh interfaces
                maeMainUI.resetTablePanel();
                maeMainUI.updateMenus();
                maeMainUI.resetSpans();
                maeMainUI.returnToNormalMode(false);

                maeMainUI.getTextPanel().setStyledDocument(new DefaultStyledDocument());
                maeMainUI.getTextPanel().setContentType("text/plain; charset=UTF-8");
                maeMainUI.getMainFrame().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                if (FileOperations.hasTags(file)) {
                    XMLFileLoader xfl = new XMLFileLoader(file);
                    StyledDocument d = maeMainUI.getTextPanel().getStyledDocument();
                    Style def = StyleContext.getDefaultStyleContext()
                            .getStyle(StyleContext.DEFAULT_STYLE);
                    Style regular = d.addStyle("regular", def);
                    d.insertString(0, xfl.getTextChars(), regular);
                    // newTags is a hash from tagType to attib list
                    // each attrib is stored in a has from att name to value
                    MappedList<String, Hashtable<String, String>> newTags
                            = xfl.getTagHash();
                    if (newTags.size() > 0) {
                        maeMainUI.processTagHash(newTags);
                    }
                } else {  // that is, if it's only a text file
                    StyledDocument d = maeMainUI.getTextPanel().getStyledDocument();
                    maeMainUI.getTextPanel().setStyledDocument(FileOperations.setText(file, d));
                }
                maeMainUI.getTextPanel().requestFocus(true);
                maeMainUI.getTextPanel().getCaret().setDot(0);
                maeMainUI.getTextPanel().getCaret().moveDot(1);
            } catch (Exception ex) {
                maeMainUI.setFileOpen(false);
                ex.printStackTrace();
                succeed = false;
                status = "Error loading file";
            }
        }
        maeMainUI.getMainFrame().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        maeMainUI.getTextPanel().setCaretPosition(0);
        // refresh status bar after all caret events
        if (succeed) {
            status = "File load succeed! Click anywhere to continue.";
        }
        maeMainUI.getStatusBar().setText(status);

    }

    void loadFile() {
        boolean succeed = true;
        String status = "";
        if (maeMainUI.getLoadFC().showOpenDialog(maeMainUI) == JFileChooser.APPROVE_OPTION) {
            File file = maeMainUI.getLoadFC().getSelectedFile();
            maeMainUI.setWorkingFileName(file.getName());
            try {
                maeMainUI.updateTitle();
                maeMainUI.setFileOpen(true);
                maeMainUI.getTask().resetDb();
                maeMainUI.getTask().resetIdTracker();
                maeMainUI.getTask().setWorkingFile(maeMainUI.getWorkingFileName());

                // refresh interfaces
                maeMainUI.resetTablePanel();
                maeMainUI.updateMenus();
                maeMainUI.resetSpans();
                maeMainUI.returnToNormalMode(false);

                maeMainUI.getTextPanel().setStyledDocument(new DefaultStyledDocument());
                maeMainUI.getTextPanel().setContentType("text/plain; charset=UTF-8");
                maeMainUI.getMainFrame().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                if (FileOperations.hasTags(file)) {
                    XMLFileLoader xfl = new XMLFileLoader(file);
                    StyledDocument d = maeMainUI.getTextPanel().getStyledDocument();
                    Style def = StyleContext.getDefaultStyleContext()
                            .getStyle(StyleContext.DEFAULT_STYLE);
                    Style regular = d.addStyle("regular", def);
                    d.insertString(0, xfl.getTextChars(), regular);
                    // newTags is a hash from tagType to attib list
                    // each attrib is stored in a has from att name to value
                    MappedList<String, Hashtable<String, String>> newTags
                            = xfl.getTagHash();
                    if (newTags.size() > 0) {
                        maeMainUI.processTagHash(newTags);
                    }
                } else {  // that is, if it's only a text file
                    StyledDocument d = maeMainUI.getTextPanel().getStyledDocument();
                    maeMainUI.getTextPanel().setStyledDocument(FileOperations.setText(file, d));
                }
                maeMainUI.getTextPanel().requestFocus(true);
                maeMainUI.getTextPanel().getCaret().setDot(0);
                maeMainUI.getTextPanel().getCaret().moveDot(1);
            } catch (Exception ex) {
                maeMainUI.setFileOpen(false);
                ex.printStackTrace();
                succeed = false;
                status = "Error loading file";
            }
        }
        maeMainUI.getMainFrame().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        maeMainUI.getTextPanel().setCaretPosition(0);
        // refresh status bar after all caret events
        if (succeed) {
            status = "File load succeed! Click anywhere to continue.";
        }
        maeMainUI.getStatusBar().setText(status);

    }

    void saveRtf() {
        String rtfName = maeMainUI.getWorkingFileName() + maeMainUI.getFilenameSuffix() + ".rtf";
        maeMainUI.getSaveFC().setSelectedFile(new File(rtfName));
        if (maeMainUI.getSaveFC().showSaveDialog(maeMainUI) == JFileChooser.APPROVE_OPTION) {
            File file = maeMainUI.getSaveFC().getSelectedFile();
            maeMainUI.setTaskChanged(false);
            try {
                FileOperations.saveRTF(file, maeMainUI.getTextPanel());
                maeMainUI.getStatusBar().setText("Save Complete :" + rtfName);
            } catch (Exception ex) {
                ex.printStackTrace();
                maeMainUI.getStatusBar().setText("Error saving RTF file");
            }
        }

    }

    void saveXml() {
        String xmlName;
        if (maeMainUI.getWorkingFileName().endsWith(".xml")) {
            String pureName = maeMainUI.getWorkingFileName().
                    substring(0, maeMainUI.getWorkingFileName().length() - 4);
            if (pureName.endsWith(maeMainUI.getFilenameSuffix())) {
                xmlName = maeMainUI.getWorkingFileName();
            } else {
                xmlName = pureName + maeMainUI.getFilenameSuffix() + ".xml";
            }
        } else {
            xmlName = maeMainUI.getWorkingFileName() + maeMainUI.getFilenameSuffix() + ".xml";
        }
        maeMainUI.getSaveFC().setSelectedFile(new File(xmlName));
        if (maeMainUI.getSaveFC().showSaveDialog(maeMainUI) == JFileChooser.APPROVE_OPTION) {
            File file = maeMainUI.getSaveFC().getSelectedFile();
            maeMainUI.setTaskChanged(false);
            maeMainUI.setWorkingFileName(file.getName());
            maeMainUI.getTask().setWorkingFile(maeMainUI.getWorkingFileName());
            try {
                FileOperations.saveXML(file,
                        maeMainUI.getTextPanel(), maeMainUI.getElementTables(),
                        maeMainUI.getTask().getAllTagTypes(), maeMainUI.getTask().getDTDName());
                maeMainUI.updateTitle();
                maeMainUI.getStatusBar().setText(
                        String.format("Save Complete: %s", maeMainUI.getWorkingFileName()));
            } catch (Exception ex) {
                ex.printStackTrace();
                maeMainUI.getStatusBar().setText("Error saving XML file");
            }
        }
    }

    void closeFile() {
        maeMainUI.setFileOpen(false);
        maeMainUI.getTextPanel().setStyledDocument(new DefaultStyledDocument());
        maeMainUI.getTask().resetDb();
        maeMainUI.getActiveLinks().clear();
        maeMainUI.getActiveExts().clear();
        maeMainUI.assignColors();
        maeMainUI.resetTablePanel();
        maeMainUI.updateMenus();
        maeMainUI.resetSpans();
        maeMainUI.returnToNormalMode(false);
        maeMainUI.getStatusBar().setText("All Files closed");
        maeMainUI.setWorkingFileName("");
        maeMainUI.setTaskChanged(false);
        maeMainUI.updateTitle();
    }
}
