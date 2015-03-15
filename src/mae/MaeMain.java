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

package mae;


import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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

public class MaeMain extends JPanel {

    private static final long serialVersionUID = 9404268L;

    private Hashtable<String, Color> mColorTable;
    private HashSet<String> mActiveLinks;
    private HashSet<String> mActiveExts;

    //Here is where to change the colors that get assigned to tags
    // these are for text colors
    private Color mRed = new Color(255, 0, 0);
    private Color mLightBlue = new Color(11, 162, 188);
    private Color mOrange = new Color(234, 160, 0);
    private Color mDarkGreen = new Color(12, 153, 72);
    private Color mMagenta = new Color(255, 0, 255);
    private Color mDarkBlue = new Color(42, 92, 140);
    private Color mYellow = new Color(255, 255, 0);
    private Color mPurple = new Color(150, 20, 120);
    private Color mGray = new Color(200, 200, 200);
    private Color mViolet = new Color(102, 75, 153);
    private Color mGold = new Color(207, 181, 59);
    private Color mBlue = new Color(0, 0, 255);
    private Color mDarkOrange = new Color(153, 102, 0);

    private Color[] mColors = {
            mRed, mLightBlue, mOrange, mDarkGreen, mMagenta, mDarkBlue,
            mYellow, mPurple, mGray, mViolet, mGold, mBlue, mDarkOrange};

    // thses are for highlighter colors
    private Color mLightOrange = new Color(255, 204, 51);
    private Color mGreen = Color.green;
    private Color mPink = Color.pink;
    private Color mCyan = Color.cyan;
    private Color mLightGray = Color.lightGray;

    private TextHighlightPainter mOrangeHL = new TextHighlightPainter(mLightOrange);
    private TextHighlightPainter mGreenHL = new TextHighlightPainter(mGreen);
    private TextHighlightPainter mPinkHL = new TextHighlightPainter(mPink);
    private TextHighlightPainter mCyanHL = new TextHighlightPainter(mCyan);
    private TextHighlightPainter mGrayHL = new TextHighlightPainter(mLightGray);
    private Highlighter.HighlightPainter mDefHL = DefaultHighlighter.DefaultPainter;

    // default color is excluded from the list; it's for indicating selection
    private TextHighlightPainter[] mHighlighters = {
            mOrangeHL, mGreenHL, mPinkHL, mCyanHL, mGrayHL};

    //some booleans that help keep track of the status of the annotation
    private boolean isFileOpen;
    private boolean isTaskChanged;
    private boolean isTextSelected;

    // krim: additional booleans to keep track of annotation mode
    private final int M_NORMAL = 0;
    private final int M_MULTI_SPAN = 1;
    private final int M_ARG_SEL = 2;
    private int mMode;


    // krim: instead of using 2 integers, start & end, now we use a set of tuples
    private ArrayList<int[]> mSpans;

    // variables for link creation
    private LinkedList<String> mUnderspecified;
    private ArrayList<String> mPossibleArgIds;
    private String mFileFullName;
    private String mFileName;
    private String mXmlName;
    // krim: column number of some fixed attributes
    // this might be hard-coding, but MAE always create an extent element 
    // with id, spans, text as the first three attribs of it
    private int ID_COL = 0;
    private int SPANS_COL = 1;
    private int TEXT_COL = 2;

    //GUI components
    private static JFrame mMainFrame;
    private JFrame mLinkPopupFrame;
    private JScrollPane mScrollPane;
    private Hashtable<String, JTable> mElementTables;
    private JTabbedPane mBottomTable;
    private JTextPane mTextPane;
    private JPanel mTopPanel;

    private JMenuBar mMenuBar;
    private JLabel mStatusBar;

    private JPopupMenu mTextPopup;
    private JPopupMenu mTablePopup;

    private JFileChooser mLoadFC;
    private JFileChooser mSaveFC;

    //the helper function for talking to the database
    private static AnnotationTask mTask;

    public MaeMain() {
        super(new BorderLayout());

        mTask = new AnnotationTask();

        isFileOpen = false;
        isTaskChanged = false;
        isTextSelected = false;

        mUnderspecified = new LinkedList<String>();
        mPossibleArgIds = new ArrayList<String>();

        mFileFullName = "";
        mFileName = "";
        mXmlName = "";

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
        mLoadFC.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
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
        mTextPane.addCaretListener(new MaeCaretListener());
        mTextPane.addMouseListener(new TextMouseAdapter());
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

        resetSpans();


    }

    // ***********************
    // Section: classes and listeners

    /**
     * Allows new highlighters for the JTextPane
     */
    private class TextHighlightPainter extends DefaultHighlighter.DefaultHighlightPainter {
        private TextHighlightPainter(Color color) {
            super(color);
        }
    }

    /**
     * Timer Task for timed messages in the status bar
     */
    private class TimedUpdateStatusBar extends TimerTask {
        @Override
        public void run() {
            updateStatusBar();
        }
    }

    /**
     * Listener for the File menu; determines what action to take for loading/saving
     * documents.
     */
    private class FileMenuListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            int returnVal;
            String command = actionEvent.getActionCommand();

