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
import edu.brandeis.cs.nlp.mae.database.LocalSqliteDriverImpl;
import edu.brandeis.cs.nlp.mae.database.MaeDBException;
import edu.brandeis.cs.nlp.mae.database.MaeDriverI;
import edu.brandeis.cs.nlp.mae.model.ExtentTag;
import edu.brandeis.cs.nlp.mae.model.LinkTag;
import edu.brandeis.cs.nlp.mae.model.Tag;
import edu.brandeis.cs.nlp.mae.model.TagType;
import edu.brandeis.cs.nlp.mae.util.ColorHandler;
import edu.brandeis.cs.nlp.mae.view.MaeMainView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by krim on 12/30/2015.
 */
public class MaeMainController extends JPanel {

    public static final int MODE_NORMAL = 0;
    public static final int MODE_MULTI_SPAN = 1;
    public static final int MODE_ARG_SEL = 2;
    public static final int MODE_ADJUD = 9;
    protected static final Logger logger = LoggerFactory.getLogger(MaeMainController.class.getName());
    private int mode;

    private JFrame mainFrame;


    private StatusBarController statusBar; // 1/1/2016 drafted
    private TextPanelController textPanel;  // 1/5/2016 drafted

    // TODO: 12/31/2015 add att edit
    private TablePanelController tablePanel; // 1/8/2016 drafted

    // TODO: 2016-01-09 18:09:29EST all of actions and menuitems, including contextmenus
    private MaeControllerI menu;

    // TODO: 2016-01-09 18:10:19EST add linkCreation popup view
    private DialogController dialogs;

    // TODO: 1/4/2016 create etag, create ltag, add argument, ...
//    private LinkCreationController linkCreator;

    // some booleans for user preference

    // booleans for user preferences
    private boolean normalModeOnCreation = true; // on by default
    private String mFilenameSuffix = ""; // for file operation

    // database connectors
    private List<MaeDriverI> drivers;
    private MaeDriverI currentDriver;

    private ColorHandler textHighlighColors;
    private ColorHandler documentTabColors;

    public MaeMainController() {

        drivers = new ArrayList<>();

        //used to keep track of what color goes with what tag
        // keep track of which tag type is highlighted in text pane

        mode = MODE_NORMAL;
        documentTabColors = new ColorHandler(6); // by default, 6 colors allowed to distinguish documents

        // these components are not attached to mainFrame, but will be called when necessary
        menu = new MenuController(this);
        textPanel = new TextPanelController(this);
        tablePanel = new TablePanelController(this);
        statusBar = new StatusBarController(this);
        dialogs = new DialogController(this);

//        linkCreator = new LinkCreationController(this);
    }

