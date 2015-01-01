/*
 * MAE - Multi-purpose Annotation Environment
 * 
 * Copyright Amber Stubbs (astubbs@cs.brandeis.edu)
 * Department of Computer Science, Brandeis University
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package mae;


import java.io.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import java.util.Timer;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.table.TableColumn;
import javax.swing.table.DefaultTableModel;


/** 
 * MaeMain is the main class for MAE; it manages all the GUI attributes
 * and manages how the annotation information is loaded, interacted with,
 * and displayed.
 * 
 * @author Amber Stubbs
 * @revised Keigh Rim
*/



public class MaeMain extends JPanel {

    // add by krim: constant strings to be used in GUI title bar
    protected final static String VERSION = "0.10.0";
    protected final static String TITLE_PREFIX = "MAE " + VERSION;

    // add by krim: constant string to be used in string representation of spans
    protected final static String SPANDELIMITER = "~";
    protected final static String SPANSEPARATOR = ",";
    protected final static String SPANTEXTTRUNC = " ... ";

    private static final long serialVersionUID = 9404268L;

    private Hashtable<String, JTable> mElementTables;
    private Hashtable<String, Color> mColorTable;
    private Hashtable<String,Integer> mDisplayingLinkTypeOf;

    //Here is where to change the colors that get assigned to tags
    private Color mBlue = Color.blue;
    private Color mRed = Color.red;
    private Color mGreen = Color.green;
    private Color mMagenta = Color.magenta;
    private Color mDarkOrange = new Color(153,102,0);
    private Color mPink = Color.pink;
    private Color mCyan = Color.cyan;
    private Color mLightOrange = new Color(255,204,51);
    private Color mLightBlue = new Color(0,172,188);
    private Color mOrange = new Color (234,160,0);
    private Color mPurple = new Color(102,75,153);
    private Color mGray = Color.lightGray;

    private Color[] colors = {
            mBlue, mRed, mGreen, mMagenta, mDarkOrange, mPink, mCyan,
            mLightOrange, mLightBlue, mOrange, mPurple, mGray };

    private TextHighlightPainter mOrangeHL = new TextHighlightPainter(mLightOrange);
    private TextHighlightPainter mGreenHL = new TextHighlightPainter(mGreen);
    private TextHighlightPainter mGrayHL = new TextHighlightPainter(mGray);
    private TextHighlightPainter mDefHL
            = new TextHighlightPainter(SystemColor.textHighlight);

    //some booleans that help keep track of the status of the annotation
    private boolean isFileOpen;
    private boolean isTextSelected;
    private boolean isCtrlPressed;

    // add by krim: additional booleans to keep track of annotation mode
    private final int NORMAL = 0;
    private final int MSPAN = 1;
    private final int MLINK = 2;
    private int mMode;


    //ints and Strings that are handy to have widely available
    private int mPrevSpan; // this will be used when adding link (ctrl+drag UI)
    private ArrayList<int[]> mSpans; // added krim
    // instead of using 2 integers, start & end, now we use a set of tuples

    private String linkFrom;
    private String linkName;
    private String linkTo;
    private String mFileName;
    private String mXmlName;

    //GUI components
    private static JFrame mMainFrame;
    private JFrame mLinkPopupFrame;
    private JScrollPane mScrollPane;
    private JTabbedPane mBottomTable;
    private JTextPane mTextPane;
    private JPanel mTopPanel;

    private JMenuBar mMenuBar;
    private JMenu mFileMenu;
    private JMenu mNCMenu; // mod by krim: rename
    private JMenu mDisplayMenu; // mod by krim: rename
    // add by krim: this mode menu will be used for toggling special input mode
    // eg> multiple-span extent tag
    private JMenu mModeMenu;
    private JMenu mHelpMenu;

    // add by krim: this JLabel will work as a status bar to deliver real-time messages
    private JLabel mStatusBar;

    // mod by krim: renamed to disambiguate
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
        isTextSelected =false;
        isCtrlPressed =false;

        mPrevSpan = -1;
        mMode = NORMAL;
        // mod by krim: init start-end to (-1, -1) pair
        mSpans = new ArrayList<int[]>();
        resetSpans();

        linkFrom="";
        linkName="";
        linkTo="";

        mFileName = "";
        mXmlName = "";

        //used to keep track of what color goes with what tag
        mColorTable = new Hashtable<String,Color>();

        mElementTables = new Hashtable<String, JTable>();
        mDisplayingLinkTypeOf = new Hashtable<String,Integer>();

        /* GUI components */
        // krim: only help menu has constant, context-free items
        // thus we create it only once here in the constructor
        mLoadFC = new JFileChooser(".");
        mLoadFC.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        mSaveFC = new JFileChooser(".");
        mSaveFC.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        mTextPopup = new JPopupMenu();
        mTablePopup = new JPopupMenu();
        mLinkPopupFrame = new JFrame();

        mTextPane = new JTextPane(new DefaultStyledDocument());
        mTextPane.setEditable(false);
        mTextPane.setContentType("text/plain; charset=UTF-8");
        mTextPane.addCaretListener(new AnnCaretListener());
        mTextPane.addKeyListener(new ModKeyListener());
        mTextPane.addMouseListener(new TextMouseAdapter());

        mScrollPane = new JScrollPane(mTextPane);
        mScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // add by krim: add a status bar in the bottom of the text pane
        mStatusBar = new JLabel();
        updateStatusBar();

        mTopPanel = new JPanel(new BorderLayout());
        mTopPanel.add(mScrollPane, BorderLayout.CENTER);
        mTopPanel.add(mStatusBar, BorderLayout.SOUTH);

        mBottomTable = new JTabbedPane();
        JComponent panel1 = makeTextPanel("No DTD");
        mBottomTable.addTab("Tab", panel1);

