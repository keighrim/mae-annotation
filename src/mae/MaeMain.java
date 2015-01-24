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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * For feedback, reporting bugs, use the project repo on github
 * <https://github.com/keighrim/mae-annotation>
 *
 * @author Amber Stubss, Keigh Rim
 * @version v0.11
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
 */

public class MaeMain extends JPanel {

    // add by krim: constant strings to be used in GUI title bar
    protected final static String PROJECT_WEBPAGE = "https://github.com/keighrim/mae-annotation";
    protected final static String CUR_YEAR = "2015";
    protected final static String VERSION = "0.11.6";
    protected final static String TITLE_PREFIX = "MAE " + VERSION;

    protected final static String COMBO_DELIMITER = " - ";
    // add by krim: constant string to be used in string representation of spans
    protected final static String SPANDELIMITER = "~";
    protected final static String SPANSEPARATOR = ",";
    protected final static String SPANTEXTTRUNC = " ... ";

    private static final long serialVersionUID = 9404268L;

    private Hashtable<String, Color> mColorTable;
    private Hashtable<String,Integer> mDisplayingLinkTypeOf;

    //Here is where to change the colors that get assigned to tags
    // these are for text colors
    private Color mBlue = Color.blue;
    private Color mRed = Color.red;
    private Color mDarkGreen = new Color(12,153,72);
    private Color mMagenta = Color.magenta;
    private Color mDarkOrange = new Color(153,102,0);
    private Color mDarkPink = new Color(170, 85, 133);
    private Color mDarkBlue = new Color(42, 92, 206);
    private Color mLightBlue = new Color(11,162,188);
    private Color mOrange = new Color (234,160,0);
    private Color mPurple = new Color(102,75,153);
    private Color mGray = Color.darkGray;
    
    
    private Color[] mColors = {
            mBlue, mRed, mDarkGreen, mMagenta, mDarkOrange, mDarkPink, mDarkBlue,
            mLightBlue, mOrange, mPurple, mGray };

    // thses are for highlighter colors
    private Color mLightOrange = new Color(255,204,51);
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
    private boolean isTextSelected;
    private boolean isCtrlPressed;

    // add by krim: additional booleans to keep track of annotation mode
    private final int M_NORMAL = 0;
    private final int M_MULTI_SPAN = 1;
    private final int M_MULTI_ARG = 2;
    private int mMode;


    //ints and Strings that are handy to have widely available
    private ArrayList<int[]> mSpans; // added krim
    // instead of using 2 integers, start & end, now we use a set of tuples

    // variables for link creation
    private LinkedList<String> mUnderspecified;
    private ArrayList<String[]> mPossibleArgs;
    private String mFileName;
    private String mXmlName;
    private String ADD_NC_COMMAND = "ADDNC:";
    private String ADD_LINK_COMMAND = "ADDLINK:";
    static final String SEP = "@#";
    static final String ID_SUF = "ID";

    //GUI components
    private static JFrame mMainFrame;
    private JFrame mLinkPopupFrame;
    private JScrollPane mScrollPane;
    private Hashtable<String, JTable> mElementTables;
    private JTabbedPane mBottomTable;
    private JTextPane mTextPane;
    private JPanel mTopPanel;

    private JMenuBar mMenuBar;
    private JMenu mFileMenu;
    private JMenu mNCMenu;
    private JMenu mLinkMenu;
    private JMenu mDisplayMenu;
    // add by krim: this mode menu will be used for toggling special input mode
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

        mMode = M_NORMAL;
        // mod by krim: init start-end to (-1, -1) pair
        mSpans = new ArrayList<int[]>();
        resetSpans();