    public void addDocument(File annotationFile) {
        if (getDriver().isTaskLoaded()) {
            try {
                if (getDriver().isAnnotationLoaded()) {
                    // TODO: 1/3/2016 fix here for multi file annotation in the future
                    // TODO: 1/3/2016 maybe we need a method driver.resetAnnotation() to purge out all annotations(tags and atts), but keep task structure
                    String taskFileName = getDriver().getTaskFileName();
                    File taskFile = new File(taskFileName);
                    destroyCurrentDriver();
                    newTask(MaeStrings.ANN_DB_FILE, taskFile);
                    getDriver().readTask(taskFile);
                }
                getDriver().readAnnotation(annotationFile);
            } catch (Exception e) {
                showError(e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        MaeMainController main = new MaeMainController();
        JFrame mainFrame = main.initUI();
        main.setWindowFrame(mainFrame);
        mainFrame.pack();
        mainFrame.setVisible(true);
    }

    private void setWindowFrame(JFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    private JFrame initUI() {
        return new MaeMainView(menu.getView(), textPanel.getView(), statusBar.getView(), tablePanel.getView());
    }

    public MaeControllerI getMenu() {
        return null;
    }

    public DialogController getDialogs() {
        return dialogs;
    }

    public boolean showWarning(String message) {
        boolean response = getDialogs().showWarning(message) == JOptionPane.OK_OPTION;
        logger.warn(message + ": " + response);
        return response;

    }

    public boolean showUnsavedChangeWarning() {
        String warning = "Warning! You will lose all your unsaved changes. \n Are you sure to continue?";
        return showWarning(warning);

    }

    public boolean showBatchDeletionWarning() {
        String warning = ("Deleting extent tag(s) will also delete \n" +
                "any links that use these extents.  Would you like to continue?");
        return showWarning(warning);
    }

    public void showError(String message) {
        getDialogs().showError(message);
        logger.error(message);
    }

    public boolean isTaskLoaded() {
        return !drivers.isEmpty() && currentDriver.isTaskLoaded();
    }

    public Set<TagType> getActiveLinkTags() {
        return getTablePanel().getActiveLinkTags();
    }

    public Set<TagType> getActiveExtentTags() {
        return getTablePanel().getActiveExtentTags();
    }

    public boolean isAnnotationOn() {
        return isTaskLoaded() && getDriver().isAnnotationLoaded();
    }

    public boolean isAnnotationChanged() {
        return getDriver().isAnnotationChanged();
    }

    public void annotationIsChanged() {
        getDriver().setAnnotationChanged(true);
    }

    public boolean normalModeOnCreation() {
        return normalModeOnCreation;
    }

    public void setnormalModeOnCreation(boolean b) {
        this.normalModeOnCreation = b;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public JFrame getMainWindow() {
        return mainFrame;
    }

    public String getFilenameSuffix() {
        return mFilenameSuffix;
    }

    public void setFilenameSuffix(String mFilenameSuffix) {
        this.mFilenameSuffix = mFilenameSuffix;
    }

//    public LinkCreationController getLinkPopupFrame() {
//        return null;
//    }

//    public void setLinkPopupFrame(LinkCreationController linkCreator) {
//        this.linkCreator = linkCreator;
//    }

    public TablePanelController getTablePanel() {
        return tablePanel;
    }

    public TextPanelController getTextPanel() {
        return textPanel;
    }

    public StatusBarController getStatusBar() {
        return statusBar;
    }

    public MaeDriverI getDriver() {
        return currentDriver;
    }

    public MaeDriverI getDriverAt(int i) {
        // TODO: 1/4/2016 finish this for multi file support
        return drivers.get(i);
    }

    public void destroyDriverAt(int i) {
        // TODO: 1/4/2016 finish this for multi file support
        try {
            getDriverAt(i).destroy();
            drivers.remove(i);
//            getTextPanel().closeDocument(i);
        } catch (MaeDBException e) {
            showError(e.getMessage());
        }
    }

    public void destroyCurrentDriver() {
        // TODO: 1/4/2016 finish this for multi file support
//        destroyDriverAt(getTextPanel().selectedTab());
        currentDriver = null;
    }

    public void sendNotification(String message) {
        getStatusBar().setText(message);
        logger.info(message);
    }

    public void sendTemporaryNotification(String message, long periodMillisecond) {
        sendNotification(message);
        getStatusBar().delayedReset(periodMillisecond);
    }

    public void sendWaitMessage() {
        getStatusBar().setText(MaeStrings.WAIT_MESSAGE);
    }


    /**
     * Adds an extent tag to the database, one item per character location Note
     * that, id DB, all tag is associated with the location of all characters in the
     * span , meaning 3 letter word ends up with 3 items in the DB mod by krim: to
     * use list(spans), not 2 integers(start/end)
     *
     * @param elemName the type of tag being added
     * @param newId    the ID of the tag being added
     */
    public void addExtTagToDb(String elemName, String newId) {
        // TODO: 12/31/2015 write this (makeTagListener)

    }

    /**
     * Adds a link tag to the database. Unlike an extent tag, a link tag only
     * occupies one item in the DB
     */
    public void addLinkTagToDb(String elemName, String newId,
                               List<String> argIds, List<String> argTypes) {
        // TODO: 12/31/2015 write this
    }

    /**
     * Returns the text associated with an id.  Checks the table so that if there is
     * a note entered for a non-consuming tag, that information will be there
     *
     * @param elem the type of tag of the text being looked for
     * @param id   The ID of the tag associated with the text being looked for
     * @return the text being searched for
     */
    public String getTextByID(String elem, String id, boolean fullText) {
        // TODO: 12/31/2015 write this
        String text = "";
        return text;
    }

    /**
     * Sets MAE mode to Normal
     */
    public void returnToNormalMode(boolean statusBarAlert) {

        if (mode != MODE_NORMAL && statusBarAlert) {
            sendTemporaryNotification(MaeStrings.SB_NORM_MODE_NOTI, 3000);
        }
        mode = MODE_NORMAL;
    }

    public void newTask(String dbFile, File taskFile) {
        // this always wipes out on-going annotation works,
        // even with multi-file support, an instance of MAE requires all works share the same DB schema
        try {
            drivers.clear();
            drivers.add(new LocalSqliteDriverImpl(dbFile));
            currentDriver = drivers.get(0);
            getDriver().readTask(taskFile);
            resetTextPanel();
            // TODO: 1/4/2016  at this point, driver should know everything about DB schema, make use of driver inside the table panel
            resetTablePanel();
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    public void newAnnotation(File annotationFile) {
        try {
            // TODO: 1/4/2016 resetting is limitting multi file support, to support multi files, need a driver init here
            resetTextPanel();
            getDriver().readAnnotation(annotationFile);
            getTextPanel().addDocument(getDriver().getAnnotationFileName(), getDriver().getPrimaryText());
            // TODO: 1/4/2016 update tables as per to newly stored annotations from readAnno()
            resetTablePanel();
        } catch (Exception e) {
            showError(e.getMessage());
        }

    }

    public void switchAnnotationTab(int tabId) {
        // TODO: 12/31/2015 this is for multi file support
//        textPanel.selectTab(tabId);
        currentDriver = drivers.get(tabId);
    }

    public void resetMenuBar() {
//        try {
//            ((MenuBarController) getMenu()).reset();
//    } catch (Exception e) {
//        showError(e.getMessage());
//        }
    }

    public void resetTextPanel() {
        try {
            getTextPanel().reset();
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    public void resetTablePanel() {
        try {
            ((TablePanelController) getTablePanel()).reset();
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    public void resetStatusBar() {
        getStatusBar().reset();
    }

    public int[] getSelectedTextSpans() {
        return getTextPanel().getSelected();
    }

    public File selectSingleFile() {
        return getDialogs().showFileChooseDialogAndSelect();
        // TODO: 1/1/2016 implement multiple files
    }

    public void assignColors() {
        try {
            getTextPanel().assignColorsAllActiveTags();
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    public void updateTitle() {
    }

    public void resetAll() {

        resetDrivers();
        resetMenuBar();
        resetTextPanel();
        resetTablePanel();
        resetStatusBar();
        returnToNormalMode(false);
    }

    private void resetDrivers() {
        for (MaeDriverI driver : drivers) {
            try {
                driver.destroy();
            } catch (Exception e) {
                showError(e.getMessage());
            }
        }
    }

    public List<ExtentTag> getExtentTagsInSelectedSpans() {
        try {
            return getDriver().getTagsIn(getTextPanel().getSelected());
        } catch (Exception e) {
            showError(e.getMessage());
            return new ArrayList<>();
        }
    }

    public JFileChooser getFileChooser() {
        return getDialogs().getFileChooser();
    }

    public Color getHighlightColor(TagType type) {
        return getTextHighlighColors().getColor(getTablePanel().getTabIndexOfTagType(type));
    }

    public ColorHandler getTextHighlighColors() {
        return textHighlighColors;
    }

    public void setDriver(int tabId) {
        // TODO: 2016-01-08 23:51:59EST finish for multi file support
        currentDriver = drivers.get(tabId);
    }

    public boolean isTextSelected() {
        return getTextPanel().isTextSelected();
    }

    public void removeAllHighlights() {
        getTextPanel().removeAllHighlights();
    }

    public void highlightTagSpans(ExtentTag eTag, Highlighter.HighlightPainter painter) {
        try {
            getTextPanel().highlightTextSpans(eTag.getSpansAsArray(), painter);
        } catch (Exception e) {
            showError(e.getMessage());
        }

    }

    public void propagateSelectionFromTextPanel() {
        try {
            List<ExtentTag> releventTags = getDriver().getTagsIn(getSelectedTextSpans());
            for (ExtentTag tag : releventTags) {
                getTablePanel().selectTableRows(tag);
            }
            getStatusBar().reset();
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }
    public void propagateSelectionFromTablePanel(String tid) {
        removeAllHighlights();
        try {
            Tag tag = getDriver().getTagByTid(tid);
            if (tag.getTagtype().isExtent()) {
                highlightTagSpans((ExtentTag) tag, ColorHandler.getVividHighliter());
            } else {
                for (ExtentTag argTag : ((LinkTag) tag).getArgumentTags()) {
                    highlightTagSpans(argTag, ColorHandler.getVividHighliter());
                }
            }

        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    public JPopupMenu createTableContextMenu() {
        // TODO: 1/4/2016 write this
        return null;
    }

    public JPopupMenu createTextContextMenu() {
        // TODO: 1/4/2016 write this
        return null;
    }

}

