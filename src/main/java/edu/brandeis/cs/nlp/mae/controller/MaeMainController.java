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
import edu.brandeis.cs.nlp.mae.controller.tablepanel.HighlightToggleListener;
import edu.brandeis.cs.nlp.mae.controller.tablepanel.TablePanelController;
import edu.brandeis.cs.nlp.mae.controller.textpanel.TextPanelController;
import edu.brandeis.cs.nlp.mae.database.LocalSqliteDriverImpl;
import edu.brandeis.cs.nlp.mae.database.MaeDBException;
import edu.brandeis.cs.nlp.mae.database.MaeDriverI;
import edu.brandeis.cs.nlp.mae.io.MaeIOException;
import edu.brandeis.cs.nlp.mae.model.*;
import edu.brandeis.cs.nlp.mae.util.ColorHandler;
import edu.brandeis.cs.nlp.mae.util.MappedSet;
import edu.brandeis.cs.nlp.mae.util.SpanHandler;
import edu.brandeis.cs.nlp.mae.view.MaeMainView;
import edu.brandeis.cs.nlp.mae.view.TablePanelView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * MainController handles user interactions by coordinates all GUI controllers.
 * GUI is mainly made of two parts: <b>TextPanel</b> (at the top half), <b>TablePanel</b> (at the bottom half).
 * As well as miscellaneous componenets such as <b>Menu bar</b>, <b>Status bar</b> and <b>Dialogs/Popups</b>.
 * All GUI controllers subclasse from MaeControllerI which has their own "view" associated.
 * However, MainController directly subclasses JPanel to be initiated and embedded into JFrame when the program starts.
 * Also note that DialogController does not subclass MaeControllerI, since it doesn't need a "view".
 */
public class MaeMainController extends JPanel {

    public static final int MODE_NORMAL = 0;
    public static final int MODE_MULTI_SPAN = 1;
    public static final int MODE_ARG_SEL = 2;
    public static final int MODE_ADJUD = 9;
    private static final Logger logger = LoggerFactory.getLogger(MaeMainController.class.getName());
    private int mode;

    private JFrame mainFrame;
    private MaeMainView view;
    private Timer temporaryNotificationTimer;

    private StatusBarController statusBar;
    private TextPanelController textPanel;
    private TablePanelController tablePanel;
    private MenuController menu;
    private DialogController dialogs;

    // booleans for user preferences
    private boolean normalModeOnCreation = true; // on by default
    private String mFilenameSuffix = ""; // for file operation

    // database connectors
    private List<MaeDriverI> drivers;
    private MaeDriverI currentDriver;
    private final int adjudDriverIndex = 0;

    private ColorHandler textHighlighColors;
    private List<TagType> tagsForColor;
    private Map<TagType, Boolean> coloredTagsInLastDocument;
    private ColorHandler documentTabColors;
    private Set<Tag> adjudicatingTags;
    private boolean isAdjudicating;

    public MaeMainController() {

        drivers = new ArrayList<>();

        mode = MODE_NORMAL;
        tagsForColor = new ArrayList<>();
        adjudicatingTags = new HashSet<>();
        isAdjudicating = false;

        // documentTabColors are used when adjudicating
        // by default, 6 colors allowed to distinguish documents
        // also, handler starts with black color, which will be used for GS file tab
        documentTabColors = new ColorHandler(6, true);

        try {
            menu = new MenuController(this);
            textPanel = new TextPanelController(this);
            tablePanel = new TablePanelController(this);
            statusBar = new StatusBarController(this);
            dialogs = new DialogController(this);
            coloredTagsInLastDocument = new HashMap<>();
        } catch (MaeException e) {
            showError(e);
        }
    }