        // krim: only help menu has constant, context-free items
        // thus we create it only once here in the constructor
        mHelpMenu = createHelpMenu();
        mMenuBar = new JMenuBar();
        updateMenus();

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT, mTopPanel, mBottomTable);

        addKeyListener(new ModKeyListener());
        add(mMenuBar,BorderLayout.NORTH);
        add(splitPane,BorderLayout.CENTER);
        splitPane.setDividerLocation(250);
    }

    // ***********************
    // Section: classes and listeners

    /**
     * Allows new highlighters for the JTextPane
     * mod by krim: re-named for consistency with MAI
     *
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
     * Listener for the File menu; determines what action to take for
     * loading/saving documents.
     *
     */
    private class getFile implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e){
            int returnVal;
            String s = e.getActionCommand();
            if (s.equals("Load DTD")) {
                if (isFileOpen) {
                    showSaveWarning();
                }
                returnVal = mLoadFC.showOpenDialog(MaeMain.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = mLoadFC.getSelectedFile();
                    try {
                        mTextPane.setStyledDocument(new DefaultStyledDocument());
                        DTDLoader dtdl = new DTDLoader(file);
                        mTask.reset_db();
                        mTask.setDTD(dtdl.getDTD());
                        mDisplayingLinkTypeOf.clear();
                        resetTabPane();
                        assignColors();

                        // add by krim: need to refresh interfaces
                        updateMenus();
                        returnToNormalMode();
                        mStatusBar.setText("DTD load succeed! Click anywhere to continue.");

                        if (mTask.getElements().size() > 20) {
                            mBottomTable.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
                        } else {
                            mBottomTable.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
                        }

                        isFileOpen = false;
                    } catch (Exception ex) {
                        System.out.println("Error loading DTD");
                        ex.printStackTrace();

                        // print out the error message on the status bar
                        mStatusBar.setText("Error loading DTD: " + ex.toString());
                    }
                }

            } else if (s.equals("Load File")) {
                if (isFileOpen) {
                    showSaveWarning();
                }
                returnVal = mLoadFC.showOpenDialog(MaeMain.this);
                boolean succeed = true;
                String status = "";
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = mLoadFC.getSelectedFile();
                    String fileName = file.getName();
                    int endName = fileName.lastIndexOf(".");
                    mFileName = fileName.substring(0, endName);
                    mXmlName = mFileName + ".xml";
                    try {
                        // mod by krim: to show tool version on title bar
                        mMainFrame.setTitle(TITLE_PREFIX + " - " + fileName);

                        isFileOpen = true;
                        mTask.reset_db();
                        mTask.reset_IDTracker();

                        // add by krim: need to refresh interfaces
                        resetTabPane();
                        updateMenus();
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
                            HashCollection<String, Hashtable<String, String>> newTags = xfl.getTagHash();
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
                // add by krim: status bar refreshing come last
                // since any caret update event (like the aboves) updates status bar
                // thus we need to set text after caret events
                if (succeed) {
                    status = "File load succeed! Click anywhere to continue.";
                }
                mStatusBar.setText(status);

            } else if (s.equals("Save RTF")) {
                String rtfName = mFileName + ".rtf";
                mSaveFC.setSelectedFile(new File(rtfName));
                returnVal = mSaveFC.showSaveDialog(MaeMain.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = mSaveFC.getSelectedFile();
                    try {
                        FileOperations.saveRTF(file, mTextPane);
                        mStatusBar.setText("Save Complete :" + rtfName);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        mStatusBar.setText("Error saving RTF file");
                    }
                }

            } else if (s.equals("Save XML")) {
                mSaveFC.setSelectedFile(new File(mXmlName));
                returnVal = mSaveFC.showSaveDialog(MaeMain.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = mSaveFC.getSelectedFile();
                    String fullName = file.getName();
                    try {
                        FileOperations.saveXML(file, mTextPane,
                                mElementTables, mTask.getElements(), mTask.getDTDName());
                        mMainFrame.setTitle(fullName);
                        mXmlName = fullName;
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
    private class AboutListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e){
            showAboutDialog();
        }
    }

    /**
     * Class that changes the size of the text from the top menu
     */
    private class DisplayListener implements ActionListener{
        public void actionPerformed(ActionEvent e){
            String action = e.getActionCommand();
            if (action.equals("Font++")){
                Font font = mTextPane.getFont();
                Font font2 = new Font(font.getName(),font.getStyle(),font.getSize()+1);
                mTextPane.setFont(font2);
                mBottomTable.setFont(font2);
            } else if (action.equals("Font--")){
                Font font = mTextPane.getFont();
                Font font2 = new Font(font.getName(),font.getStyle(),font.getSize()-1);
                mTextPane.setFont(font2);
                mBottomTable.setFont(font2);
            }
        }
    }

    /**
     * AnnTableModel creates a TableModel that
     * allows the ID column to be uneditable.  This
     * helps prevent user-created database conflicts by
     * ensuring the IDs being generated will not be changed,
     * and makes it so that users can double-click on the
     * ID in order to see where that tag appears in the text.
     *
     */
    private class AnnTableModel extends DefaultTableModel{
        static final long serialVersionUID = 552012L;
        @Override
        public boolean isCellEditable(int row, int col){
            return col != 0;
        }
    }

    /**
     * Called when the user selects the option to delete the highlighted
     * rows from the table in view.  Rows are removed both from the
     * database and the table.
     *
     */
    private class RemoveSelectedTableRows implements ActionListener{
        public void actionPerformed(ActionEvent actionEvent) {
            boolean check = showDeleteWarning();
            if (check){
                String action = actionEvent.getActionCommand();
                Elem elem = mTask.getElem(action);
                JTable tab = mElementTables.get(action);
                int[] selectedViewRows = tab.getSelectedRows();

                //convert the rows of the table view into the rows of the
                //table model so that the correct rows are deleted
                int[] selectedRows = new int[selectedViewRows.length];
                for (int i=0;i<selectedRows.length;i++){
                    selectedRows[i]=tab.convertRowIndexToModel(selectedViewRows[i]);
                }

                DefaultTableModel tableModel = (DefaultTableModel)tab.getModel();
                //find the id column
                int cols = tableModel.getColumnCount();
                int idCol = -1;
                for(int i=0;i<cols;i++){
                    String colname = tableModel.getColumnName(i);
                    if(colname.equalsIgnoreCase("id")){
                        idCol = i;
                    }
                }
                /*get the id for each selected row and remove id*/
                String id;
                for (int i=selectedRows.length-1;i>=0;i--){
                    int row = selectedRows[i];
                    id = (String)tableModel.getValueAt(row,idCol);
                    mTask.removeExtentByID(action, id);
                    if(elem instanceof ElemExtent){

                        // mod by krim: instead of take 2 integers,
                        // take a string of possibly multiple spans
                        String spanString = (String)tableModel.getValueAt(row,1);
                        ArrayList<int[]> spans = parseSpansString(spanString);

                        assignTextColor(spans);
                        HashCollection<String,String> links
                                = mTask.getLinksByExtentID(action,id);
                        //remove links that use the tag being removed
                        removeLinkTableRows(links);
                    }
                    tableModel.removeRow(selectedRows[i]);
                }
            }
        }
    }

    /**
     * This is the class that's called when an extent tag is
     * selected from the popup menu.
     */
    private class MakeTagListener implements ActionListener {
        public void actionPerformed(ActionEvent actionEvent) {
            String action = actionEvent.getActionCommand();
            //if the tag being added is non-consuming, make sure
            //start and end are set to -1
            //krim: instead, clear the spans list then insert (-1, -1) pair
            if (action.contains("addN-C:")) {
                resetSpans();
                action = action.split(":")[1];
            }
            JTable tab = mElementTables.get(action);
            DefaultTableModel tableModel = (DefaultTableModel) tab.getModel();
            //create array for data for row
            String[] newdata = new String[tableModel.getColumnCount()];
            for (int i = 0; i < tableModel.getColumnCount(); i++) {
                newdata[i] = "";
            }
            // get the Elem that the table was based on, and go through
            // the attributes.  Put in the start and end bits*/
            Hashtable<String, Elem> elements = mTask.getElemHash();
            Elem elem = elements.get(action);
            // get ID number. This isn't as hard-coded as it looks:
            // the columns for the table are created from the Attributes array list
            String newID = "";
            ArrayList<Attrib> attributes = elem.getAttributes();
            for (int i = 0; i < attributes.size(); i++) {
                if (attributes.get(i) instanceof AttID) {
                    newID = mTask.getNextID(action);
                    newdata[i] = newID;
                }
                if (attributes.get(i).hasDefaultValue()) {
                    newdata[i] = attributes.get(i).getDefaultValue();
                }
            }

            //put in start and end values
            if (elem instanceof ElemExtent) {
                attributes = elem.getAttributes();
                for (int i = 0; i < attributes.size(); i++) {
                    //this also isn't as hard-coded as it looks, because
                    //all extent elements have these attributes

                    // mod by krim:
                    // try to put in spanString, not take out.

                    if (attributes.get(i).getName().equals("spans")) {
                        newdata[i] = spansToString(mSpans);
                    } else if (attributes.get(i).getName().equals("text") &&
                            !isSpansEmpty()) {
                        newdata[i] = getTextIn(mSpans);
                    }
                }
            }

            //add new row of tag info to the table and set appropriate attributes
            tableModel.addRow(newdata);
            tab.clearSelection();
            tab.setRowSelectionInterval(
                    tableModel.getRowCount() - 1, tableModel.getRowCount() - 1);
            Rectangle rect = tab.getCellRect(
                    tableModel.getRowCount() - 1, 0, true);
            tab.scrollRectToVisible(rect);
            addTags(action, newID);
            if (!isSpansEmpty()) {
                assignTextColor(mSpans);
            }
            resetSpans();
        }
    }

    /**
     * Listens to the keyboard to see if the key for
     * creating links is being pressed
     *
     */
    private class ModKeyListener implements KeyListener{
        public void keyPressed(KeyEvent e) {
            int keyCode = e.getKeyCode();

            String p = System.getProperty("os.name");
            if(p.toLowerCase().contains("mac")){
                if (keyCode == 18 || keyCode == 157){
                    isCtrlPressed = true;
                }
            }
            else {
                if ( keyCode == 17){
                    isCtrlPressed = true;
                }
            }
        }

        public void keyReleased(KeyEvent e){
            String p = System.getProperty("os.name");
            int keyCode = e.getKeyCode();
            if(p.toLowerCase().contains("mac")){
                if (keyCode == 18 || keyCode == 157){
                    isCtrlPressed = false;
                }
            }
            else {
                if ( keyCode == 17){
                    isCtrlPressed = false;
                }
            }
        }
        public void keyTyped(KeyEvent e){
            //do nothing
        }
    }

    /**
     * RemoveExtentTag is triggered when
     * an extent tag is removed through the
     * text-area popup window
     */
    private class RemoveExtentTag implements ActionListener{
        public void actionPerformed(ActionEvent e){
            boolean check = showDeleteWarning();
            if (check){
                String action = e.getActionCommand();
                Elem elem = mTask.getElem(action.split(", ")[0]);
                //remove rows from DB
                HashCollection<String,String> links = new HashCollection<String,String>();
                //removes extent tags and related link tags from DB, returns HashCollection
                //of link ids for removal from the DB
                String elemType = action.split(", ")[0];
                String id = action.split(", ")[1];
                links = mTask.getLinksByExtentID(elemType,id);
                mTask.removeExtentByID(elemType, id);
                //remove extent tags and recolors text area
                removeTableRows(elem,id);
                //remove links that use the tag being removed
                removeLinkTableRows(links);
            }
        }
    }

    /**
     * When the DisplayLinkListener is called from the Display menu,
     * the text window italicizes and bolds the text of the extent tags
     * that are participants in the type of link selected from the menu.
     *
     */
    private class DisplayLinkListener implements ActionListener{
        public void actionPerformed(ActionEvent actionEvent){
            String cmd = actionEvent.getActionCommand();
            Integer stat = mDisplayingLinkTypeOf.get(cmd);
            String elemName = cmd.split(":")[1];
            if (stat.equals(0)){
                //get list of locations associated with the selected link
                Hashtable<Integer,String> locs = mTask.getLocationsbyElemLink(elemName);
                DefaultStyledDocument styleDoc =
                        (DefaultStyledDocument) mTextPane.getStyledDocument();
                for (Enumeration<Integer> e = locs.keys(); e.hasMoreElements();){
                    Integer inte = e.nextElement();
                    Element el = styleDoc.getCharacterElement(inte);
                    AttributeSet as = el.getAttributes();
                    SimpleAttributeSet sas = new SimpleAttributeSet(as);
                    StyleConstants.setItalic(sas, true);
                    StyleConstants.setBold(sas, true);
                    styleDoc.setCharacterAttributes(inte,1,sas,false);
                }
                mDisplayingLinkTypeOf.put(cmd, 1);
            }
            else {
                //if boldness is being removed, have to make sure it doesn't
                //take away boldness of other tags that are selected
                DefaultStyledDocument styleDoc =
                        (DefaultStyledDocument) mTextPane.getStyledDocument();
                //get list of active displays
                ArrayList<String> active = new ArrayList<String>();
                for (Enumeration<String> e = mDisplayingLinkTypeOf.keys(); e.hasMoreElements();){
                    String elem = e.nextElement();
                    if(mDisplayingLinkTypeOf.get(elem).equals(1)){
                        active.add(elem.split(":")[1]);
                    }
                }
                active.remove(elemName);
                Hashtable<Integer,String> locs =
                        mTask.getLocationsbyElemLink(elemName,active);

                for (Enumeration<Integer> e = locs.keys(); e.hasMoreElements();){
                    Integer inte = e.nextElement();
                    Element el = styleDoc.getCharacterElement(inte);
                    AttributeSet as = el.getAttributes();
                    SimpleAttributeSet sas = new SimpleAttributeSet(as);
                    StyleConstants.setItalic(sas, false);
                    StyleConstants.setBold(sas, false);
                    styleDoc.setCharacterAttributes(inte,1,sas,false);
                }
                mDisplayingLinkTypeOf.put(cmd, 0);
            }
        }
    }

    /**
     * AnnCaretListener keeps track of what extents have been selected
     * so that other methods can use that information in the display
     * and links.
     *
     * mod by krim: overall re-written for multi-span mode
     *
     */
    private class AnnCaretListener implements CaretListener{
        public void caretUpdate(CaretEvent e) {
            Highlighter hl = mTextPane.getHighlighter();
            //when the caret is moved, remove the any link highlights
            hl.removeAllHighlights();

            int dot = e.getDot();
            int mark = e.getMark();
            if((isCtrlPressed) && (mPrevSpan == -1)){
                mPrevSpan = dot;
            }
            else if(isCtrlPressed && mPrevSpan != -1){
                showLinkPopup(mPrevSpan, dot);
                isCtrlPressed = false;
                mPrevSpan =-1;
            }

            // mod by krim.
            // Not just set start and end field to caret selection,
            // but clear the spans set first, then fill it with caret selection span.
            // Consequently the array get one int[] in it.

            // krim: if in normal mode, need to clear first, to remove default (-1, -1) pair
            if (isSpansEmpty()) {
                mSpans.clear();
            }
            int start, end;
            if (dot!=mark){         // mouse is dragged
                isTextSelected =true;
                if(dot<mark){
                    start=dot;
                    end=mark;
                } else {
                    start=mark;
                    end=dot;
                }
                int[] newSpan = new int[]{start, end};
                boolean dup = false;
                for (int[] span : mSpans) {
                    if (span[0] == newSpan [0] && span[1] == newSpan[1]) {
                        dup = true;
                        break;
                    }
                }
                if (!dup) {
                    mSpans.add(new int[]{start, end});
                }
            } else {                // no span selected (eg> single click)
                // krim: in normal mode, reset spans to (-1, -1)
                if (mMode != MSPAN) {
                    isTextSelected = false;
                    resetSpans();
                }
            }

            // highlight corresponding row of table, in normal mode
            // TODO make table highlighting also for MSPAN mode
            if (mMode != MSPAN) {
                findHighlightRows();
            }

            // add by krim: need to update current selection and status bar
            if (!isSpansEmpty()) {
                highlightTextSpans(hl, mSpans, mDefHL);
            }
            updateStatusBar();

        }

    }

    /**
     * The class that listens to the link creation window and
     * creates a link when the information is set and the
     * user clicks OK.
     *
     */
    private class linkListener implements ActionListener{
        public void actionPerformed(ActionEvent e){
            clearTableSelections();
            //check to make sure that linkFrom, linkName, and linkTo
            //are all valid ids/link names

            linkFrom = linkFrom.split(" \\(")[0];
            String from_id = linkFrom.split(" - ")[1];
            String from_type = linkFrom.split(" - ")[0];

            linkTo = linkTo.split(" \\(")[0];
            String to_id = linkTo.split(" - ")[1];
            String to_type = linkTo.split(" - ")[0];
            String from_text = getTextByID(linkFrom.split(" - ")[0],from_id);
            String to_text = getTextByID(linkTo.split(" - ")[0],to_id);

            //add link to appropriate table
            JTable tab = mElementTables.get(linkName);
            DefaultTableModel tableModel = (DefaultTableModel)tab.getModel();

            String[] newdata = new String[tableModel.getColumnCount()];
            for(int i=0;i<tableModel.getColumnCount();i++){
                newdata[i]="";
            }
            //get the Elem that the table was based on, and go through
            //the attributes.  Put in the start and end bits
            Hashtable<String,Elem> elements = mTask.getElemHash();
            Elem elem = elements.get(linkName);

            //get ID number for link
            String newID = "";
            ArrayList<Attrib> attributes = elem.getAttributes();
            for(int i=0;i<attributes.size();i++){
                Attrib a = attributes.get(i);
                if(a instanceof AttID){
                    newID= mTask.getNextID(linkName);
                    newdata[i]=newID;
                }
                if((a instanceof AttData) &&
                        (attributes.get(i).getName().equals("fromID"))){
                    newdata[i]=from_id;
                }
                if((a instanceof AttData) &&
                        (attributes.get(i).getName().equals("toID"))){
                    newdata[i]=to_id;
                }
                if((a instanceof AttData) &&
                        (attributes.get(i).getName().equals("fromText"))){
                    newdata[i]=from_text;
                }
                if((a instanceof AttData) &&
                        (attributes.get(i).getName().equals("toText"))){
                    newdata[i]=to_text;
                }
            }
            tableModel.addRow(newdata);
            tab.clearSelection();
            tab.setRowSelectionInterval(tableModel.getRowCount() - 1, tableModel.getRowCount() - 1);
            Rectangle rect =  tab.getCellRect(tableModel.getRowCount()-1, 0, true);
            tab.scrollRectToVisible(rect);

            mTask.addToDB(newID, linkName, from_id, from_type, to_id, to_type, true);

            //reset variables
            linkFrom="";
            linkName="";
            linkTo="";

            mLinkPopupFrame.setVisible(false);
        }

    }

    /**
     * Listens to the link creation window and sets global
     * variables for each link anchor and the link type.
     *
     */
    private class jboxListener implements ActionListener{
        public void actionPerformed(ActionEvent e){
            JComboBox box = (JComboBox)e.getSource();
            String select = (String)box.getSelectedItem();
            if (e.getActionCommand().equals("fromID")){
                linkFrom = select;
            }
            else if (e.getActionCommand().equals("link")){
                linkName = select;
            }
            else if (e.getActionCommand().equals("toID")){
                linkTo = select;
            }
        }
    }

    /**
     * JTableListener determines if the ID of a tag has
     * been double-clicked, and if it has it highlights the
     * appropriate text extent/extents.
     */
    private class TableMouseAdapter extends MouseAdapter {
        public void mousePressed(MouseEvent e){
            if (SwingUtilities.isLeftMouseButton(e)){
                if (isCtrlPressed) {
                }
            }
            maybeShowTablePopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            maybeShowTablePopup(e);
        }

        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount()==2) {
                int index = mBottomTable.getSelectedIndex();
                String title = mBottomTable.getTitleAt(index);
                JTable tab = mElementTables.get(title);
                Elem el = mTask.getElemHash().get(title);
                Highlighter hl = mTextPane.getHighlighter();
                hl.removeAllHighlights();

                if (el instanceof ElemExtent) {
                    int selectedRow = tab.getSelectedRow();

                    // mod by krim: use table column[1] to get spanString then parse it
                    ArrayList<int[]> spansSelect
                            = parseSpansString((String) tab.getValueAt(selectedRow, 1));
                    highlightTextSpans(hl, spansSelect, mOrangeHL);
                } //end if ElemExtent

                // krim: below block is used to highlight linked extents
                // "from" extent get yellow color,
                // "to" extent get green color,
                if(el instanceof ElemLink){
                    int selectedRow = tab.getSelectedRow();
                    String fromSelect = (String)tab.getValueAt(selectedRow,1);
                    String toSelect = (String)tab.getValueAt(selectedRow,3);

                    ArrayList<int[]> fromSpans
                            = parseSpansString(mTask.getLocByID(fromSelect));
                    ArrayList<int[]> toSpans
                            = parseSpansString(mTask.getLocByID(toSelect));
                    if (toSpans != null) {
                        highlightTextSpans(hl, toSpans, mGreenHL);
                    }
                    // mod by krim: since highlightTextSpans() moves cursor,
                    // fromSpans need to be highlighted later
                    if (fromSpans != null) {
                        highlightTextSpans(hl, fromSpans, mGrayHL);
                    }
                }//end if ElemLink
            }
        }

        //if the user right-clicks on a link
        private void maybeShowTablePopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                mTablePopup = tableContextMenu();
                mTablePopup.show(e.getComponent(),
                        e.getX(), e.getY());
            }
        }
    }

    /**
     * PopupListener determines whether the link
     * creation window should be displayed.
     */
    private class TextMouseAdapter extends MouseAdapter {
        public void mousePressed(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e)){
                if (isCtrlPressed) {
                }
            }
            maybeShowTextPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            maybeShowTextPopup(e);
        }


        private void maybeShowTextPopup(MouseEvent e) {
            if (e.isPopupTrigger() && isTextSelected) {
                mTextPopup = textContextMenu();
                mTextPopup.show(e.getComponent(),
                        e.getX(), e.getY());
            }
        }
    }

    /**
     * Change mode to normal
     * add by krim
     */
    private class ExitModeListener implements ActionListener {
        public void actionPerformed(ActionEvent actionEvent) {
            returnToNormalMode();
            new Timer().schedule(new TimedUpdateStatusBar(), 3000);
        }
    }

    /**
     * Change text selection mode to multiple span mode
     * add by krim
     */
    private class MultiSpanListener implements ActionListener {
        public void actionPerformed(ActionEvent actionEvent) {
            mMode = MSPAN;
            mSpans.clear();
            updateMenus();
            mStatusBar.setText("Entering Multi-span mode! Click anywhere to continue.");
            new Timer().schedule(new TimedUpdateStatusBar(), 3000);
        }
    }

    /**
     * Remove last selected text span from spans list
     * Used only in multi-span mode
     * add by krim
     */
    private class UndoSelectListener implements ActionListener {
        public void actionPerformed(ActionEvent actionEvent) {
            if (mSpans.size()>0) {
                int[] lastSpan = mSpans.remove(mSpans.size()-1);
                ArrayList<int[]> tmp = new ArrayList<int[]>();
                tmp.add(lastSpan);

                Highlighter hl = mTextPane.getHighlighter();
                hl.removeAllHighlights();
                highlightTextSpans(hl, tmp, mGrayHL);
                highlightTextSpans(hl, mSpans, mDefHL);

                mStatusBar.setText("Removed '" + getTextBetween(lastSpan[0], lastSpan[1]) + "'!" +
                        " Click anywhere to continue.");
            } else {
                mStatusBar.setText("No text selected! Click anywhere to continue.");
            }
            new Timer().schedule(new TimedUpdateStatusBar(), 3000);

        }
    }

    // end Section: classes
    // *******************************


    // *******************************
    // Section: tag/database processing methods

    /**
     * This takes the hashCollection created by the XMLHandler
     * and loads it into the tables and database
     *
     * @param newTags the HashCollection passed from XMLHandler
     */
    private void processTagHash(HashCollection<String,Hashtable<String,String>>  newTags){
        ArrayList<String> elements = newTags.getKeyList();
        //first, add the extent tags

        for (String element : elements) {

            Elem elem = mTask.getElemHash().get(element);
            if (elem instanceof ElemExtent &&
                    mElementTables.containsKey(element)) {
                /*for each element type there is a list of tag information*/
                ArrayList<Hashtable<String, String>> tags = newTags.get(element);

                for (Hashtable<String, String> tag : tags) {
                    if (updateIDandDB(tag, element)) {
                        addRowFromHash(tag, element);
                    }
                }
                mTask.batchExtents();
            }
        }
        //then, go back and add the link tags (since they rely on the extent tag
        //info, the need to be added later
        for (int i = 0;i<elements.size();i++){
            String elemName = elements.get(i);
            Elem elem = mTask.getElemHash().get(elemName);
            if(elem instanceof ElemLink &&
                    mElementTables.containsKey(elemName)){
                /*for each element type there is a list of tag information*/
                ArrayList<Hashtable<String,String>> tags = newTags.get(elemName);

                for (Hashtable<String, String> tag : tags) {
                    if (updateIDandDB(tag, elemName)) {
                        addRowFromHash(tag, elemName);
                    }
                }
                mTask.batchLinks();
            }
        }
        //set colors for the whole document at once
        assignTextColors();
    }

    /**
     * addExtentToDBFromHash is called for each
     * tag in the HashCollection used in processTagHash.
     *
     * @param a the Hashtable with the attribute information
     * @param elemName the name of the tag being processed
     * @param newID the ID of the tag being added
     */
    private void addExtentToDBFromHash(Hashtable<String,String> a,String elemName,String newID){

        // mod by krim
        // take a string of spans and init a set of spans(start-end int pairs)
        String spansString = a.get("spans");
        mSpans = parseSpansString(spansString);
        if (!isSpansEmpty()) {
            for (int[] span : mSpans) {
                int start = span[0], end = span[1];
                for (int i = start; i < end; i++) {
                    mTask.addToDB(i, elemName, newID, false);
                }
            }
        } else {
            mTask.addToDB(-1, elemName, newID, false);
        }
        // mod by krim: resetting start-end for NC tag addition
        resetSpans();
    }

    /**
     * addLinkToDBFromHash is called for each
     * tag in the HashCollection used in processTagHash.
     *
     * @param a the Hashtable with the attribute information
     * @param elemName the name of the tag being processed
     * @param newID the ID of the tag being added
     */
    private void addLinkToDBFromHash(Hashtable<String,String> a,String elemName,String newID){
        //getElementByID
        String from_id = a.get("fromID");
        String to_id = a.get("toID");
        String from_type = mTask.getElementByID(from_id);
        String to_type = mTask.getElementByID(to_id);
        mTask.addToDB(newID, elemName, from_id, from_type, to_id, to_type, false);
    }

    /**
     * updateIDandDB sends tag information to the database, and
     *  returns a boolean that indicates whether or not the
     * tag was successfully added.
     *
     * @param a the Hashtable of tag attributes
     * @param elemName the name of the tag
     * @return a boolean indicating whether the transaction was successful
     */
    private boolean updateIDandDB(Hashtable<String,String> a,String elemName){
        Hashtable<String,Elem> elements = mTask.getElemHash();
        Elem elem = elements.get(elemName);
        ArrayList<Attrib> attributes = elem.getAttributes();
        for (Attrib attribute : attributes) {
            if (attribute instanceof AttID) {
                String newID = a.get(attribute.getName());
                if (!mTask.idExists(elemName, newID)) {
                    if (elem instanceof ElemExtent) {
                        addExtentToDBFromHash(a, elemName, newID);
                    } else if (elem instanceof ElemLink) {
                        addLinkToDBFromHash(a, elemName, newID);
                    }
                } else {
                    System.out.println("ID " + newID + " already exists.  Skipping this " + elemName + " tag");
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * addRowFromHash is called when new tag information has been added to
     * the database successfully, and will now be added to the appropriate
     * tag table.
     *
     * @param a Hashtable of attributes
     * @param elemName type of tag being added
     */
    private void addRowFromHash(Hashtable<String,String> a, String elemName){
        JTable tab = mElementTables.get(elemName);
        DefaultTableModel tableModel = (DefaultTableModel)tab.getModel();
        String[] newdata = new String[tableModel.getColumnCount()];
        for (int k=0;k<tableModel.getColumnCount();k++) {
            String colName = tableModel.getColumnName(k);
            String value = a.get(colName);
            if(value!=null){
                newdata[k]=value;
            } else {
                newdata[k]="";
            }
        }
        tableModel.addRow(newdata);
    }

    /**
     * Removes links from the table and DB
     * @param links HashCollection of types and IDs of links being removed
     */
    private void removeLinkTableRows(HashCollection<String,String> links){
        ArrayList<String> linkTags = links.getKeyList();
        for (String tag : linkTags) {
            Elem elem = mTask.getElem(tag);
            ArrayList<String> link_ids = links.getList(elem.getName());
            if (elem instanceof ElemLink) {
                for (String id : link_ids) {
                    removeTableRows(elem, id);
                }
            }
        }
    }

    /**
     * This removes the table rows containing the id given.
     * If the id belongs to and extent tag, then it recolors the
     * related text portion.
     *
     * @param elem type of tag being removed
     * @param id ID of tag being removed
     */
    private void removeTableRows(Elem elem, String id){
        JTable tab = mElementTables.get(elem.getName());
        DefaultTableModel tableModel = (DefaultTableModel)tab.getModel();
        int rows = tableModel.getRowCount();
        //has to go backwards or the wrong rows get deleted
        for (int i=rows-1;i>=0;i--){
            String value = (String)tableModel.getValueAt(i,0);
            if (value.equals(id)){
                //redo color for this text--assumes that lines
                //have already been removed from the DB
                if(elem instanceof ElemExtent){
                    String spanString = (String) tableModel.getValueAt(i,1);
                    assignTextColor(parseSpansString(spanString));
                }
                tableModel.removeRow(i);
            }
        }
    }

    /**
     * Returns the text associated with an id.  Checks the table so that if there
     * is a note entered for a non-consuming tag, that information will be there
     *
     * @param elem the type of tag of the text being looked for
     * @param id The ID of the tag associated with the text being looked for
     * @return the text being searched for
     */
    private String getTextByID(String elem, String id){
        String text = "";
        JTable tab = mElementTables.get(elem);
        DefaultTableModel tableModel = (DefaultTableModel)tab.getModel();
        int rows = tableModel.getRowCount();
        int idCol = -1;
        int textCol = -1;
        int cols = tableModel.getColumnCount();
        for(int i=0;i<cols;i++){
            String colname = tableModel.getColumnName(i);
            if(colname.equalsIgnoreCase("id")){
                idCol = i;
            } else if(colname.equalsIgnoreCase("text")){
                textCol = i;
            }
        }

        for (int i=rows-1;i>=0;i--){
            String value = (String)tableModel.getValueAt(i,idCol);
            if (value.equals(id)){
                text = (String)tableModel.getValueAt(i,textCol);
            }
        }
        return text;
    }

    /**
     * Finds which rows in the table get highlighted based
     * on the span that was selected in the text panel
     */
    private void findHighlightRows(){
        clearTableSelections();
        //first, get ids and types of elements in selected extents
        // mod by krim: getTagsSpan need to take 'spans', not start & end
        HashCollection<String,String> idHash = mTask.getTagsSpan(mSpans);
        if (idHash.size()>0){
            ArrayList<String> elems = idHash.getKeyList();
            for (String e : elems) {
                ArrayList<String> ids = idHash.get(e);
                for (String id : ids) {
                    highlightTableRows(e, id);
                    //returns HashCollection of link ids connected to this
                    HashCollection<String, String> links
                            = mTask.getLinksByExtentID(e, id);
                    if (links.size() > 0) {
                        highlightTableRowsHash(links);
                    }
                }
            }
        }
    }

    /**
     * Adds extent tags to the database, one tag per character location
     * mod by krim: to use list(spans), not 2 integers(start/end)
     *
     * @param element the type of tag being added
     * @param id the ID of the tag being added
     */
    private void addTags(String element, String id){
        if(!isSpansEmpty()) {
            for (int[] span : mSpans) {
                int start = span[0], end = span[1];
                for (int i = start; i < end; i++) {
                    mTask.addToDB(i, element, id, true);
                }
            }
        }
        else {
            mTask.addToDB(-1, element, id, true);
        }
    }


    // *******************************
    // Section: GUI methods
    // the methods that create/display GUI modules

    /**
     * Separate function used to highlight link rows
     * associated with selected extents.
     *
     * @param hash Hashtable with tag names as keys and
     * IDs as values
     */
    private void highlightTableRowsHash(HashCollection<String,String> hash){
        ArrayList<String> elems = hash.getKeyList();
        for (String e : elems) {
            ArrayList<String> ids = hash.get(e);
            for (String id : ids) {
                highlightTableRows(e, id);
            }
        }
    }

    /** this method is for coloring/underlining text
     *  in the entire text window.  It is called only when
     *  a new file is loaded
     */
    private void assignTextColors(){
        //Get hashCollection of where tags are in the document
        //    <String location,<String elements>>.
        HashCollection<String,String>elems = mTask.getElementsAllLocs();
        ArrayList<String> locations = elems.getKeyList();
        for (String location : locations) {
            ArrayList<String> elements = elems.getList(location);
            if (elements.size()>1){
                setColorAtLocation(mColorTable.get(elements.get(0)),Integer.parseInt(location),1,true);
            } else {
                setColorAtLocation(mColorTable.get(elements.get(0)),Integer.parseInt(location),1,false);
            }
        }
    }

    /**
     *     This method is for coloring/underlining text
     *  in the text window.  It detects overlaps, and
     *  should be called every time a tag is added
     *  or removed.
     *
     *  mod by krim to support multiple spans
     *
     * @param spans a sorted set of all spans
     */
    private void assignTextColor(ArrayList<int[]> spans){
        //go through each part of the word being changed and
        //  find what tags are there, and what color it should be.
        for (int[] span : spans) {
            int start = span[0], end = span[1];
            assignTextColor(start, end);
        }
    }

    /**
     *  This method is for coloring/underlining text
     *  in the text window.  It detects overlaps, and
     *  should be called every time a tag is added
     *  or removed.
     *
     * @param begin the location of the first character in the extent
     * @param end the location of the last character in the extent
     */
    private void assignTextColor(int begin, int end){
        //go through each part of the word being changed and
        //  find what tags are there, and what color it should be.
        for(int i=0;i<end-begin;i++){
            ArrayList<String> c = mTask.getElemntsLoc(begin+i);
            if (c.size()==1) {
                //use color of only tag
                setColorAtLocation(mColorTable.get(c.get(0)),begin+i,1,false);
            } else if (c.size()>1) {
                //set color to that of first tag also set underline
                setColorAtLocation(mColorTable.get(c.get(0)),begin+i,1,true);
            } else {
                //set color to black, remove underline
                setColorAtLocation(Color.black,begin+i,1,false);
            }
        }
    }

    /**
     * Sets the color of a specific span of text.  Called for each
     * extent tag.
     *
     * @param color The color the text will become.
     * Determined by the tag name and colorTable (Hashtable)
     * @param pos the location of the start of the extent
     * @param len the location of the end of the extent
     * @param b whether or not the text will be underlined
     */
    private void setColorAtLocation(Color color, int pos, int len, boolean b){
        DefaultStyledDocument styleDoc =
                (DefaultStyledDocument) mTextPane.getStyledDocument();
        SimpleAttributeSet aset = new SimpleAttributeSet();
        StyleConstants.setForeground(aset, color);
        StyleConstants.setUnderline(aset, b);
        styleDoc.setCharacterAttributes(pos,len,aset,false);
    }

    /**
     * Retrieves the text between two offsets from the document.
     * mod by krim: take a string representing span(s), not 2 integers
     *
     * @param spans text spans
     * @return the text of the tag spans
     */
    private String getTextIn(ArrayList<int[]> spans){
        String text = "";
        for (int[] span : spans) {
            text += getTextBetween(span[0], span[1]) + SPANTEXTTRUNC;
        }
        // fence posting
        return text.substring(0,text.length()-SPANTEXTTRUNC.length());
    }

    /**
     * Retrieves the text between two offsets from the document.
     * (Original Amber's code)
     *
     * @param start start location of the text
     * @param end end location of the text
     * @return the text
     */
    private String getTextBetween(int start, int end){
        DefaultStyledDocument styleDoc
                = (DefaultStyledDocument) mTextPane.getStyledDocument();
        String text;
        try{
            text = styleDoc.getText(start,end-start);
        } catch(Exception e) {
            e.printStackTrace();
            text = "Error getting text from a selected span";
        }
        return text;
    }

    /**
     * Displays the link creation window, populated with the information
     * about the links at each location that was clicked.
     *
     * @param loc location of the first link anchor
     * @param loc2 location of the second link anchor
     */
    private void showLinkPopup(int loc, int loc2){
        JPanel linkPane = new JPanel(new BorderLayout());
        JPanel boxPane = new JPanel(new GridLayout(3,2));
        mLinkPopupFrame = new JFrame();

        JComboBox fromList = new JComboBox();
        fromList.addActionListener(new jboxListener());
        fromList.setActionCommand("fromID");

        HashCollection<String,String> idHash =  mTask.getTagsSpanAndNC(loc,loc+1);
        ArrayList<String> elements = idHash.getKeyList();
        if (elements.size()>0){
            if (elements.size()>1){
                fromList.addItem("");
            }
            for (String element : elements) {
                ArrayList<String> tags = idHash.get(element);
                for (String tag : tags) {
                    //create the string for the table list
                    String puttag = (element + " - " + tag);
                    //get the text for the words by id and element
                    String text = getTextByID(element, tag);
                    puttag = puttag + " (" + text + ")";
                    //add string to JComboBox
                    fromList.addItem(puttag);
                }
            }
        }

        JComboBox linkList = new JComboBox();
        linkList.setActionCommand("link");
        linkList.addActionListener(new jboxListener());

        ArrayList<Elem> taskElements = mTask.getElements();
        //create a tab for each element in the annotation task

        ArrayList<String> linkitems = new ArrayList<String>();

        for (Elem taskElement : taskElements) {
            String name = taskElement.getName();
            if (taskElement instanceof ElemLink) {
                linkitems.add(name);
            }
        }
        if (linkitems.size()>1){
            linkList.addItem("");
        }
        for (String linkitem : linkitems) {
            linkList.addItem(linkitem);
        }


        JComboBox toList = new JComboBox();
        toList.setActionCommand("toID");
        toList.addActionListener(new jboxListener());

        idHash =  mTask.getTagsSpanAndNC(loc2,loc2+1);
        elements = idHash.getKeyList();
        if (elements.size()>0){
            if (elements.size()>1){
                toList.addItem("");
            }
            for (String element : elements) {
                ArrayList<String> tags = idHash.get(element);
                for (String tag : tags) {
                    String puttag = (element +
                            " - " + tag);
                    //get the text for the words by id and element
                    String text = getTextByID(element, tag);
                    puttag = puttag + " (" + text + ")";
                    //add option to JComboBox
                    toList.addItem(puttag);
                }
            }
        }

        JButton makeLink = new JButton("Create Link");
        makeLink.addActionListener(new linkListener());
        boxPane.add(new JLabel("Link from:"));
        boxPane.add(fromList);
        boxPane.add(new JLabel("Link type:"));
        boxPane.add(linkList);
        boxPane.add(new JLabel("Link to:"));
        boxPane.add(toList);
        linkPane.add(boxPane,BorderLayout.CENTER);
        linkPane.add(makeLink,BorderLayout.SOUTH);
        mLinkPopupFrame.setBounds(90, 70, 400, 300);
        mLinkPopupFrame.add(linkPane);
        mLinkPopupFrame.setVisible(true);

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
     * @return the GUI component containing the JTable for the
     * tag provided
     */
    private JComponent makeTablePanel(Elem e) {

        AnnTableModel model = new AnnTableModel();
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
        for (int i=0;i<attributes.size();i++){
            Attrib a = attributes.get(i);
            TableColumn c = table.getColumnModel().getColumn(i);
            if (a instanceof AttList){
                AttList att = (AttList)a;
                JComboBox options = makeComboBox(att);
                c.setCellEditor(new DefaultCellEditor(options));
            }
        }
        return scrollPane;
    }

    /**
     * Removes all the tags from the table when a new DTD is loaded.
     *
     */
    private void resetTabPane(){
        mBottomTable.removeAll();
        ArrayList<Elem> elements = mTask.getElements();
        //create a tab for each element in the annotation task
        for (Elem element : elements) {
            String name = element.getName();
            mBottomTable.addTab(name, makeTablePanel(element));
        }
    }

    /**
     * Create a menuitem for each element in the annotation task
     * when a section of the text is highlighted and right-clicked.
     *
     * @return a pop-up menu with all extent tags listed, as well
     * as information about existing tags at the selected location
     */
    private JPopupMenu textContextMenu() {
        JPopupMenu jp = new JPopupMenu();

        // TODO if in link mode, do not populate menu with elements
        ArrayList<Elem> elements = mTask.getElements();

        for (Elem element : elements) {
            String name = element.getName();
            JMenuItem menuItem = new JMenuItem(name);
            menuItem.addActionListener(new MakeTagListener());
            if (element instanceof ElemExtent) {
                jp.add(menuItem);
            }
        }

        //get a hash collection of the element type and id- add info to
        //the action command for that menuItem
        //this is only for extent tags

        switch (mMode) {
            case MSPAN:
                jp.addSeparator();
                JMenuItem undo = new JMenuItem("Delete the last selected");
                undo.setActionCommand("Delete");
                undo.addActionListener(new UndoSelectListener());
                JMenuItem exit = new JMenuItem("Exit Multi-span Mode");
                exit.setActionCommand("Exit");
                exit.addActionListener(new ExitModeListener());

                jp.add(undo);
                jp.add(exit);
                break;
            case MLINK:
                // TODO if in link mode, retrieve existing tags and populate with (from, to etc) items
                break;

            case NORMAL:
                HashCollection<String, String> idHash
                        = mTask.getTagsSpan(mSpans);
                if (idHash.size() > 0) {
                    jp.addSeparator();
                    ArrayList<String> elems = idHash.getKeyList();
                    for (String elem : elems) {
                        ArrayList<String> ids = idHash.get(elem);
                        for (String id : ids) {
                            String name = "Remove " + id;
                            JMenuItem menuItem = new JMenuItem(name);
                            menuItem.setActionCommand(elem + ", " + id);
                            menuItem.addActionListener(new RemoveExtentTag());
                            jp.add(menuItem);
                        }
                    }
                }
                break;
        }
        return jp;
    }

    /**
     * Creates the menu with the option to remove selected table rows
     * mod by krim: re-named
     *
     * @return GUI menu
     *
     * TODO add menu item to add link or so
     *
     */
    private JPopupMenu tableContextMenu(){
        JPopupMenu jp = new JPopupMenu();
        int index = mBottomTable.getSelectedIndex();
        String title = mBottomTable.getTitleAt(index);
        String action = "Remove selected " + title + " rows";
        JMenuItem menuItem = new JMenuItem(action);
        menuItem.setActionCommand(title);
        menuItem.addActionListener(new RemoveSelectedTableRows());
        jp.add(menuItem);
        return jp;
    }

    /**
     * highlights the row in the table with the given ID
     *
     * @param elem name of the tag type being highlighted
     * @param id id of the tag being highlighted
     */
    private void highlightTableRows(String elem, String id){
        JTable tab = mElementTables.get(elem);
        DefaultTableModel tableModel = (DefaultTableModel)tab.getModel();
        int rows = tableModel.getRowCount();
        for (int i=rows-1;i>=0;i--){
            String value = (String)tableModel.getValueAt(i,0);
            if (value.equals(id)){
                tab.addRowSelectionInterval(tab.convertRowIndexToView(i),tab.convertRowIndexToView(i));
            }
        }
    }

    /** Remove all highlights from table rows
     */
    private void clearTableSelections(){
        for (Enumeration<String> tables = mElementTables.keys(); tables.hasMoreElements();){
            JTable tab = mElementTables.get(tables.nextElement());
            DefaultTableModel tableModel = (DefaultTableModel)tab.getModel();
            int rows = tableModel.getRowCount();
            if(rows>0)
                tab.removeRowSelectionInterval(0,rows-1);
        }
    }

    /**
     * Displays the warning for saving your work before opening a new
     * file or DTD.
     *
     */
    private static void showSaveWarning(){
        JOptionPane save = new JOptionPane();
        save.setLocation(100,100);
        String text = ("Warning! Opening a new file or DTD will \n" +
                "delete any unsaved data.  \nPlease save your data before continuing");
        JOptionPane.showMessageDialog(mMainFrame, text);
    }

    /**
     * Shows message warning that deleting an extent
     * will also delete any links the extent is an anchor in.
     *
     * Currently is shows whether the extent is in a link or not.
     *
     * @return boolean indicating the user accepted the warning or
     * canceled the action.
     */
    private boolean showDeleteWarning(){
        //JOptionPane delete = new JOptionPane();
        String text = ("Deleting extent tag(s) will also delete \n" +
                "any links that use these extents.  Would you like to continue?");

        int message = JOptionPane.showConfirmDialog(mMainFrame,
                text, "Warning!", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        return message == 0;
    }

    /**
     * Shows information about MAE
     * TODO - Add credit
     */
    private void showAboutDialog(){
        JOptionPane about = new JOptionPane();
        about.setLocation(100,100);
        about.setAlignmentX(Component.CENTER_ALIGNMENT);
        about.setAlignmentY(Component.CENTER_ALIGNMENT);
        about.setMessage("MAE \n Multi-purpose Annotation Editor \n" +
                "Version " + VERSION + "\n\n" +
                "Copyright Amber Stubbs\nastubbs@cs.brandeis.edu \n Lab for " +
                "Linguistics and Computation, Brandeis University 2010-2012." +
                "\n\nThis distribution of MAE (the software and the source code) \n" +
                " is covered under the GNU General Public License version 3.\n" +
                "http://www.gnu.org/licenses/");
        JDialog dialog = about.createDialog(mMainFrame, "About MAE");
        dialog.setVisible(true);
        //about.showMessageDialog(mMainFrame, text);
    }

    /**
     * Creates a drop-down comboBox for the table from the
     * AttList attribute
     *
     * @param att a list-type attribute
     * @return comboBox with attribute options
     */
    private JComboBox makeComboBox(AttList att){
        //makes comboBox from List-type attribute
        JComboBox options = new JComboBox();
        options.addItem("");
        for(int j=0;j<att.getList().size();j++){
            options.addItem(att.getList().get(j));
        }
        return options;
    }

    /**
     * assigns colors to the elements in the DTD
     */
    private void assignColors(){
        ArrayList<String> elements = mTask.getExtentElements();
        for (int i=0;i<elements.size();i++){
            int l = colors.length;
            int k = i;
            if (i>=l){
                k = i%l;
            }
            mColorTable.put(elements.get(i), colors[k]);
        }
    }

    /**
     * Creates the File menu for the top bar
     *
     * @return JMenu with all available options
     */
    private JMenu createFileMenu() {
        JMenu menu = new JMenu("File");
        JMenuItem loadDTD = new JMenuItem("Load DTD");
        loadDTD.setActionCommand("Load DTD");
        loadDTD.addActionListener(new getFile());
        menu.add(loadDTD);

        JMenuItem loadFile = new JMenuItem("Load File");
        loadFile.setActionCommand("Load File");
        loadFile.addActionListener(new getFile());
        if (!mTask.hasDTD()) {
            loadFile.setEnabled(false);
        } else {
            loadFile.setEnabled(true);
        }
        menu.add(loadFile);

        menu.addSeparator();
        JMenuItem saveFileRTF = new JMenuItem("Create RTF");
        saveFileRTF.setActionCommand("Save RTF");
        saveFileRTF.addActionListener(new getFile());
        if (!isFileOpen) {
            saveFileRTF.setEnabled(false);
        } else {
            saveFileRTF.setEnabled(true);
        }
        menu.add(saveFileRTF);
        menu.addSeparator();

        JMenuItem saveFileXML = new JMenuItem("Save File As XML");
        saveFileXML.setActionCommand("Save XML");
        saveFileXML.addActionListener(new getFile());
        if(!isFileOpen){
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
    private JMenu createDisplayMenu(){
        JMenu menu = new JMenu("Display");

        JMenuItem increaseFont = new JMenuItem("Font Size ++");
        increaseFont.setActionCommand("Font++");
        increaseFont.addActionListener(new DisplayListener());

        menu.add(increaseFont);

        JMenuItem decreaseFont = new JMenuItem("Font Size --");
        decreaseFont.setActionCommand("Font--");
        decreaseFont.addActionListener(new DisplayListener());

        menu.add(decreaseFont);

        if(mTask.hasDTD()){
            menu.addSeparator();
            JMenu linkDisplay = new JMenu("Show linked extents");
            ArrayList<String> links = mTask.getLinkElements();
            for (String e : links) {
                JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(e);
                menuItem.addActionListener(new DisplayLinkListener());
                String command = "displayLinks:" + e;
                menuItem.setActionCommand(command);
                mDisplayingLinkTypeOf.put(command, 0);
                linkDisplay.add(menuItem);

            }
            menu.add(linkDisplay);
        }

        return menu;

    }

    /**
     * Creates the menu with non-consuming tag options
     *
     * @return JMenu for creating non-consuming tags
     */
    private JMenu createNCMenu(){
        JMenu menu = new JMenu("NC elements");

        if(mTask.hasDTD()){
            ArrayList<Elem> nc = mTask.getNCElements();

            if (nc.size() == 0) {
                JMenuItem none = new JMenuItem("no NC elements");
                none.setEnabled(false);
                menu.add(none);
            } else {
                for (Elem e : nc) {
                    JMenuItem menuItem = new JMenuItem(e.getName());
                    menuItem.addActionListener(new MakeTagListener());
                    menuItem.setActionCommand("addN-C:" + e.getName());
                    if (!isFileOpen) {
                        menuItem.setEnabled(false);
                    }
                    menu.add(menuItem);
                }
            }
        }
        else {
            JMenuItem none = new JMenuItem("no NC elements");
            none.setEnabled(false);
            menu.add(none);
        }

        return menu;
    }

    /**
     * Creates the Help menu for MAE
     *
     * @return JMenu Help for the top bar
     */
    private JMenu createHelpMenu(){
        JMenu menu = new JMenu("Help");
        JMenuItem about = new JMenuItem("About MAE");
        about.addActionListener(new AboutListener());
        menu.add(about);
        return menu;
    }

    /**
     * Creates the menu for special input modes
     * add by krim
     *
     * @return JMenu Mode for the menu bar
     */
    private JMenu createModeMenu() {
        JMenu menu = new JMenu("Mode");
        JMenuItem multiSpan = new JMenuItem("Multi-span Mode");
        if(mMode != NORMAL){
            multiSpan.setEnabled(false);
        } else {
            multiSpan.setEnabled(true);
        }
        multiSpan.addActionListener(new MultiSpanListener());

        JMenuItem multiLink = new JMenuItem("Multi-link Mode");
        multiLink.setEnabled(false);
        JMenuItem mlInfo = new JMenuItem("Multi-link menu currently not working.");
        mlInfo.setEnabled(false);

        JMenuItem exitMode = new JMenuItem("Exit to Normal Mode");
        if(mMode != NORMAL){
            exitMode.setEnabled(true);
        } else {
            exitMode.setEnabled(false);
        }
        exitMode.addActionListener(new ExitModeListener());

        menu.add(multiSpan);
        menu.addSeparator();
        menu.add(multiLink);
        menu.add(mlInfo);
        menu.addSeparator();
        menu.add(exitMode);

        return menu;
    }

    /**
     * Refreshes the GUI menus when a new DTD or file is loaded
     */
    private void updateMenus(){
        mMenuBar.removeAll();

        // mod by krim: some menus are used only after a file is loaded
        mFileMenu = createFileMenu();
        mMenuBar.add(mFileMenu);
        if (isFileOpen) {
            mDisplayMenu = createDisplayMenu();
            mMenuBar.add(mDisplayMenu);
            mNCMenu = createNCMenu();
            mMenuBar.add(mNCMenu);

            // add by krim: special mode menu
            mModeMenu = createModeMenu();
            mMenuBar.add(mModeMenu);
        }

        // krim: note that we don't have to re-create Help menu
        mMenuBar.add(mHelpMenu);

        mMenuBar.updateUI();

    }

    /**
     * Takes a string representing possibly multiple spans of an extent tag
     * Return array of integer pairs
     *
     * @param spansString - string of spans
     * @return a ArrayList of int[]
     */
    protected ArrayList<int[]> parseSpansString(String spansString) {

        // this list will be returned
        ArrayList<int[]> spans = new ArrayList<int[]>();

        // check if the tag being processed is non-consuming
        if (spansString.equals("-1~-1")) {
            spans.add(new int[]{-1, -1});
            return spans;
        }

        // split each span
        String[] pairs = spansString.split(SPANSEPARATOR);
        for (String pair : pairs) {
            int[] span = new int[2];

            // parse start and end points
            span[0] = Integer.parseInt(pair.split(SPANDELIMITER)[0]);
            span[1] = Integer.parseInt(pair.split(SPANDELIMITER)[1]);

            spans.add(span);
        }
        return spans;
    }

    /**
     * Takes an array of integer pairs, then merge it into a string.
     * Each span separated by a comma, start and end point of each span joined with a hyphen.
     *
     * @param spans - an sorted set of integer pairs
     * @return a string representing spans of a tag
     */
    protected String spansToString(ArrayList<int[]> spans) {
        String spanString = "";
        Iterator<int[]> iter = spans.iterator();
        while(iter.hasNext()) {
            int[] span = iter.next();
            if (iter.hasNext()) {
                spanString += span[0] + SPANDELIMITER + span[1] + SPANSEPARATOR;
            } else {
                spanString += span[0] + SPANDELIMITER + span[1];
            }
        }
        return spanString;
    }

    /**
     * Highlight given spans with given highlighter and painter(color)
     *
     * @param hl - Highlighter OBJ from text panel
     * @param spans - desired text spans to be highlighted
     * @param painter - highlighter OBJ with color
     */
    private void highlightTextSpans(Highlighter hl,
                                    ArrayList<int[]> spans,
                                    TextHighlightPainter painter) {

        for (int[] span : spans) {
            int start = span[0], end = span[1];
            try {
                hl.addHighlight(start, end, painter);
                mTextPane.scrollRectToVisible(mTextPane.modelToView(start));
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Updates the status bar mDisplayMenu
     * add by krim
     * TODO add MLINK mode
     *
     */
    private void updateStatusBar() {
        if (!mTask.hasDTD()) {
            mStatusBar.setText("No DTD loaded.");
        } else if (!isFileOpen) {
            mStatusBar.setText("No file loaded.");
        } else {
            switch (mMode) {
                case NORMAL:
                    if (isSpansEmpty()) {
                        mStatusBar.setText("No text selected.");
                    } else {
                        mStatusBar.setText("Selected: " + spansToString(this.mSpans));
                    }
                    break;
                case MSPAN:
                    if (isSpansEmpty()) {
                        mStatusBar.setText("[Multi-span mode] No text selected.");
                    } else {
                        mStatusBar.setText("[Multi-span mode] Selected: " + spansToString(this.mSpans));
                    }
                    break;
                case MLINK:
                    mStatusBar.setText("Multiple link mode: ");
                    break;
            }
        }
    }

    /**
     * Sets MAE mode to Normal
     * add by krim
     */
    private void returnToNormalMode() {

        mMode = NORMAL;
        resetSpans();
        updateMenus();

        mStatusBar.setText("Exit to normal mode! Click anywhere to continue.");
    }

    private Boolean isSpansEmpty() {
        return this.mSpans.size() == 0 || this.mSpans.get(0)[0] == -1;
    }

    /**
     * Resets the selected spans to default non-selection (-1~-1)
     */
    private void resetSpans() {
        this.mSpans.clear();
        if (mMode != MSPAN) {
            this.mSpans.add(new int[]{-1, -1});
        }
    }


    /**
     * Creates the GUI
     */
    private static void createAndShowGUI() {
        JFrame.setDefaultLookAndFeelDecorated(true);

        //Create and set up the window.
        mMainFrame = new JFrame(TITLE_PREFIX);
        mMainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        JComponent newContentPane = new MaeMain();
        newContentPane.setOpaque(true); //content panes must be opaque
        mMainFrame.setContentPane(newContentPane);

        //Display the window.
        mMainFrame.pack();
        mMainFrame.setSize(900,500);
        mMainFrame.setVisible(true);
    }

    /**
     * Main
     *
     * @param args not currently used
     */
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        createAndShowGUI();
                    }
                });
    }


    /**
     * Comparator class to be provided to ArrayList of spans
     */
    private class SpanComparator implements Comparator<int[]> {
        public int compare(int[] x, int[] y) {
            if (x == null || x.length < 2) {
                return y == null || y.length < 2 ? 0 : -1;
            } else if (y == null || y.length < 2) {
                return 1;
            } else {
                if (x[0] == y[0]) {
                    return x[1] - y[1];
                } else {
                    return x[0] - y[0];
                }
            }
        }
    }
}