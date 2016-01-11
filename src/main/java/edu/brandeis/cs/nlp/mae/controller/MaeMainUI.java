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


import edu.brandeis.cs.nlp.mae.controller.deprecated.*;
import edu.brandeis.cs.nlp.mae.util.ColorHandler;
import edu.brandeis.cs.nlp.mae.util.MappedList;
import edu.brandeis.cs.nlp.mae.MaeHotKeys;
import edu.brandeis.cs.nlp.mae.MaeStrings;
import edu.brandeis.cs.nlp.mae.database.AnnotationTask;
import edu.brandeis.cs.nlp.mae.database.MakeTagListener;
import edu.brandeis.cs.nlp.mae.database.RemoveExtentTagListener;
import edu.brandeis.cs.nlp.mae.model.*;
import edu.brandeis.cs.nlp.mae.controller.deprecated.FileMenuListener;
import edu.brandeis.cs.nlp.mae.controller.deprecated.FontSizeMenuListener;
import edu.brandeis.cs.nlp.mae.controller.deprecated.HelpMenuListener;
import edu.brandeis.cs.nlp.mae.controller.deprecated.ModeMenuListener;
import edu.brandeis.cs.nlp.mae.util.SpanHandler;
import edu.brandeis.cs.nlp.mae.view.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.Timer;


/**
 * MaeMain is the main class for MAE; it manages all the GUI attributes
 * and manages how the annotation information is loaded, interacted with,
 * and displayed.
 *
 * @author Amber Stubbs, Keigh Rim
 */

public class MaeMainUI extends JPanel {

    // TODO: 12/29/2015 re-factor any swing components as class fields into separate classes
    private static final Logger logger = LoggerFactory.getLogger(MaeMainUI.class.getName());

    private static final long serialVersionUID = 9404268L;

    private Hashtable<String, Color> mColorTable;
    private HashSet<String> mActiveLinks;
    private HashSet<String> mActiveExts;

    public HashSet<String> getActiveLinks() {
        return mActiveLinks;
    }

    public void setActiveLinks(HashSet<String> mActiveLinks) {
        this.mActiveLinks = mActiveLinks;
    }

    public HashSet<String> getActiveExts() {
        return mActiveExts;
    }

    public void setActiveExts(HashSet<String> mActiveExts) {
        this.mActiveExts = mActiveExts;
    }


    private List<Color> mColors = new ColorHandler(30).getColors();

    // thses are for highlighter colors
    private Color mLightOrange = new Color(255, 204, 51);
    private Color mGreen = Color.green;
    private Color mPink = Color.pink;
    private Color mCyan = Color.cyan;
    private Color mLightGray = Color.lightGray;

    private Highlighter.HighlightPainter mDefHL = ColorHandler.getDefaultHighlighter();
    private Highlighter.HighlightPainter mGrayHL = ColorHandler.getFadingHighlighter();

    // default color is excluded from the list; it's for indicating selection
//    private TextHighlightPainter[] mHighlighters = ColorHandler.getHighlighters();

    public boolean isFileOpen() {
        return isFileOpen;
    }

    public void setFileOpen(boolean fileOpen) {
        isFileOpen = fileOpen;
    }

    //some booleans that help keep track of the status of the annotation
    private boolean isFileOpen;

    public boolean isTaskChanged() {
        return isTaskChanged;
    }

    public void setTaskChanged(boolean taskChanged) {
        isTaskChanged = taskChanged;
    }

    public boolean isTextSelected() {
        return isTextSelected;
    }

    public void setTextSelected() {
        setTextSelected(true);
    }

    public void setTextSelected(boolean textSelected) {
        isTextSelected = textSelected;
    }

    private boolean isTaskChanged;
    private boolean isTextSelected;

    public boolean isOptionExitOnCreation() {
        return mOptionExitOnCreation;
    }

    public void setOptionExitOnCreation(boolean mOptionExitOnCreation) {
        this.mOptionExitOnCreation = mOptionExitOnCreation;
    }

    // some booleans for user preference
    private boolean mOptionExitOnCreation = true; // on by default

    // krim: additional booleans to keep track of annotation mode
    public static final int M_NORMAL = 0;
    public static final int M_MULTI_SPAN = 1;
    public static final int M_ARG_SEL = 2;
    public static final int M_ADJUD = 9;

    public int getMode() {
        return mMode;
    }

    public void setMode(int mode) {
        this.mMode = mode;
    }

    private int mMode;

    private ArrayList<int[]> mSpans;
    private ArrayList<ArrayList<int[]>> mPrevSpans;
    private ArrayList<int[]> mLastSelection;

    public ArrayList<int[]> getSpans() {
        return mSpans;
    }

    public void setSpans(ArrayList<int[]> spans) {
        this.mSpans = spans;
    }

    public ArrayList<ArrayList<int[]>> getPrevSpans() {
        return mPrevSpans;
    }

    public ArrayList<int[]> getLastSelection() {
        return mLastSelection;
    }

    public LinkedList<String> getUnderspecified() {
        return mUnderspecified;
    }

    public void setUnderspecified(LinkedList<String> mUnderspecified) {
        this.mUnderspecified = mUnderspecified;
    }

    public ArrayList<String> getPossibleArgIds() {
        return mPossibleArgIds;
    }

    public void setPossibleArgIds(ArrayList<String> mPossibleArgIds) {
        this.mPossibleArgIds = mPossibleArgIds;
    }

    // variables for link creation
    private LinkedList<String> mUnderspecified;
    private ArrayList<String> mPossibleArgIds;

    public String getWorkingFileName() {
        return mWorkingFileName;
    }

    public JFrame getMainFrame() {
//        return (JFrame) this.getParent();
        return mMainFrame;
    }

    public void setMainFrame(JFrame frame) {
        mMainFrame = frame;
    }

    public void setWorkingFileName(String mWorkingFileName) {
        this.mWorkingFileName = mWorkingFileName;
    }

    // for file operation
    private String mWorkingFileName;

    public String getFilenameSuffix() {
        return mFilenameSuffix;
    }

    public void setFilenameSuffix(String mFilenameSuffix) {
        this.mFilenameSuffix = mFilenameSuffix;
    }

    private String mFilenameSuffix = "";

    // krim: column number of some fixed attributes
    // this might be hard-coding, but MAE always create an extent element
    // with id, spans, text as the first three attribs of it
    public final int SRC_COL = 0;
    public final int ID_COL = 1;
    public final int SPANS_COL = 2;
    public final int TEXT_COL = 3;

    // should match last essential column (currently text_col) + 1
    public int LAST_ESSE_COL = 4;

    //GUI components
    protected JFrame mMainFrame;

    public JFrame getLinkPopupFrame() {
        return mLinkPopupFrame;
    }

    public void setLinkPopupFrame(JFrame mLinkPopupFrame) {
        this.mLinkPopupFrame = mLinkPopupFrame;
    }

    protected JFrame mLinkPopupFrame;
    private JScrollPane mScrollPane;

    public Hashtable<String, JTable> getElementTables() {
        return mElementTables;
    }

    public void setElementTables(Hashtable<String, JTable> mElementTables) {
        this.mElementTables = mElementTables;
    }

    private Hashtable<String, JTable> mElementTables;

    public JTabbedPane getBottomTable() {
        return mBottomTable;
    }

    public void setBottomTable(JTabbedPane mBottomTable) {
        this.mBottomTable = mBottomTable;
    }

    protected JTabbedPane mBottomTable;

    public JTextPane getTextPanel() {
        return mTextPane;
    }

    public void setTextPane(JTextPane mTextPane) {
        this.mTextPane = mTextPane;
    }

    protected JTextPane mTextPane;
    private JPanel mTopPanel;

    private JMenuBar mMenuBar;

    // TODO refactor this into separate class
    protected JLabel mStatusBar;

    public JPopupMenu getTextPopup() {
        return mTextPopup;
    }

    public void setTextPopup(JPopupMenu mTextPopup) {
        this.mTextPopup = mTextPopup;
    }

    protected JPopupMenu mTextPopup;

    public JPopupMenu getTablePopup() {
        return mTablePopup;
    }

    public void setTablePopup(JPopupMenu mTablePopup) {
        this.mTablePopup = mTablePopup;
    }

    protected JPopupMenu mTablePopup;

    public JFileChooser getLoadFC() {
        return mLoadFC;
    }

    public void setLoadFC(JFileChooser mLoadFC) {
        this.mLoadFC = mLoadFC;
    }

    public JFileChooser getSaveFC() {
        return mSaveFC;
    }

    public void setSaveFC(JFileChooser mSaveFC) {
        this.mSaveFC = mSaveFC;
    }

    protected JFileChooser mLoadFC;
    protected JFileChooser mSaveFC;

    public AnnotationTask getTask() {
        return mTask;
    }

    public void setTask(AnnotationTask mTask) {
        mTask = mTask;
    }

    //the helper function for talking to the database
    private AnnotationTask mTask;

    public JLabel getStatusBar() {
        return mStatusBar;
    }

    public void setStatusBar(JLabel mStatusBar) {
        this.mStatusBar = mStatusBar;
    }