        mUnderspecified = new LinkedList<String>();
        mPossibleArgs = new ArrayList<String[]>();

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
        mHelpMenu = createHelpMenu("Help");
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
    private class FileMenuListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent actionEvent){
            int returnVal;
            String command = actionEvent.getActionCommand();
            if (command.equals("Load DTD")) {
                if (isFileOpen) {
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
                        mDisplayingLinkTypeOf.clear();
                        assignColors();
                        resetTabPane();

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

            } else if (command.equals("Load File")) {
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
                        mTask.resetDb();
                        mTask.resetIdTracker();

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

            } else if (command.equals("Save RTF")) {
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

            } else if (command.equals("Save XML")) {
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
    private class HelpMenuListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent actionEvent){
            String command = actionEvent.getActionCommand();
            if (command.equals("about")) {
                showAboutDialog();
            } else if (command.equals("web")) {
                if(Desktop.isDesktopSupported())
                {
                    try {
                        Desktop.getDesktop().browse(new URI(PROJECT_WEBPAGE));
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    } catch (URISyntaxException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Class that changes the size of the text from the top menu
     */
    private class FontSizeMenuListener implements ActionListener{
        public void actionPerformed(ActionEvent actionEvent){
            String command = actionEvent.getActionCommand();
            if (command.equals("Font++")){
                Font font = mTextPane.getFont();
                Font font2 = new Font(font.getName(),font.getStyle(),font.getSize()+1);
                mTextPane.setFont(font2);
                mBottomTable.setFont(font2);
            } else if (command.equals("Font--")){
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
                String command = actionEvent.getActionCommand();
                Elem elem = mTask.getElem(command);
                JTable tab = mElementTables.get(command);
                int[] selectedViewRows = tab.getSelectedRows();

                //convert the rows of the table view into the rows of the
                //table model so that the correct rows are deleted
                // krim - why did she copy a int[] to int[] ?
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
                    mTask.removeExtentByID(command, id);
                    if(elem instanceof ElemExtent){

                        // mod by krim: instead of take 2 integers,
                        // take a string of possibly multiple spans
                        String spanString = (String)tableModel.getValueAt(row,1);
                        ArrayList<int[]> spans = parseSpansString(spanString);

                        assignTextColor(spans);
                        HashCollection<String,String> links
                                = mTask.getLinksByExtentID(command,id);
                        //remove links that use the tag being removed
                        removeLinkTableRows(links);
                    }
                    tableModel.removeRow(selectedRows[i]);
                }
            }
        }
    }

    /**
     * This is the class that's called 
     * when creating an extent tag (by either popup menu or NC menu)
     */
    private class MakeTagListener implements ActionListener {
        public void actionPerformed(ActionEvent actionEvent) {
            String command = actionEvent.getActionCommand();

            // first get tag type; is it a link? is it a NC tag?
            boolean isLink = false;
            String newTagName;
            if (command.startsWith(ADD_LINK_COMMAND)) {
                newTagName = command.substring(ADD_LINK_COMMAND.length());
                isLink = true;
            } else if (command.startsWith(ADD_NC_COMMAND)) {
                // if the tag being added is non-consuming, make sure
                // it's added with (-1, -1) span
                resetSpans();
                newTagName = command.substring(ADD_NC_COMMAND.length());
            } else {
                newTagName = command;
            }

            JTable tab = mElementTables.get(newTagName);
            DefaultTableModel tableModel = (DefaultTableModel) tab.getModel();

            // get a new ID
            Elem e = mTask.getElem(newTagName);
            String newId = mTask.getNextID(newTagName);

            if (isLink) {
                // get number of arguments associated with this link tag
                ElemLink el = (ElemLink) e;
                int argNum = el.getArgNum();

                // initiate lists with empty strings as dummy arguments and comm with DB
                String[] argIds = new String[argNum], argTypes = new String[argNum];
                Arrays.fill(argIds, "");
                Arrays.fill(argTypes, "");
                addLinkTagToDb(newTagName, newId,
                        Arrays.asList(argIds), Arrays.asList(argTypes));

                // add id of the new tag to underspecified set for further lookup
                System.out.println("IN MAKR TAG LIS: " + newId);
                mUnderspecified.add(newId);
            } else {
                addExtTagToDb(newTagName, newId);
            }

            //  create a dummy data set for inserting new table into table and insert
            String[] newEmptyData = createEmptyRowData(newTagName, newId);
            tableModel.addRow(newEmptyData);

            // move cursor and set focus to newly added tag
            mBottomTable.setSelectedIndex(mBottomTable.indexOfTab(newTagName));
            tab.clearSelection();
            tab.setRowSelectionInterval(
                    tableModel.getRowCount() - 1, tableModel.getRowCount() - 1);
            Rectangle rect = tab.getCellRect(
                    tableModel.getRowCount() - 1, 0, true);
            tab.scrollRectToVisible(rect);

            // assign colors if necessary
            if (!isSpansEmpty()) {
                assignTextColor(mSpans);
            }
            returnToNormalMode();
            new Timer().schedule(new TimedUpdateStatusBar(), 3000);
        }
    }
    
    private String[] createEmptyRowData(String elemName, String newId) {
        // get the target element and a list of its attrib
        Elem e = mTask.getElem(elemName);
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

    private class SetAsArgListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            // command looks like this:
            // linkType(0), linkId(1), argName(2), argId(3), argText(4)
            String[] command = actionEvent.getActionCommand().split(SEP);
            setArgumentInTable(command[0], command[1], command[2], command[3], command[4]);
        }
    }
    
    private void setArgumentInTable(String linkType, String linkId,
                                    String argName, String argId, String argText) {
        DefaultTableModel tableModel
                = (DefaultTableModel) mElementTables.get(linkType).getModel();
        int rows = tableModel.getRowCount();
        int idRow = -1, argCol = -1;

        // first get indices of columns of argument IDs in the table
        TreeSet<Integer> argColumns = getArgColIndices(linkType);

        // then find which row and column to look for
        for (int i = rows - 1; i >= 0; i--) {
            System.out.println("IN SETARG ROW: " + tableModel.getValueAt(i, 0));
            if (tableModel.getValueAt(i, 0).equals(linkId)) {
                idRow = i;
            }
        }
        for (Integer i : argColumns) {
            System.out.println("IN SETARG: " + tableModel.getColumnName(i));
            if (tableModel.getColumnName(i).equals(argName + ID_SUF)) {
                argCol = i;
            }
        }

        // set values for argID and argTEXT
        if (idRow == -1 || argCol == -1) {
            mStatusBar.setText("ERROR! Link ID and Arg name cannot be found in the table");
            new Timer().schedule(new TimedUpdateStatusBar(), 3000);
        } else {
            tableModel.setValueAt(argId, idRow, argCol);
            tableModel.setValueAt(argText, idRow, argCol+1);
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
        public void actionPerformed(ActionEvent actionEvent){
            boolean check = showDeleteWarning();
            if (check){
                String command = actionEvent.getActionCommand();
                Elem elem = mTask.getElem(command.split(", ")[0]);
                //remove rows from DB
                HashCollection<String,String> links;
                //removes extent tags and related link tags from DB, returns HashCollection
                //of link ids for removal from the DB
                String elemType = command.split(", ")[0];
                String id = command.split(", ")[1];
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
    private class LinkDisplayMenuListener implements ActionListener{
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
    private class AnnCaretListener implements CaretListener {
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
                }
            }

            // highlight corresponding row of table
            findHighlightRows();

            // add by krim: need to update current selection and status bar
            if (!isSpansEmpty()) {
                highlightTextSpans(hl, mSpans, mDefHL);
            }
            updateStatusBar();

        }
    }


    /**
     * LinkListner listens to menu items that creating a link tag with arguments
     * from selected text or table rows
     *
     */ 
    private class MultiArgListener implements ActionListener {
        public void actionPerformed(ActionEvent actionEvent) {
            String command = actionEvent.getActionCommand();
            final ElemLink target = (ElemLink) mTask.getElem(command);
            mLinkPopupFrame = new JFrame();

            clearTableSelections();
            // TODO need some thinking...
            if (!isSpansEmpty()) {
                System.out.println("IN MAIN: span empty");
                updateArgList();
//                resetSpans(); // TODO do we need? user can mistakenly open wrong link tag
            }

            JPanel boxPane = new JPanel(new GridLayout(target.getArgNum() + 1, 2));
            
            // information for creating a link tag
            final String newName = command;
            final String newId = mTask.getNextID(newName);
            final String[] argIds = new String[target.getArgNum()];
            final String[] argTypes = new String[target.getArgNum()];

            // OK button
            JButton okay = new JButton("OK");
            okay.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    System.out.println(newId + "||" + newName);
                    System.out.println(Arrays.toString(argIds));
                    System.out.println(Arrays.toString(argTypes));
                    addLinkTagToDb(newName, newId,
                            Arrays.asList(argIds), Arrays.asList(argTypes));
                    // add new row of tag info to the table and set appropriate attributes
                    DefaultTableModel tableModel
                            = (DefaultTableModel) mElementTables.get(newName).getModel();
                    tableModel.addRow(createEmptyRowData(newName, newId));
                    for (int i=0; i<argIds.length; i++) {
                        setArgumentInTable(newName, newId, target.getArguments().get(i), argIds[i],
                                getTextByID(argTypes[i], argIds[i]));
                    }
                    mLinkPopupFrame.setVisible(false);
                    mLinkPopupFrame.dispose();
                }
            });

            // cancel button
            JButton cancel = new JButton("Cancel");
            cancel.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    mLinkPopupFrame.setVisible(false);
                    mLinkPopupFrame.dispose();
                }
            });

            for (int i = 0; i < target.getArgNum(); i++) {
                JComboBox candidates = new JComboBox();
                for (String item : getComboItems(mPossibleArgs, false)) {
                    candidates.addItem(item);
                }
                // front of mPossibleArgs is sorted by selection order
                candidates.setSelectedIndex(i % candidates.getItemCount());
                // set initial argid and argtype from seleted item
                String selected = (String) candidates.getSelectedItem();
                argIds[i] = selected.split(COMBO_DELIMITER)[1];
                argTypes[i] = selected.split(COMBO_DELIMITER)[0];

                // action command is simply index of current argument
                candidates.setActionCommand(String.valueOf(i));
                // action listener for changing selection of an item
                candidates.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        int command = Integer.parseInt(e.getActionCommand());
                        System.out.println(command);
                        JComboBox box = (JComboBox) e.getSource();
                        String item = (String) box.getSelectedItem();
                        System.out.println(item);
                        argIds[command]
                                = item.split(COMBO_DELIMITER)[1];
                        argTypes[command]
                                = item.split(COMBO_DELIMITER)[0];

                    }
                });
                boxPane.add(new JLabel(target.getArguments().get(i)));
                boxPane.add(candidates);

            }
            boxPane.add(okay);
            boxPane.add(cancel);
            mLinkPopupFrame.add(boxPane);
            mLinkPopupFrame.pack();
            mLinkPopupFrame.setTitle(String.format("Creating %s - %s", newName, newId));
            mLinkPopupFrame.setLocation(300, 200);
            mLinkPopupFrame.setVisible(true);

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
                String elemName 
                        = mBottomTable.getTitleAt(mBottomTable.getSelectedIndex());
                JTable tab = mElementTables.get(elemName);
                Elem el = mTask.getElemHash().get(elemName);
                Highlighter hl = mTextPane.getHighlighter();
                hl.removeAllHighlights();

                if (el instanceof ElemExtent) {
                    int selectedRow = tab.getSelectedRow();

                    // use table column[1] to get spanString then parse it
                    ArrayList<int[]> spansSelect
                            = parseSpansString((String) tab.getValueAt(selectedRow, 1));
                    highlightTextSpans(hl, spansSelect, mOrangeHL);
                } //end if ElemExtent

                // krim: below block is used to highlight linked extents
                if(el instanceof ElemLink){
                    int selectedRow = tab.getSelectedRow();
                    
                    // get relevant argument columns
                    TreeSet<Integer> argColumns = getArgColIndices(elemName);
                    
                    int j = 0;
                    for (Integer i : argColumns) {
                        String argId = (String) tab.getValueAt(selectedRow, i);
                        ArrayList<int[]> argSpans
                                = parseSpansString(mTask.getLocByID(argId));
                        highlightTextSpans(hl, argSpans, mHighlighters[j]);
                        j++;
                    }
                }//end if ElemLink
            }
        }

        //if the user right-clicks on a link
        private void maybeShowTablePopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                resetSpans();
                mTablePopup = tableContextMenu(e);
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
            if (e.isPopupTrigger()) {
                mTextPopup = textContextMenu();
                mTextPopup.show(e.getComponent(),
                        e.getX(), e.getY());
            }
        }
    }

    private class ModeMenuListener implements ActionListener {
        public void actionPerformed(ActionEvent actionEvent) {
            int action = Integer.parseInt(actionEvent.getActionCommand());

            switch (action) {
                // return to normal mode
                case M_NORMAL:
                    returnToNormalMode();
                    new Timer().schedule(new TimedUpdateStatusBar(), 3000);
                    break;
                case M_MULTI_SPAN:
                    mMode = M_MULTI_SPAN;
                    mSpans.clear();
                    updateMenus();
                    mStatusBar.setText("Entering Multi-span mode! Click anywhere to continue.");
                    new Timer().schedule(new TimedUpdateStatusBar(), 3000);
                    break;
                case M_MULTI_ARG:

                    break;
            }
        }
    }

    /**
     * Remove last selected text span from spans list
     * Used only in multi-span mode
     * add by krim
     */
    private class UndoSelectListener implements ActionListener {
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

                    mStatusBar.setText("Removed '" + getTextBetween(lastSpan[0], lastSpan[1]) + "' from selection!" +
                            " Click anywhere to continue.");
                } else {
                    mStatusBar.setText("No text selected! Click anywhere to continue.");
                }
            } else if (command.equals("Over")) {
                resetSpans();
                mStatusBar.setText("No text selected! Click anywhere to continue.");

            }
            new Timer().schedule(new TimedUpdateStatusBar(), 3000);
            if (mMode == M_MULTI_ARG) {
//                updateArgList();
            }

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
    private void processTagHash(HashCollection<String,Hashtable<String,String>> newTags){
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
                mTask.runBatchExtents();
            }
        }
        // then, go back and add the link tags
        // since they rely on the extent tag info, they need to be added later
        for (String elemName : elements) {
            Elem elem = mTask.getElemHash().get(elemName);
            if (elem instanceof ElemLink &&
                    mElementTables.containsKey(elemName)) {
                /*for each element type there is a list of tag information*/
                ArrayList<Hashtable<String, String>> tags = newTags.get(elemName);

                for (Hashtable<String, String> tag : tags) {
                    if (updateIDandDB(tag, elemName)) {
                        addRowFromHash(tag, elemName);
                    }
                }
                mTask.runBatchLinks();
            }
        }
        //set colors for the whole document at once
        assignTextColors();
    }

    /**
     * addExtToDbFromHash is called for each
     * tag in the HashCollection used in processTagHash.
     *
     * @param a the Hashtable with the attribute information
     * @param elemName the name of the tag being processed
     * @param newId the ID of the tag being added
     */
    private void addExtToDbFromHash(Hashtable<String, String> a, String elemName, String newId){

        // mod by krim
        // take a string of spans and init a set of spans(start-end int pairs)
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
        // mod by krim: resetting start-end for NC tag addition
        resetSpans();
    }

    /**
     * addLinkToDbFromHash is called for each
     * tag in the HashCollection used in processTagHash.
     *
     * @param a the Hashtable with the attribute information
     * @param elemName the name of the tag being processed
     * @param newId the ID of the tag being added
     */
    private void addLinkToDbFromHash(Hashtable<String, String> a, String elemName, String newId){
        //getElementByID
        ArrayList<String> args = mTask.getArguments(elemName);
        ArrayList<String> argIDs = new ArrayList<String>();
        ArrayList<String> argTypes = new ArrayList<String>();
        for (String arg : args) {
            String argId = a.get(arg+ID_SUF);
            // check id value is a dummy, add the link tag to the underspecified for further lookup
            if (argId.equals("")) {
                mUnderspecified.add(argId);
            }
            String type = mTask.getElementByID(argId);
            argIDs.add(argId);
            argTypes.add(type);
        }
        mTask.addLinkToBatch(elemName, newId, argIDs, argTypes);
        // no need to run batch for each adding, to in at once in processTagHash()
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
        String newId = "";
        for (Attrib attribute : attributes) {
            if (attribute instanceof AttID) {
                newId = a.get(attribute.getName());
            }
        }
        if (!newId.equals("")) {
            if (!mTask.idExists(elemName, newId)) {
                if (elem instanceof ElemExtent) {
                    addExtToDbFromHash(a, elemName, newId);
                } else if (elem instanceof ElemLink) {
                    addLinkToDbFromHash(a, elemName, newId);
                }
            } else {
                System.out.println("ID " + newId + " already exists.  Skipping this " + elemName + " tag");
                return false;
            }
        } else {
            System.out.println("ID was not found");
            return false;
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
        DefaultTableModel tableModel 
                = (DefaultTableModel) mElementTables.get(elemName).getModel();
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
        DefaultTableModel tableModel
                = (DefaultTableModel) mElementTables.get(elem.getName()).getModel();
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
        DefaultTableModel tableModel 
                = (DefaultTableModel) mElementTables.get(elem).getModel();
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
        HashCollection<String,String> idHash = mTask.getTagsIn(mSpans);
        if (idHash.size()>0){
            ArrayList<String> elems = idHash.getKeyList();
            for (String e : elems) {
                ArrayList<String> ids = idHash.get(e);
                for (String id : ids) {
                    highlightTableRows(e, id);
                    //returns HashCollection of link ids connected to this
                    HashCollection<String, String> links
                            = mTask.getLinksByExtentID(e, id);
                    highlightTableRowsHash(links);
                }
            }
        }
    }

    /**
     * Adds an extent tag to the database, one item per character location
     * Note that, id DB, all tag is associated with the location of all characters in the span 
     * , meaning 3 letter word ends up with 3 items in the DB
     * mod by krim: to use list(spans), not 2 integers(start/end)
     *
     * @param elemName the type of tag being added
     * @param newId the ID of the tag being added
     */
    private void addExtTagToDb(String elemName, String newId){
        if(!isSpansEmpty()) {
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
     * Adds a link tag to the database. 
     * Unlike an extent tag, a link tag only occupies one item in the DB
     *
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

    private ArrayList<String> getComboItems(
            ArrayList<String[]> targetTags, boolean emptyFirst) {
        ArrayList<String> items = new ArrayList<String>();
        if (emptyFirst) {
            items.add("");
        }
        for (String[] tag : targetTags) {
            String type = tag[0], id = tag[1];
            // format relevant info, then add to the list
            items.add(String.format("%s%s%s%s%s",
                    type, COMBO_DELIMITER, id, COMBO_DELIMITER, getTextByID(type, id)));
            
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
                AttList att = (AttList) a;
                JComboBox options = makeValidValuesComboBox(att);
                c.setCellEditor(new DefaultCellEditor(options));
            }
            else if (a.isIdRef()) {
                /* TODO need to implement this part, currently not working
                final JComboBox tags = new JComboBox();
                // tags.setModel(new DefaultComboBoxModel(mPossibleArgs));
                
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
     * Removes all the tags from the table when a new DTD is loaded.
     *
     */
    private void resetTabPane(){
        mBottomTable.removeAll();
        ArrayList<Elem> elements = mTask.getElements();
        //create a tab for each element in the annotation task
        for (Elem element : elements) {
            String name = element.getName();
            if (mColorTable.containsKey(name)) {
                ColorRect indicator = new ColorRect(
                        mTextPane.getFont().getSize(), mColorTable.get(name));
                mBottomTable.addTab(name, indicator, makeTablePanel(element));
            } else {
                mBottomTable.addTab(name, makeTablePanel(element));
            }
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

        ArrayList<Elem> elements = mTask.getElements();

        // add menus for creating Ext tags, only if text selected
        if (isTextSelected) {
            addGuideItem(jp, "Create a tag with selected text");
            for (Elem element : elements) {
                String name = element.getName();
                JMenuItem menuItem = new JMenuItem(name);
                menuItem.addActionListener(new MakeTagListener());
                if (element instanceof ElemExtent) {
                    jp.add(menuItem);
                }
            }
            jp.addSeparator();
        }
        // add common menus for NC and Link tag creation
        jp.add(createNCMenu("Create an NC tag"));
        jp.add(createLinkMenu("Create a Link tag"));

        //get a hash collection of the element type and id- add info to
        //the action command for that menuItem
        //this is only for extent tags

        switch (mMode) {
            case M_MULTI_SPAN:
                jp.addSeparator();
                JMenuItem undo = new JMenuItem("Undo last selection");
                undo.setActionCommand("Undo");
                undo.addActionListener(new UndoSelectListener());
                JMenuItem over = new JMenuItem("Start over");
                over.setActionCommand("Over");
                over.addActionListener(new UndoSelectListener());
                JMenuItem exit = new JMenuItem("Exit Multi-span Mode");
                exit.setActionCommand(Integer.toString(M_NORMAL));
                exit.addActionListener(new ModeMenuListener());

                jp.add(undo);
                jp.add(over);
                jp.add(exit);
                break;
            case M_MULTI_ARG:
                // TODO item to "Create a link using these"
                jp.addSeparator();
                undo = new JMenuItem("Undo last selection");
                undo.setActionCommand("Undo");
                undo.addActionListener(new UndoSelectListener());
                over = new JMenuItem("Start over");
                over.setActionCommand("Over");
                over.addActionListener(new UndoSelectListener());
                exit = new JMenuItem("Exit Multi-argument Mode");
                exit.setActionCommand(Integer.toString(M_NORMAL));
                exit.addActionListener(new ModeMenuListener());
                break;

            case M_NORMAL:
                HashCollection<String, String> idHash
                        = mTask.getTagsIn(mSpans);
                if (idHash.size() > 0) {
                    ArrayList<String> elems = idHash.getKeyList();
                    for (String elem : elems) {
                        ArrayList<String> ids = idHash.get(elem);
                        for (String id : ids) {
                            jp.addSeparator();
                            String text = getTextByID(elem, id);
                            if (text.equals("")) {
                                text = "NC tag";
                            }
                            JMenu idItem = new JMenu(String.format(
                                    "%s (%S)", id, text));

                            // add menu items for removing
                            JMenuItem menuItem = new JMenuItem("Remove");
                            menuItem.setActionCommand(elem + ", " + id);
                            menuItem.addActionListener(new RemoveExtentTag());
                            idItem.add(menuItem);

                            // add menu items for adding tag as an arg
                            idItem.add(createSetAsArgMenu(String.format(
                                    "Set %s as an argument of", id), elem, id));
                            jp.add(idItem);
                        }
                    }
                }
                break;
        }
        return jp;
    }

    private JMenu createSetAsArgMenu(String menuTitle, String argType, String argId) {
        JMenu menu = new JMenu(menuTitle);
        
        // waterfall menu top level - link names
        for (String linkType : mTask.getLinkElemNames()) {

            ArrayList<String> linkIds = mTask.getLinkIdsByName(linkType);
            
            // check if a tag in each type of link element exists
            if (linkIds.size() == 0) {
                addGuideItem(menu, String.format("no %s links", linkType));
                continue;
            }
            // add a link type as a menu item, only when it has real tags
            JMenu linkTypeMenu = new JMenu(linkType);
            
            // next level - actual relevant arguments
            for (String argName : mTask.getArguments(linkType)) {
                JMenu linkArgMenu = new JMenu(argName);
                // copy names of all ids (list of ids will be recycled for all args)
                ArrayList<String> id2Add = new ArrayList<String>(linkIds);
                DefaultTableModel tableModel
                        = (DefaultTableModel) mElementTables.get(linkType).getModel();
                int rows = tableModel.getRowCount();
                TreeSet<Integer> argCols = getArgColIndices(linkType);
                int argCol = 0;

                // find which column to look for
                for (Integer i : argCols) {
                    if (tableModel.getColumnName(i).equals(argName + ID_SUF)) {
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
                                // check id is matching first, then check argument is a dummy
                                if (tableModel.getValueAt(i, 0).equals(unspecId) &&
                                        tableModel.getValueAt(i, argCol).equals("")) {
                                    // add guidance item to menu
                                    if (!prior) {
                                        addGuideItem(linkArgMenu, "Underspecifed");
                                        prior = true;
                                    }

                                    // add id as a menu item
                                    JMenuItem unspecIdItem = new JMenuItem(unspecId);
                                    unspecIdItem.addActionListener(new SetAsArgListener());
                                    unspecIdItem.setActionCommand(
                                            linkType + SEP +
                                                    unspecId + SEP +
                                                    argName + SEP +
                                                    argId + SEP +
                                                    getTextByID(argType, argId));
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
                            linkType + SEP +
                                    item + SEP +
                                    argName + SEP +
                                    argId + SEP +
                                    getTextByID(argType, argId));
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
    private JPopupMenu tableContextMenu(MouseEvent event){
        JPopupMenu jp = new JPopupMenu();

        // get the title of current tab
        String elemName 
                = mBottomTable.getTitleAt(mBottomTable.getSelectedIndex());

        // get tab and count selected rows
        JTable tab = mElementTables.get(elemName);
        int clickedRow = tab.rowAtPoint(event.getPoint());
        int selected = tab.getSelectedRowCount();

        // switch selection to clicked row only if one or zero row is selected before
        if (selected <= 1) {
            tab.setRowSelectionInterval(clickedRow, clickedRow);
            selected = 1;
        }

        String remove;
        String setArg;
        switch (selected) {
            case 1:
                // krim - case 1 always means clickedRow is the only selected row
                // thus getting the value from clickedRow is not that hard-coded logic
                String id = (String) tab.getValueAt(clickedRow, 0);

                remove = "Remove " + id;
                JMenuItem removeItem = new JMenuItem(remove);
                removeItem.setActionCommand(elemName);
                removeItem.addActionListener(new RemoveSelectedTableRows());
                jp.add(removeItem);

                if (mTask.getElem(elemName) instanceof ElemExtent) {
                    String target = String.format("%s (%s)", id, getTextByID(elemName, id));
                    setArg = "Set " + target + " as an argument of";
                    jp.add(createSetAsArgMenu(setArg, elemName, id));
                }
                break;
            
            default:
                // when two or more rows are selected, first add "delete all" item
                remove = "Remove selected " + selected + " rows";
                removeItem = new JMenuItem(remove);
                removeItem.setActionCommand(elemName);
                removeItem.addActionListener(new RemoveSelectedTableRows());
                jp.add(removeItem);

                // then if they are extent tags, add item for creating a link with them
                if (mTask.getElem(elemName) instanceof ElemExtent) {
                    // retrieve ids of all selected tags
                    mPossibleArgs.clear();
                    DefaultTableModel tableModel = (DefaultTableModel) tab.getModel();
                    int[] selectedRows = tab.getSelectedRows();
                    int cols = tableModel.getColumnCount();
                    int idCol = -1;
                    for(int i=0;i<cols;i++){
                        String colname = tableModel.getColumnName(i);
                        if(colname.equalsIgnoreCase("id")){
                            idCol = i;
                        }
                    }
                    // put selected tags as argument candidates
                    for (Integer r : selectedRows) {
                        mPossibleArgs.add(new String[]{elemName,
                                (String) tableModel.getValueAt(r, idCol)});
                    }
                    
                    String makeLink = "Create a link tag with selected tags";
                    JMenu makeLinkItem = new JMenu(makeLink);
                    for (String link : mTask.getLinkElemNames()) {
                        JMenuItem linkItem = new JMenuItem(link);
                        linkItem.setActionCommand(link);
                        linkItem.addActionListener(new MultiArgListener());
                        makeLinkItem.add(linkItem);
                    }
                    jp.add(makeLinkItem);
                }
                break;
        }
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


    private TreeSet<Integer> getArgColIndices(String linkType) {

        JTable tab = mElementTables.get(linkType);
        ArrayList<String> argNames = mTask.getArguments(linkType);
        TreeSet<Integer> argColumns = new TreeSet<Integer>();
        for (int i = 0; i < tab.getModel().getColumnCount(); i++) {
            for (String argName : argNames) {
                if (tab.getModel().getColumnName(i).equals(argName + ID_SUF)) {
                    argColumns.add(i);
                }
            }
        }
        return argColumns;
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
     */
    private void showAboutDialog(){
        JOptionPane about = new JOptionPane();
        about.setLocation(100,100);
        about.setAlignmentX(Component.CENTER_ALIGNMENT);
        about.setAlignmentY(Component.CENTER_ALIGNMENT);
        about.setMessage("MAE \n Multi-purpose Annotation Editor \n" +
                "Version " + VERSION + "\n\n" +
                "Developed in Lab for Linguistics and Computation, Brandeis University 2010-" + CUR_YEAR + ".\n\n" +
                "MAE is a free software. " +
                "\nThis distribution of MAE (the software and the source code) \n" +
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
    private JComboBox makeValidValuesComboBox(AttList att){
        //makes comboBox from List-type attribute
        JComboBox options = new JComboBox();
        options.addItem("");
        for(int j=0;j<att.getVaildValues().size();j++){
            options.addItem(att.getVaildValues().get(j));
        }
        return options;
    }

    /**
     * assigns colors to the elements in the DTD
     */
    private void assignColors(){
        ArrayList<String> elements = mTask.getExtElemNames();
        for (int i=0;i<elements.size();i++){
            int l = mColors.length;
            int k = i;
            if (i>=l){
                k = i%l;
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
        loadDTD.setAccelerator(
                KeyStroke.getKeyStroke('N',
                        Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));
        menu.add(loadDTD);

        JMenuItem loadFile = new JMenuItem("Load File");
        loadFile.setActionCommand("Load File");
        loadFile.addActionListener(new FileMenuListener());
        loadFile.setAccelerator(
                KeyStroke.getKeyStroke('O',
                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
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
        saveFileRTF.setAccelerator(
                KeyStroke.getKeyStroke('R',
                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
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
        saveFileXML.setAccelerator(
                KeyStroke.getKeyStroke('S',
                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
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
    private JMenu createDisplayMenu(String menuTitle){
        JMenu menu = new JMenu(menuTitle);

        JMenuItem increaseFont = new JMenuItem("Font Size ++");
        increaseFont.setActionCommand("Font++");
        increaseFont.addActionListener(new FontSizeMenuListener());
        increaseFont.setAccelerator(
                KeyStroke.getKeyStroke('=',
                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

        menu.add(increaseFont);

        JMenuItem decreaseFont = new JMenuItem("Font Size --");
        decreaseFont.setActionCommand("Font--");
        decreaseFont.addActionListener(new FontSizeMenuListener());
        decreaseFont.setAccelerator(
                KeyStroke.getKeyStroke('-',
                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

        menu.add(decreaseFont);

        if(mTask.hasDTD()){
            menu.addSeparator();
            JMenu linkDisplay = new JMenu("Show linked extents");
            ArrayList<String> links = mTask.getLinkElemNames();
            for (String e : links) {
                JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(e);
                menuItem.addActionListener(new LinkDisplayMenuListener());
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
     * Creates the menu for creating link tags
     *
     * @return JMenu for creating link tags
     */
    private JMenu createLinkMenu(String menuTitle){
        JMenu menu = new JMenu(menuTitle);

        if (mTask.hasDTD()) {
            ArrayList<String> linkTypes = mTask.getLinkElemNames();

            if (linkTypes.size() == 0) {
                addGuideItem(menu, "no link tags defined");
            } else {
                for (String linkType : linkTypes) {
                    JMenuItem menuItem = new JMenuItem(linkType);
                    menuItem.addActionListener(new MakeTagListener());
                    menuItem.setActionCommand(ADD_LINK_COMMAND + linkType);
                    if (!isFileOpen) {
                        menuItem.setEnabled(false);
                    }
                    menu.add(menuItem);
                }
            }
        }
        else {
            addGuideItem(menu, "no DTD is loaded");
        }

        return menu;
    }

    /**

     /**
     * Creates the menu with non-consuming tag options
     *
     * @return JMenu for creating non-consuming tags
     */
    private JMenu createNCMenu(String menuTitle){
        JMenu menu = new JMenu(menuTitle);

        if(mTask.hasDTD()){
            ArrayList<Elem> nc = mTask.getNCElements();

            if (nc.size() == 0) {
                addGuideItem(menu, "no NC tag defined");
            } else {
                for (Elem e : nc) {
                    JMenuItem menuItem = new JMenuItem(e.getName());
                    menuItem.addActionListener(new MakeTagListener());
                    menuItem.setActionCommand(ADD_NC_COMMAND + e.getName());
                    if (!isFileOpen) {
                        menuItem.setEnabled(false);
                    }
                    menu.add(menuItem);
                }
            }
        }
        else {
            addGuideItem(menu, "no DTD is loaded");
        }

        return menu;
    }

    /**
     * Creates the Help menu for MAE
     *
     * @return JMenu Help for the top bar
     */
    private JMenu createHelpMenu(String menuTitle){
        JMenu menu = new JMenu(menuTitle);
        HelpMenuListener helpMenuListener = new HelpMenuListener();
        JMenuItem about = new JMenuItem("About MAE");
        about.setActionCommand("about");
        about.addActionListener(helpMenuListener);
        JMenuItem github = new JMenuItem("Visit project website(Github)");
        github.setActionCommand("web");
        github.addActionListener(helpMenuListener);
        github.setAccelerator(KeyStroke.getKeyStroke("F1"));
        menu.add(about);
        menu.addSeparator();
        menu.add(github);

        return menu;
    }

    /**
     * Creates the menu for special input modes
     * add by krim
     *
     * @return JMenu Mode for the menu bar
     */
    private JMenu createModeMenu(String menuTitle) {
        JMenu menu = new JMenu(menuTitle);
        JMenuItem multiSpan = new JMenuItem("Multi-span Mode");
        ModeMenuListener modemenuListen = new ModeMenuListener();
        if(mMode != M_NORMAL){
            multiSpan.setEnabled(false);
        } else {
            multiSpan.setEnabled(true);
        }
        multiSpan.setActionCommand(Integer.toString(M_MULTI_SPAN));
        multiSpan.addActionListener(modemenuListen);
        multiSpan.setAccelerator(
                KeyStroke.getKeyStroke('1',
                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

        JMenuItem multiArgs = new JMenuItem("Multi-link Mode");
        multiArgs.setEnabled(false);
        JMenuItem mlInfo = new JMenuItem("Multi-link menu currently not working.");
        mlInfo.setEnabled(false);
        multiArgs.setActionCommand(Integer.toString(M_MULTI_SPAN));
        multiArgs.addActionListener(modemenuListen);
        multiArgs.setAccelerator(
                KeyStroke.getKeyStroke('2',
                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

        JMenuItem exitMode = new JMenuItem("Exit to Normal Mode");
        if(mMode != M_NORMAL){
            exitMode.setEnabled(true);
        } else {
            exitMode.setEnabled(false);
        }
        exitMode.setActionCommand(Integer.toString(M_NORMAL));
        exitMode.addActionListener(modemenuListen);
        exitMode.setAccelerator(
                KeyStroke.getKeyStroke('E',
                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

        menu.add(multiSpan);
        menu.addSeparator();
        menu.add(multiArgs);
        menu.add(mlInfo);
        menu.addSeparator();
        menu.add(exitMode);

        return menu;
    }


    private void addGuideItem(JPopupMenu menu, String guide) {
        JMenuItem item = new JMenuItem(guide);
        item.setEnabled(false);
        menu.add(item);
    }

    private void addGuideItem(JMenu menu, String guide) {
        JMenuItem item = new JMenuItem(guide);
        item.setEnabled(false);
        menu.add(item);
    }

    /**
     * Refreshes the GUI menus when a new DTD or file is loaded
     */
    private void updateMenus(){
        mMenuBar.removeAll();

        // mod by krim: some menus are used only after a file is loaded
        mFileMenu = createFileMenu("File");
        mMenuBar.add(mFileMenu);
        if (isFileOpen) {
            mDisplayMenu = createDisplayMenu("Display");
            mMenuBar.add(mDisplayMenu);
            mLinkMenu = createLinkMenu("Link elements");
            mMenuBar.add(mLinkMenu);
            mNCMenu = createNCMenu("NC elements");
            mMenuBar.add(mNCMenu);

            // add by krim: special mode menu
            mModeMenu = createModeMenu("Modes");
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
        ArrayList<int[]> spans = new ArrayList<int[]>();
        if (spansString == null || spansString.equals("")) {
            spans.add(new int[]{-1, -1});
            return spans;
        }

        // this list will be returned

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

    /**
     * Updates the list of all extent elements in mSpan or table selection
     * added by krim
     *
     */
    private void updateArgList() {
        mPossibleArgs.clear();
        ArrayList<String[]> back = new ArrayList<String[]>();
        
        int i = 0;
        for (int[] span : mSpans) {
            HashCollection<String, String> hc 
                    = mTask.getTagsBetween(span[0], span[1]);
            ArrayList<String[]> listed = new ArrayList<String[]>();
            for (String tagName : hc.getKeyList()) {
                for (String tagId : hc.get(tagName)) {
                    listed.add(new String[]{tagName, tagId});
                }
            }
            if (listed.size() > 0) {
                // put first items of each span in order in front of PossibleArgs
                mPossibleArgs.add(i, listed.remove(0));
                // for anything left
                if (listed.size() > 0) {
                    back.addAll(listed);
                }
            }
        }
        mPossibleArgs.addAll(back);
    }

    /**
     * Updates the status bar mDisplayMenu
     * add by krim
     *
     */
    private void updateStatusBar() {
        if (!mTask.hasDTD()) {
            mStatusBar.setText("No DTD loaded.");
        } else if (!isFileOpen) {
            mStatusBar.setText("No file loaded.");
        } else {
            switch (mMode) {
                case M_NORMAL:
                    if (isSpansEmpty()) {
                        mStatusBar.setText("No text selected.");
                    } else {
                        mStatusBar.setText("Selected: " + spansToString(this.mSpans));
                    }
                    break;
                case M_MULTI_SPAN:
                    if (isSpansEmpty()) {
                        mStatusBar.setText("[Multi-span mode] No text selected.");
                    } else {
                        mStatusBar.setText("[Multi-span mode] Selected: "
                                + spansToString(this.mSpans));
                    }
                    break;
                case M_MULTI_ARG:
                    if (isSpansEmpty()) {
                        mStatusBar.setText("[Multi-span mode] No text selected.");
                    } else {
                        mStatusBar.setText( "[Multi-span mode] " + mPossibleArgs.size()
                                + "consuming tags in selected text.");
                    }
                    break;
            }
        }
    }

    /**
     * Sets MAE mode to Normal
     * add by krim
     */
    private void returnToNormalMode() {

        if (mMode != M_NORMAL) {
            mStatusBar.setText("Exit to normal mode! Click anywhere to continue.");
            new Timer().schedule(new TimedUpdateStatusBar(), 3000);
        }
        mMode = M_NORMAL;
        mPossibleArgs.clear();
        resetSpans();
        updateMenus();

    }

    private Boolean isSpansEmpty() {
        return this.mSpans.size() == 0 || this.mSpans.get(0)[0] == -1;
    }

    /**
     * Resets the selected spans to default non-selection (-1~-1)
     */
    private void resetSpans() {
        isTextSelected = false;
        mSpans.clear();
        mSpans.add(new int[]{-1, -1});
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

    public class ColorRect implements Icon {
        private int size;
        private Color color;


        public ColorRect(int size, Color c) {
            this.size = size;
            this.color   = c;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.drawRect(x, y, size, size);
            g.setColor(color);
            g.fillRect(x, y, size, size);
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }
    }
}