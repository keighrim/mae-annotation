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
import edu.brandeis.cs.nlp.mae.database.LocalSqliteDriverImpl;
import edu.brandeis.cs.nlp.mae.database.MaeDBException;
import edu.brandeis.cs.nlp.mae.database.MaeDriverI;
import edu.brandeis.cs.nlp.mae.model.*;
import edu.brandeis.cs.nlp.mae.util.ColorHandler;
import edu.brandeis.cs.nlp.mae.util.SpanHandler;
import edu.brandeis.cs.nlp.mae.view.MaeMainView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by krim on 12/30/2015.
 */
public class MaeMainController extends JPanel {

    public static final int MODE_NORMAL = 0;
    public static final int MODE_MULTI_SPAN = 1;
    public static final int MODE_ARG_SEL = 2;
    public static final int MODE_ADJUD = 9;
    private static final Logger logger = LoggerFactory.getLogger(MaeMainController.class.getName());
    private int mode;

    private JFrame mainFrame;


    private StatusBarController statusBar; // 1/1/2016 drafted
    private TextPanelController textPanel;  // 1/5/2016 drafted
    private TablePanelController tablePanel; // 1/8/2016 drafted

    // TODO: 2016-01-09 18:09:29EST all of actions and menuitems, including contextmenus
    private MenuController menu;

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
    private List<TagType> tagsForColor;
    private ColorHandler documentTabColors;