    public MaeMainUI() {
        super(new BorderLayout());

        mTask = new AnnotationTask();

        isFileOpen = false;
        isTaskChanged = false;
        isTextSelected = false;

        mUnderspecified = new LinkedList<String>();
        mPossibleArgIds = new ArrayList<String>();

        mWorkingFileName = "";

        //used to keep track of what color goes with what tag
        mColorTable = new Hashtable<String, Color>();
        // keep track of which tag type is highlighted in text pane
        mActiveLinks = new HashSet<String>();
        mActiveExts = new HashSet<String>();

        // collection for bottom tables for each tag type
        mElementTables = new Hashtable<String, JTable>();

        /* GUI components */
        // file choosers
        mLoadFC = new JFileChooser(".");
        mLoadFC.setFileSelectionMode(JFileChooser.FILES_ONLY);
        mSaveFC = new JFileChooser(".");
        mSaveFC.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        // popup menus
        mTextPopup = new JPopupMenu();
        mTablePopup = new JPopupMenu();
        mLinkPopupFrame = new JFrame();

        // main text pane
        mTextPane = new JTextPane(new DefaultStyledDocument());
        mTextPane.setEditable(false);
        mTextPane.setContentType("text/plain; charset=UTF-8");
        mTextPane.addCaretListener(new MaeCaretListener(this));
        mTextPane.addMouseListener(new TextMouseAdapter(this));
        mScrollPane = new JScrollPane(mTextPane);
        mScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // add a status bar in the bottom of the text pane
        mStatusBar = new JLabel();
        updateStatusBar();

        mTopPanel = new JPanel(new BorderLayout());
        mTopPanel.add(mScrollPane, BorderLayout.CENTER);
        mTopPanel.add(mStatusBar, BorderLayout.SOUTH);

        // bottom tabbed table panel
        mBottomTable = new JTabbedPane();
        JComponent panel1 = makeTextPanel(MaeStrings.NO_TASK_GUIDE);
        mBottomTable.addTab(MaeStrings.NO_TASK_IND, panel1);

        // main menu bar
        mMenuBar = new JMenuBar();
        updateMenus();

        // glue up everything
        JSplitPane splitPane = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT, mTopPanel, mBottomTable);

        add(mMenuBar, BorderLayout.NORTH);

        add(splitPane, BorderLayout.CENTER);

        splitPane.setDividerLocation(250);

        // set everything to default value
        mMode = M_NORMAL;
        // init start-end to (-1, -1) pair
        mSpans = new ArrayList<int[]>();
        mPrevSpans = new ArrayList<ArrayList<int[]>>();
        mLastSelection = new ArrayList<int[]>();
        resetSpans();