            if (command.equals("Load DTD")) {
                if (isFileOpen && isTaskChanged) {
                    showSaveWarning();
                }
                returnVal = mLoadFC.showOpenDialog(MaeMain.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = mLoadFC.getSelectedFile();
                    try {
                        mTextPane.setStyledDocument(new DefaultStyledDocument());
                        DTDLoader dtdl = new DTDLoader(file);
                        mTask.resetDb();
                        DTD d = dtdl.getDTD();
                        mTask.setDtd(d);
                        mActiveLinks.clear();
                        mActiveExts.clear();
                        assignColors();
                        resetTabPane();

                        // refresh interfaces
                        updateMenus();
                        resetSpans();
                        returnToNormalMode();
                        mStatusBar.setText("DTD load succeed! Click anywhere to continue.");

                        if (mTask.getElements().size() > 20) {
                            mBottomTable.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
                        } else {
                            mBottomTable.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
                        }

                        isFileOpen = false;
                    } catch (Exception ex) {
                        System.err.println("Error loading DTD");
                        ex.printStackTrace();

                        // print out the error message on the status bar
                        mStatusBar.setText("Error loading DTD: " + ex.toString());
                    }
                }

            } else if (command.equals("Load File")) {
                if (isFileOpen && isTaskChanged) {
                    showSaveWarning();
                }
                returnVal = mLoadFC.showOpenDialog(MaeMain.this);
                boolean succeed = true;
                String status = "";
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = mLoadFC.getSelectedFile();
                    mFileFullName = file.getName();
                    int endName = mFileFullName.lastIndexOf(".");
                    mFileName = mFileFullName.substring(0, endName);
                    mXmlName = mFileName + ".xml";
                    try {
                        updateTitle();
                        isFileOpen = true;
                        mTask.resetDb();
                        mTask.resetIdTracker();

                        // refresh interfaces
                        resetTabPane();
                        updateMenus();
                        resetSpans();
                        returnToNormalMode();

                        mTextPane.setStyledDocument(new DefaultStyledDocument());
                        mTextPane.setContentType("text/plain; charset=UTF-8");
                        mMainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                        if (FileOperations.hasTags(file)) {
                            XMLFileLoader xfl = new XMLFileLoader(file);
                            StyledDocument d = mTextPane.getStyledDocument();
                            Style def = StyleContext.getDefaultStyleContext()
                                    .getStyle(StyleContext.DEFAULT_STYLE);
                            Style regular = d.addStyle("regular", def);
                            d.insertString(0, xfl.getTextChars(), regular);
                            // newTags is a hash from tagType to attib list
                            // each attrib is stored in a has from att name to value
                            HashCollection<String, Hashtable<String, String>> newTags
                                    = xfl.getTagHash();
                            if (newTags.size() > 0) {
                                processTagHash(newTags);
                            }
                        } else {  // that is, if it's only a text file
                            StyledDocument d = mTextPane.getStyledDocument();
                            mTextPane.setStyledDocument(FileOperations.setText(file, d));
                        }
                        mTextPane.requestFocus(true);
                        mTextPane.getCaret().setDot(0);
                        mTextPane.getCaret().moveDot(1);
                    } catch (Exception ex) {
                        isFileOpen = false;
                        ex.printStackTrace();
                        succeed = false;
                        status = "Error loading file";
                    }
                }
                mMainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                mTextPane.setCaretPosition(0);
                // refresh status bar after all caret events
                if (succeed) {
                    status = "File load succeed! Click anywhere to continue.";
                }
                mStatusBar.setText(status);

            } else if (command.equals("Save RTF")) {
                String rtfName = mFileName + ".rtf";
                mSaveFC.setSelectedFile(new File(rtfName));
                returnVal = mSaveFC.showSaveDialog(MaeMain.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = mSaveFC.getSelectedFile();
                    isTaskChanged = false;
                    try {
                        FileOperations.saveRTF(file, mTextPane);
                        mStatusBar.setText("Save Complete :" + rtfName);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        mStatusBar.setText("Error saving RTF file");
                    }
                }

            } else if (command.equals("Save XML")) {
                mSaveFC.setSelectedFile(new File(mXmlName));
                returnVal = mSaveFC.showSaveDialog(MaeMain.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = mSaveFC.getSelectedFile();
                    isTaskChanged = false;
                    mFileFullName = file.getName();
                    try {
                        FileOperations.saveXML(file,
                                mTextPane,
                                mElementTables,
                                mTask.getElements(),
                                mTask.getDTDName());
                        updateTitle();
                        mXmlName = mFileFullName;
                        mStatusBar.setText("Save Complete :" + mXmlName);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        mStatusBar.setText("Error saving XML file");
                    }
                }
            }
            // reset status bar after 3 secs
            new Timer().schedule(new TimedUpdateStatusBar(), 3000);
        }
    }

    /**
     * Listens for the request from the Help Menu
     */
    private class HelpMenuListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            String command = actionEvent.getActionCommand();
            if (command.equals("about")) {
                showAboutDialog();
            } else if (command.equals("web")) {
                if (Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().browse(
                                new URI(MaeStrings.PROJECT_WEBPAGE));
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Class that changes the size of the text from the top menu
     */
    private class FontSizeMenuListener implements ActionListener {
        public void actionPerformed(ActionEvent actionEvent) {
            String command = actionEvent.getActionCommand();
            if (command.equals("Font++")) {
                Font font = mTextPane.getFont();
                Font font2 = new Font(font.getName(), font.getStyle(), font.getSize() + 1);
                mTextPane.setFont(font2);
            } else if (command.equals("Font--")) {
                Font font = mTextPane.getFont();
                Font font2 = new Font(font.getName(), font.getStyle(), font.getSize() - 1);
                mTextPane.setFont(font2);
            }
        }
    }

    /**
     * AnnTableModel creates a TableModel that allows the ID column to be
     * uneditable.  This helps prevent user-created database conflicts by ensuring
     * the IDs being generated will not be changed, and makes it so that users can
     * double-click on the ID in order to see where that tag appears in the text.
     */
    private class MaeTableModel extends DefaultTableModel {
        static final long serialVersionUID = 552012L;

        @Override
        public boolean isCellEditable(int row, int col) {
            return col != 0;
        }
    }

    /**
     * ALlTableModel creates a TableModel that is not editable at all This is only
     * used to create the all extents tab
     */
    private class AllTableModel extends DefaultTableModel {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    }

    /**
     * Called when the user selects the option to delete the highlighted rows from
     * the table in view.  Rows are removed both from the database and the table.
     */
    private class RemoveSelectedTableRows implements ActionListener {
        public void actionPerformed(ActionEvent actionEvent) {
            if (showDeleteWarning()) {
                // command is concatenated ids from selected rows, so first split it
                String[] ids = actionEvent.getActionCommand().split(MaeStrings.SEP);
                for (String id : ids) {
                    // load corresponding table and its back-end model
                    String elemName = mTask.getElemNameById(id);
                    Elem elem = mTask.getElemByName(elemName);
                    JTable table = mElementTables.get(elemName);
                    DefaultTableModel tableModel
                            = (DefaultTableModel) table.getModel();

                    // search in the table model for matching id, remove that row
                    for (int i = 0; i < tableModel.getRowCount(); i++) {
                        if (tableModel.getValueAt(i, 0).equals(id)) {
                            // if removing an extent tag, re-assign highlighting
                            if (elem instanceof ElemExtent) {
                                mTask.removeExtentByID(id);
                                assignTextColor(parseSpansString(
                                        (String) tableModel.getValueAt(i, 1)));
                                //remove links that use the tag being removed
                                HashCollection<String, String> links
                                        = mTask.getLinksByExtentID(elemName, id);
                                removeLinkTableRows(links);
                                // also remove item from all extents tab
                                removeAllTableRow(id);
                            } else {
                                mTask.removeLinkByID(id);
                            }
                            tableModel.removeRow(i);
                            break;
                        }

                    }
                }
            }
            isTaskChanged = true;
            updateTitle();
        }
    }

    /**
     * TabTitle is a renderer class to render tabs in the bottom table with an
     * element name and a highlight-toggling checkbox.
     */
    private class TabTitle extends JPanel {
        private String elemName;
        private Color elemColor;
        private JCheckBox toggle;
        private JLabel title;

        /**
         * constructor for a link element tab note that this constructor accepts
         * only name since a link tag doesn't have a color assigned to highlight
         *
         * @param elemName name of the element
         */
        TabTitle(String elemName) {
            this.elemName = elemName;
            this.elemColor = Color.white;
            this.toggle = new JCheckBox();
            this.init();
        }

        /**
         * constructor for a extent element tab
         *
         * @param elemName  name of the element
         * @param elemColor color assigned to highlight the element
         */
        TabTitle(String elemName, Color elemColor) {
            this.elemName = elemName;
            this.elemColor = elemColor;
            Icon unselected = new BorderRect(this.elemColor, 13);
            Icon selected = new ColorRect(this.elemColor, 13);
            this.toggle = new JCheckBox(unselected);
            this.toggle.setSelectedIcon(selected);
            this.init();
        }

        /**
         * common constructor
         */
        private void init() {
            // set layout and transparency
            this.setLayout(new GridBagLayout());
            this.setOpaque(false);

            // make components to be set on title and place them
            if (this.elemName.equals(MaeStrings.ALL_TABLE_BACK_NAME)) {
                this.title = new JLabel(MaeStrings.ALL_TABLE_FRONT_NAME);
            } else {
                this.title = new JLabel(this.elemName);
            }
            this.toggle.addItemListener(new ToggleHighlightListener(elemName));
            this.add(this.toggle);
            this.add(this.title);
        }

        /**
         * returns the name of the tab of current tab *
         *
         * @return element name
         */
        String getTitleName() {
            return this.elemName;
        }

        /**
         * set the highlighting status of current tab
         *
         * @param b whether turn on or off highlighting
         */
        void setHighlighted(Boolean b) {
            this.toggle.setSelected(b);
        }

        /**
         * returns where current tab is highlighted or not
         *
         * @return true if current tab is highlighted
         */
        boolean isHighlighted() {
            return this.toggle.isSelected();
        }
    }

    /**
     * This is the class to toggle hightlight color for a specific tagnamw
     */
    private class ToggleHighlightListener implements ItemListener {

        private String elemName;
        private boolean isLink;

        public ToggleHighlightListener(String elemName) {
            this.elemName = elemName;
            this.isLink = mTask.getLinkNames().contains(elemName);
        }

        public String getElemName() {
            return this.elemName;
        }

        @Override
        public void itemStateChanged(ItemEvent itemEvent) {
            mStatusBar.setText("Processing...");
            int index = mBottomTable.indexOfTab(this.getElemName());

            TabTitle tab = (TabTitle) mBottomTable.getTabComponentAt(index);
            // checking 0 might be a little bit hardcoding
            // toggle all extent elements
            if (index == 0) {
                if (tab.isHighlighted()) {
                    assignAllColors();
                } else {
                    unassignAllColors();
                }
            }
            // toggle a single link element
            else if (index > 0 && isLink) {
                if (tab.isHighlighted()) {
                    mActiveLinks.add(this.elemName);
                    this.turnOnLink();
                } else {
                    mActiveLinks.remove(this.elemName);
                    this.turnOffLink();
                }
            }
            // toggle a single extent element
            else {
                if (tab.isHighlighted()) {
                    mActiveExts.add(this.elemName);

                    // when all single tabs are turned on, turn all_extents tab on
                    if (mActiveExts.size() == mTask.getExtNames().size()) {
                        // since allTab is created after all single tabs are created
                        // getTabComponentAt() will return null while loading up 
                        // a new DTD file, and will cause a nullpointer exception
                        TabTitle allTab = (TabTitle) mBottomTable.getTabComponentAt(0);
                        if (allTab != null) {
                            allTab.setHighlighted(true);
                        }
                    }
                } else {
                    mActiveExts.remove(this.elemName);

                    // when all single tabs are turned off, turn all_extents tab off
                    if (mActiveExts.size() == 0) {
                        TabTitle allTab = (TabTitle) mBottomTable.getTabComponentAt(0);
                        allTab.setHighlighted(false);
                    }
                }
                this.updateElemColor();
            }
            new Timer().schedule(new TimedUpdateStatusBar(), 1000);
        }

        private void turnOnLink() {
            DefaultStyledDocument styleDoc =
                    (DefaultStyledDocument) mTextPane.getStyledDocument();
            //get list of locations associated with the selected link
            Hashtable<Integer, String> locs
                    = mTask.getLocationsbyElemLink(elemName);
            for (Enumeration<Integer> e = locs.keys(); e.hasMoreElements(); ) {
                Integer i = e.nextElement();
                Element el = styleDoc.getCharacterElement(i);
                AttributeSet as = el.getAttributes();
                SimpleAttributeSet sas = new SimpleAttributeSet(as);
                StyleConstants.setItalic(sas, true);
                StyleConstants.setBold(sas, true);
                styleDoc.setCharacterAttributes(i, 1, sas, false);
            }
        }

        private void turnOffLink() {
            DefaultStyledDocument styleDoc =
                    (DefaultStyledDocument) mTextPane.getStyledDocument();
            //if boldness is being removed, have to make sure it doesn't
            //take away boldness of other tags that are selected
            //get list of active displays
            ArrayList<String> active = new ArrayList<String>();
            for (String linkName : mActiveLinks) {
                active.add(linkName);
            }
            active.remove(elemName);
            Hashtable<Integer, String> locs =
                    mTask.getLocationsbyElemLink(elemName, active);

            for (Enumeration<Integer> e = locs.keys(); e.hasMoreElements(); ) {
                Integer i = e.nextElement();
                Element el = styleDoc.getCharacterElement(i);
                AttributeSet as = el.getAttributes();
                SimpleAttributeSet sas = new SimpleAttributeSet(as);
                StyleConstants.setItalic(sas, false);
                StyleConstants.setBold(sas, false);
                styleDoc.setCharacterAttributes(i, 1, sas, false);
            }
        }

        private void updateElemColor() {
            for (String id : mTask.getExtIdsByName(this.elemName)) {
                assignTextColor(mTask.getLocByID(id));
            }
        }

    }

    /**
     * This is the class that's called when creating an extent tag (by either popup
     * menu or NC menu) * LinkListner listens to menu items that creating a link tag
     * with arguments from selected text or table rows Creating a link tag from
     * multiple arguments are done by an additional popup window, mLinkPopupFrame
     */
    private class MakeTagListener implements ActionListener {
        private boolean isLink;
        private boolean isArgLink;
        private String newName;
        private String newId;
        private Elem newElem;

        public void actionPerformed(ActionEvent actionEvent) {
            String command = actionEvent.getActionCommand();

            clearTableSelections();

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
                resetSpans();
                newName = command.substring(MaeStrings.ADD_NC_COMMAND.length());
            } else {
                newName = command;
            }

            // get a new ID
            newElem = mTask.getElemByName(newName);
            newId = mTask.getNextID(newName);

            // first add a new tag to table
            insertToTable();

            // then add to DB
            if (isLink) {
                processLink();
            } else if (isArgLink) {
                processLinkWithArgs();
            } else {
                addExtTagToDb(newName, newId);
            }

            // assign colors if necessary
            if (!isSpansEmpty()) {
                assignTextColor(mSpans);
            }

            // post to the user
            resetSpans();
            mStatusBar.setText(String.format("%s is created!", newId));
            new Timer().schedule(new TimedUpdateStatusBar(), 3000);
            isTaskChanged = true;
            updateTitle();
        }

        private void insertToTable() {
            // bring up corresponding panel from the bottom table
            JTable tab = mElementTables.get(newName);
            DefaultTableModel tableModel = (DefaultTableModel) tab.getModel();

            //  create a dummy data set and insert to table
            String[] newEmptyData = createEmptyRowData(newName, newId);
            tableModel.addRow(newEmptyData);

            // when adding an extent tag, also insert to all_table
            if (!isLink && !isArgLink) {
                DefaultTableModel allTableModel = (DefaultTableModel)
                        mElementTables.get(
                                MaeStrings.ALL_TABLE_BACK_NAME).getModel();
                String[] newdataForAll = Arrays.copyOfRange(newEmptyData, 0, 3);
                allTableModel.addRow(newdataForAll);
            }

            // move cursor and set focus to newly added tag
            mBottomTable.setSelectedIndex(mBottomTable.indexOfTab(newName));
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
            addLinkTagToDb(newName, newId,
                    Arrays.asList(argIds), Arrays.asList(argTypes));

            // add id of the new tag to underspecified set for further lookup
            mUnderspecified.add(newId);
        }

        private void processLinkWithArgs() {
            final ElemLink target = (ElemLink) newElem;
            mLinkPopupFrame = new JFrame();


            JPanel boxPane = new JPanel(new GridLayout(target.getArgNum() + 1, 2));

            // information for creating a link tag
            final String[] argIds = new String[target.getArgNum()];
            final String[] argTypes = new String[target.getArgNum()];

            // OK button
            JButton okay = new JButton("OK");
            okay.setMnemonic(MaeHotKeys.OK_BUTTON);
            okay.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    addLinkTagToDb(newName, newId,
                            Arrays.asList(argIds), Arrays.asList(argTypes));
                    for (int i = 0; i < argIds.length; i++) {
                        setArgumentInTable(
                                // name, id, argType, argId, argText
                                newName, newId,
                                target.getArguments().get(i), argIds[i],
                                getTextByID(argTypes[i], argIds[i], true));
                    }
                    mLinkPopupFrame.setVisible(false);
                    mLinkPopupFrame.dispose();
                }
            });

            // cancel button
            JButton cancel = new JButton("Cancel");
            cancel.setMnemonic(MaeHotKeys.CANCEL_BUTTON);
            cancel.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    mLinkPopupFrame.setVisible(false);
                    mLinkPopupFrame.dispose();
                    removeTableRows(newElem, newId);
                    mStatusBar.setText("Canceled");
                    new Timer().schedule(new TimedUpdateStatusBar(), 3000);
                }
            });

            // comboboxes to select arguments
            for (int i = 0; i < target.getArgNum(); i++) {
                JComboBox<String> candidates = new JComboBox<String>();
                for (String item : getComboItems(mPossibleArgIds, false)) {
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
            mLinkPopupFrame.add(boxPane);
            mLinkPopupFrame.pack();
            mLinkPopupFrame.setTitle(String.format("Creating %s - %s", newName, newId));
            mLinkPopupFrame.setLocation(300, 200);
            mLinkPopupFrame.setVisible(true);
            mLinkPopupFrame.setAlwaysOnTop(true);
            mLinkPopupFrame.requestFocus();

        }


        private String[] createEmptyRowData(String elemName, String newId) {
            // get the target element and a list of its attrib
            Elem e = mTask.getElemByName(elemName);
            ArrayList<Attrib> attributes = e.getAttributes();
            String[] newData = new String[attributes.size()];

            // go through the list of attributes, fill newdata array with proper values
            for (int i = 0; i < attributes.size(); i++) {
                // get ID number. This isn't as hard-coded as it looks:
                // the columns for the table are created from the Attributes array list
                if (attributes.get(i) instanceof AttID) {
                    newData[i] = newId;
                    // since link tags never have spans and text, below is safe
                } else if (attributes.get(i).getName().equals("spans")) {
                    newData[i] = spansToString(mSpans);
                } else if (attributes.get(i).getName().equals("text") && !isSpansEmpty()) {
                    newData[i] = getTextIn(mSpans);
                }
                // for the rest slots of newdata, make sure it's not staying in null value
                else {
                    if (attributes.get(i).hasDefaultValue()) {
                        newData[i] = attributes.get(i).getDefaultValue();
                    } else {
                        newData[i] = "";
                    }
                }
            }
            return newData;
        }
    }

    /**
     * This listener is associated with 'set as argument of...' menu items
     * callable from context menus from either bottom table or main text pane
     */
    private class SetAsArgListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            // command looks like this:
            // linkType(0), linkId(1), argName(2), argId(3), argText(4)
            String[] command = actionEvent.getActionCommand().split(MaeStrings.SEP);
            setArgumentInTable(command[0], command[1], command[2], command[3], command[4]);
            int argNum = mTask.getArguments(command[0]).indexOf(command[2]);
            String argType = mTask.getElemNameById(command[3]);
            mTask.addArgument(command[1], argNum, command[3], argType);
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
    private void setArgumentInTable(String linkName, String linkId,
                                    String argName, String argId, String argText) {
        DefaultTableModel tableModel
                = (DefaultTableModel) mElementTables.get(linkName).getModel();
        int rows = tableModel.getRowCount();
        int idRow = -1, argCol = -1;

        // first get indices of columns of argument IDs in the table
        TreeSet<Integer> argColumns = getArgColIndices(linkName);

        // then find which row and column to look for
        for (int i = rows - 1; i >= 0; i--) {
            if (tableModel.getValueAt(i, 0).equals(linkId)) {
                idRow = i;
            }
        }
        for (Integer i : argColumns) {
            if (tableModel.getColumnName(i).equals(argName + MaeStrings.ID_SUF)) {
                argCol = i;
            }
        }

        // set values for argID and argTEXT
        if (idRow == -1 || argCol == -1) {
            mStatusBar.setText("ERROR! Link ID and Arg name cannot be found in the table");
            new Timer().schedule(new TimedUpdateStatusBar(), 3000);
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

    /**
     * RemoveExtentTag is triggered when an extent tag is removed through the
     * text-area popup window
     */
    private class RemoveExtentTag implements ActionListener {
        public void actionPerformed(ActionEvent actionEvent) {
            boolean check = showDeleteWarning();
            if (check) {
                String command = actionEvent.getActionCommand();
                Elem elem = mTask.getElemByName(command.split(MaeStrings.SEP)[0]);
                //remove rows from DB
                HashCollection<String, String> links;
                // removes extent tags and related link tags from DB
                String elemName = command.split(MaeStrings.SEP)[0];
                String id = command.split(MaeStrings.SEP)[1];
                links = mTask.getLinksByExtentID(elemName, id);
                mTask.removeExtentByID(id);
                //remove extent tags and recolors text area
                removeTableRows(elem, id);
                removeAllTableRow(id);
                //remove links that use the tag being removed
                removeLinkTableRows(links);
                isTaskChanged = true;
                updateTitle();
            }
        }
    }

    /**
     * AnnCaretListener keeps track of what extents have been selected so that other
     * methods can use that information in the display and links.
     */
    private class MaeCaretListener implements CaretListener {
        @Override
        public void caretUpdate(CaretEvent e) {
            Highlighter hl = mTextPane.getHighlighter();
            //when the caret is moved, remove the any link highlights
            hl.removeAllHighlights();

            int dot = e.getDot();
            int mark = e.getMark();

            /*
            mod by krim.
            Not just set start and end field to caret selection,
            but clear the spans set first, then fill it with caret selection span.
            Consequently the array get one int[] in it.
            */

            // in normal mode, reset span for every mouse event
            if (mMode == M_NORMAL) {
                resetSpans();
            }

            // before selecting a text span, clear default (-1, -1) pair in mSpan
            if (isSpansEmpty()) {
                mSpans.clear();
            }
            int start, end;

            // mouse is dragged
            if (dot != mark) {
                isTextSelected = true;
                if (dot < mark) {
                    start = dot;
                    end = mark;
                } else {
                    start = mark;
                    end = dot;
                }
                int[] newSpan = new int[]{start, end};

                // not to add duplicate span
                boolean dup = false;
                for (int[] span : mSpans) {
                    if (Arrays.equals(span, newSpan)) {
                        dup = true;
                        break;
                    }
                }
                if (!dup) {
                    mSpans.add(new int[]{start, end});
                    if (mMode == M_ARG_SEL) {
                        updateArgList();
                    }
                }
            }

            // highlight corresponding row of table
            findHighlightRows();

            // krim: need to update current selection and status bar
            if (!isSpansEmpty()) {
                highlightTextSpans(hl, mSpans, mDefHL);
            }
            updateStatusBar();

        }
    }

    /**
     * JTableListener determines if the ID of a tag has been double-clicked, and if
     * it has it highlights the appropriate text extent/extents.
     */
    private class TableMouseAdapter extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {
            maybeShowTablePopup(e);
        }
        
        @Override
        public void mouseReleased(MouseEvent e) {
            maybeShowTablePopup(e);
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
                String elemName
                        = mBottomTable.getTitleAt(mBottomTable.getSelectedIndex());
                JTable tab = mElementTables.get(elemName);
                int selectedRow = tab.getSelectedRow();
                String elemId = (String) tab.getValueAt(selectedRow, 0);
                Elem el = mTask.getElemByName(mTask.getElemNameById(elemId));
                Highlighter hl = mTextPane.getHighlighter();
                hl.removeAllHighlights();

                if (el instanceof ElemExtent) {
                    // use table column[1] to get spanString then parse it
                    ArrayList<int[]> spansSelect = parseSpansString(
                            (String) tab.getValueAt(selectedRow, 1));
                    highlightTextSpans(hl, spansSelect, mOrangeHL);
                } //end if ElemExtent

                // krim: below is used to highlight linked extents
                if (el instanceof ElemLink) {

                    // get relevant argument columns
                    TreeSet<Integer> argColumns = getArgColIndices(elemName);

                    int j = 0;
                    for (Integer i : argColumns) {
                        String argId = (String) tab.getValueAt(selectedRow, i);
                        ArrayList<int[]> argSpans
                                = mTask.getLocByID(argId);
                        highlightTextSpans(hl, argSpans, mHighlighters[j]);
                        j++;
                    }
                }//end if ElemLink
            }
        }

        //if the user right-clicks on the table
        private void maybeShowTablePopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                resetSpans();
                mTablePopup = createTableContextMenu(e);
                mTablePopup.show(e.getComponent(),
                        e.getX(), e.getY());
            }
        }
    }

    /**
     * PopupListener determines whether the link creation window should be
     * displayed.
     */
    private class TextMouseAdapter extends MouseAdapter {
        
        @Override
        public void mousePressed(MouseEvent e) {
            maybeShowTextPopup(e);
        }
        
        @Override
        public void mouseReleased(MouseEvent e) {
            maybeShowTextPopup(e);
        }

        private void maybeShowTextPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                mTextPopup = createTextContextMenu();
                mTextPopup.show(e.getComponent(),
                        e.getX(), e.getY());
            }
        }
    }

    /**
     * Listener to select special modes
     */
    private class ModeMenuListener implements ActionListener {
        public void actionPerformed(ActionEvent actionEvent) {
            int action = Integer.parseInt(actionEvent.getActionCommand());

            switch (action) {
                // return to normal mode
                case M_NORMAL:
                    returnToNormalMode();
                    break;
                case M_MULTI_SPAN:
                    mMode = M_MULTI_SPAN;
                    mStatusBar.setText(
                            "Multi-span mode! Click anywhere to continue.");
                    break;
                case M_ARG_SEL:
                    mMode = M_ARG_SEL;
                    mStatusBar.setText(
                            "Argument select mode! Click anywhere to continue.");
                    break;
            }
            updateMenus();
            resetSpans();
            new Timer().schedule(new TimedUpdateStatusBar(), 3000);
        }
    }

    /**
     * Remove last selected text span from spans list
     * Used only in multi-span mode or n-ary argument selection mode
     */
    private class UndoSelectListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            String command = actionEvent.getActionCommand();
            if (command.equals("Undo")) {
                if (mSpans.size() > 0) {
                    int[] lastSpan = mSpans.remove(mSpans.size() - 1);
                    ArrayList<int[]> tmp = new ArrayList<int[]>();
                    tmp.add(lastSpan);

                    Highlighter hl = mTextPane.getHighlighter();
                    hl.removeAllHighlights();
                    highlightTextSpans(hl, tmp, mGrayHL);
                    highlightTextSpans(hl, mSpans, mDefHL);

                    mStatusBar.setText(String.format(
                            "Removed '%s' from selection!" +
                                    " Click anywhere to continue."
                            , getTextBetween(lastSpan[0], lastSpan[1])));
                } else {
                    mStatusBar.setText(
                            "No text selected! Click anywhere to continue.");
                }
            } else if (command.equals("Over")) {
                resetSpans();
                mStatusBar.setText(
                        "No text selected! Click anywhere to continue.");

            }
            new Timer().schedule(new TimedUpdateStatusBar(), 1000);
            if (mMode == M_ARG_SEL) {
                updateArgList();
            }

        }
    }

    /** filled color icon class to be used in tab titles as toggle buttons */
    public class ColorRect implements Icon {
        private int size;
        private Color color;

        public ColorRect(Color c, int size) {
            this.color = c;
            this.size = size;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(this.color);
            g.fillRect(x, y, this.size, this.size);
        }

        @Override
        public int getIconWidth() {
            return this.size;
        }

        @Override
        public int getIconHeight() {
            return this.size;
        }
    }

    /** Rectangle icon class to be used in tab titles as toggle buttons */
    public class BorderRect implements Icon {
        private int size;
        private Color color;

        public BorderRect(Color c, int size) {
            this.color = c;
            this.size = size;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(this.color);
            g.drawRect(x, y, this.size, this.size);
        }

        @Override
        public int getIconWidth() {
            return this.size;
        }

        @Override
        public int getIconHeight() {
            return this.size;
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
    private void processTagHash(
            HashCollection<String, Hashtable<String, String>> elementsToProcess) {
        ArrayList<String> elemNames = elementsToProcess.getKeyList();
        //first, add the extent tags

        for (String elemName : elemNames) {
            Elem elem = mTask.getElemByName(elemName);
            if (elem instanceof ElemExtent &&
                    mElementTables.containsKey(elemName)) {
                // for each element type there is a list of tag information
                ArrayList<Hashtable<String, String>> elemInstances
                        = elementsToProcess.get(elemName);

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
            Elem elem = mTask.getElemByName(elemName);
            if (elem instanceof ElemLink &&
                    mElementTables.containsKey(elemName)) {
                /*for each element type there is a list of tag information*/
                ArrayList<Hashtable<String, String>> elemInstances
                        = elementsToProcess.get(elemName);

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
        mSpans = parseSpansString(spansString);
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
        ArrayList<String> args = mTask.getArguments(elemName);
        ArrayList<String> argIDs = new ArrayList<String>();
        ArrayList<String> argTypes = new ArrayList<String>();
        for (String arg : args) {
            String argId = a.get(arg + MaeStrings.ID_SUF);
            // check if id value is a dummy,
            // if is, add the link tag to the underspecified for further lookup
            if (argId.equals("")) {
                mUnderspecified.add(argId);
            } else {
                String type = mTask.getElemNameById(argId);
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
        Elem elem = mTask.getElemByName(elemName);
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
                            MaeStrings.ALL_TABLE_BACK_NAME).getModel();
            // extent tag tables are always initialized with
            // id, spans, text in first three columns
            String[] newdataForAll = Arrays.copyOfRange(newdata, 0, 3);
            allTableModel.addRow(newdataForAll);
        }
    }

    /**
     * delete an item from all_extent_tags table given an ID of the item
     * @param id id of the item to delete
     */
    private void removeAllTableRow(String id) {
        DefaultTableModel tableModel
                = (DefaultTableModel) mElementTables.get(
                MaeStrings.ALL_TABLE_BACK_NAME).getModel();
        int rows = tableModel.getRowCount();
        //has to go backwards or the wrong rows get deleted
        for (int i = rows - 1; i >= 0; i--) {
            if (id.equals(tableModel.getValueAt(i, 0))) {
                tableModel.removeRow(i);
            }
        }
    }

    /**
     * Removes links from the table and DB
     *
     * @param links HashCollection of types and IDs of links being removed
     */
    private void removeLinkTableRows(HashCollection<String, String> links) {
        ArrayList<String> linkTags = links.getKeyList();
        for (String tag : linkTags) {
            Elem elem = mTask.getElemByName(tag);
            ArrayList<String> link_ids = links.getList(elem.getName());
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
    private void removeTableRows(Elem elem, String id) {
        DefaultTableModel tableModel
                = (DefaultTableModel) mElementTables.get(elem.getName()).getModel();
        int rows = tableModel.getRowCount();
        //has to go backwards or the wrong rows get deleted
        for (int i = rows - 1; i >= 0; i--) {
            if (id.equals(tableModel.getValueAt(i, 0))) {
                //redo color for this text--assumes that lines
                //have already been removed from the DB
                if (elem instanceof ElemExtent) {
                    assignTextColor(parseSpansString(
                            (String) tableModel.getValueAt(i, 1)));
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
    private String getTextByID(String elem, String id, boolean fullText) {
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
    private void findHighlightRows() {
        clearTableSelections();
        //first, get ids and types of elements in selected extents
        HashCollection<String, String> idHash = mTask.getTagsIn(mSpans);
        if (idHash.size() > 0) {
            ArrayList<String> elemNames = idHash.getKeyList();
            for (String elemName : elemNames) {
                ArrayList<String> ids = idHash.get(elemName);
                for (String id : ids) {
                    highlightTableRows(elemName, id);
                    //returns HashCollection of link ids connected to this
                    HashCollection<String, String> links
                            = mTask.getLinksByExtentID(elemName, id);
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
    private void addExtTagToDb(String elemName, String newId) {
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
    private void addLinkTagToDb(String elemName, String newId,
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
    private void highlightTableRowsHash(HashCollection<String, String> hash) {
        ArrayList<String> elems = hash.getKeyList();
        for (String e : elems) {
            ArrayList<String> ids = hash.get(e);
            for (String id : ids) {
                highlightTableRows(e, id);
            }
        }
    }

    /**
     * This method is for coloring/underlining text in the entire text window.  It
     * is called when a new file is loaded or toggling all_extents
     */
    private void assignAllColors() {
        //Get hashCollection of where tags are in the document
        //    <String location,<String elements>>.
        HashCollection<String, String> locElem = mTask.getLocElemHash();
        ArrayList<String> locs = locElem.getKeyList();
        for (String loc : locs) {
            ArrayList<String> elements = locElem.getList(loc);
            if (elements.size() > 1) {
                setColorAtLocation(mColorTable.get(elements.get(0)), Integer.parseInt(loc), 1, true);
            } else {
                setColorAtLocation(mColorTable.get(elements.get(0)), Integer.parseInt(loc), 1, false);
            }
        }
        ArrayList<String> elemNames = mTask.getExtNames();
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
    private void unassignAllColors() {
        HashCollection<String, String> locElem = mTask.getLocElemHash();
        ArrayList<String> locs = locElem.getKeyList();
        for (String loc : locs) {
            setColorAtLocation(Color.black, Integer.parseInt(loc), 1, false);
        }
        for (String elemName : mTask.getExtNames()) {
            TabTitle tab = (TabTitle) mBottomTable.getTabComponentAt(
                    mBottomTable.indexOfTab(elemName));
            tab.setHighlighted(false);
        }
        mActiveExts.clear();
    }

    /**
     * krim: This method is for coloring/underlining discontinuous spans in the text
     * window.
     *
     * @param spans a list of spans
     */
    private void assignTextColor(ArrayList<int[]> spans) {
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
            for (String elemName : mTask.getElementsAtLoc(i)) {
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
    private String getTextIn(ArrayList<int[]> spans) {
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
    private String getTextBetween(int start, int end) {
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
     * @param targetIds target tag set
     * @param emptyFirst add a empty dummy as the first item in the list
     * @return list of argument strings "type - id - text"
     */
    private ArrayList<String> getComboItems(
            ArrayList<String> targetIds, boolean emptyFirst) {
        ArrayList<String> items = new ArrayList<String>();
        if (emptyFirst) {
            items.add("");
        }
        for (String id : targetIds) {
            String type = mTask.getElemNameById(id);
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

        MaeTableModel model = new MaeTableModel();
        JTable table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        JScrollPane scrollPane = new JScrollPane(table);

        mElementTables.put(e.getName(), table);
        table.addMouseListener(new TableMouseAdapter());
        //go through element attributes and add colums
        ArrayList<Attrib> attributes = e.getAttributes();
        //for some reason, it's necessary to add the columns first,
        //then go back and add the cell renderers.
        for (Attrib attribute : attributes) {
            model.addColumn(attribute.getName());
        }
        for (int i = 0; i < attributes.size(); i++) {
            Attrib a = attributes.get(i);
            TableColumn c = table.getColumnModel().getColumn(i);
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
        return scrollPane;
    }

    /**
     * creates a table to store all extent tags in one place
     * @return the GUI component containing the JTable for all extent tags
     */
    private JComponent makeAllTablePanel() {

        AllTableModel model = new AllTableModel();
        JTable table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        JScrollPane scrollPane = new JScrollPane(table);

        mElementTables.put(MaeStrings.ALL_TABLE_BACK_NAME, table);
        table.addMouseListener(new TableMouseAdapter());

        // since all extent tags have three common attribs,
        // use only those as columns of all_table
        model.addColumn("id");
        model.addColumn("spans");
        model.addColumn("text");

        return scrollPane;
    }

    /**
     * Removes all the tags from the table when a new DTD is loaded.
     */
    private void resetTabPane() {
        mBottomTable.removeAll();
        ArrayList<Elem> elements = mTask.getElements();
        // create a tan for all extents and place it at first
        mBottomTable.addTab(MaeStrings.ALL_TABLE_BACK_NAME, makeAllTablePanel());
        //create a tab for each element in the annotation task
        for (Elem element : elements) {
            String name = element.getName();
            mBottomTable.addTab(name, makeTablePanel(element));
            TabTitle newTab;
            if (mColorTable.containsKey(name)) {
                // extent tags are assigned their colors, and by default highlighted
                newTab = new TabTitle(name, mColorTable.get(name));
                mBottomTable.setTabComponentAt(
                        mBottomTable.indexOfTab(name), newTab);
                mActiveExts.add(name);
                newTab.setHighlighted(true);
            } else {
                // link tags are don't have colors, and by default not highlighted
                newTab = new TabTitle(name);
                mBottomTable.setTabComponentAt(
                        mBottomTable.indexOfTab(name), newTab);
                mActiveLinks.remove(name);
                newTab.setHighlighted(false);
            }
        }
        // set toggle button in all extents tab after creating all other tabs
        TabTitle allTab = new TabTitle(MaeStrings.ALL_TABLE_BACK_NAME);
        mBottomTable.setTabComponentAt(0, allTab);
        allTab.setHighlighted(true);
    }

    /**
     * Creates a context menu from selected text
     *
     * @return a pop-up menu for creating different tags, as well as information
     * about existing tags at the selected location
     */
    private JPopupMenu createTextContextMenu() {
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

        if (mMode == M_ARG_SEL || mMode == M_MULTI_SPAN) {
            JMenuItem undo = new JMenuItem("Undo last selection");
            undo.setActionCommand("Undo");
            undo.addActionListener(new UndoSelectListener());
            undo.setAccelerator(MaeHotKeys.UNDO);

            JMenuItem over = new JMenuItem("Start over");
            over.setActionCommand("Over");
            over.addActionListener(new UndoSelectListener());
            over.setAccelerator(MaeHotKeys.STARTOVER);

            JMenuItem exit = new JMenuItem("Exit Multi-span Mode");
            exit.setActionCommand(Integer.toString(M_NORMAL));
            exit.addActionListener(new ModeMenuListener());
            exit.setAccelerator(MaeHotKeys.NORMALMODE);

            if (mMode == M_ARG_SEL) {
                String makeLink = "Create a Link tag with selected elements";
                JMenu makeLinkItem = new JMenu(makeLink);
                makeLinkItem.setMnemonic(MaeHotKeys.LINKARGMENU);
                int i = 0;
                for (String link : mTask.getLinkNames()) {
                    JMenuItem linkItem = new JMenuItem(link);
                    linkItem.setActionCommand(
                            MaeStrings.ADD_LINK_WITH_ARGS_COMMAND + link);
                    linkItem.addActionListener(new MakeTagListener());
                    if (i < 10) {
                        linkItem.setAccelerator(MaeHotKeys.noneNums[i]);
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
            HashCollection<String, String> idHash = mTask.getTagsIn(mSpans);
            if (idHash.size() > 0) {
                ArrayList<String> elems = idHash.getKeyList();
                for (String elem : elems) {
                    ArrayList<String> ids = idHash.get(elem);
                    for (String id : ids) {
                        jp.addSeparator();
                        String text = getTextByID(elem, id, false);
                        if (text.equals("")) {
                            text = "NC tag";
                        }
                        JMenu idItem = new JMenu(String.format(
                                "%s (%S)", id, text));

                        // add menu items for removing
                        JMenuItem removeItem = new JMenuItem("Remove");
                        removeItem.setActionCommand(elem + MaeStrings.SEP + id);
                        removeItem.setAccelerator(MaeHotKeys.DELETE);
                        removeItem.addActionListener(new RemoveExtentTag());
                        idItem.add(removeItem);

                        // add menu items for adding tag as an arg
                        JMenu setArg = createSetAsArgMenu(String.format(
                                "Set %s as an argument of", id), elem, id);
                        setArg.setMnemonic(MaeHotKeys.SETARGMENU);
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
     * @param argType element name of current extent tag
     * @param argId element id if current extent tag
     * @return a waterfall menu, goes down to 3rd level,
     *         for each link type, for each argument type, for each link instance
     */
    private JMenu createSetAsArgMenu(
            String menuTitle, String argType, String argId) {
        JMenu menu = new JMenu(menuTitle);

        // waterfall menu top level - link names
        int j = -1;
        for (String linkType : mTask.getLinkNames()) {

            j++;
            ArrayList<String> linkIds = mTask.getLinkIdsByName(linkType);

            // check if a tag in each type of link element exists
            if (linkIds.size() == 0) {
                addGuideItem(menu, String.format("no %s links", linkType));
                continue;
            }
            // add a link type as a menu item, only when it has real tags
            JMenu linkTypeMenu;
            if (j < 10) {
                linkTypeMenu = new JMenu(String.format("%d %s", j+1, linkType));
                linkTypeMenu.setMnemonic(MaeHotKeys.numKeys[j]);
            } else {
                linkTypeMenu = new JMenu(linkType);
            }

            // next level - actual relevant arguments
            int k = 0;
            for (String argName : mTask.getArguments(linkType)) {
                JMenu linkArgMenu;
                if (k < 10) {
                    linkArgMenu = new JMenu(String.format("%d %s", k+1, argName));
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
                            equals(argName + MaeStrings.ID_SUF)) {
                        argCol = i;
                    }
                }

                // final level - ids of each link
                // needs to move underspecified items to the top of the menu
                boolean prior = false;
                if (mUnderspecified.size() > 0) {
                    for (String unspecId : mUnderspecified) {
                        if (id2Add.contains(unspecId)) {
                            // find which row to look for,
                            // Note that it has to go backwards for efficiency
                            for (int i = rows - 1; i >= 0; i--) {
                                // check if id is matching first
                                // then check if argument is a dummy
                                if (tableModel.getValueAt(i, 0).equals(unspecId) &&
                                        tableModel.getValueAt(i, argCol).equals("")) {
                                    // add a menu guidance
                                    if (!prior) {
                                        addGuideItem(linkArgMenu, "Underspecifed");
                                        prior = true;
                                    }

                                    // add ids as menu items
                                    JMenuItem unspecIdItem
                                            = new JMenuItem(unspecId);
                                    unspecIdItem.addActionListener(
                                            new SetAsArgListener());
                                    unspecIdItem.setActionCommand(
                                            linkType + MaeStrings.SEP +
                                                    unspecId + MaeStrings.SEP +
                                                    argName + MaeStrings.SEP +
                                                    argId + MaeStrings.SEP +
                                                    getTextByID(argType, argId, true));
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
                    JMenuItem idItem = new JMenuItem(item);
                    idItem.addActionListener(new SetAsArgListener());
                    idItem.setActionCommand(
                            linkType + MaeStrings.SEP +
                                    item + MaeStrings.SEP +
                                    argName + MaeStrings.SEP +
                                    argId + MaeStrings.SEP +
                                    getTextByID(argType, argId, true));
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
     *
     * @return GUI menu
     */
    private JPopupMenu createTableContextMenu(MouseEvent event) {
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
            String id = (String) table.getValueAt(clickedRow, 0);

            JMenuItem removeItem = new JMenuItem(String.format("Remove %s", id));
            removeItem.setActionCommand(id);
            removeItem.setAccelerator(MaeHotKeys.DELETE);
            removeItem.addActionListener(new RemoveSelectedTableRows());
            jp.add(removeItem);

            // if selection was in all_tab, replace elemName
            if (elemName.equals(MaeStrings.ALL_TABLE_BACK_NAME)) {
                elemName = mTask.getElemNameById(id);
            }
            if (mTask.getElemByName(elemName) instanceof ElemExtent) {
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
            removeItem.addActionListener(new RemoveSelectedTableRows());
            jp.add(removeItem);

            // then if they are extent tags, add item for creating a link with them
            if (elemName.equals(MaeStrings.ALL_TABLE_BACK_NAME) ||
                    mTask.getElemByName(elemName) instanceof ElemExtent) {
                // calling table context menu will reset text selection
                resetSpans();
                // retrieve ids of all selected tags
                for (String id : ids) {
                    if (elemName.equals(MaeStrings.ALL_TABLE_BACK_NAME)) {
                        mPossibleArgIds.add(id);
                    } else {
                        mPossibleArgIds.add(id);
                    }
                }
                String makeLink = "Create a Link tag with selected elements";
                JMenu makeLinkItem = new JMenu(makeLink);
                makeLinkItem.setMnemonic(MaeHotKeys.LINKARGMENU);
                int i = 0;
                for (String link : mTask.getLinkNames()) {
                    JMenuItem linkItem = new JMenuItem(link);
                    linkItem.setActionCommand(
                            MaeStrings.ADD_LINK_WITH_ARGS_COMMAND + link);
                    linkItem.addActionListener(new MakeTagListener());
                    if (i < 10) {
                        linkItem.setAccelerator(MaeHotKeys.noneNums[i]);
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
            String value = (String) tableModel.getValueAt(i, 0);
            if (value.equals(id)) {
                tab.addRowSelectionInterval(
                        tab.convertRowIndexToView(i), tab.convertRowIndexToView(i));
            }
        }
        // then make highlight in the all_tag tab
        // this only happens when coloring elem is activated
        if (mActiveExts.contains(elem)) {
            tab = mElementTables.get(MaeStrings.ALL_TABLE_BACK_NAME);
            tableModel = (DefaultTableModel) tab.getModel();
            rows = tableModel.getRowCount();
            for (int i = rows - 1; i >= 0; i--) {
                String value = (String) tableModel.getValueAt(i, 0);
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
    private void clearTableSelections() {
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
    private TreeSet<Integer> getArgColIndices(String linkType) {

        JTable tab = mElementTables.get(linkType);
        ArrayList<String> argNames = mTask.getArguments(linkType);
        TreeSet<Integer> argColumns = new TreeSet<Integer>();
        for (int i = 0; i < tab.getModel().getColumnCount(); i++) {
            for (String argName : argNames) {
                if (tab.getModel().getColumnName(i).
                        equals(argName + MaeStrings.ID_SUF)) {
                    argColumns.add(i);
                }
            }
        }
        return argColumns;
    }

    /**
     * Displays the warning for saving your work before opening a new file or DTD.
     */
    private static void showSaveWarning() {
        JOptionPane save = new JOptionPane();
        save.setLocation(100, 100);
        String text = ("Warning! Opening a new file or DTD will \n" +
                "delete any unsaved data.  " +
                "\nPlease save your data before continuing");
        JOptionPane.showMessageDialog(mMainFrame, text);
    }

    /**
     * Shows message warning that deleting an extent will also delete any links the
     * extent is an anchor in.
     * Currently is shows whether the extent is in a link or not.
     *
     * @return boolean indicating the user accepted the warning or canceled the
     * action.
     */
    private boolean showDeleteWarning() {
        //JOptionPane delete = new JOptionPane();
        String text = ("Deleting extent tag(s) will also delete \n" +
                "any links that use these extents.  Would you like to continue?");

        int message = JOptionPane.showConfirmDialog(mMainFrame,
                text, "Warning!", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        return message == 0;
    }

    /**
     * Shows information about MAE
     */
    private void showAboutDialog() {
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
        JComboBox<String> options = new JComboBox();
        options.addItem("");
        for (int j = 0; j < att.getVaildValues().size(); j++) {
            options.addItem(att.getVaildValues().get(j));
        }
        return options;
    }

    /**
     * assigns colors to the elements in the DTD
     */
    private void assignColors() {
        ArrayList<String> elements = mTask.getExtNames();
        for (int i = 0; i < elements.size(); i++) {
            int l = mColors.length;
            int k = i;
            if (i >= l) {
                k = i % l;
            }
            mColorTable.put(elements.get(i), mColors[k]);
        }
    }

    /**
     * Creates the File menu for the top bar
     *
     * @return JMenu with all available options
     */
    private JMenu createFileMenu(String menuTitle) {
        JMenu menu = new JMenu(menuTitle);
        JMenuItem loadDTD = new JMenuItem("Load DTD");
        loadDTD.setActionCommand("Load DTD");
        loadDTD.addActionListener(new FileMenuListener());
        loadDTD.setAccelerator(MaeHotKeys.NEWTASK);
        menu.add(loadDTD);

        JMenuItem loadFile = new JMenuItem("Load File");
        loadFile.setActionCommand("Load File");
        loadFile.addActionListener(new FileMenuListener());
        loadFile.setAccelerator(MaeHotKeys.OPENFILE);
        if (!mTask.hasDTD()) {
            loadFile.setEnabled(false);
        } else {
            loadFile.setEnabled(true);
        }
        menu.add(loadFile);

        menu.addSeparator();
        JMenuItem saveFileRTF = new JMenuItem("Create RTF");
        saveFileRTF.setActionCommand("Save RTF");
        saveFileRTF.addActionListener(new FileMenuListener());
        saveFileRTF.setAccelerator(MaeHotKeys.SAVERTF);
        if (!isFileOpen) {
            saveFileRTF.setEnabled(false);
        } else {
            saveFileRTF.setEnabled(true);
        }
        menu.add(saveFileRTF);
        menu.addSeparator();

        JMenuItem saveFileXML = new JMenuItem("Save File As XML");
        saveFileXML.setActionCommand("Save XML");
        saveFileXML.addActionListener(new FileMenuListener());
        saveFileXML.setAccelerator(MaeHotKeys.SAVEXML);
        if (!isFileOpen) {
            saveFileXML.setEnabled(false);
        } else {
            saveFileXML.setEnabled(true);
        }

        menu.add(saveFileXML);
        return menu;
    }

    /**
     * Creates the Display menu for the top bar
     *
     * @return JMenu with all available display options
     */
    private JMenu createDisplayMenu(String menuTitle) {
        JMenu menu = new JMenu(menuTitle);

        JMenuItem increaseFont = new JMenuItem("Font Size ++");
        increaseFont.setActionCommand("Font++");
        increaseFont.addActionListener(new FontSizeMenuListener());
        increaseFont.setAccelerator(MaeHotKeys.FONTBIG);
        menu.add(increaseFont);

        JMenuItem decreaseFont = new JMenuItem("Font Size --");
        decreaseFont.setActionCommand("Font--");
        decreaseFont.addActionListener(new FontSizeMenuListener());
        decreaseFont.setAccelerator(MaeHotKeys.FONTSMALL);
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
            ArrayList<String> linkNames = mTask.getLinkNames();

            if (linkNames.size() == 0) {
                addGuideItem(menu, "no link tags defined");
            } else {
                int i = 0;
                for (String linkName : linkNames) {
                    JMenuItem menuItem;
                    if (mainMenu) {
                        menuItem = new JMenuItem(
                                String.format("(%d) %s", i+1, linkName));
                    } else {
                        menuItem = new JMenuItem(linkName);
                    }
                    menuItem.addActionListener(new MakeTagListener());
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

    private JMenu createTagMenu(String menuTitle, boolean mainMenu) {
        JMenu menu = new JMenu(menuTitle);
        if (!isTextSelected) {
            JMenuItem noText = new JMenuItem(MaeStrings.MENU_NOTEXT);
            noText.setEnabled(false);
            menu.add(noText);
            return menu;
        }

        int i = 0;
        for (String elemName : mTask.getExtNames()) {
            JMenuItem menuItem;
            if (mainMenu) {
                menuItem = new JMenuItem( 
                        String.format("(%d) %s", i + 1, elemName));
                
                
            } else {
                menuItem = new JMenuItem(elemName);
            }
            menuItem.addActionListener(new MakeTagListener());
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
            ArrayList<Elem> ncElems = mTask.getNCElements();

            if (ncElems.size() == 0) {
                addGuideItem(menu, "no NC tag defined");
            } else {
                int i = 0;
                for (Elem ncElem : ncElems) {
                    JMenuItem menuItem;
                    if (mainMenu) {
                        menuItem = new JMenuItem(
                                String.format("(%d) %s", i+1, ncElem.getName()));
                    } else {
                        menuItem = new JMenuItem(ncElem.getName());
                    }
                    menuItem.addActionListener(new MakeTagListener());
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
        HelpMenuListener helpMenuListener = new HelpMenuListener();
        JMenuItem about = new JMenuItem("About MAE");
        about.setAccelerator(MaeHotKeys.ABOUT);
        about.setActionCommand("about");
        about.addActionListener(helpMenuListener);
        JMenuItem github = new JMenuItem("Visit project website(Github)");
        github.setActionCommand("web");
        github.addActionListener(helpMenuListener);
        github.setAccelerator(MaeHotKeys.WEB);
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
        JMenuItem multiSpan = new JMenuItem("Multi-span Mode");
        ModeMenuListener modemenuListen = new ModeMenuListener();
        multiSpan.setActionCommand(Integer.toString(M_MULTI_SPAN));
        multiSpan.addActionListener(modemenuListen);
        multiSpan.setAccelerator(MaeHotKeys.MSPANMODE);

        JMenuItem multiArgs = new JMenuItem("Argument selection Mode");
        multiArgs.setActionCommand(Integer.toString(M_ARG_SEL));
        multiArgs.addActionListener(modemenuListen);
        multiArgs.setAccelerator(MaeHotKeys.ARGSMODE);

        JMenuItem exitMode = new JMenuItem("Exit to Normal Mode");
        if (mMode != M_NORMAL) {
            exitMode.setEnabled(true);
        } else {
            exitMode.setEnabled(false);
        }
        exitMode.setActionCommand(Integer.toString(M_NORMAL));
        exitMode.addActionListener(modemenuListen);
        exitMode.setAccelerator(MaeHotKeys.NORMALMODE);

        menu.add(multiSpan);
        menu.add(multiArgs);
        menu.addSeparator();
        menu.add(exitMode);

        return menu;
    }

    /**
     * creates a grayed-out menu item that informs user of something
     * @param menu parent menu to which 'guide' goes in
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
    private void updateMenus() {
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

        JMenu helpMenu = createHelpMenu("Help");
        helpMenu.setMnemonic(MaeHotKeys.HELPMENU);
        mMenuBar.add(helpMenu);

        mMenuBar.updateUI();
    }

    /**
     * Takes a string representing possibly multiple spans of an extent tag Return
     * array of integer pairs
     *
     * @param spansString - string of spans
     * @return a ArrayList of int[]
     */
    protected ArrayList<int[]> parseSpansString(String spansString) {
        ArrayList<int[]> spans = new ArrayList<int[]>();
        if (spansString == null || spansString.equals("")) {
            spans.add(new int[]{-1, -1});
            return spans;
        }

        // check if the tag being processed is non-consuming
        if (spansString.equals("-1~-1")) {
            spans.add(new int[]{-1, -1});
            return spans;
        }

        // split each span
        String[] pairs = spansString.split(MaeStrings.SPANSEPARATOR);
        for (String pair : pairs) {
            int[] span = new int[2];

            // parse start and end points
            span[0] = Integer.parseInt(pair.split(MaeStrings.SPANDELIMITER)[0]);
            span[1] = Integer.parseInt(pair.split(MaeStrings.SPANDELIMITER)[1]);

            spans.add(span);
        }
        return spans;
    }

    /**
     * Takes an array of integer pairs, then merge it into a string. Each span
     * separated by SPANSEPARATOR start and end point of each span joined with
     * SPANDELIMITER
     *
     * @param spans - an sorted set of integer pairs
     * @return a formatted string of spans of a tag
     */
    protected String spansToString(ArrayList<int[]> spans) {
        String spanString = "";
        Iterator<int[]> iter = spans.iterator();
        while (iter.hasNext()) {
            int[] span = iter.next();
            if (iter.hasNext()) {
                spanString += span[0] + MaeStrings.SPANDELIMITER + span[1]
                        + MaeStrings.SPANSEPARATOR;
            } else {
                spanString += span[0] + MaeStrings.SPANDELIMITER + span[1];
            }
        }
        return spanString;
    }

    /**
     * Highlight given spans with given highlighter and painter(color)
     *
     * @param hl      - Highlighter OBJ from text panel
     * @param spans   - desired text spans to be highlighted
     * @param painter - highlighter OBJ with color
     */
    private void highlightTextSpans(Highlighter hl,
                                    ArrayList<int[]> spans,
                                    Highlighter.HighlightPainter painter) {

        for (int[] span : spans) {
            int start = span[0], end = span[1];
            // do highlighting only for real spans; (-1, -1) is a dummy for NC tags
            if (start != -1 || end != -1) {
                try {
                    hl.addHighlight(start, end, painter);
                    mTextPane.scrollRectToVisible(mTextPane.modelToView(start));
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /** make a list all extent elements in mSpan */
    private void updateArgList() {
        mPossibleArgIds.clear();

        int i = 0;
        for (int[] span : mSpans) {
            HashCollection<String, String> elems
                    = mTask.getTagsBetween(span[0], span[1]);
            boolean first = true;
            for (String elemName : elems.getKeyList()) {
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

    /** Updates the status bar */
    private void updateStatusBar() {
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
                                + spansToString(this.mSpans));
                    }
                    break;
                case M_MULTI_SPAN:
                    if (isSpansEmpty()) {
                        mStatusBar.setText(
                                MaeStrings.SB_MSPAN_NOTEXT);
                    } else {
                        mStatusBar.setText(String.format(
                                MaeStrings.SB_MSPAN_TEXT +
                                spansToString(this.mSpans)));
                    }
                    break;
                case M_ARG_SEL:
                    if (isSpansEmpty()) {
                        mStatusBar.setText(MaeStrings.SB_MARGS_NOTAG);
                    } else {
                        ArrayList<String> argList = new ArrayList<String>();
                        for (String id : mPossibleArgIds) {
                            argList.add(String.format("%s - %s"
                                    , id
                                    , getTextByID(
                                    mTask.getElemNameById(id), id, false)));
                        }
                        mStatusBar.setText(String.format(
                                MaeStrings.SB_MARGS_TAG
                                , mPossibleArgIds.size(), argList.toString()));
                    }
                    break;
            }
        }
    }
    
    /** add asterisk to windows title when file is changed */
    private void updateTitle() {
        if (isTaskChanged) {
            mMainFrame.setTitle(
                    MaeStrings.TITLE_PREFIX + " - " + mFileFullName + " *");
        } else {
            mMainFrame.setTitle(
                    MaeStrings.TITLE_PREFIX + " - " + mFileFullName);

        }
        
        
    }

    /** Sets MAE mode to Normal */
    private void returnToNormalMode() {

        if (mMode != M_NORMAL) {
            mStatusBar.setText(MaeStrings.SB_NORM_MODE);
            new Timer().schedule(new TimedUpdateStatusBar(), 3000);
        }
        mMode = M_NORMAL;

    }

    /** check if anything is selected in text pane */
    private Boolean isSpansEmpty() {
        return this.mSpans.size() == 0 || this.mSpans.get(0)[0] == -1;
    }

    /** Resets the selected spans to default non-selection (-1~-1) */
    private void resetSpans() {
        isTextSelected = false;

        mSpans.clear();
        mSpans.add(new int[]{-1, -1});
        Highlighter hl = mTextPane.getHighlighter();
        hl.removeAllHighlights();

        mPossibleArgIds.clear();
    }

    /** Creates the GUI */
    private static void createAndShowGUI() {
        JFrame.setDefaultLookAndFeelDecorated(true);

        //Create and set up the window.
        mMainFrame = new JFrame(MaeStrings.TITLE_PREFIX);
        mMainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        JComponent newContentPane = new MaeMain();
        newContentPane.setOpaque(true); //content panes must be opaque
        mMainFrame.setContentPane(newContentPane);

        //Display the window.
        mMainFrame.pack();
        mMainFrame.setSize(900, 500);
        mMainFrame.setVisible(true);
    }

    /** Main */
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        createAndShowGUI();
                    }
                });
    }
}