    public MaeMainController() {

        drivers = new ArrayList<>();

        //used to keep track of what color goes with what tag
        // keep track of which tag type is highlighted in text pane

        mode = MODE_NORMAL;
        tagsForColor = new ArrayList<>();
        documentTabColors = new ColorHandler(6); // by default, 6 colors allowed to distinguish documents

        // these components are not attached to mainFrame, but will be called when necessary
        try {
            menu = new MenuController(this);
            textPanel = new TextPanelController(this);
            tablePanel = new TablePanelController(this);
            statusBar = new StatusBarController(this);
            dialogs = new DialogController(this);
        } catch (MaeException e) {
            showError(e);
        }

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
                    setupScheme(MaeStrings.ANN_DB_FILE, taskFile, false);
                    getDriver().readTask(taskFile);
                }
                getDriver().readAnnotation(annotationFile);
            } catch (Exception e) {
                showError(e);
            }
        }
    }

    public static void main(String[] args) {
        MaeMainController main = new MaeMainController();
        JFrame mainFrame = main.initUI();
        main.setWindowFrame(mainFrame);
        mainFrame.pack();
        mainFrame.setSize(900, 700);
        mainFrame.setVisible(true);

        if (args.length > 0) {
            boolean argCmd = false;
            List<String> argsList = new ArrayList<>();
            String taskFilename = null;
            String docFilename = null;
            for (String arg : args) {
                argsList.add(arg);
            }
            if (argsList.contains("--task")) {
                taskFilename = argsList.get(argsList.indexOf("--task") + 1);
                argCmd = true;
                if (argsList.contains("--doc")) {
                    docFilename = argsList.get(argsList.indexOf("--doc") + 1);

                }
            }
            if (!argCmd) {
                System.out.println("TODO: show some help text");
            }

            if (taskFilename != null) {
                main.setupScheme(MaeStrings.ANN_DB_FILE, new File(taskFilename), true);
                if (docFilename != null) {
                    main.newAnnotation(new File(docFilename));
                }
            }
        }
    }

    private void setWindowFrame(JFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    private JFrame initUI() {
        logger.debug("initiating UI components.");
        return new MaeMainView(menu.getView(), textPanel.getView(), statusBar.getView(), tablePanel.getView());
    }

    public MenuController getMenu() {
        return menu;
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
        if (isDocumentOpen() && isAnnotationChanged()) {
            String warning = "Warning! You will lose all your unsaved changes. \n Are you sure to continue?";
            return showWarning(warning);
        } else {
            return true;
        }

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

    public void showError(Exception e) {
        getDialogs().showError(e);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        logger.error(sw.toString());
    }

    public boolean isTaskLoaded() {
        return !drivers.isEmpty() && getDriver().isTaskLoaded();
    }

    public Set<TagType> getActiveLinkTags() {
        return getTablePanel().getActiveLinkTags();
    }

    public Set<TagType> getActiveExtentTags() {
        return getTablePanel().getActiveExtentTags();
    }

    public boolean isDocumentOpen() {
        return isTaskLoaded() && getDriver().isAnnotationLoaded();
    }

    public boolean isAnnotationChanged() {
        return getDriver().isAnnotationChanged();
    }

    public void updateSavedStatusInTextPanel() {
        try {
            getTextPanel().updateTabTitles();
        } catch (MaeDBException e) {
            showError(e);
        }
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

    public int getDrivers() {
        return drivers.size();
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
            showError(e);
        }
    }

    public void destroyCurrentDriver() {
        // TODO: 1/4/2016 finish this for multi file support
//        destroyDriverAt(getTextPanel().getSelectedTabIndex());
        currentDriver = null;
    }

    public void sendNotification(String message) {
        getStatusBar().setText(message);
        logger.info(message);
    }

    public void resetNotificationMessageIn(long millisecond) {
        getStatusBar().delayedReset(millisecond);
    }

    public void sendTemporaryNotification(String message, long periodMillisecond) {
        sendNotification(message);
        resetNotificationMessageIn(periodMillisecond);
    }

    public void sendWaitMessage() {
        getStatusBar().setText(MaeStrings.WAIT_MESSAGE);
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

    public void setupScheme(String dbFile, File taskFile, boolean fromNewTask) {
        // this always wipes out on-going annotation works,
        // even with multi-file support, an instance of MAE requires all works share the same DB schema
        try {
            // TODO: 2016-01-17 12:39:34EST 4MF, resetting should not be done
//            if (fromNewTask) {
            resetDrivers(); // one driver for one annotation instance, setting up a new task will wipe out all ongoing instances
//            }
            currentDriver = new LocalSqliteDriverImpl(dbFile);
            drivers.add(currentDriver);
            getDriver().readTask(taskFile);
            logger.info(String.format("task \"%s\" is loaded, has %d extent tags and %d link tags", getDriver().getTaskName(), getDriver().getExtentTagTypes().size(), getDriver().getLinkTagTypes().size()));
            resetColors();
            if (fromNewTask) {
                getMenu().reset();
                getStatusBar().reset();
                getTextPanel().reset();
                getMainWindow().setTitle(String.format("%s :: %s", MaeStrings.TITLE_PREFIX, taskFile));
                sendTemporaryNotification(MaeStrings.SB_NEWTASK, 3000);

            }
            getTablePanel().reset();
            getTablePanel().makeAllTables();
        } catch (Exception e) {
            showError(e);
        }
    }

    public void newAnnotation(File annotationFile) {
        try {
            setupScheme(MaeStrings.ANN_DB_FILE, new File(getDriver().getTaskFileName()), false);
            getDriver().readAnnotation(annotationFile);
            getTextPanel().addDocument(getDriver().getAnnotationFileBaseName(), getDriver().getPrimaryText());

            getTablePanel().insertAllTags();
            getMenu().reset();
            getTextPanel().reset();
            getStatusBar().reset();
        } catch (Exception e) {
            showError(e);
        }

    }

    public void switchAnnotationTab(int tabId) {
        // TODO: 12/31/2015 4MF this is for multi file support
//        textPanel.selectTab(tabId);
        currentDriver = drivers.get(tabId);
    }

    public String getTextIn(int...locations) {
        try {
            return getTextPanel().getTextIn(locations, false);
        } catch (MaeControlException e) {
            showError(e);
        }
        return null;
    }

    public String getSelectedText() {
        try {
            return getTextPanel().getSelectedText();
        } catch (MaeControlException e) {
            showError(e);
        }
        return null;
    }

    public int[] getSelectedTextSpans() {
        return getTextPanel().getSelected();
    }

    public File selectSingleFile(String defautName, boolean saveFile) {
        return getDialogs().showFileChooseDialogAndSelect(defautName, saveFile);
        // TODO: 1/1/2016 4MF implement multiple files
    }

    public void assignTextColorsOver(List<Integer> anchors) {
        try {
            getTextPanel().assignFGColorOver(anchors);
        } catch (Exception e) {
            showError(e);
        }
    }

    public void assignAllTextColors() {
        try {
            getTextPanel().assignAllFGColors();
        } catch (MaeDBException e) {
            showError(e);
        }
    }

    public void unassignAllTextColors() {
        try {
            getTextPanel().unassignAllFGColors();
        } catch (MaeDBException e) {
            showError(e);
        }
    }

    public void updateTitle() {
    }

    void resetColors() throws MaeDBException {
        textHighlighColors = new ColorHandler(getDriver().getExtentTagTypes().size());
        tagsForColor.clear();
    }

    private void resetControllers() {
        try {
            getMenu().reset();
            getTextPanel().reset();
            getTablePanel().reset();
            getStatusBar().reset();
            returnToNormalMode(false);
        } catch (MaeException e) {
            showError(e);
        }


    }

    private void resetAll() {

        resetDrivers();
        resetControllers();
    }

    private void resetDrivers() {
        for (MaeDriverI driver : drivers) {
            try {
                driver.destroy();
            } catch (Exception e) {
                showError(e);
            }
        }
        drivers.clear();
    }

    public List<ExtentTag> getExtentTagsInSelectedSpans() {
        try {
            return getDriver().getTagsIn(getSelectedTextSpans());
        } catch (Exception e) {
            showError(e);
            return new ArrayList<>();
        }
    }

    public JFileChooser getFileChooser() {
        return getDialogs().getFileChooser();
    }

    public Color getFGColor(TagType type) {
        if (!tagsForColor.contains(type)) {
            tagsForColor.add(type);
        }
        return getTextHighlighColors().getColor(tagsForColor.indexOf(type));

    }

    public int colorableTagTypes() {
        return tagsForColor.size();
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
        getTextPanel().removeAllBGColors();
    }

    public void propagateSelectionFromTextPanel() {
        getTablePanel().clearTableSelections();
        try {
            List<ExtentTag> releventTags = getDriver().getTagsIn(getSelectedTextSpans());
            for (ExtentTag tag : releventTags) {
                getTablePanel().selectTagFromTable(tag);
            }
            getStatusBar().update();
        } catch (Exception e) {
            showError(e);
        }
    }
    public void propagateSelectionFromTablePanel(String tid) {
        removeAllHighlights();
        try {
            int[] spans = SpanHandler.convertIntegerlistToIntegerarray(getDriver().getAnchorsByTid(tid));
            getTextPanel().addBGColorOver(spans, ColorHandler.getVividHighliter());
        } catch (Exception e) {
            showError(e);
        }
    }

    public JPopupMenu createTableContextMenu(JTable table) {
        try {
            logger.info("creating context menu from table panel");
            return getMenu().createTableContextMenu(table);
        } catch (MaeDBException e) {
            showError(e);
        }
        return null;
    }

    public JPopupMenu createTextContextMenu() {
        try {
            logger.info("creating context menu from text panel");
            return getMenu().createTextContextMenu();
        } catch (MaeDBException e) {
            showError(e);
        }
        return null;
    }

    public void removeTag(Tag tag) {
        try {
            getTablePanel().removeTagFromTable(tag);
        } catch (MaeDBException e) {
            showError(e);
        }
    }

    public void createNCTag(TagType tagType) {
        logger.info(String.format("creating DB row for a NC extent tag: (%s)", tagType.getName()));
        if (tagType.isLink()) {
            showError("Link tag cannot be non-consuming!");
        }
        try {
            String tid = getDriver().getNextId(tagType);
            ExtentTag tag = getDriver().createExtentTag(tid, tagType, null, null);
            populateDefaultAttributes(tag);
            getTablePanel().insertTagIntoTable(tag);
            assignTextColorsOver(tag.getSpansAsList());
        } catch (MaeException e) {
            showError(e);
        }

    }

    public void createTagFromTextSelection(TagType tagType) {
        logger.info(String.format("creating DB row from text selection: (%s) \"%s\"", tagType.getName(), getSelectedText()));
        try {
            Tag tag;
            String tid = getDriver().getNextId(tagType);
            if (tagType.isExtent()) {
                tag = getDriver().createExtentTag(tid, tagType, getSelectedText(), getSelectedTextSpans());
            } else {
                tag = getDriver().createLinkTag(tid, tagType);
                // TODO: 2016-01-20 16:55:31EST  do we need this? creating a link from text will always end up in an empty link
//                for (ExtentTag arg : tag.getArgumentTags()) {
//                    assignTextColorsOver(arg.getSpansAsList());
//                }
            }
            populateDefaultAttributes(tag);
            getTablePanel().insertTagIntoTable(tag);
            if (tagType.isExtent()) {
                assignTextColorsOver(((ExtentTag) tag).getSpansAsList());
            }
        } catch (MaeException e) {
            showError(e);
        }

    }

    void populateDefaultAttributes(Tag tag) throws MaeDBException {
        for (AttributeType attType : tag.getTagtype().getAttributeTypes()) {
            String defaultValue = attType.getDefaultValue();
            if (defaultValue != null && defaultValue.length() > 0) {
                getDriver().addOrUpdateAttribute(tag, attType, defaultValue);
            }
        }
    }

    public void createTagFromTableInsertion(TagType tagType, Map<String, String> insertedRow) {
        logger.info(String.format("inserting DB row based on table insertion: (%s) %s", tagType.getName(), insertedRow.toString()));
        try {
            Tag newTag;
            if (tagType.isExtent()) {
                newTag = getDriver().createExtentTag(
                        insertedRow.remove(MaeStrings.ID_COL_NAME),
                        tagType,
                        insertedRow.remove(MaeStrings.SPANS_COL_NAME),
                        SpanHandler.convertStringToArray(insertedRow.remove(MaeStrings.TEXT_COL_NAME)));
            } else {
                newTag = getDriver().createLinkTag(insertedRow.remove(MaeStrings.ID_COL_NAME), tagType);
                for (ArgumentType argType : tagType.getArgumentTypes()) {
                    String argIdColName = argType.getName() + MaeStrings.ARG_IDCOL_SUF;
                    String argTextColName = argType.getName() + MaeStrings.ARG_TEXTCOL_SUF;
                    String argumentTid = insertedRow.remove(argIdColName);
                    if (argumentTid.length() > 0 && insertedRow.remove(argTextColName).length() > 0) {
                        getDriver().addOrUpdateArgument((LinkTag) newTag, argType, (ExtentTag) getDriver().getTagByTid(argumentTid));
                    }
                }
            }
            for (String attName : insertedRow.keySet()) {
                String attValue = insertedRow.get(attName);
                if (attValue != null && attValue.length() > 0) {
                    AttributeType attType = getDriver().getAttributeTypeOfTagTypeByName(tagType, attName);
                    getDriver().addOrUpdateAttribute(newTag, attType, attValue);
                }
            }
            updateSavedStatusInTextPanel();
        } catch (MaeException e) {
            showError(e);
        }

    }

    public void deleteTagFromTableDeletion(Tag tag) {
        logger.info(String.format("removing DB row based on table deletion: \"%s\"", tag.getId()));
        try {
            getDriver().deleteTag(tag);
            if (tag.getTagtype().isExtent()) {
                assignTextColorsOver(((ExtentTag) tag).getSpansAsList());
            } else {
                for (ExtentTag arg : ((LinkTag) tag).getArgumentTags()) {
                    assignTextColorsOver(arg.getSpansAsList());
                }
            }
            updateSavedStatusInTextPanel();
        } catch (MaeDBException e) {
            showError(e);
        }
    }

    public boolean updateDBFromTableUpdate(String tid, String colName, String value) {
        logger.info(String.format("modifying DB based on table update: setting \"%s\" of %s to \"%s\"", colName, tid, value));
        boolean succeed = false;
        try {
            Tag tag = getDriver().getTagByTid(tid);
            if (tag.getTagtype().isExtent() && colName.equals(MaeStrings.SPANS_COL_NAME)) {
                succeed = getDriver().updateTagSpans((ExtentTag) tag, SpanHandler.convertStringToArray(value));
            } else if (tag.getTagtype().isExtent() && colName.equals(MaeStrings.TEXT_COL_NAME)) {
                succeed = getDriver().updateTagText((ExtentTag) tag, value);
            } else if (tag.getTagtype().isLink() && colName.endsWith(MaeStrings.ARG_IDCOL_SUF)) {
                String argTypeName = colName.substring(0, colName.length() - MaeStrings.ARG_IDCOL_SUF.length());
                ArgumentType argType = getDriver().getArgumentTypeOfTagTypeByName(tag.getTagtype(), argTypeName);
                LinkTag linker = (LinkTag) getDriver().getTagByTid(tid);
                ExtentTag arg = (ExtentTag) getDriver().getTagByTid(value);
                if (arg == null) {
                    showError("No such a tag stored in DB: " + value);
                    return false;
                }
                succeed = (getDriver().addOrUpdateArgument(linker, argType, arg) != null);
            } else if (tag.getTagtype().isLink() && colName.endsWith(MaeStrings.ARG_TEXTCOL_SUF)) {
                // do nothing, will be automatically updated when argId is updated
                return true;
            } else {
                AttributeType attType = getDriver().getAttributeTypeOfTagTypeByName(tag.getTagtype(), colName);
                succeed = (getDriver().addOrUpdateAttribute(tag, attType, value) != null);
            }
        } catch (MaeException e) {
            showError(e);
        }
        if (succeed) {
            updateSavedStatusInTextPanel();
        }
        return succeed;
    }

    public void updateStatusBar() {
        getStatusBar().update();
    }
}