    public void setWindowFrame(JFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.mainFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.mainFrame.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent winEvt) {
                boolean allChecked = true;
                if (isDocumentOpen()) {
                    for (int i = 0; i < getDrivers().size(); i++) {
                        if (!showUnsavedChangeWarningAt(i)) {
                            allChecked = false;
                            break;
                        }
                    }
                }
                if (allChecked) {
                    sendWaitMessage();
                    SwingWorker<Boolean, String> wiper = new WipeDriversWorker(drivers);
                    wiper.execute();
                    while (!wiper.isDone()) {
                        // wait until wiper finishes its job
                    }
                    System.exit(0);
                }
            }
        });

    }

    public JFrame initUI() {
        logger.debug("initiating UI components.");

        view = new MaeMainView(menu.getView(), textPanel.getView(), statusBar.getView(), tablePanel.getView());
        return view;
    }

    private MenuController getMenu() {
        return menu;
    }

    private DialogController getDialogs() {
        return dialogs;
    }

    public boolean showWarning(String message) {
        boolean response = getDialogs().showWarning(message) == JOptionPane.OK_OPTION;
        logger.warn(message + ": " + response);
        return response;

    }

    public void popupMessage(String message) {
        getDialogs().popupMessage(message);
        logger.warn(message);
    }

    public boolean showUnsavedChangeWarningAt(int tabIdx) {
        if (getDriverAt(tabIdx).isAnnotationChanged()) {
            String warning = null;
            try {
                warning = String.format(
                        "Warning! You have unsaved changes. \n%s\n Are you sure to continue?"
                        , getDriverAt(tabIdx).getAnnotationFileBaseName());
            } catch (MaeDBException ignored) {
            }
            return showWarning(warning);
        }
        return true;
    }

    public boolean showCurrentUnsavedChangeWarning() {
        if (getDriver().isAnnotationChanged()) {
            String warning = null;
            try {
                warning = String.format(
                        "Warning! You have unsaved changes. \n%s\n Are you sure to continue?"
                        , getDriver().getAnnotationFileBaseName());
            } catch (MaeDBException ignored) {
            }
            return showWarning(warning);
        }
        return true;
    }

    public boolean showAllUnsavedChangeWarning() {
        List<String> unsavedFiles = new LinkedList<>();
        for (MaeDriverI driver : getDrivers()) {
            if (driver.isAnnotationChanged()) {
                try {
                    unsavedFiles.add(driver.getAnnotationFileBaseName());
                } catch (MaeDBException ignored) { // this won't happen
                }
            }
        }
        if (unsavedFiles.size() > 0) {
            String warning = String.format("Warning! You have unsaved changes. \n%s\n Are you sure to continue?"
                    , unsavedFiles.toString());
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

    public void showError(Exception e) {
        logException(e);
        SwingUtilities.invokeLater(() -> getDialogs().showError(e));
    }

    public void showError(String message, Exception e) {
        logException(e);
        logger.error(message);
        SwingUtilities.invokeLater(() -> getDialogs().showError(message, e));
    }

    void logException(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        logger.error(sw.toString());
    }

    public void showError(String message) {
        logger.error(message);
        SwingUtilities.invokeLater(() -> getDialogs().showError(message));
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

    public boolean isAdjudicating() {
        return isAdjudicating;
    }

    private void setAdjudicating(boolean b) {
        this.isAdjudicating = b;
    }

    public void updateSavedStatusInTextPanel() {
        try {
            getTextPanel().updateTabTitles(isAdjudicating());
        } catch (MaeDBException e) {
            showError(e);
        }
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

    public JFrame getMainWindow() {
        return mainFrame;
    }

    public String getFilenameSuffix() {
        return mFilenameSuffix;
    }

    public void setFilenameSuffix(String mFilenameSuffix) {
        this.mFilenameSuffix = mFilenameSuffix;
    }

    private TablePanelController getTablePanel() {
        return tablePanel;
    }

    private TextPanelController getTextPanel() {
        return textPanel;
    }

    private StatusBarController getStatusBar() {
        return statusBar;
    }

    public MaeDriverI getDriver() {
        return currentDriver;
    }

    public MaeDriverI getDriverOf(String srcName) {
        for (MaeDriverI driver : getDrivers()) {
            try {
                if (driver.getAnnotationFileName().endsWith(srcName)) {
                    return driver;
                }
            } catch (MaeDBException e) {
                showError(e);
            }
        }
        return null;
    }

    public List<MaeDriverI> getDrivers() {
        return drivers;
    }

    public MaeDriverI getDriverAt(int i) {
        return drivers.get(i);
    }

    public int getCurrentDocumentTabIndex() {
        return getTextPanel().getCurrentTab();
    }

    public void closeCurrentDocument() {
        if (isAdjudicating()) {
            switchToAnnotationMode();
        } else {
            closeDocumentAt(getCurrentDocumentTabIndex());
        }
    }

    public void closeDocumentAt(int i) {
        int openDrivers = getDrivers().size();
        if (i > openDrivers || openDrivers != getTextPanel().getOpenTabCount()) {
            showError("#drivers and #documents do not match!");
        }
        try {
            if (getDrivers().size() > 1) {
                getDriverAt(i).destroy();
                drivers.remove(i);
                getTextPanel().closeDocumentTab(i);
            } else {
                File taskFile = new File(getDriver().getTaskFileName());
                setUpTask(taskFile);
            }
        } catch (MaeException e) {
            showError(e);
        }
    }

    public void sendNotification(String message) {
        getStatusBar().setText(message);
        unblockGUI();
        logger.debug(message);
    }

    public void updateNotificationAreaIn(int millisecond) {
        this.temporaryNotificationTimer = new Timer(0, e -> updateNotificationArea());
        temporaryNotificationTimer.setInitialDelay(millisecond);
        temporaryNotificationTimer.start();

    }

    public synchronized void updateNotificationArea() {
        if (temporaryNotificationTimer != null && temporaryNotificationTimer.isRunning()) {
            temporaryNotificationTimer.stop();
        }
        getStatusBar().refresh();
        unblockGUI();

    }

    public void sendTemporaryNotification(String message, int periodMillisecond) {
        updateNotificationArea();
        sendNotification(message);
        updateNotificationAreaIn(periodMillisecond);
    }

    public void sendWaitMessage() {
        if (temporaryNotificationTimer != null && temporaryNotificationTimer.isRunning()) {
            temporaryNotificationTimer.stop();
        }
        getStatusBar().setText(MaeStrings.WAIT_MESSAGE);
        blockGUI();
    }

    void unblockGUI() {
        getMainWindow().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        view.hideWait();
    }

    private void blockGUI() {
        getMainWindow().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        view.showWait();
    }

    public void switchToArgSelMode() {

        if (mode != MODE_ARG_SEL) {
            clearTextSelection();
            mode = MODE_ARG_SEL;
            sendNotification(MaeStrings.SB_ARGSEL_MODE_NOTI);
            getMenu().resetMenus(MaeStrings.MENU_MODE);
        }
    }

    public void switchToMSpanMode() {

        if (mode != MODE_MULTI_SPAN) {
            clearTextSelection();
            mode = MODE_MULTI_SPAN;
            sendNotification(MaeStrings.SB_MSPAN_MODE_NOTI);
            getMenu().resetMenus(MaeStrings.MENU_MODE);
        }
    }

    public void switchToAdjudMode() {
        // TODO: 2016-02-19 16:58:37EST prevent entering adjud with unsaved annotations
        if (getDrivers().size() == 1) {
            showError("Cannot start adjudication with a single annotation instance");
            return;
        } else if (checkTextSharing().size() > 0) {
            String message = String.format(
                    "Adjudication requires annotations on the same text. \nFound different texts: %s", checkTextSharing().toString());
            // TODO: 2016-02-07 16:12:20EST instead of show error, show warning to choose to close all irrelevant documents
            showError(message);
            return;
        }
        if (!isAdjudicating() && showAllUnsavedChangeWarning()) {
            File goldstandard = null;
            try {
                goldstandard = getDialogs().showStartAdjudicationDialog();
            } catch (MaeException e) {
                showError(e);
            }
            if (goldstandard == null) { // terminate if the user cancelled file selection
                return;
            }
            setAdjudicating(true);
            mode = MODE_NORMAL;
            addAdjudication(goldstandard);

            removeAllBGColors();
            getMenu().resetMenus(MaeStrings.MENU_FILE, MaeStrings.MENU_MODE);
            sendNotification(MaeStrings.SB_NORM_MODE_NOTI);
        }
    }

    public void switchToAnnotationMode() {
        if (isAdjudicating()) {
            try {
                getDrivers().remove(adjudDriverIndex).destroy();
                currentDriver = getDriverAt(adjudDriverIndex);
                getTextPanel().removeAdjudicationTab();
                setAdjudicating(false);
                getTablePanel().prepareAllTables();
                getTablePanel().insertAllTags();
                assignAllFGColor();
                sendNotification(MaeStrings.SB_NORM_MODE_NOTI);
                getMenu().resetMenus(MaeStrings.MENU_FILE);
            } catch (MaeException e) {
                showError(e);
            }
        }
    }

    public void switchToNormalMode() {

        if (mode != MODE_NORMAL) {
            mode = MODE_NORMAL;
            clearTextSelection();
            sendNotification(MaeStrings.SB_NORM_MODE_NOTI);
            removeAllBGColors();
            getMenu().resetMenus(MaeStrings.MENU_MODE);
        }
    }

    public void setUpTask(final File taskFile) {
        // This always wipes out on-going annotation works, even when multiple
        // files are open.
        // Since an instance of MAE requires all works share the same DB schema
        sendWaitMessage();
        new WipeDriversWorker(drivers).execute(); // destroy existing drivers in BG
        drivers = new ArrayList<>();
        new SetUpTaskWorker(taskFile).execute();

    }

    private void addAndSwitchDriver(MaeDriverI driver) {
        currentDriver = driver;
        drivers.add(currentDriver);
    }

    private MaeDriverI setUpDriver(File taskFile) throws MaeException {
        String dbFilename = String.format("mae-%d", System.currentTimeMillis());
        File dbFile;
        try {
            dbFile = File.createTempFile(dbFilename, ".sqlite");
        } catch (IOException e) {
            throw new MaeIOException("Could not generate DB file:", e);
        }
        MaeDriverI driver = new LocalSqliteDriverImpl(dbFile.getAbsolutePath());
        try {
            driver.readTask(taskFile);
            logger.info(String.format("task \"%s\" is loaded, has %d extent tag definitions and %d link tag definitions",
                    driver.getTaskName(), driver.getExtentTagTypes().size(), driver.getLinkTagTypes().size()));
            return driver;
        } catch (FileNotFoundException e) {
            driver.destroy();
            // wrap NotFoundError into a MaeException
            throw new MaeIOException("Could not open the task definition file: ", e);
        } catch (MaeException e) {
            driver.destroy();
            throw e;
        }
    }

    public void addDocument(final File annotationFile) {
        if (checkDuplicateDocs(annotationFile)) return;

        final boolean firstDocument = !isDocumentOpen();
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                try {
                    timeConsumingAddDocument(annotationFile, firstDocument);
                    return true;
                } catch (final Exception e) {
                    showError(e);
                    return false;
                }
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        if (firstDocument) {
                            getMenu().resetMenus(MaeStrings.MENU_FILE,
                                    MaeStrings.MENU_TAGS,
                                    MaeStrings.MENU_MODE);
                        }
                        adjustUIForNewDocument();
                    } else {
                        updateNotificationArea();
                    }
                } catch (InterruptedException | ExecutionException e) {
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                }
            }
        };
        sendWaitMessage();
        worker.execute();
    }

    private void timeConsumingAddDocument(File annotationFile, boolean firstDocument) throws MaeException {

        String xmlParseWarnings;
        if (!firstDocument) {
            addAndSwitchDriver(setUpDriver(new File(getDriver().getTaskFileName())));
        }
        try {
            // setting up the scheme will switch driver to the new one

            xmlParseWarnings = getDriver().readAnnotation(annotationFile);
            logger.info(String.format("document \"%s\" is loaded into DB.",
                    getDriver().getAnnotationFileBaseName()));

        } catch (Exception e) {
            destroyIncompleteDriver(); // this includes resetting statBar
            throw e;
        }
        if (firstDocument) {
            try {
                getTablePanel().insertAllTags(); // from second, inserting into table is done by tab change listener
            } catch (MaeException e) {
                closeCurrentDocument();
                throw e;
            }
            logger.info("inserting is done");
        }
        if (xmlParseWarnings.length() > 0) {
            popupMessage(xmlParseWarnings);
        }

    }

    public void addAdjudication(File goldstandard) {
        try {
            try {
                addAndSwitchDriver(setUpDriver(new File(getDriver().getTaskFileName()))); // will set up a new dirver for GS
                getDrivers().add(adjudDriverIndex, getDrivers().remove(getDrivers().size() - 1)); // move gold driver to the front
                String xmlParseWarnings = getDriver().readAnnotation(goldstandard);
                getTextPanel().addAdjudicationTab(goldstandard.getName(), getDriver().getPrimaryText());
                getTablePanel().prepareAllTables();
                switchAdjudicationTag();
                logger.info(String.format("gold standard for adjudication \"%s\" is open.", getDriver().getAnnotationFileBaseName()));
                if (xmlParseWarnings.length() > 0) {
                    popupMessage(xmlParseWarnings);
                }
            } catch (MaeIOException e) {
                showError(e);
                destroyIncompleteDriver();
            }
        } catch (MaeException e) {
            showError(e);
        }
    }

    private void adjustUIForNewDocument() {
        try {
            getTextPanel().addDocumentTab(getDriver().getAnnotationFileBaseName(), getDriver().getPrimaryText());

            if (isAdjudicating()) {
                currentDriver = drivers.get(adjudDriverIndex);
                assignAdjudicationColors();
            } else {
                assignAllFGColor();
                logger.info("painting is done");
                showCurrentDocumentIncompleteTagsWarning(true);
            }

            sendTemporaryNotification(MaeStrings.SB_FILEOPEN, 4000);
        } catch (MaeException e) {
            showError(e);
            closeCurrentDocument();
            updateNotificationArea();
        }
    }

    // TODO: 1/16/2017 delete this method
    private void destroyIncompleteDriver() {
        logger.error("Failed to load file due to an error in DB; destroying incomplete DB");
        try {
            if (drivers.size() == 1) { // means no annotation file is open (only task file open)
                String taskFileName = getDriver().getTaskFileName();
                getDriver().destroy();
                drivers.clear();
                addAndSwitchDriver(setUpDriver(new File(taskFileName)));
            } else {
                getDriver().destroy();
                drivers.remove(drivers.size() - 1);
                currentDriver = drivers.get(drivers.size() - 1);
            }
            updateNotificationArea();
        } catch (MaeException e) {
            showError(e);
        }

    }

    Set<String> checkTextSharing() {
        Set<String> differs = new HashSet<>();
        try {
            String workingText = getDriver().getPrimaryText();
            for (MaeDriverI driver : getDrivers()) {
                if (!driver.getPrimaryText().equals(workingText)) {
                    differs.add(driver.getAnnotationFileBaseName());
                }
            }
        } catch (MaeDBException e) {
            showError(e);
        }
        return differs;
    }

    boolean checkDuplicateDocs(File annotationFile) {
        for (MaeDriverI driver : getDrivers()) {
            try {
                if (driver.getAnnotationFileName() != null) {
                    String curDriverFilename = driver.getAnnotationFileName().replace("/./", "/");
                    if (annotationFile.getAbsolutePath().replace("/./", "/").equals(curDriverFilename)) {
                        showError(String.format("%s \nis already open!", annotationFile.getName()));
                        return true;

                    }
                }
            } catch (MaeDBException ignored) {
            }
        }
        return false;
    }

    public void switchAdjudicationTag() throws MaeDBException {
        // throws exception because it can cause half-read adjudication driver when starting adjudication
        propagateSelectionFromTextPanel();
        assignAdjudicationColors();
    }
    void assignAllFGColor() throws MaeDBException {
        getTextPanel().assignAllFGColor();
    }

    void assignAdjudicationColors() throws MaeDBException {
        // TODO: 2016-02-20 11:09:09EST clear coloring is extremely slow: need optimization
        getTextPanel().clearColoring();
        getTextPanel().clearSelection();
        TagType type = getAdjudicatingTagType();
        Set<Integer> goldAnchors = new HashSet<>(getDriver().getAllAnchorLocationsOfTagType(type));
        paintOverlappingStat(type, goldAnchors);
        paintGoldTags(goldAnchors);
    }

    void paintGoldTags(Collection<Integer> goldAnchors) {
        for (Integer goldAnchor : goldAnchors) {
            getTextPanel().assignOverlappingColorAt(goldAnchor, ColorHandler.getVividForeground(), false);
        }
//        getTextPanel().assignOverlappingColorOver(new LinkedList<>(goldAnchors), ColorHandler.getVividForeground(), false);
    }

    void paintOverlappingStat(TagType type, Set<Integer> goldAnchors) throws MaeDBException {
        MappedSet<Integer, Integer> anchorToDriverIndex = new MappedSet<>();
        // 0th is the driver for gold, skipping.
        for (int i = 1; i < getDrivers().size(); i++) {
            MaeDriverI driver = getDriverAt(i);
            List<Integer> anchors = driver.getAllAnchorLocationsOfTagType(type);
            for (Integer anchor : anchors) {
                if (!goldAnchors.contains(anchor)) {
                    anchorToDriverIndex.putItem(anchor, i);
                }
            }
        }
        for (Integer anchor : anchorToDriverIndex.keySet()) {
            Set<Integer> drivers = (Set<Integer>) anchorToDriverIndex.get(anchor);
            if (drivers.size() == 1) {
                Integer driverIndex = drivers.iterator().next();
                getTextPanel().assignOverlappingColorAt(anchor, documentTabColors.getColor(driverIndex), false);
            } else if (drivers.size() == getDrivers().size() - 1) { // full overlap
                getTextPanel().assignOverlappingColorAt(anchor, ColorHandler.getFadingForeground(), true);
            } else { // partial overlap
                getTextPanel().assignOverlappingColorAt(anchor, ColorHandler.getFadingForeground(), false);
            }
        }
    }

    public void switchAnnotationDocument(int tabId) {
        if (!isAdjudicating()) {
            try {
                sendWaitMessage();
                if (getDriver().isAnnotationLoaded()) {
                    getTablePanel().wipeAllTables();
                    getTextPanel().clearSelection();
                    getTextPanel().clearCaret();
                    updateNotificationArea();
                }
                currentDriver = getDrivers().get(tabId);
                getTablePanel().insertAllTags();
                assignTextColorsOver(getAnchorsToRepaint());
                storePaintedStates();
                logger.info(String.format("switched to document \"%s\", using DB file at \"%s\"",
                        getDriver().getAnnotationFileName(), getDriver().getDBSourceName()));
                updateNotificationArea();
            } catch (MaeException e) {
                showError(e);
            }
        }

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

    public List<ExtentTag> getExtentTagsInSelectedSpans() {
        return getExtentTagsIn(getSelectedTextSpans());
    }

    public List<ExtentTag> getExtentTagsIn(int[] locations) {
        // will return sorted list of tags
        try {
            return getDriver().getTagsIn(locations);
        } catch (Exception e) {
            showError(e);
            return new ArrayList<>();
        }
    }

    public List<ExtentTag> getExtentTagsFromAllDocumentsInSelectedSpans() {
        return getExtentTagsFromAllDocumentsIn(getSelectedTextSpans());
    }

    public List<ExtentTag> getExtentTagsOfATypeFromAllDocumentsInSelectedSpans(TagType type) {
        if (isTextSelected()) {
            return getExtentTagsOfATypeFromAllDocumentsIn(type, getSelectedTextSpans());
        } else {
            return getNCTagsOfATypeFromAllDocuments(type);
        }
    }

    public List<ExtentTag> getNCTagsOfATypeFromAllDocuments(TagType type) {
        List<ExtentTag> nctags = new LinkedList<>();
        try {
            for (MaeDriverI driver : getDrivers()) {
                nctags.addAll(driver.getAllNCTagsOfType(type));
            }
        } catch (MaeDBException e) {
            showError(e);
        }
        return nctags;

    }

    public List<ExtentTag> getExtentTagsOfATypeFromAllDocumentsIn(TagType type, int[] locations) {
        List<ExtentTag> tags = new LinkedList<>();
        try {
            for (MaeDriverI driver : getDrivers()) {
                tags.addAll(driver.getTagsOfTypeIn(type, locations));
            }
        } catch (MaeDBException e) {
            showError(e);
        }
        return tags;

    }

    public List<ExtentTag> getExtentTagsFromAllDocumentsIn(int[] locations) {
        Set<ExtentTag> tags = new HashSet<>();
        try {
            for (MaeDriverI driver : getDrivers()) {
                tags.addAll(driver.getTagsIn(locations));
            }
        } catch (MaeDBException e) {
            showError(e);
        }
        return new ArrayList<>(tags);
    }

    public boolean isArgumentsSelected() {
        return getSelectedArguments() != null && getSelectedArguments().size() > 0;
    }

    public List<ExtentTag> getSelectedArguments() {
        // will return list of tags in selected order
        try {
            return getTextPanel().getSelectedArgumentsInOrder();
        } catch (MaeDBException e) {
            showError(e);
        }
        return null;
    }

    public void undoLastSelection() {
        String notification;
        int[] unselected = getTextPanel().undoSelection();
        if (unselected != null) {
            removeAllBGColors();
            addBGColorOver(getSelectedTextSpans(), ColorHandler.getDefaultHighlighter());
            addBGColorOver(unselected, ColorHandler.getFadingHighlighter());
            notification = String.format(
                    "Removed '%s' from selection! Click anywhere to continue."
                    , getTextIn(unselected));
        } else {
            notification = "Nothing to undo! Click anywhere to continue.";
        }

        sendNotification(notification);

    }

    public void clearTextSelection() {
        getTextPanel().clearSelection();
        propagateSelectionFromTextPanel();
        sendTemporaryNotification("Cleared!, Click anywhere to continue", 1000);
    }

    public File selectSingleFile(String defautName, boolean saveFile) {
        return getDialogs().showFileChooseDialogAndSelect(defautName, saveFile);
    }

    public void assignTextColorsOver(List<Integer> anchors) {
        try {
            if (anchors.size() > 100) {
                getTextPanel().massivelyAssignFGColors(anchors);
            } else {
                getTextPanel().assignFGColorOver(anchors);
            }
        } catch (Exception e) {
            showError(e);
        }
    }

    void resetPaintableColors() {
        try {
            textHighlighColors = new ColorHandler(getDriver().getExtentTagTypes().size());
            tagsForColor.clear();
        } catch (MaeDBException e) {
            showError(e);
        }
    }

    void storePaintedStates() {
        Set<TagType> activated = getTablePanel().getActiveTags();
        try {
            for (TagType type : getDriver().getAllTagTypes()) {
                if (activated.contains(type)) {
                    coloredTagsInLastDocument.put(type, Boolean.TRUE);
                } else {
                    coloredTagsInLastDocument.put(type, Boolean.FALSE);
                }
            }
        } catch (MaeDBException e) {
            showError(e);
        }
    }

    List<Integer> getAnchorsToRepaint() {
        Set<Integer> toRepaint = new HashSet<>();
        Set<TagType> currentlyActivated = getTablePanel().getActiveTags();
        for (TagType type : coloredTagsInLastDocument.keySet()) {
            if ((currentlyActivated.contains(type) && !coloredTagsInLastDocument.get(type))
                    || (!currentlyActivated.contains(type) && coloredTagsInLastDocument.get(type))) {
                try {
                    toRepaint.addAll(getDriver().getAllAnchorLocationsOfTagType(type));
                } catch (MaeDBException e) {
                    showError(e);
                }
            }
        }
        return new ArrayList<>(toRepaint);
    }

    public Color getDocumentColor(String documentName) {
        try {
            for (int i = 1; i < getDrivers().size(); i++) {
                MaeDriverI driver = getDriverAt(i);
                if (driver.getAnnotationFileName().equals(documentName)) {
                    return getDocumentColor(i);
                }
            }
            showError("No such document open: " + documentName);
        } catch (MaeDBException e) {
            showError(e);
        }
        return Color.BLACK;
    }

    public Color getDocumentColor(int documentTabIndex) {
        return documentTabColors.getColor(documentTabIndex);
    }

    public Color getFGColor(TagType type) {
        if (!tagsForColor.contains(type)) {
            tagsForColor.add(type);
        }
        return getTextHighlightColors().getColor(tagsForColor.indexOf(type));

    }

    public int paintableTagTypes() {
        return tagsForColor.size();
    }

    public ColorHandler getTextHighlightColors() {
        return textHighlighColors;
    }

    public void setFGColor(TagType tagType, Color newColor) {
        getTextHighlightColors().setColor(newColor, tagsForColor.indexOf(tagType));
        if (getTablePanel().getActiveExtentTags().contains(tagType)) {
            try {
                assignTextColorsOver(getDriver().getAllAnchorLocationsOfTagType(tagType));
            } catch (MaeDBException e) {
                showError(e);
            }
        }
    }

    public boolean isTextSelected() {
        return getTextPanel().isTextSelected();
    }

    public void addBGColorOver(List<Integer> spans, Highlighter.HighlightPainter painter) {
        addBGColorOver(SpanHandler.convertIntegerlistToIntegerarray(spans), painter);

    }

    public void addBGColorOver(int[] spans, Highlighter.HighlightPainter painter) {
        try {
            getTextPanel().addBGColorOver(spans, painter);
        } catch (MaeControlException e) {
            showError(e);
        }
    }

    public void removeAllBGColors() {
        getTextPanel().removeAllBGColors();
    }

    public void increaseTextFontSize() {
        getTextPanel().increaseFontSize();

    }

    public void decreaseTextFontSize() {
        getTextPanel().decreaseFontSize();

    }

    public void resetFontSize() {
        getTextPanel().resetFontSize();

    }

    public void setAsArgumentFromDialog(String argumentTid) {
        try {
            getDialogs().setAsArgumentDialog(argumentTid);
        } catch (MaeDBException e) {
            showError(e);
        }

    }

    public LinkTag createLinkFromDialog(TagType linkType, List<ExtentTag> candidates) {
        try {
            LinkTag link = getDialogs().createLinkDialog(linkType, candidates);
            return link;
        } catch (MaeDBException e) {
            showError(e);
            return null;
        }
    }

    public void propagateSelectionFromTextPanel() {
        // when adjudicating, table is populated dynamically as user select text span
        // this is enabled only in normal mode because the purpose of dynamic table filling
        // is for reviewing, not for producing a new tag
        // basically, adjudicator are ideally not allowed make new tags
        if (isAdjudicating() && getMode() == MODE_NORMAL) {
            propagateToAdjudicationArea();
        } else if (isAdjudicating()) {
            // don't populate table
        } else {
            propagateToAnnotationArea();
        }
        updateNotificationArea();
    }

    public TagType getAdjudicatingTagType() {
        return getTablePanel().getCurrentTagType();
    }

    Set<Tag> getAdjudicatingTags() {
        return adjudicatingTags;
    }

    void propagateToAdjudicationArea() {
        getTablePanel().clearAdjudicationTable();
        adjudicatingTags.clear();
        TagType currentType = getAdjudicatingTagType();
        try {
            if (currentType.isExtent()) {
                List<ExtentTag> selectedTags = getExtentTagsOfATypeFromAllDocumentsInSelectedSpans(currentType);
                for (ExtentTag tag : selectedTags) {
                    getTablePanel().insertTagIntoAdjudicationTable(tag);
                    adjudicatingTags.add(tag);
                }
            } else {
                Set<LinkTag> linkers = new HashSet<>();
                List<ExtentTag> selectedTags = getExtentTagsFromAllDocumentsInSelectedSpans();
                for (ExtentTag tag : selectedTags) {
                    MaeDriverI driver = getDriverOf(tag.getFilename());
                    Set<LinkTag> linkersFromAFile = driver.getLinksHasArgumentTag(tag);
                    for (LinkTag linker : linkersFromAFile) {
                        if (linker.getTagtype().equals(currentType)) {
                            linkers.add(linker);
                        }
                    }
                }
                for (LinkTag linker : linkers) {
                    getTablePanel().insertTagIntoAdjudicationTable(linker);
                    adjudicatingTags.add(linker);
                }
            }
        } catch (MaeDBException e) {
            e.printStackTrace();
        }

    }

    void propagateToAnnotationArea() {
        getTablePanel().clearTableSelections();
        List<ExtentTag> releventTags = getExtentTagsInSelectedSpans();
        getTablePanel().clearTableSelections();
        for (ExtentTag tag : releventTags) {
            try {
                getTablePanel().selectTagFromTable(tag);
            } catch (MaeDBException e) {
                showError(e);
            }
        }
    }

    public void propagateSelectionFromTablePanel(String tid) {
        removeAllBGColors();
        try {
            addBGColorOver(getDriver().getAnchorLocationsByTid(tid), ColorHandler.getVividHighliter());
        } catch (Exception e) {
            showError(e);
        }
    }

    public JPopupMenu createTableContextMenu(JTable table) {
        logger.debug("creating context menu from table panel");
        try {
            return getMenu().createTableContextMenu(table);
        } catch (MaeDBException e) {
            showError(e);
        }
        return null;
    }

    public JPopupMenu createTextContextMenu() {
        logger.debug("creating context menu from text panel");
        try {
            return getMenu().createTextContextMenu();
        } catch (MaeDBException e) {
            showError(e);
        }
        return null;
    }

    void adjudicationStatUpdate() {
        try {
            assignAdjudicationColors();
        } catch (MaeDBException e) {
            showError(e);
        }
        clearTextSelection();
    }

    public void deleteTag(Tag tag) {
        try {
            if (!isAdjudicating()) {
                getTablePanel().removeTagFromTable(tag);
            } else {
                if (tag.getTagtype().isExtent()) {
                    for (LinkTag link : getDriver().getLinksHasArgumentTag((ExtentTag) tag)) {
                        deleteTagFromDB(link);
                    }
                }
                deleteTagFromDB(tag);
                adjudicationStatUpdate();
            }
        } catch (MaeDBException e) {
            showError(e);
        }
    }

    public Tag createTagFromMenu(TagType tagType) {

        boolean nc = getSelectedTextSpans() == null || getSelectedTextSpans().length == 0;
        String message;
        if (tagType.isLink()) {
            message = String.format("creating DB row for a yet-empty Link tag: (%s)", tagType.getName());
        } else if (nc) {
            message = String.format("creating DB row for a NC extent tag: (%s)", tagType.getName());
        } else {
            message = String.format("creating DB row from text selection: (%s) \"%s\"", tagType.getName(), getSelectedText());
        }
        logger.info(message);
        try {
            Tag tag;
            String tid = getDriver().getNextId(tagType);
            if (tagType.isLink()) {
                tag = getDriver().createLinkTag(tid, tagType);
                // creating a link from text popup will always end up in an empty link,
                // no need to populate or repaint its arguments
            } else if (nc) {
                tag = getDriver().createExtentTag(tid, tagType, null, null);
            } else {
                tag = getDriver().createExtentTag(tid, tagType, getSelectedText(), getSelectedTextSpans());
            }
            getTablePanel().insertNewTagIntoTable(tag, tagType);
            if (isAdjudicating()) {
                adjudicationStatUpdate();
            } else {
                selectTagAndTable(tag);
                if (tagType.isExtent()) {
                    assignTextColorsOver(((ExtentTag) tag).getSpansAsList());
                } else {

                }
            }
            updateSavedStatusInTextPanel();
            if (normalModeOnCreation()) {
                switchToNormalMode();
            }
            return tag;
        } catch (MaeException e) {
            showError(e);
        }
        return null;
    }

    public void addArgument(LinkTag linker, ArgumentType argType, String argTid) {
        try {
            ExtentTag arg = (ExtentTag) getDriver().getTagByTid(argTid);
            logger.info(String.format(
                    "adding an argument %s of type \"%s\" to %s", arg.toString(), argType.getName(), linker.getId()));
            getDriver().addArgument(linker, argType, arg);
            assignTextColorsOver(arg.getSpansAsList());
        } catch (MaeDBException e) {
            showError(e);
        }
    }

    public Tag getTagBySourceAndTid(String sourceFileName, String tid) {
        try {
            return getDriverOf(sourceFileName).getTagByTid(tid);
        } catch (MaeDBException e) {
            showError(e);
        }
        return null;
    }

    public Tag copyTag(Tag tag) {
        TagType type = tag.getTagtype();
        try {
            if (type.isExtent()) {
                ExtentTag etag = (ExtentTag) tag;
                return copyExtentTag(etag);
            } else {
                LinkTag ltag = (LinkTag) tag;
                return copyLinkTag(ltag);

            }
        } catch (MaeDBException e) {
            showError(e);
        }
        return null;

    }

    ExtentTag copyExtentTag(ExtentTag original) throws MaeDBException {
        TagType type = original.getTagtype();
        ExtentTag newTag = getDriver().createExtentTag(type, original.getText(), original.getSpansAsArray());
        Map<String, String> attMap = original.getAttributesWithNames();
        for (String attTypeName : attMap.keySet()) {
            AttributeType attType = getDriver().getAttributeTypeOfTagTypeByName(type, attTypeName);
            getDriver().addAttribute(newTag, attType, attMap.get(attTypeName));
        }
        adjudicationStatUpdate();
        return newTag;
    }

    LinkTag copyLinkTag(LinkTag original) throws MaeDBException {
        String warning = ("Copying a link tag will also copy its arguments,\n" +
                "unless an extent tag with the same spans is found in GS.\n" +
                "(non-consuming arguments are always copied!)" +
                "\nDo you want to continue?");
        if (showWarning(warning)) {
            MaeDriverI originalDriver = getDriverOf(original.getFilename());
            TagType type = original.getTagtype();
            LinkTag newTag = getDriver().createLinkTag(type);
            Map<String, String> attMap = original.getAttributesWithNames();
            for (ArgumentType argType : type.getArgumentTypes()) {
                String originalArgId = attMap.get(argType.getName() + MaeStrings.ARG_IDCOL_SUF);
                if (originalArgId != null && originalArgId.length() > 0) {
                    ExtentTag originalArg = (ExtentTag) originalDriver.getTagByTid(originalArgId);
                    boolean matchExists = false;
                    ExtentTag newArg = null;
                    for (ExtentTag argCandidate : getDriver().getTagsOfTypeIn(originalArg.getTagtype(), originalArg.getSpansAsArray())) {
                        argCandidate.getSpansAsString().equals(originalArg.getSpansAsString());
                        newArg = argCandidate;
                        matchExists = true;
                        break;
                    }
                    if (!matchExists) {
                        newArg = copyExtentTag(originalArg);
                    }
                    getDriver().addArgument(newTag, argType, newArg);
                }

            }
            for (AttributeType attType : type.getAttributeTypes()) {
                String attValue = attMap.get(attType.getName());
                if (attValue != null && attValue.length() > 0) {
                    getDriver().addAttribute(newTag, attType, attValue);
                }
            }
            adjudicationStatUpdate();
            return newTag;
        }
        return null;
    }

    public void selectTagAndTable(Tag tag) {
        try {
            getTablePanel().clearTableSelections();
            getTablePanel().selectTagFromTable(tag);
            getTablePanel().selectTabOf(tag.getTagtype());
            propagateSelectionFromTablePanel(tag.getId());
        } catch (MaeDBException e) {
            showError(e);
        }
    }

    public void deleteTagFromDB(Tag tag) {
        logger.debug(String.format("removing DB row: \"%s\"", tag.getId()));
        try {
            getDriver().deleteTag(tag);
            if (!isAdjudicating()) {
                getTextPanel().repaintFGColor(tag);
            }
            updateSavedStatusInTextPanel();
        } catch (MaeDBException e) {
            showError(e);
        }
    }

    public boolean updateDBFromTableUpdate(String tid, String colName, String value) {
        logger.debug(String.format("modifying DB based on table update: updating \"%s\" of %s to \"%s\"", colName, tid, value));
        boolean succeed = false;
        try {
            Tag tag = getTagByTid(tid);
            if (tag.getTagtype().isExtent() && colName.equals(MaeStrings.SPANS_COL_NAME)) {
                succeed = getDriver().updateTagSpans((ExtentTag) tag, SpanHandler.convertStringToArray(value));
            } else if (tag.getTagtype().isExtent() && colName.equals(MaeStrings.TEXT_COL_NAME)) {
                succeed = getDriver().updateTagText((ExtentTag) tag, value);
            } else if (tag.getTagtype().isLink() && colName.endsWith(MaeStrings.ARG_IDCOL_SUF)) {
                String argTypeName = colName.substring(0, colName.length() - MaeStrings.ARG_IDCOL_SUF.length());
                ArgumentType argType = getDriver().getArgumentTypeOfTagTypeByName(tag.getTagtype(), argTypeName);
                LinkTag linker = (LinkTag) getTagByTid(tid);
                if (value.length() == 0) {
                    succeed = (getDriver().UpdateArgument(linker, argType, null) == null);
                } else {
                    ExtentTag arg = (ExtentTag) getTagByTid(value);
                    if (arg == null) {
                        showError("Argument not found: " + value);
                        return false;
                    }
                    succeed = (getDriver().UpdateArgument(linker, argType, arg) != null);
                }
            } else if (tag.getTagtype().isLink() && colName.endsWith(MaeStrings.ARG_TEXTCOL_SUF)) {
                // do nothing, will be automatically updated when argId is updated
                return true;
            } else {
                AttributeType attType = getDriver().getAttributeTypeOfTagTypeByName(tag.getTagtype(), colName);
                Attribute updated = getDriver().updateAttribute(tag, attType, value);
                if (value != null && value.length() > 0) {
                    succeed = updated != null;
                } else if (!attType.isRequired()) {
                    succeed = updated == null;
                } else if (attType.getDefaultValue().length() > 0) {
                    succeed = (getDriver().updateAttribute(tag, attType, attType.getDefaultValue()) != null);
                } else {
                    // This is a very dangerous DTD, since a required att has no def_value
                    // However, user should be able to do that;
                    // setting default value might mislead annotators
                    // Thus, if this happens, just return true to leave the value empty
                    // , which will later be checked by incompleteness check when saving
                    return true;
                }
            }
        } catch (MaeException e) {
            showError(e);
        }
        if (succeed) {
            updateSavedStatusInTextPanel();
        }
        return succeed;
    }

    public void surgicallyUpdateCell(Tag tag, String colName, String value) {
        logger.info(String.format("updating %s's %s to %s", tag.getId(), colName, value));
        getTablePanel().insertValueIntoCell(tag, colName, value);
    }

    public Tag getTagByTid(String tid) {
        try {
            return getDriver().getTagByTid(tid);
        } catch (MaeDBException e) {
            showError(e);
        }
        return null;
    }

    public Set<Tag> getIncompleteTagsAt(int tabIdx) {
        // TODO: 2016-04-05 15:58:18EDT optimized this method
        // TODO: 2016-04-05 15:59:10EDT add supplement for checking adjudication file
        MaeDriverI driver = getDriverAt(tabIdx);
        try {
            Set<Tag> incomplete = new TreeSet<>();
            for (TagType type : driver.getAllTagTypes()) {
                if (type.isExtent()) {
                    for (ExtentTag tag : driver.getAllExtentTagsOfType(type)) {
                        if (!tag.isComplete()) {
                            incomplete.add(tag);
                        }
                    }
                } else {
                    for (LinkTag tag : driver.getAllLinkTagsOfType(type)) {
                        if (!tag.isComplete()) {
                            incomplete.add(tag);
                        }
                    }
                }
            }
            return incomplete;
        } catch (MaeDBException e) {
            showError(e);
        }
        return null;

    }

    public Set<Tag> getCurrentDocumentIncompleteTags() {
        return getIncompleteTagsAt(getCurrentDocumentTabIndex());
    }

    public boolean showIncompleteTagsWarningAt(int tabIdx, boolean simplyWarn) {
        Set<Tag> incompletes = getIncompleteTagsAt(tabIdx);
        if (incompletes.size() > 0) {
            getTextPanel().getView().selectTab(tabIdx);
            return getDialogs().showIncompleteTagsWarning(incompletes, simplyWarn);
        }
        return true;
    }

    public boolean showCurrentDocumentIncompleteTagsWarning(boolean simplyWarn) {
        return getDialogs().showIncompleteTagsWarning(getCurrentDocumentIncompleteTags(), simplyWarn);
    }

    public void presentation() {
        getTextPanel().bigFontSize();
        getTablePanel().bigFontSize();
    }

    private class WipeDriversWorker extends SwingWorker<Boolean, String> {
        private final Collection<MaeDriverI> driversToDelete;

        WipeDriversWorker(Collection<MaeDriverI> driversToDelete) {
            this.driversToDelete = driversToDelete;
        }

        @Override
        protected Boolean doInBackground() throws Exception {
            int totalDrivers = driversToDelete.size();
            int driversLeft = 0;
            for (MaeDriverI driver : driversToDelete) {
                try {
                    driver.destroy();
                    publish(String.format("%d/%d", ++driversLeft, totalDrivers));
                } catch (Exception e) {
                    showError(e);
                    return false;
                }
            }
            return true;
        }

        @Override
        protected void process(List<String> list) {
            getStatusBar().setText(String.format(
                    MaeStrings.SB_BG_WIPING, list.get(list.size() - 1)));
        }
    }

    private class PrepareAllTablesWorker extends SwingWorker<Boolean, Object[]> {

        @Override
        protected Boolean doInBackground() throws Exception {
            SwingUtilities.invokeAndWait(() -> {
                try {
                    if (!isTaskLoaded()) {
                        throw new MaeControlException("Cannot make tables without a task definition!");
                    }
                    getTablePanel().emptyTagTables();
                } catch (MaeDBException e) {
                    showError("Found an error in DB!", e);
                } catch (MaeControlException e) {
                    showError("Failed to sort out tag tables!", e);
                }
            });
            List<TagType> types = getDriver().getAllTagTypes();
            logger.debug(String.format("start creating tables for %d tag types", types.size()));
            if (isAdjudicating()) {
                for (TagType type : types) {
                    String name = type.getName();
                    JLabel title = new JLabel(name);
                    publish(new Object[]{name, title,
                            getTablePanel().makeAdjudicationArea(type)});
                    logger.info(String.format("created adjudication table for \"%s\"", name));
                }
            } else {
                TablePanelView.TogglingTabTitle allTagsTabTitle
                        = new TablePanelView.TogglingTabTitle(TablePanelController.dummyForAllTagsTab);
                publish(new Object[]{MaeStrings.ALL_TABLE_TAB_BACK_NAME, allTagsTabTitle,
                        getTablePanel().makeAllExtentTagsArea()});
                logger.info("created annotation table for ALL extent tags");
                for (TagType type : types) {
                    String name = type.getName();
                    TablePanelView.TogglingTabTitle title = type.isExtent() ?
                            new TablePanelView.TogglingTabTitle(type, getFGColor(type)) :
                            new TablePanelView.TogglingTabTitle(type);
                    HighlightToggleListener toggleListener
                            = new HighlightToggleListener(getTablePanel(), false, title);
                    if (type.isExtent()) {
                        title.addMouseListener(toggleListener);
                        title.addToggleListener(toggleListener);
                    }
                    publish(new Object[]{name, title,
                            getTablePanel().makeAnnotationArea(type)});
                    logger.info(String.format("created annotation table for \"%s\"", name));
                }
            }
            return true;
        }

        @Override
        protected void process(List<Object[]> list) {
            for (Object[] o : list) {
                getTablePanel().getView().addTab((String) o[0], (JComponent) o[1], (JComponent) o[2]);
            }
        }

        @Override
        protected void done() {
            if (!isAdjudicating()) {
                TablePanelView.TogglingTabTitle allTagsTabTitle =
                        (TablePanelView.TogglingTabTitle) getTablePanel().getView().getTabs().getTabComponentAt(0);
                allTagsTabTitle.addToggleListener(new HighlightToggleListener(getTablePanel(), true, allTagsTabTitle));
                // this will turn on each extent tag title
                allTagsTabTitle.setHighlighted(true);

            }
        }
    }

    private class SetUpTaskWorker extends SwingWorker<Boolean, Void> {
        private final File taskFile;

        SetUpTaskWorker(File taskFile) {
            this.taskFile = taskFile;
        }

        @Override
        protected Boolean doInBackground() {
            try {
                addAndSwitchDriver(setUpDriver(taskFile));
                resetPaintableColors();
                setAdjudicating(false);
                getMenu().resetMenus(MaeStrings.MENU_FILE, MaeStrings.MENU_MODE);
                SwingUtilities.invokeLater(() -> {
                            getTextPanel().addGuideTab();
                        }
                );
                PrepareAllTablesWorker worker = new PrepareAllTablesWorker();
                worker.execute();
                boolean tableDone = worker.get();
                if (tableDone) {
                    logger.info("All tables are prepared");
                }
                return true;
            } catch (final Exception e) {
                showError(e);
                return false;
            }
        }

        @Override
        protected void done() {
            try {
                if (get())
                    sendTemporaryNotification(MaeStrings.SB_NEWTASK, 4000);
                else {
                    updateNotificationArea();
                }
            } catch (ExecutionException | InterruptedException e) {
                Thread.currentThread().interrupt();
                showError(e);
            }
        }
    }
}