        logger.info("Hello world! MAE started successfully.");


    }

    // ***********************
    // Section: classes and listeners

    /**
     * Timer Task for timed messages in the status bar
     */
    protected class TimedUpdateStatusBar extends TimerTask {
        @Override
        public void run() {
            updateStatusBar();
        }
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
    public void setArgumentInTable(String linkName, String linkId,
                                   String argName, String argId, String argText) {
        DefaultTableModel tableModel
                = (DefaultTableModel) mElementTables.get(linkName).getModel();
        int rows = tableModel.getRowCount();
        int idRow = -1, argCol = -1;

        // first get indices of columns of argument IDs in the table
        TreeSet<Integer> argColumns = getArgColIndices(linkName);

        // then find which row and column to look for
        for (int i = rows - 1; i >= 0; i--) {
            if (tableModel.getValueAt(i, ID_COL).equals(linkId)) {
                idRow = i;
            }
        }
        for (Integer i : argColumns) {
            if (tableModel.getColumnName(i).equals(argName + MaeStrings.ARG_IDCOL_SUF)) {
                argCol = i;
            }
        }

        // set values for argID and argTEXT
        if (idRow == -1 || argCol == -1) {
            mStatusBar.setText("ERROR! Link ID and Arg name cannot be found in the table");
            delayedUpdateStatusBar(3000);
        } else {
            tableModel.setValueAt(argId, idRow, argCol);
            tableModel.setValueAt(argText, idRow, argCol + 1);
        }

        // finally, check this link has a complete set of arguments
        boolean fulfilled = true;
        for (int argColumn : argColumns) {
            if (tableModel.getValueAt(idRow, argColumn).equals("")) {
                fulfilled = false;
            }
        }
        // then remove this on from the list of the underspecified
        if (fulfilled) {
            mUnderspecified.remove(linkId);
        }
    }

    // end Section: classes
    // *******************************


    // *******************************
    // Section: tag/database processing methods

    /**
     * This takes the hashCollection created by the XMLHandler and loads it into the
     * tables and database
     *
     * @param elementsToProcess the HashCollection passed from XMLHandler
     */
    public void processTagHash(
            MappedList<String, Hashtable<String, String>> elementsToProcess) {
        ArrayList<String> elemNames = elementsToProcess.keyList();
        //first, add the extent tags

        for (String elemName : elemNames) {
            Elem elem = mTask.getTagTypeByName(elemName);
            if (elem instanceof ElemExtent &&
                    mElementTables.containsKey(elemName)) {
                // for each element type there is a list of tag information
                ArrayList<Hashtable<String, String>> elemInstances
                        = (ArrayList<Hashtable<String, String>>) elementsToProcess.get(elemName);

                for (Hashtable<String, String> instance : elemInstances) {
                    if (updateIDandDB(instance, elemName)) {
                        addRowFromHash(instance, elemName, true);
                    }
                }
                mTask.runBatchExtents();
            }
        }
        // then, go back and add the link tags
        // since they rely on the extent tag info, they need to be added later
        for (String elemName : elemNames) {
            Elem elem = mTask.getTagTypeByName(elemName);
            if (elem instanceof ElemLink &&
                    mElementTables.containsKey(elemName)) {
                /*for each element type there is a list of tag information*/
                ArrayList<Hashtable<String, String>> elemInstances
                        = (ArrayList<Hashtable<String, String>>) elementsToProcess.get(elemName);

                for (Hashtable<String, String> instance : elemInstances) {
                    if (updateIDandDB(instance, elemName)) {
                        addRowFromHash(instance, elemName, false);
                    }
                }
                mTask.runBatchLinks();
            }
        }
        //set colors for the whole document at once
        assignAllColors();
    }

    /**
     * addExtToDbFromHash is called for each tag in the HashCollection used in
     * processTagHash.
     *
     * @param a        the Hashtable with the attribute information
     * @param elemName the name of the tag being processed
     * @param newId    the ID of the tag being added
     */
    private void addExtToDbFromHash(
            Hashtable<String, String> a, String elemName, String newId) {

        // krim: take a string of spans and init a set of spans(start-end int pairs)
        String spansString = a.get("spans");
        mSpans = SpanHandler.convertStringToPairs(spansString);
        if (!isSpansEmpty()) {
            for (int[] span : mSpans) {
                int start = span[0], end = span[1];
                for (int i = start; i < end; i++) {
                    mTask.addExtToBatch(i, elemName, newId);
                }
            }
        } else {
            mTask.addExtToBatch(-1, elemName, newId);
        }
        // krim: resetting start-end for NC tag addition
        resetSpans();
    }

    /**
     * addLinkToDbFromHash is called for each tag in the HashCollection used in
     * processTagHash.
     *
     * @param a        the Hashtable with the attribute information
     * @param elemName the name of the tag being processed
     * @param newId    the ID of the tag being added
     */
    private void addLinkToDbFromHash(
            Hashtable<String, String> a, String elemName, String newId) {
        List<String> args = mTask.getArgumentTypesOfLinkTagType(elemName);
        ArrayList<String> argIDs = new ArrayList<String>();
        ArrayList<String> argTypes = new ArrayList<String>();
        for (String arg : args) {
            String argId = a.get(arg + MaeStrings.ARG_IDCOL_SUF);
            // check if id value is a dummy,
            // if is, add the link tag to the underspecified for further lookup
            if (argId.equals("")) {
                mUnderspecified.add(argId);
            } else {
                String type = mTask.getTagTypeByTid(argId);
                argIDs.add(argId);
                argTypes.add(type);
            }
        }
        mTask.addLinkToBatch(elemName, newId, argIDs, argTypes);
        // no need to run batch for each adding, to in at once in processTagHash()
    }

    /**
     * updateIDandDB sends tag information to the database, and returns a boolean
     * that indicates whether or not the tag was successfully added.
     *
     * @param elemAttVal the Hashtable of tag attributes
     * @param elemName   the name of the tag
     * @return a boolean indicating whether the transaction was successful
     */
    private boolean updateIDandDB(
            Hashtable<String, String> elemAttVal, String elemName) {
        Elem elem = mTask.getTagTypeByName(elemName);
        ArrayList<Attrib> attributes = elem.getAttributes();
        String newId = "";
        for (Attrib attribute : attributes) {
            if (attribute instanceof AttID) {
                newId = elemAttVal.get(attribute.getName());
            }
        }
        if (!newId.equals("")) {
            if (!mTask.idExists(elemName, newId)) {
                if (elem instanceof ElemExtent) {
                    addExtToDbFromHash(elemAttVal, elemName, newId);
                } else if (elem instanceof ElemLink) {
                    addLinkToDbFromHash(elemAttVal, elemName, newId);
                }
            } else {
                System.err.println(String.format(
                        "ID %s of %s already in DB. Skipping addition"
                        , newId, elemName));
                return false;
            }
        } else {
            System.err.println("ID was not found");
            return false;
        }
        return true;
    }

    /**
     * addRowFromHash is called when new tag information has been added to the
     * database successfully, and will now be added to the appropriate tag table.
     *
     * @param a        Hashtable of attributes
     * @param elemName type of tag being added
     */
    private void addRowFromHash(
            Hashtable<String, String> a, String elemName, boolean isExt) {
        DefaultTableModel tableModel
                = (DefaultTableModel) mElementTables.get(elemName).getModel();
        String[] newdata = new String[tableModel.getColumnCount()];
        for (int k = 0; k < tableModel.getColumnCount(); k++) {
            String colName = tableModel.getColumnName(k);
            String value = a.get(colName);
            if (value != null) {
                newdata[k] = value;
            } else {
                newdata[k] = "";
            }
        }
        tableModel.addRow(newdata);
        if (isExt) {
            DefaultTableModel allTableModel = (DefaultTableModel)
                    mElementTables.get(
                            MaeStrings.ALL_TABLE_TAB_BACK_NAME).getModel();
            // extent tag tables are always initialized with
            // id, spans, text in first three columns
            String[] newdataForAll = Arrays.copyOfRange(newdata, 0, 3);
            allTableModel.addRow(newdataForAll);
        }
    }

    /**
     * delete an item from all_extent_tags table given an ID of the item
     *
     * @param id id of the item to delete
     */
    public void removeAllTableRow(String id) {
        DefaultTableModel tableModel
                = (DefaultTableModel) mElementTables.get(
                MaeStrings.ALL_TABLE_TAB_BACK_NAME).getModel();
        int rows = tableModel.getRowCount();
        //has to go backwards or the wrong rows get deleted
        for (int i = rows - 1; i >= 0; i--) {
            if (id.equals(tableModel.getValueAt(i, ID_COL))) {
                tableModel.removeRow(i);
            }
        }
    }

    /**
     * Removes links from the table and DB
     *
     * @param links HashCollection of types and IDs of links being removed
     */
    public void removeLinkTableRows(MappedList<String, String> links) {
        ArrayList<String> linkTags = links.keyList();
        for (String tag : linkTags) {
            Elem elem = mTask.getTagTypeByName(tag);
            ArrayList<String> link_ids = (ArrayList<String>) links.get(elem.getName());
            if (elem instanceof ElemLink) {
                for (String id : link_ids) {
                    removeTableRows(elem, id);
                }
            }
        }
    }

    /**
     * This removes the table rows containing the id given. If the id belongs to and
     * extent tag, then it recolors the related text portion.
     *
     * @param elem type of tag being removed
     * @param id   ID of tag being removed
     */
    public void removeTableRows(Elem elem, String id) {
        DefaultTableModel tableModel
                = (DefaultTableModel) mElementTables.get(elem.getName()).getModel();
        int rows = tableModel.getRowCount();
        //has to go backwards or the wrong rows get deleted
        for (int i = rows - 1; i >= 0; i--) {
            if (id.equals(tableModel.getValueAt(i, ID_COL))) {
                //redo color for this text--assumes that lines
                //have already been removed from the DB
                if (elem instanceof ElemExtent) {
                    assignTextColor(SpanHandler.convertStringToPairs(
                            (String) tableModel.getValueAt(i, SPANS_COL)));
                }
                tableModel.removeRow(i);
            }
        }
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
        String text = "";
        DefaultTableModel tableModel
                = (DefaultTableModel) mElementTables.get(elem).getModel();
        int rows = tableModel.getRowCount();

        for (int i = rows - 1; i >= 0; i--) {
            String value = (String) tableModel.getValueAt(i, ID_COL);
            if (value.equals(id)) {
                text = (String) tableModel.getValueAt(i, TEXT_COL);
            }
        }
        if (text.length() > 20 && !fullText) {
            String[] words = text.split(" ");
            return words[0] + MaeStrings.LONGTEXTTRUNC + words[words.length - 1];
        }
        return text;
    }

    /**
     * Finds which rows in the table get highlighted based on the span that was
     * selected in the text panel
     */
    public void findHighlightRows() {
        clearTableSelections();
        //first, get ids and types of elements in selected extents
        MappedList<String, String> idHash = mTask.getTagsByTypesIn(mSpans);
        if (idHash.size() > 0) {
            ArrayList<String> elemNames = idHash.keyList();
            for (String elemName : elemNames) {
                ArrayList<String> ids = (ArrayList<String>) idHash.get(elemName);
                for (String id : ids) {
                    highlightTableRows(elemName, id);
                    //returns HashCollection of link ids connected to this
                    MappedList<String, String> links
                            = mTask.getLinksHasArgumentOf(elemName, id);
                    highlightTableRowsHash(links);
                }
            }
        }
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
        if (!isSpansEmpty()) {
            for (int[] span : mSpans) {
                int start = span[0], end = span[1];
                for (int i = start; i < end; i++) {
                    mTask.addExtToBatch(i, elemName, newId);
                }
            }
        } else {
            mTask.addExtToBatch(-1, elemName, newId);
        }
        mTask.runBatchExtents();
    }

    /**
     * Adds a link tag to the database. Unlike an extent tag, a link tag only
     * occupies one item in the DB
     */
    public void addLinkTagToDb(String elemName, String newId,
                               List<String> argIds, List<String> argTypes) {
        mTask.addLinkToBatch(elemName, newId, argIds, argTypes);
        mTask.runBatchLinks();
    }

    // *******************************
    // Section: GUI methods
    // the methods that create/display GUI modules

    /**
     * Separate function used to highlight link rows associated with selected
     * extents.
     *
     * @param hash Hashtable with tag names as keys and IDs as values
     */
    private void highlightTableRowsHash(MappedList<String, String> hash) {
        ArrayList<String> elems = hash.keyList();
        for (String e : elems) {
            ArrayList<String> ids = (ArrayList<String>) hash.get(e);
            for (String id : ids) {
                highlightTableRows(e, id);
            }
        }
    }

    /**
     * This method is for coloring/underlining text in the entire text window.  It
     * is called when a new file is loaded or toggling all_extents
     */
    public void assignAllColors() {
        //Get hashCollection of where tags are in the document
        //    <String location,<String elements>>.
        // TODO 151214 replace mTask.getLocElemHash() with DBDriver.getLocationsWithTags()
        // TODO 151216 renamed first, but let's see how it affects
        MappedList<String, String> locElem = mTask.getAllLocationsWithTags();
        ArrayList<String> locs = locElem.keyList();
        for (String loc : locs) {
            ArrayList<String> elements = (ArrayList<String>) locElem.get(loc);
            if (elements.size() > 1) {
                setColorAtLocation(mColorTable.get(elements.get(0)), Integer.parseInt(loc), 1, true);
            } else {
                setColorAtLocation(mColorTable.get(elements.get(0)), Integer.parseInt(loc), 1, false);
            }
        }
        List<String> elemNames = mTask.getExtentTagTypes();
        mActiveExts = new HashSet<String>(elemNames);
        for (String elemName : elemNames) {
            TabTitle tab = (TabTitle) mBottomTable.getTabComponentAt(
                    mBottomTable.indexOfTab(elemName));
            tab.setHighlighted(true);
        }
    }

    /**
     * krim: This method is for removing all color/underline highlighting from the
     * whole text windows. It is called when toggling all_extents
     */
    public void unassignAllColors() {
        MappedList<String, String> locElem = mTask.getAllLocationsWithTags();
        ArrayList<String> locs = locElem.keyList();
        for (String loc : locs) {
            setColorAtLocation(Color.black, Integer.parseInt(loc), 1, false);
        }
        for (String elemName : mTask.getExtentTagTypes()) {
            TabTitle tab = (TabTitle) mBottomTable.getTabComponentAt(
                    mBottomTable.indexOfTab(elemName));
            tab.setHighlighted(false);
        }
        mActiveExts.clear();
    }

    /**
     * krim: This method is for coloring/underlining discontinuous spans in the text
     * window.
     * TODO 151216 - make spans int[]
     *
     * @param spans a list of spans
     */
    public void assignTextColor(ArrayList<int[]> spans) {
        //go through each part of the word being changed and
        //  find what tags are there, and what color it should be.
        for (int[] span : spans) {
            int start = span[0], end = span[1];
            assignTextColor(start, end);
        }
    }

    /**
     * This method is for coloring/underlining text in the text window.  It detects
     * overlaps, and should be called every time a tag is added or removed.
     * TODO also assign boldness when adding link tags
     *
     * @param begin the location of the first character in the extent
     * @param end   the location of the last character in the extent
     */
    private void assignTextColor(int begin, int end) {
        // go through each part of the span being changed and
        // find what tags are there, and what color it should be.
        for (int i = begin; i < end; i++) {

            // get all elements at given location
            ArrayList<String> cand = new ArrayList<String>();

            // exclude unactivated elements
            for (String elemName : mTask.getTagsAt(i)) {
                if (mActiveExts.contains(elemName)) {
                    cand.add(elemName);
                }
            }

            // determine color seeing first tag
            Color c;
            try {
                c = mColorTable.get(cand.get(0));
            } catch (IndexOutOfBoundsException e) {
                c = Color.black;
            }

            // set color and underline
            if (cand.size() > 1) {
                setColorAtLocation(c, i, 1, true);
            } else {
                setColorAtLocation(c, i, 1, false);
            }
        }
    }

    /**
     * Sets the color of a specific span of text.  Called for each extent tag.
     *
     * @param color The color the text will become. Determined by the tag name and
     *              colorTable (Hashtable)
     * @param pos   the location of the start of the extent
     * @param len   the location of the end of the extent
     * @param b     whether or not the text will be underlined
     */
    private void setColorAtLocation(Color color, int pos, int len, boolean b) {
        DefaultStyledDocument styleDoc =
                (DefaultStyledDocument) mTextPane.getStyledDocument();
        SimpleAttributeSet aset = new SimpleAttributeSet();
        StyleConstants.setForeground(aset, color);
        StyleConstants.setUnderline(aset, b);
        styleDoc.setCharacterAttributes(pos, len, aset, false);
    }

    /**
     * Retrieves the text between two offsets from the document. krim: take a string
     * representing span(s), not 2 integers
     *
     * @param spans text spans
     * @return the text of the tag spans
     */
    public String getTextIn(ArrayList<int[]> spans) {
        String text = "";
        for (int[] span : spans) {
            text += getTextBetween(span[0], span[1]) + MaeStrings.SPANTEXTTRUNC;
        }
        // fence posting
        return text.substring(0, text.length() - MaeStrings.SPANTEXTTRUNC.length());
    }

    /**
     * Retrieves the text between two offsets from the document. (Original Amber's
     * code)
     *
     * @param start start location of the text
     * @param end   end location of the text
     * @return the text
     */
    public String getTextBetween(int start, int end) {
        // TODO implement stripping out white spaces with a proper option
        DefaultStyledDocument styleDoc
                = (DefaultStyledDocument) mTextPane.getStyledDocument();
        String text;
        try {
            text = styleDoc.getText(start, end - start);
        } catch (Exception e) {
            e.printStackTrace();
            text = "Error getting text from a selected span";
        }
        return text;
    }

    /**
     * krim: create a list of arguments from a give target tag set used in a
     * link-creation popup windows
     *
     * @param targetIds  target tag set
     * @param emptyFirst add a empty dummy as the first item in the list
     * @return list of argument strings "type - id - text"
     */
    public ArrayList<String> getComboItems(
            ArrayList<String> targetIds, boolean emptyFirst) {
        ArrayList<String> items = new ArrayList<>();
        if (emptyFirst) {
            items.add("");
        }
        for (String id : targetIds) {
            String type = mTask.getTagTypeByTid(id);
            // format relevant info, then add to the list
            items.add(String.format("%s%s%s%s%s",
                    type, MaeStrings.COMBO_DELIMITER,
                    id, MaeStrings.COMBO_DELIMITER,
                    getTextByID(type, id, false)));

        }
        return items;
    }

    /**
     * Creates panel containing text for the GUI
     *
     * @param text the text added to the panel
     * @return the panel with the text
     */
    private JComponent makeTextPanel(String text) {
        JPanel panel = new JPanel(false);
        JLabel filler = new JLabel(text);
        filler.setHorizontalAlignment(JLabel.CENTER);
        panel.setLayout(new GridLayout(1, 1));
        panel.add(filler);
        return panel;
    }

    /**
     * Creates a table for the element (tag) provided
     *
     * @param e the tag getting a table
     * @return the GUI component containing the JTable for the tag provided
     */
    private JComponent makeTablePanel(Elem e) {

        MaeTableModel model = new MaeTableModel(this);
        JTable table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        JScrollPane scrollPane = new JScrollPane(table);

        mElementTables.put(e.getName(), table);
        table.addMouseListener(new TableMouseAdapter(this));
        //go through element attributes and add colums
        ArrayList<Attrib> attributes = e.getAttributes();
        //for some reason, it's necessary to add the columns first,
        //then go back and add the cell renderers.
        model.addColumn(MaeStrings.SRC_COL_NAME);
        for (Attrib attribute : attributes) {
            model.addColumn(attribute.getName());
        }
        for (int i = 0; i < attributes.size(); i++) {
            Attrib a = attributes.get(i);
            TableColumn c = table.getColumnModel().getColumn(1 + i);
            // TODO add listeners to ID, SPANS, TEXT columns to update all_tab when edited
            if (a instanceof AttList) {
                AttList att = (AttList) a;
                JComboBox options = makeValidValuesComboBox(att);
                c.setCellEditor(new DefaultCellEditor(options));
            } else if (a.isIdRef()) {
                /* TODO need to implement this part, currently not working
                final JComboBox tags = new JComboBox();
                // tags.setModel(new DefaultComboBoxModel(mPossibleArgIds));

                tags.addItem("");
                tags.setVisible(true);

                tags.addPopupMenuListener(
                        new PopupMenuListener() {
                            @Override
                            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                                tags.removeAllItems();
                                ArrayList<String> items = getComboItems(mTask.getAllExtTags(true));
                                System.out.println(items.size());
                                for (String item : items) {
                                    System.out.println(item);
                                    tags.addItem(item);
                                }
                                System.out.println("NO? YES?");
                                tags.setVisible(true);
                            }

                            @Override
                            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {

                            }

                            @Override
                            public void popupMenuCanceled(PopupMenuEvent e) {

                            }
                        } );
                c.setCellEditor(new DefaultCellEditor(tags));
*/
            }
            // maybe adding a button to pop up to select an argument?
        }
        // if not in adjudication, suppress SRC column to be invisible
        if (mMode != M_ADJUD) {
            table.getColumnModel().removeColumn(
                    table.getColumnModel().getColumn(SRC_COL));
        }
        return scrollPane;
    }

    /**
     * creates a table to store all extent tags in one place
     *
     * @return the GUI component containing the JTable for all extent tags
     */
    private JComponent makeAllTablePanel() {

        AllTableModel model = new AllTableModel();
        JTable table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        JScrollPane scrollPane = new JScrollPane(table);

        mElementTables.put(MaeStrings.ALL_TABLE_TAB_BACK_NAME, table);
        table.addMouseListener(new TableMouseAdapter(this));

        // since all extent tags have three common attribs,
        // use only those as columns of all_table
        model.addColumn(MaeStrings.SRC_COL_NAME);
        model.addColumn(MaeStrings.ID_COL_NAME);
        model.addColumn(MaeStrings.SPANS_COL_NAME);
        model.addColumn(MaeStrings.TEXT_COL_NAME);

        if (mMode != M_ADJUD) {
            table.getColumnModel().removeColumn(
                    table.getColumnModel().getColumn(SRC_COL));
        }

        return scrollPane;
    }

    /**
     * Removes all the tags from the table when a new DTD is loaded.
     */
    public void resetTablePanel() {
        mBottomTable.removeAll();
        List<Elem> elements = mTask.getAllTagTypes();
        // create a tan for all extents and place it at first
        mBottomTable.addTab(MaeStrings.ALL_TABLE_TAB_BACK_NAME, makeAllTablePanel());
        //create a tab for each element in the annotation task
        for (Elem element : elements) {
            String name = element.getName();
            mBottomTable.addTab(name, makeTablePanel(element));
            TabTitle newTab;
            if (mColorTable.containsKey(name)) {
                // extent tags are assigned their colors, and by default highlighted
                newTab = new TabTitle(this, name, mColorTable.get(name));
                mBottomTable.setTabComponentAt(
                        mBottomTable.indexOfTab(name), newTab);
                mActiveExts.add(name);
                newTab.setHighlighted(true);
            } else {
                // link tags are don't have colors, and by default not highlighted
                newTab = new TabTitle(this, name);
                mBottomTable.setTabComponentAt(
                        mBottomTable.indexOfTab(name), newTab);
                mActiveLinks.remove(name);
                newTab.setHighlighted(false);
            }
        }
        // set toggle button in all extents tab after creating all other tabs
        TabTitle allTab = new TabTitle(this, MaeStrings.ALL_TABLE_TAB_BACK_NAME);
        mBottomTable.setTabComponentAt(0, allTab);
        allTab.setHighlighted(true);
    }

    /**
     * Creates a context menu from selected text
     *
     * @return a pop-up menu for creating different tags, as well as information
     * about existing tags at the selected location
     */
    public JPopupMenu createTextContextMenu() {
        JPopupMenu jp = new JPopupMenu();

        // add menus for creating Ext tags,
        // only if text selected and not in arg_sel mode
        if (isTextSelected && mMode != M_ARG_SEL) {
            JMenu tagMenu = createTagMenu("Create an Extent tag with selected text", false);
            tagMenu.setMnemonic(MaeHotKeys.TAGMENU);

            jp.add(tagMenu);
        }
        // add common menus for NC and Link tag creation
        JMenu ncMenu = createNCMenu("Create a NC tag", false);
        ncMenu.setMnemonic(MaeHotKeys.NCMENU);
        jp.add(ncMenu);
        JMenu linkMenu = createLinkMenu("Create a Link tag without arguments", false);
        linkMenu.setMnemonic(MaeHotKeys.LINKMENU);
        jp.add(linkMenu);

        //get a hash collection of the element type and id- add info to
        //the action command for that menuItem
        //this is only for extent tags

        UndoSelectListener undoSelectListener = new UndoSelectListener(this);
        if (mMode == M_ARG_SEL || mMode == M_MULTI_SPAN) {
            JMenuItem undo = createMenuItem("Undo last selection", MaeHotKeys.UNDO,
                    "Undo", undoSelectListener);
            if (mSpans.size() < 1) {
                undo.setEnabled(false);
            }

            JMenuItem over = createMenuItem("Start Over", MaeHotKeys.STARTOVER,
                    "Over", undoSelectListener);
            if (mSpans.size() < 1) {
                over.setEnabled(false);
            }

            JMenuItem exit = createMenuItem(
                    "Exit Multi-span Mode", MaeHotKeys.NORMALMODE,
                    Integer.toString((M_NORMAL)), new ModeMenuListener(this));

            if (mMode == M_ARG_SEL) {
                String makeLink = "Create a Link tag with selected elements";
                JMenu makeLinkItem = new JMenu(makeLink);
                makeLinkItem.setMnemonic(MaeHotKeys.LINKARGMENU);
                int i = 0;
                for (String link : mTask.getLinkTagTypes()) {
                    JMenuItem linkItem;
                    MakeTagListener makeTagListener = new MakeTagListener(this);

                    if (i < 10) {
                        linkItem = createMenuItem(
                                link, MaeHotKeys.noneNums[i],
                                MaeStrings.ADD_LINK_WITH_ARGS_COMMAND + link,
                                makeTagListener);
                    } else {
                        linkItem = createMenuItem(
                                link, null,
                                MaeStrings.ADD_LINK_WITH_ARGS_COMMAND + link,
                                makeTagListener);

                    }
                    i++;
                    makeLinkItem.add(linkItem);
                }
                jp.add(makeLinkItem);
            }

            jp.addSeparator();
            jp.add(undo);
            jp.add(over);
            jp.add(exit);

        } else {
            MappedList<String, String> idHash = mTask.getTagsByTypesIn(mSpans);
            // if only item is retrieved, display directly
            if (idHash.isSizeOne()) {
                String elem = idHash.keyList().get(0);
                String id = ((ArrayList<String>) idHash.get(elem)).get(0);
                // remove a tag
                JMenuItem removeItem = createMenuItem(
                        String.format("Remove %s", id),
                        MaeHotKeys.DELETE,
                        elem + MaeStrings.SEP + id,
                        new RemoveExtentTagListener(this));
                // set a ext tag as an argument
                JMenu setArg = createSetAsArgMenu(String.format(
                        "Set %s as an argument of", id), elem, id);
                setArg.setMnemonic(MaeHotKeys.SETARGMENU);

                jp.addSeparator();
                jp.add(removeItem);
                jp.add(setArg);

            }
            // else create waterfall menu
            else if (idHash.size() > 0) {
                ArrayList<String> elems = idHash.keyList();
                for (String elem : elems) {
                    ArrayList<String> ids = (ArrayList<String>) idHash.get(elem);
                    for (String id : ids) {
                        jp.addSeparator();
                        String text = getTextByID(elem, id, false);
                        if (text.equals("")) {
                            text = "NC tag";
                        }
                        JMenu idItem = new JMenu(String.format(
                                "%s (%S)", id, text));

                        // menu items for removing
                        JMenuItem removeItem = createMenuItem(
                                "Remove", MaeHotKeys.DELETE,
                                elem + MaeStrings.SEP + id,
                                new RemoveExtentTagListener(this));

                        // menu items for adding tag as an arg
                        JMenu setArg = createSetAsArgMenu(String.format(
                                "Set %s as an argument of", id), elem, id);
                        setArg.setMnemonic(MaeHotKeys.SETARGMENU);

                        idItem.add(removeItem);
                        idItem.add(setArg);
                        jp.add(idItem);
                    }
                }
            }
        }
        return jp;
    }

    /**
     * Creates a waterfall menu to add current extent tag as an argument of a link tag
     *
     * @param menuTitle string for top menu item
     * @param argType   element name of current extent tag
     * @param argId     element id if current extent tag
     * @return a waterfall menu, goes down to 3rd level,
     * for each link type, for each argument type, for each link instance
     */
    private JMenu createSetAsArgMenu(
            String menuTitle, String argType, String argId) {
        JMenu menu = new JMenu(menuTitle);

        // waterfall menu top level - link names
        int j = -1;
        for (String linkType : mTask.getLinkTagTypes()) {

            j++;
            List<String> linkIds = mTask.getAllLinkTagsOfType(linkType);

            // check if a tag in each type of link element exists
            if (linkIds.size() == 0) {
                addGuideItem(menu, String.format("no %s links", linkType));
                continue;
            }
            // add a link type as a menu item, only when it has real tags
            JMenu linkTypeMenu;
            if (j < 10) {
                linkTypeMenu = new JMenu(String.format("%d %s", j + 1, linkType));
                linkTypeMenu.setMnemonic(MaeHotKeys.numKeys[j]);
            } else {
                linkTypeMenu = new JMenu(linkType);
            }

            // next level - actual relevant arguments
            int k = 0;
            for (String argName : mTask.getArgumentTypesOfLinkTagType(linkType)) {
                JMenu linkArgMenu;
                if (k < 10) {
                    linkArgMenu = new JMenu(String.format("%d %s", k + 1, argName));
                    linkArgMenu.setMnemonic(MaeHotKeys.numKeys[k]);
                } else {
                    linkArgMenu = new JMenu(argName);
                }
                k++;
                // copy names of all ids (list of ids will be recycled for all args)
                ArrayList<String> id2Add = new ArrayList<String>(linkIds);
                DefaultTableModel tableModel = (DefaultTableModel)
                        mElementTables.get(linkType).getModel();
                int rows = tableModel.getRowCount();
                TreeSet<Integer> argCols = getArgColIndices(linkType);
                int argCol = 0;

                // find which column to look for
                for (Integer i : argCols) {
                    if (tableModel.getColumnName(i).
                            equals(argName + MaeStrings.ARG_IDCOL_SUF)) {
                        argCol = i;
                    }
                }

                // final level - ids of each link
                // needs to move underspecified items to the top of the menu
                boolean prior = false;
                SetAsArgListener setAsArgListener = new SetAsArgListener(this);
                if (mUnderspecified.size() > 0) {
                    for (String unspecId : mUnderspecified) {
                        if (id2Add.contains(unspecId)) {
                            // find which row to look for,
                            // Note that it has to go backwards for efficiency
                            for (int i = rows - 1; i >= 0; i--) {
                                // check if id is matching first
                                // then check if argument is a dummy
                                if (tableModel.getValueAt(i, ID_COL).equals(unspecId) &&
                                        tableModel.getValueAt(i, argCol).equals("")) {
                                    // add a menu guidance
                                    if (!prior) {
                                        addGuideItem(linkArgMenu, "Underspecifed");
                                        prior = true;
                                    }
                                    // add ids as menu items
                                    JMenuItem unspecIdItem
                                            = createMenuItem(unspecId, null,
                                            linkType + MaeStrings.SEP +
                                                    unspecId + MaeStrings.SEP +
                                                    argName + MaeStrings.SEP +
                                                    argId + MaeStrings.SEP +
                                                    getTextByID(argType, argId, true),
                                            setAsArgListener);
                                    linkArgMenu.add(unspecIdItem);
                                    id2Add.remove(unspecId);
                                }
                            }
                        }
                    }
                    if (prior) {
                        linkArgMenu.addSeparator();
                    }
                }

                // then add the rest of the list as menu items
                for (String item : id2Add) {
                    JMenuItem idItem = createMenuItem(item, null,
                            linkType + MaeStrings.SEP +
                                    item + MaeStrings.SEP +
                                    argName + MaeStrings.SEP +
                                    argId + MaeStrings.SEP +
                                    getTextByID(argType, argId, true),
                            setAsArgListener);
                    linkArgMenu.add(idItem);
                }
                linkTypeMenu.add(linkArgMenu);
            }
            menu.add(linkTypeMenu);
        }
        return menu;
    }

    /**
     * Creates the menu with the option to remove selected table rows
     * TODO refactor to database package
     *
     * @return GUI menu
     */
    public JPopupMenu createTableContextMenu(MouseEvent event) {
        JPopupMenu jp = new JPopupMenu();

        // get the title of current tab
        String elemName
                = mBottomTable.getTitleAt(mBottomTable.getSelectedIndex());

        // get tab and count selected rows
        JTable table = mElementTables.get(elemName);
        int clickedRow = table.rowAtPoint(event.getPoint());
        int selected = table.getSelectedRowCount();

        // switch selection to clicked row only if one or zero row is selected before
        if (selected <= 1) {
            table.setRowSelectionInterval(clickedRow, clickedRow);
            selected = 1;
        }

        if (selected == 1) {
            // krim - case 1 always means clickedRow is the only selected row
            // thus getting the value from clickedRow is not that hard-coded logic
            String id = (String) table.getModel().getValueAt(clickedRow, ID_COL);

            JMenuItem removeItem = createMenuItem(
                    String.format("Remove %s", id), MaeHotKeys.DELETE,
                    id, new RemoveSelectedTableRows(this));
            jp.add(removeItem);

            // if selection was in all_tab, replace elemName
            if (elemName.equals(MaeStrings.ALL_TABLE_TAB_BACK_NAME)) {
                elemName = mTask.getTagTypeByTid(id);
            }
            if (mTask.getTagTypeByName(elemName) instanceof ElemExtent) {
                String target = String.format("%s (%s)",
                        id, getTextByID(elemName, id, false));
                JMenu setArg = createSetAsArgMenu(String.format(
                        "Set %s as an argument of", target), elemName, id);
                setArg.setMnemonic(MaeHotKeys.SETARGMENU);
                jp.add(setArg);
            }
        }
        // when two or more rows are selected
        else {

            // first get ids from selected rows
            int[] selectedViewRows = table.getSelectedRows();
            //convert the rows of the table view into the rows of the
            //table model so that the correct rows are deleted
            int[] selectedRows = new int[selectedViewRows.length];
            for (int i = 0; i < selectedRows.length; i++) {
                selectedRows[i] = table.convertRowIndexToModel(selectedViewRows[i]);
            }

            DefaultTableModel tableModel = (DefaultTableModel) table.getModel();

            String[] ids = new String[selected];
            for (int i = 0; i < selected; i++) {
                ids[i] = (String) tableModel.getValueAt(selectedRows[i], ID_COL);
            }
            // concat ids into a string, this will be used for remove item
            String idsString = "";
            for (String id : ids) {
                idsString += id + MaeStrings.SEP;
            }
            idsString = idsString.
                    substring(0, idsString.length() - MaeStrings.SEP.length());

            // then add "delete all" menu item
            JMenuItem removeItem = new JMenuItem(
                    String.format("Remove selected %d rows", selected));
            removeItem.setActionCommand(idsString);
            removeItem.setAccelerator(MaeHotKeys.DELETE);
            removeItem.addActionListener(new RemoveSelectedTableRows(this));
            jp.add(removeItem);

            // then if they are extent tags, add item for creating a link with them
            if (elemName.equals(MaeStrings.ALL_TABLE_TAB_BACK_NAME) ||
                    mTask.getTagTypeByName(elemName) instanceof ElemExtent) {
                // calling table context menu will reset text selection
                resetSpans();
                // retrieve ids of all selected tags
                for (String id : ids) {
                    if (elemName.equals(MaeStrings.ALL_TABLE_TAB_BACK_NAME)) {
                        mPossibleArgIds.add(id);
                    } else {
                        mPossibleArgIds.add(id);
                    }
                }
                String makeLink = "Create a Link tag with selected elements";
                JMenu makeLinkItem = new JMenu(makeLink);
                makeLinkItem.setMnemonic(MaeHotKeys.LINKARGMENU);
                int i = 0;
                for (String link : mTask.getLinkTagTypes()) {
                    MakeTagListener makeTagListener = new MakeTagListener(this);

                    JMenuItem linkItem;
                    if (i < 10) {
                        linkItem = createMenuItem(link,
                                MaeHotKeys.noneNums[i],
                                MaeStrings.ADD_LINK_WITH_ARGS_COMMAND + link,
                                makeTagListener);
                    } else {
                        linkItem = createMenuItem(link, null,
                                MaeStrings.ADD_LINK_WITH_ARGS_COMMAND + link,
                                makeTagListener);
                    }
                    i++;
                    makeLinkItem.add(linkItem);
                }
                jp.add(makeLinkItem);
            }
        }
        return jp;
    }

    /**
     * highlights the row in the table with the given ID
     *
     * @param elem name of the tag type being highlighted
     * @param id   id of the tag being highlighted
     */
    private void highlightTableRows(String elem, String id) {
        // first make highlight in elem's own tab
        JTable tab = mElementTables.get(elem);
        DefaultTableModel tableModel = (DefaultTableModel) tab.getModel();
        int rows = tableModel.getRowCount();
        for (int i = rows - 1; i >= 0; i--) {
            String value = (String) tableModel.getValueAt(i, ID_COL);
            if (value.equals(id)) {
                tab.addRowSelectionInterval(
                        tab.convertRowIndexToView(i), tab.convertRowIndexToView(i));
            }
        }
        // then make highlight in the all_tag tab
        // this only happens when coloring elem is activated
        if (mActiveExts.contains(elem)) {
            tab = mElementTables.get(MaeStrings.ALL_TABLE_TAB_BACK_NAME);
            tableModel = (DefaultTableModel) tab.getModel();
            rows = tableModel.getRowCount();
            for (int i = rows - 1; i >= 0; i--) {
                String value = (String) tableModel.getValueAt(i, ID_COL);
                if (value.equals(id)) {
                    tab.addRowSelectionInterval(
                            tab.convertRowIndexToView(i),
                            tab.convertRowIndexToView(i));
                }
            }
        }
    }

    /**
     * Remove all highlights from table rows
     */
    public void clearTableSelections() {
        for (Enumeration<String> tables = mElementTables.keys(); tables.hasMoreElements(); ) {
            JTable tab = mElementTables.get(tables.nextElement());
            DefaultTableModel tableModel = (DefaultTableModel) tab.getModel();
            int rows = tableModel.getRowCount();
            if (rows > 0)
                tab.removeRowSelectionInterval(0, rows - 1);
        }
    }

    /**
     * given a type of link tag, return all indices of its arguments columns in
     * bottom table
     *
     * @param linkType target link type
     * @return indices of argument columns
     */
    public TreeSet<Integer> getArgColIndices(String linkType) {

        JTable tab = mElementTables.get(linkType);
        List<String> argNames = mTask.getArgumentTypesOfLinkTagType(linkType);
        TreeSet<Integer> argColumns = new TreeSet<Integer>();
        for (int i = 0; i < tab.getModel().getColumnCount(); i++) {
            for (String argName : argNames) {
                if (tab.getModel().getColumnName(i).
                        equals(argName + MaeStrings.ARG_IDCOL_SUF)) {
                    argColumns.add(i);
                }
            }
        }
        return argColumns;
    }

    /**
     * Displays the warning for saving your work before opening a new file or DTD.
     */
    public void showSaveWarning() {
        // TODO re-write wording
        JOptionPane save = new JOptionPane();
        save.setLocation(100, 100);
        String text = ("Warning! Opening a new file or DTD will \n" +
                "delete any unsaved data.  " +
                "\nPlease save your data before continuing");
        JOptionPane.showMessageDialog(mMainFrame, text);
//        JOptionPane.showMessageDialog(this.getParent(), text);
    }

    /**
     * Shows message warning that deleting an extent will also delete any links the
     * extent is an anchor in.
     * Currently is shows whether the extent is in a link or not.
     *
     * @return boolean indicating the user accepted the warning or canceled the
     * action.
     */
    public boolean showDeleteWarning() {
        //JOptionPane delete = new JOptionPane();
        String text = ("Deleting extent tag(s) will also delete \n" +
                "any links that use these extents.  Would you like to continue?");

        int message = JOptionPane.showConfirmDialog(mMainFrame,
//        int message = JOptionPane.showConfirmDialog(this.getParent(),
                text, "Warning!", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        return message == 0;
    }

    /**
     * Shows information about MAE
     */
    public void showAboutDialog() {
        JOptionPane about = new JOptionPane();
        about.setLocation(100, 100);
        about.setAlignmentX(Component.CENTER_ALIGNMENT);
        about.setAlignmentY(Component.CENTER_ALIGNMENT);
        about.setMessage(String.format("MAE \n Multi-purpose Annotation Editor \n" +
                        "Version %s\n\n" +
                        "Developed in Lab for Linguistics and Computation, " +
                        "Brandeis University 2010-%s.\n\n" +
                        "MAE is a free software. " +
                        "\nThis distribution of MAE (the software and the source code) \n" +
                        " is covered under the GNU General Public License version 3.\n" +
                        "http://www.gnu.org/licenses/"
                , MaeStrings.VERSION, MaeStrings.CUR_YEAR));
        JDialog dialog = about.createDialog(mMainFrame, "About MAE");
//        JDialog dialog = about.createDialog(this.getParent(), "About MAE");
        dialog.setVisible(true);
    }

    /**
     * Creates a drop-down comboBox for the table from the AttList attribute
     *
     * @param att a list-type attribute
     * @return comboBox with attribute options
     */
    private JComboBox makeValidValuesComboBox(AttList att) {
        //makes comboBox from List-type attribute
        JComboBox<String> options = new JComboBox<String>();
        options.addItem("");
        for (int j = 0; j < att.getVaildValues().size(); j++) {
            options.addItem(att.getVaildValues().get(j));
        }
        return options;
    }

    /**
     * assigns colors to the elements in the DTD
     */
    public void assignColors() {
        List<String> elements = mTask.getExtentTagTypes();
        for (int i = 0; i < elements.size(); i++) {
            int l = mColors.size();
            int k = i;
            if (i >= l) {
                k = i % l;
            }
            mColorTable.put(elements.get(i), mColors.get(k));
        }
    }

    /**
     * Creates the File menu for the top bar
     *
     * @return JMenu with all available options
     */
    private JMenu createFileMenu(String menuTitle) {
        JMenu menu = new JMenu(menuTitle);
        FileMenuListener fileMenuListener = new FileMenuListener(this);

        JMenuItem loadDTD = createMenuItem("Load DTD", MaeHotKeys.NEWTASK,
                "Load DTD", fileMenuListener);

        JMenuItem loadFile = createMenuItem("Load File", MaeHotKeys.OPENFILE,
                "Load File", fileMenuListener);
        if (!mTask.hasDTD()) {
            loadFile.setEnabled(false);
        } else {
            loadFile.setEnabled(true);
        }
        if (mMode == M_ADJUD) {
            loadFile.setText("Load Gold Standard File");
        }

        JMenuItem addFile = createMenuItem("Add Annotation File", MaeHotKeys.ADDFILE,
                "Add File", fileMenuListener);
        if (!mTask.hasDTD() || mMode != M_ADJUD) {
            addFile.setVisible(false);
        }

        JMenuItem saveFileRTF = createMenuItem("Create RTF", MaeHotKeys.SAVERTF,
                "Save RTF", fileMenuListener);
        if (!isFileOpen || mMode == M_ADJUD) {
            saveFileRTF.setEnabled(false);
        } else {
            saveFileRTF.setEnabled(true);
        }

        JMenuItem saveFileXML = createMenuItem("Save File As XML", MaeHotKeys.SAVEXML,
                "Save XML", fileMenuListener);
        if (!isFileOpen) {
            saveFileXML.setEnabled(false);
        } else {
            saveFileXML.setEnabled(true);
        }
        if (mMode == M_ADJUD) {
            saveFileXML.setText("Save Gold Standard File as XML");
        }

        JMenuItem closeFile = createMenuItem(
                "Close Annotation Files", MaeHotKeys.CLOSEFILE,
                "Close File", fileMenuListener);
        if (!isFileOpen) {
            closeFile.setEnabled(false);
        } else {
            closeFile.setEnabled(true);
        }

        menu.add(loadDTD);
        menu.add(loadFile);
        menu.add(addFile);
        menu.addSeparator();
        menu.add(saveFileRTF);
        menu.addSeparator();
        menu.add(saveFileXML);
        menu.addSeparator();
        menu.add(closeFile);
        return menu;
    }

    /**
     * Creates the Preference menu for the top bar
     *
     * @return JMenu with all preferable boolean options
     */
    private JMenu createPrefMenu(String menuTitle) {
        JMenu menu = new JMenu(menuTitle);
        final JCheckBoxMenuItem exitOnCreation = new JCheckBoxMenuItem(
                "Return to normal mode after creating a tag", true);
        exitOnCreation.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                mOptionExitOnCreation = exitOnCreation.getState();
            }
        });
        menu.add(exitOnCreation);

        JMenuItem fileSuffix = createMenuItem(
                "File name suffix: " + mFilenameSuffix,
                null, "", new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
//                        JFrame frame = new JFrame("Enter suffix");
                        mFilenameSuffix = JOptionPane.showInputDialog(
                                "Enter a suffix to filename you save",
                                mFilenameSuffix);
                        updateMenus();
                    }
                });
        menu.add(fileSuffix);

        return menu;
    }

    /**
     * Creates the Display menu for the top bar
     *
     * @return JMenu with all available display options
     */
    private JMenu createDisplayMenu(String menuTitle) {
        JMenu menu = new JMenu(menuTitle);
        FontSizeMenuListener fsmListener = new FontSizeMenuListener(this);
        JMenuItem increaseFont = createMenuItem("Font Size ++", MaeHotKeys.FONTBIG,
                "Font++", fsmListener);
        JMenuItem decreaseFont = createMenuItem("Font Size --", MaeHotKeys.FONTSMALL,
                "Font--", fsmListener);
        menu.add(increaseFont);
        menu.add(decreaseFont);

        return menu;
    }

    /**
     * Creates the menu for creating link tags
     *
     * @return JMenu for creating link tags
     */
    private JMenu createLinkMenu(String menuTitle, boolean mainMenu) {
        JMenu menu = new JMenu(menuTitle);

        if (mTask.hasDTD()) {
            ArrayList<String> linkNames = mTask.getLinkTagTypes();

            if (linkNames.size() == 0) {
                addGuideItem(menu, "no link tags defined");
            } else {
                int i = 0;
                for (String linkName : linkNames) {
                    JMenuItem menuItem;
                    if (mainMenu) {
                        menuItem = new JMenuItem(
                                String.format("(%d) %s", i + 1, linkName));
                    } else {
                        menuItem = new JMenuItem(linkName);
                    }
                    menuItem.addActionListener(new MakeTagListener(this));
                    menuItem.setActionCommand(
                            MaeStrings.ADD_LINK_COMMAND + linkName);
                    if (i < 10) {
                        if (mainMenu) {
                            menuItem.setMnemonic(MaeHotKeys.numKeys[i]);

                        } else {
                            menuItem.setAccelerator(MaeHotKeys.noneNums[i]);
                        }
                    }
                    i++;
                    menu.add(menuItem);
                }
            }
        } else {
            addGuideItem(menu, "no DTD is loaded");
        }

        return menu;
    }

    /**
     * Creates the menu for creating extent tags
     *
     * @return JMenu for creating extent tags
     */
    private JMenu createTagMenu(String menuTitle, boolean mainMenu) {
        JMenu menu = new JMenu(menuTitle);
        if (!isTextSelected) {
            JMenuItem noText = new JMenuItem(MaeStrings.MENU_NOTEXT);
            noText.setEnabled(false);
            menu.add(noText);
            return menu;
        }

        int i = 0;
        for (String elemName : mTask.getExtentTagTypes()) {
            JMenuItem menuItem;
            if (mainMenu) {
                menuItem = new JMenuItem(
                        String.format("(%d) %s", i + 1, elemName));


            } else {
                menuItem = new JMenuItem(elemName);
            }
            menuItem.addActionListener(new MakeTagListener(this));
            if (i < 10) {
                if (mainMenu) {
                    menuItem.setMnemonic(MaeHotKeys.numKeys[i]);

                } else {
                    menuItem.setAccelerator(MaeHotKeys.noneNums[i]);
                }
            }
            i++;
            menu.add(menuItem);
        }
        return menu;
    }

    /**
     * Creates the menu with non-consuming tag options
     *
     * @return JMenu for creating non-consuming tags
     */
    private JMenu createNCMenu(String menuTitle, boolean mainMenu) {
        JMenu menu = new JMenu(menuTitle);

        if (mTask.hasDTD()) {
            List<Elem> ncElems = mTask.getNonConsumingTagTypes();

            if (ncElems.size() == 0) {
                addGuideItem(menu, "no NC tag defined");
            } else {
                int i = 0;
                for (Elem ncElem : ncElems) {
                    JMenuItem menuItem;
                    if (mainMenu) {
                        menuItem = new JMenuItem(
                                String.format("(%d) %s", i + 1, ncElem.getName()));
                    } else {
                        menuItem = new JMenuItem(ncElem.getName());
                    }
                    menuItem.addActionListener(new MakeTagListener(this));
                    menuItem.setActionCommand(
                            MaeStrings.ADD_NC_COMMAND + ncElem.getName());
                    if (i < 10) {
                        if (mainMenu) {
                            menuItem.setMnemonic(MaeHotKeys.numKeys[i]);

                        } else {
                            menuItem.setAccelerator(MaeHotKeys.noneNums[i]);
                        }
                    }
                    i++;
                    menu.add(menuItem);
                }
            }
        } else {
            addGuideItem(menu, "no DTD is loaded");
        }

        return menu;
    }

    /**
     * Creates the Help menu for MAE
     *
     * @return JMenu Help for the top bar
     */
    private JMenu createHelpMenu(String menuTitle) {
        JMenu menu = new JMenu(menuTitle);
        HelpMenuListener helpMenuListener = new HelpMenuListener(this);

        JMenuItem about = createMenuItem("About MAE", MaeHotKeys.ABOUT,
                "about", helpMenuListener);
        JMenuItem github = createMenuItem(
                "Visit project website(Github)", MaeHotKeys.WEB,
                "web", helpMenuListener);
        menu.add(about);
        menu.addSeparator();
        menu.add(github);

        return menu;
    }

    /**
     * krim: Creates the menu for special input modes
     *
     * @return JMenu Mode for the menu bar
     */
    private JMenu createModeMenu(String menuTitle) {
        JMenu menu = new JMenu(menuTitle);

        ModeMenuListener modemenuListener = new ModeMenuListener(this);

        JMenuItem multiSpan = createMenuItem(
                "Multi-span Mode", MaeHotKeys.MSPANMODE,
                Integer.toString(M_MULTI_SPAN), modemenuListener);
        JMenuItem multiArgs = createMenuItem(
                "Argument selection Mode", MaeHotKeys.ARGSMODE,
                Integer.toString(M_ARG_SEL), modemenuListener);
        JMenuItem adjudication = createMenuItem(
                "Adjudication Mode", MaeHotKeys.ADJUDMODE,
                Integer.toString(M_ADJUD), modemenuListener);
        adjudication.setEnabled(false);
        JMenuItem exitMode = createMenuItem(
                "Exit to Normal Mode", MaeHotKeys.NORMALMODE,
                Integer.toString(M_NORMAL), modemenuListener);
        if (mMode != M_NORMAL) {
            exitMode.setEnabled(true);
        } else {
            exitMode.setEnabled(false);
        }

        menu.add(multiSpan);
        menu.add(multiArgs);
        menu.add(adjudication);
        menu.addSeparator();
        menu.add(exitMode);

        return menu;
    }

    /**
     * creates a grayed-out menu item that informs user of something
     *
     * @param menu  parent menu to which 'guide' goes in
     * @param guide something to tell to user
     */
    private void addGuideItem(JMenu menu, String guide) {
        JMenuItem item = new JMenuItem(guide);
        item.setEnabled(false);
        menu.add(item);
    }

    /**
     * Refreshes the GUI menus when a new DTD or file is loaded
     */
    public void updateMenus() {
        mMenuBar.removeAll();

        JMenu fileMenu = createFileMenu("File");
        fileMenu.setMnemonic(MaeHotKeys.FILEMENU);
        mMenuBar.add(fileMenu);

        // some menus are used only after a file is loaded
        if (isFileOpen) {
            JMenu displayMenu = createDisplayMenu("Display");
            displayMenu.setMnemonic(MaeHotKeys.DPMENU);
            mMenuBar.add(displayMenu);
            JMenu linkMenu = createLinkMenu("Link Tags", true);
            linkMenu.setMnemonic(MaeHotKeys.LINKMENU);
            mMenuBar.add(linkMenu);
            JMenu ncMenu = createNCMenu("NC Tags", true);
            ncMenu.setMnemonic(MaeHotKeys.NCMENU);
            mMenuBar.add(ncMenu);
            JMenu modeMenu = createModeMenu("Modes");
            modeMenu.setMnemonic(MaeHotKeys.MODEMENU);
            mMenuBar.add(modeMenu);
        }
        JMenu prefMenu = createPrefMenu("Preference");
        prefMenu.setMnemonic(MaeHotKeys.PREFMENU);
        mMenuBar.add(prefMenu);

        JMenu helpMenu = createHelpMenu("Help");
        helpMenu.setMnemonic(MaeHotKeys.HELPMENU);
        mMenuBar.add(helpMenu);

        mMenuBar.updateUI();
    }

    JMenuItem createMenuItem(String menuTitle, KeyStroke hotkey,
                             String actionCommand, ActionListener listener) {
        JMenuItem item = new JMenuItem(menuTitle);
        item.setActionCommand(actionCommand);
        item.addActionListener(listener);
        if (hotkey != null) {
            item.setAccelerator(hotkey);
        }

        return item;
    }

    /**
     * Highlight given spans with given highlighter and painter(color)
     * TODO 151216 make spans to int[]
     *
     * @param hl      - Highlighter OBJ from text panel
     * @param spans   - desired text spans to be highlighted
     * @param painter - highlighter OBJ with color
     */
    public void highlightTextSpans(Highlighter hl,
                                   ArrayList<int[]> spans,
                                   Highlighter.HighlightPainter painter) {

        for (int[] span : spans) {
            int start = span[0], end = span[1];
            // do highlighting only for real spans; (-1, -1) is a dummy for NC tags
            if (start != -1 && end != -1) {
                try {
                    hl.addHighlight(start, end, painter);
                    mTextPane.scrollRectToVisible(mTextPane.modelToView(start));
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * make a list all extent elements in mSpan
     */
    public void getPotentialArgsInSelectedOrder() {
        mPossibleArgIds.clear();

        int i = 0;
        for (int[] span : mSpans) {
            MappedList<String, String> elems
                    = mTask.getTagsByTypesBetween(span[0], span[1]);
            boolean first = true;
            for (String elemName : elems.keyList()) {
                for (String elemId : elems.get(elemName)) {
                    if (!mPossibleArgIds.contains(elemId)) {
                        if (first) {
                            mPossibleArgIds.add(i, elemId);
                            first = false;
                            i++;
                        } else {
                            mPossibleArgIds.add(elemId);
                        }
                    }
                }
            }
        }
    }

    /**
     * Delayed update on the status bar
     */
    public void delayedUpdateStatusBar(long miliseconds) {
        new Timer().schedule(new TimedUpdateStatusBar(), miliseconds);
    }

    /**
     * Updates the status bar
     */
    public void updateStatusBar() {
        if (!mTask.hasDTD()) {
            mStatusBar.setText(MaeStrings.SB_NODTD);
        } else if (!isFileOpen) {
            mStatusBar.setText(MaeStrings.SB_NOFILE);
        } else {
            switch (mMode) {
                case M_NORMAL:
                    if (isSpansEmpty()) {
                        mStatusBar.setText(MaeStrings.SB_NOTEXT);
                    } else {
                        mStatusBar.setText(MaeStrings.SB_TEXT
                                + SpanHandler.convertPairsToString(this.mSpans));
                    }
                    break;
                case M_MULTI_SPAN:
                    if (isSpansEmpty()) {
                        mStatusBar.setText(
                                MaeStrings.SB_MSPAN_NOTEXT);
                    } else {
                        mStatusBar.setText(MaeStrings.SB_MSPAN_TEXT +
                                SpanHandler.convertPairsToString(this.mSpans));
                    }
                    break;
                case M_ARG_SEL:
                    if (isSpansEmpty()) {
                        mStatusBar.setText(MaeStrings.SB_MARGS_NOTAG);
                    } else {
                        ArrayList<String> argList = new ArrayList<>();
                        for (String id : mPossibleArgIds) {
                            argList.add(String.format("%s - %s"
                                    , id
                                    , getTextByID(
                                            mTask.getTagTypeByTid(id), id, false)));
                        }
                        mStatusBar.setText(String.format(
                                MaeStrings.SB_MARGS_TAG
                                , mPossibleArgIds.size(), argList.toString()));
                    }
                    break;
            }
        }
    }

    /**
     * add asterisk to windows title when file is changed
     */
    public void updateTitle() {
        String title = MaeStrings.TITLE_PREFIX + " - ";
        if (mWorkingFileName.equals("")) {
            title = title + "no file open";
        } else {
            title = title + mWorkingFileName;
        }
        if (isTaskChanged) {
            title += "*";
        }
        mMainFrame.setTitle(title);
    }

    /**
     * Sets MAE mode to Normal
     */
    public void returnToNormalMode(boolean statusBarAlert) {

        if (mMode != M_NORMAL && statusBarAlert) {
            mStatusBar.setText(MaeStrings.SB_NORM_MODE_NOTI);
            delayedUpdateStatusBar(3000);
        }
        mMode = M_NORMAL;
    }

    /**
     * check if anything is selected in text pane
     */
    public Boolean isSpansEmpty() {
        return this.mSpans.size() == 0 || this.mSpans.get(0)[0] == -1;
    }

    /**
     * Resets the selected spans to default non-selection (-1~-1)
     */
    public void resetSpans() {
        isTextSelected = false;

        mSpans.clear();
        mPrevSpans.clear();
        mLastSelection.clear();
        mSpans.add(new int[]{-1, -1});
        Highlighter hl = mTextPane.getHighlighter();
        hl.removeAllHighlights();

        mPossibleArgIds.clear();
    }

    /**
     * Creates the GUI
     */
    public static void createAndShowGUI() {
        JFrame.setDefaultLookAndFeelDecorated(true);

        //Create and set up the window.
        JFrame mainFrame = new JFrame(MaeStrings.TITLE_PREFIX);
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        MaeMainUI mainUI = new MaeMainUI();
//        JComponent newContentPane = mainUI;
//        newContentPane.setOpaque(true); //content panes must be opaque
        mainFrame.setContentPane(mainUI);
        mainUI.setMainFrame(mainFrame);

        //Display the window.
        mainFrame.pack();
        mainFrame.setSize(900, 500);
        mainFrame.setVisible(true);

    }
}

