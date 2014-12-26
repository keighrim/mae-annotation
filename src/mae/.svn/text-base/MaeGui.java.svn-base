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

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.table.TableColumn;
import javax.swing.table.DefaultTableModel;


/** 
 * MaeGui is the main class for MAE; it manages all the GUI attributes 
 * and manages how the annotation information is loaded, interacted with,
 * and displayed.
 * 
 * 
 * @author Amber Stubbs 
 * @version 0.9.6 May 10, 2012
*/

public class MaeGui extends JPanel {

	private static final long serialVersionUID = 9404268L;
	
	private Hashtable<String, JTable> elementTables;
	private Hashtable<String, Color> colorTable;
	private Hashtable<String,Integer> linkDisplayStatus;
	
	//Here is where to change the colors that get assigned to tags
	private Color[] colors = {Color.blue, Color.red,
			Color.green,Color.magenta, new Color(153,102,0),
			Color.pink,Color.cyan, new Color(255,204,51),
			new Color(0,172,188),new Color (234,160,0), 
			new Color(102,75,153),Color.lightGray};

	//some booleans that help keep track of the status of the annottion
	private boolean hasFile;
	private boolean textSelected;
	private boolean ctrlPressed;
	
	//ints and Strings that are handy to have widely available
	private int loc1;
	private int start;
	private int end;
	private String linkFrom;
	private String linkName;
	private String linkTo;
	private String fileName;
	private String xmlName;

	//Objects that will become highlighters for showing extents in links
	private Object high1;
	private Object high2;

	//GUI components
	private static JFrame frame;
	private JMenu optionMenu;  
	private JFrame linkFrame;
	private JScrollPane chronScrollPane;
	private JTabbedPane tabbedElementsPane;
	private JPanel annotatePane;
	private JTextPane displayAnnotation;
	private JMenuBar mb;
	private JMenu nc_tags;
	private JMenu display;
	private JMenu helpMenu;
	private JPopupMenu popup1;
	private JPopupMenu popup2;
	private JFileChooser fcFile;
	private JFileChooser fcSave;

	//the helper function for talking to the database
	private static AnnotationTask annotationTask;

	public MaeGui() {
		super(new BorderLayout());
		
		annotationTask = new AnnotationTask();
		
		hasFile = false;
		textSelected=false;
		ctrlPressed=false;
		
		loc1 = -1;
		start=-1;
		end=-1;

		linkFrom="";
		linkName="";
		linkTo="";

		fileName = "";
		xmlName = "";

		//used to keep track of what color goes with what tag
		colorTable = new Hashtable<String,Color>();
		
		elementTables = new Hashtable<String, JTable>();
		linkDisplayStatus = new Hashtable<String,Integer>();

		optionMenu = createFileMenu();
		nc_tags = createNCMenu();
		display = createDisplayMenu();
		helpMenu = createHelpMenu();
		mb = new JMenuBar();
		mb.add(optionMenu);
		mb.add(display);
		mb.add(nc_tags);
		mb.add(helpMenu);

		linkFrame = new JFrame();

		fcFile = new JFileChooser(".");
		fcFile.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

		fcSave = new JFileChooser(".");
		fcSave.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

		
		displayAnnotation = new JTextPane(new DefaultStyledDocument());
		displayAnnotation.setEditable(false);
		displayAnnotation.setContentType("text/plain; charset=UTF-8");
		displayAnnotation.addCaretListener(new AnnCaretListener());
		displayAnnotation.addKeyListener(new ModKeyListener());
		displayAnnotation.addMouseListener(new PopupListener());
		
		chronScrollPane = new JScrollPane(displayAnnotation);
		chronScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		
		annotatePane = new JPanel(new GridLayout(1,1));
		annotatePane.add(chronScrollPane);
		
		popup1 = new JPopupMenu();
		popup2 = new JPopupMenu();


		tabbedElementsPane = new JTabbedPane();
		JComponent panel1 = makeTextPanel("No DTD");
		tabbedElementsPane.addTab("Tab", panel1);

		JSplitPane splitPane2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT,annotatePane,tabbedElementsPane);

		this.addKeyListener(new ModKeyListener());
		
		//set up the Highlighter objects for later use
		Highlighter high = displayAnnotation.getHighlighter();
		try{
			high1 = high.addHighlight(0,0,new MyHighlightPainter(Color.yellow));
			high2 = high.addHighlight(0,0,new MyHighlightPainter(Color.yellow));
		}catch(BadLocationException b){
			System.out.println(b);
		}


		add(mb,BorderLayout.NORTH);
		add(splitPane2,BorderLayout.CENTER);
		splitPane2.setDividerLocation(250);
	}

	// ***********************
	// Section: classes and listeners
	
	/**
	 * Allows new highlighters for the JTextPane
	 * 
	 */
	private class MyHighlightPainter extends DefaultHighlighter.DefaultHighlightPainter {
		private MyHighlightPainter(Color color) {
			super(color);
		}
	}

	/**
	 * Listener for the File menu; determines what action to take for 
	 * loading/saving documents.
	 * 
	 *
	 */
	private class getFile implements ActionListener{
		public void actionPerformed(ActionEvent e){
			if (e.getActionCommand().equals("Load DTD")){
				if(hasFile){
					showSaveWarning();
				}
				int returnVal = fcFile.showOpenDialog(MaeGui.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fcFile.getSelectedFile();
					try{
						displayAnnotation.setStyledDocument(new DefaultStyledDocument());
						DTDLoader dtdl = new DTDLoader(file);
						annotationTask.reset_db();
						annotationTask.setDTD(dtdl.getDTD());
						linkDisplayStatus.clear();
						resetTabPane();
						assignColors();
						updateMenus();

						if (annotationTask.getElements().size()>20){
							tabbedElementsPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
						}
						else{
							tabbedElementsPane.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
						}

						hasFile=false;
					}catch(Exception o){
						System.out.println("Error loading DTD");
						System.out.println(o.toString());
					}
				}
			}

			else if (e.getActionCommand().equals("Load File")){
				if(hasFile){
					showSaveWarning();
				}
				int returnVal = fcFile.showOpenDialog(MaeGui.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fcFile.getSelectedFile();
					String fullName = file.getName();
					int endName = fullName.lastIndexOf(".");
					fileName = fullName.substring(0,endName);
					xmlName = fileName + ".xml";
					try{
						frame.setTitle(fullName);
						hasFile = true;
						updateMenus();
						resetTabPane();
						annotationTask.reset_db();
						annotationTask.reset_IDTracker();
						displayAnnotation.setStyledDocument(new DefaultStyledDocument());
						displayAnnotation.setContentType("text/plain; charset=UTF-8");
						frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
						if(FileOperations.hasTags(file)){
							XMLFileLoader xfl = new XMLFileLoader(file);
							StyledDocument d = displayAnnotation.getStyledDocument();
							Style def = StyleContext.getDefaultStyleContext().getStyle( StyleContext.DEFAULT_STYLE );
							Style regular = d.addStyle( "regular", def );
							d.insertString(0, xfl.getTextChars(), regular);
							HashCollection<String,Hashtable<String,String>> newTags = xfl.getTagHash();
							if (newTags.size()>0){
								processTagHash(newTags);
							}
						}
						else{  // that is, if it's only a text file
							StyledDocument d = displayAnnotation.getStyledDocument();
							displayAnnotation.setStyledDocument(FileOperations.setText(file,d));

						}
						displayAnnotation.requestFocus(true);
						displayAnnotation.getCaret().setDot(0);
						displayAnnotation.getCaret().moveDot(1);
					}catch(Exception ex){
						hasFile=false;
						System.out.println("Error loading file");
						System.out.println(ex.toString());
					}
				}
				frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				displayAnnotation.setCaretPosition(0);
			}

			else if (e.getActionCommand().equals("Save RTF")){
				String rtfName = fileName + ".rtf";
				fcSave.setSelectedFile(new File(rtfName));
				int returnVal = fcSave.showSaveDialog(MaeGui.this);
				if(returnVal == JFileChooser.APPROVE_OPTION){
					File file = fcSave.getSelectedFile();
					try{
						FileOperations.saveRTF(file,displayAnnotation);
					}catch(Exception e2){

						System.out.println(e2.toString());
					}
				}
			}

			else if(e.getActionCommand().equals("Save XML")){
				fcSave.setSelectedFile(new File(xmlName));
				int returnVal = fcSave.showSaveDialog(MaeGui.this);
				if(returnVal == JFileChooser.APPROVE_OPTION){
					File file = fcSave.getSelectedFile();
					String fullName = file.getName();
					try{
						FileOperations.saveXML(file,displayAnnotation,
								elementTables,annotationTask.getElements(),annotationTask.getDTDName());
						frame.setTitle(fullName);
						xmlName = fullName;
					}catch(Exception e2){
						System.out.println("error here!");
						System.out.println(e2.toString());
					}
				}
			}   
		}
	}
	
	private class AboutListener implements ActionListener{
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
				Font font = displayAnnotation.getFont();
				Font font2 = new Font(font.getName(),font.getStyle(),font.getSize()+1);
				displayAnnotation.setFont(font2);
				tabbedElementsPane.setFont(font2);
			}
			if (action.equals("Font--")){
				Font font = displayAnnotation.getFont();
				Font font2 = new Font(font.getName(),font.getStyle(),font.getSize()-1);
				displayAnnotation.setFont(font2);
				tabbedElementsPane.setFont(font2);
			}

		}

	}
	
	/**
	 * 
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
		public boolean isCellEditable(int row, int col){
			if (col==0){
				return false;
			}
			else return true;
		}
	}

	/**
	 * Called when the user selects the option to delete the highlighted
	 * rows from the table in view.  Rows are removed both from the
	 * database and the table.
	 *
	 */
	private class removeSelectedTableRows implements ActionListener{
		public void actionPerformed(ActionEvent actionEvent) {
			boolean check = showDeleteWarning();
			if (check){
				String action = actionEvent.getActionCommand();
				Elem elem = annotationTask.getElem(action);
				JTable tab = elementTables.get(action);
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
				String id = "";
				for (int i=selectedRows.length-1;i>=0;i--){
					int row = selectedRows[i];
					id = (String)tableModel.getValueAt(row,idCol);
					annotationTask.removeExtentByID(action,id);
					if(elem instanceof ElemExtent){
						int start = Integer.parseInt(((String)tableModel.getValueAt(row,1)));
						int end = Integer.parseInt(((String)tableModel.getValueAt(row,2)));
						assignTextColor(start,end);
						HashCollection<String,String> links = annotationTask.getLinksByExtentID(action,id);
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
	private class MakeTagListener implements ActionListener{
		public void actionPerformed(ActionEvent actionEvent) {
			String action = actionEvent.getActionCommand();
			//if the tag being added is non-consuming, make sure 
			//start and end are set to -1
			if(action.contains("addN-C:")){
				start=-1;
				end=-1;
				action = action.split(":")[1];
			}
			JTable tab = elementTables.get(action);
			DefaultTableModel tableModel = (DefaultTableModel)tab.getModel();
			//create array for data for row
			String[] newdata = new String[tableModel.getColumnCount()];
			for(int i=0;i<tableModel.getColumnCount();i++){
				newdata[i]="";
			}
			// get the Elem that the table was based on, and go through
            // the attributes.  Put in the start and end bits*/
			Hashtable<String,Elem> elements = annotationTask.getElemHash();
			Elem elem = elements.get(action);
			// get ID number. This isn't as hard-coded as it looks: 
            // the columns for the table are created from the Attributes array list
			String newID = "";
			ArrayList<Attrib> attributes = elem.getAttributes();
			for(int i=0;i<attributes.size();i++){
				if(attributes.get(i) instanceof AttID){
					newID=annotationTask.getNextID(action);
					newdata[i]=newID;
				}
				if(attributes.get(i).hasDefaultValue()){
					newdata[i]=attributes.get(i).getDefaultValue();
				}
			}

			//put in start and end values
			if (elem instanceof ElemExtent){
				attributes = elem.getAttributes();
				for(int i=0;i<attributes.size();i++){
					//this also isn't as hard-coded as it looks, because
					//all extent elements have these attributes
					if (attributes.get(i).getName()=="start"){
						newdata[i]=Integer.toString(start);
					}
					if (attributes.get(i).getName()=="end"){
						newdata[i]=Integer.toString(end);
					}
					if (attributes.get(i).getName()=="text" &&
							start != -1){
						newdata[i] = getText(start,end);
					}
				}
			}
			
			//add new row of tag info to the table and set appropriate attributes
			tableModel.addRow(newdata);
			tab.clearSelection();
			tab.setRowSelectionInterval(tableModel.getRowCount()-1,tableModel.getRowCount()-1);
			Rectangle rect =  tab.getCellRect(tableModel.getRowCount()-1, 0, true);
			tab.scrollRectToVisible(rect);
			add_tags(action,newID);
			if(start!=-1){
				assignTextColor(start,end);
			}
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
					ctrlPressed = true;
				}
			}
			else{
				if ( keyCode == 17){
					ctrlPressed = true;
				}
			}
		}

		public void keyReleased(KeyEvent e){
			String p = System.getProperty("os.name");
			int keyCode = e.getKeyCode();
			if(p.toLowerCase().contains("mac")){
				if (keyCode == 18 || keyCode == 157){
					ctrlPressed = false;
				}
			}
			else{
				if ( keyCode == 17){
					ctrlPressed = false;
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
				Elem elem = annotationTask.getElem(action.split(", ")[0]);
				//remove rows from DB
				HashCollection<String,String> links = new HashCollection<String,String>();
				//removes extent tags and related link tags from DB, returns HashCollection
				//of link ids for removal from the DB
				String elemType = action.split(", ")[0];
				String id = action.split(", ")[1];
				links = annotationTask.getLinksByExtentID(elemType,id);
				annotationTask.removeExtentByID(elemType,id);
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
			Integer stat = linkDisplayStatus.get(cmd);
			String elemName = cmd.split(":")[1];
			if (stat.equals(0)){
				//get list of locations associated with the selected link
				Hashtable<Integer,String> locs = annotationTask.getLocationsbyElemLink(elemName);
				DefaultStyledDocument styleDoc = 
						(DefaultStyledDocument)displayAnnotation.getStyledDocument();
				for (Enumeration<Integer> e = locs.keys(); e.hasMoreElements();){
					Integer inte = e.nextElement();
					Element el = styleDoc.getCharacterElement(inte);
					AttributeSet as = el.getAttributes();
					SimpleAttributeSet sas = new SimpleAttributeSet(as);
					StyleConstants.setItalic(sas, true);
					StyleConstants.setBold(sas, true);
					styleDoc.setCharacterAttributes(inte,1,sas,false);
				}
				linkDisplayStatus.put(cmd,1);
			}
			else{
				//if boldness is being removed, have to make sure it doesn't
				//take away boldness of other tags that are selected
				DefaultStyledDocument styleDoc = 
						(DefaultStyledDocument)displayAnnotation.getStyledDocument();
				//get list of active displays
				ArrayList<String> active = new ArrayList<String>();
				for (Enumeration<String> e = linkDisplayStatus.keys(); e.hasMoreElements();){
					String elem = e.nextElement();
					if(linkDisplayStatus.get(elem).equals(1)){
						active.add(elem.split(":")[1]);
					}
				}
				active.remove(elemName);
				Hashtable<Integer,String> locs = 
						annotationTask.getLocationsbyElemLink(elemName,active);

				for (Enumeration<Integer> e = locs.keys(); e.hasMoreElements();){
					Integer inte = e.nextElement();
					Element el = styleDoc.getCharacterElement(inte);
					AttributeSet as = el.getAttributes();
					SimpleAttributeSet sas = new SimpleAttributeSet(as);
					StyleConstants.setItalic(sas, false);
					StyleConstants.setBold(sas, false);
					styleDoc.setCharacterAttributes(inte,1,sas,false);
				}
				linkDisplayStatus.put(cmd,0);
			}


		}


	}

	/**
	 * AnnCaretListener keeps track of what extents have been selected
	 * so that other methods can use that information in the display 
	 * and links.
	 *
	 */
	private class AnnCaretListener implements CaretListener{
		public void caretUpdate(CaretEvent e) {
			Highlighter high = displayAnnotation.getHighlighter();
			try{
				//when the caret is moved, remove the any link highlights
				high.changeHighlight(high1,0, 0);
				high.changeHighlight(high2,0, 0);
			}catch(BadLocationException b){
			}


			int dot = e.getDot();
			int mark = e.getMark();
			if((ctrlPressed==true) && (loc1 == -1)){
				loc1 = dot;
			}
			else if(ctrlPressed==true && loc1 != -1){
				showLinkWindow(loc1,dot);
				ctrlPressed = false;
				loc1=-1;
			}

			if (dot!=mark){
				textSelected=true;
				if(dot<mark){
					start=dot;
					end=mark;
				}
				else{
					start=mark;
					end=dot;
				}
				findHighlightRows();
			}
			else{
				textSelected=false;
				start=-1;
				end=-1;

			}

		}

	}

	/**
	 * JTableListener determines if the ID of a tag has 
	 * been double-clicked, and if it has it highlights the 
	 * appropriate text extent/extents.
	 */
	
	private class JTableListener extends MouseAdapter {
		public void mousePressed(MouseEvent e){
			if (SwingUtilities.isLeftMouseButton(e)){
				if(ctrlPressed){
				}
			}
			maybeShowRemovePopup(e);
		}

		public void mouseReleased(MouseEvent e) {
			maybeShowRemovePopup(e);
		}

		public void mouseClicked(MouseEvent e) {
			if(e.getClickCount()==2){
				int index = tabbedElementsPane.getSelectedIndex();
				String title = tabbedElementsPane.getTitleAt(index);
				JTable tab = elementTables.get(title);
				Elem el = annotationTask.getElemHash().get(title);
				Highlighter high = displayAnnotation.getHighlighter();
				try{
					high.changeHighlight(high1,0, 0);
					high.changeHighlight(high2,0, 0);
				}catch(BadLocationException b){  
				}
				if(el instanceof ElemExtent){
					int selectedRow = tab.getSelectedRow();
					int startSelect = Integer.parseInt(((String)tab.getValueAt(selectedRow,1)));
					int endSelect = Integer.parseInt(((String)tab.getValueAt(selectedRow,2)));
					
					try{
						high.changeHighlight(high1,startSelect, endSelect);
						displayAnnotation.scrollRectToVisible(displayAnnotation.modelToView(startSelect));
					}catch (BadLocationException b) {
					}
				}//end if ElemExtent

				if(el instanceof ElemLink){
					int selectedRow = tab.getSelectedRow();
					String fromSelect = (String)tab.getValueAt(selectedRow,1);
					String toSelect = (String)tab.getValueAt(selectedRow,3);
					String fromLoc = annotationTask.getLocByID(fromSelect);
					String toLoc = annotationTask.getLocByID(toSelect);
					if (fromLoc != null){

						String [] locs = fromLoc.split(",");
						int startSelect = Integer.parseInt(locs[0]);
						int endSelect = Integer.parseInt(locs[1]);
						try{  
							high.changeHighlight(high1,startSelect, endSelect+1);
							displayAnnotation.scrollRectToVisible(displayAnnotation.modelToView(startSelect));
						}catch(Exception ex){
							System.out.println(ex);
						}
					}
					if (toLoc != null){
						String [] locs = toLoc.split(",");
						int startSelect = Integer.parseInt(locs[0]);
						int endSelect = Integer.parseInt(locs[1]);
						try{
							high.changeHighlight(high2,startSelect, endSelect+1);
						}catch(Exception ex){
							System.out.println(ex);
						}

					}

				}
			}
		}

		private void maybeShowRemovePopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				popup2 = removePopup();
				popup2.show(e.getComponent(),
						e.getX(), e.getY());
			}
		}
	}

	/**
	 * PopupListener determines whether the link
	 * creation window should be displayed.
	 */
	private class PopupListener extends MouseAdapter {
		public void mousePressed(MouseEvent e) {
			if (SwingUtilities.isLeftMouseButton(e)){
				if(ctrlPressed){
				}
			}
			maybeShowPopup(e);
		}

		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}


		private void maybeShowPopup(MouseEvent e) {
			if (e.isPopupTrigger() && textSelected) {
				popup1 = populatePopup();
				popup1.show(e.getComponent(),
						e.getX(), e.getY());
			}
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
			JTable tab = elementTables.get(linkName);
			DefaultTableModel tableModel = (DefaultTableModel)tab.getModel();

			String[] newdata = new String[tableModel.getColumnCount()];
			for(int i=0;i<tableModel.getColumnCount();i++){
				newdata[i]="";
			}
			//get the Elem that the table was based on, and go through
			//the attributes.  Put in the start and end bits
			Hashtable<String,Elem> elements = annotationTask.getElemHash();
			Elem elem = elements.get(linkName);

			//get ID number for link
			String newID = "";
			ArrayList<Attrib> attributes = elem.getAttributes();
			for(int i=0;i<attributes.size();i++){
				Attrib a = attributes.get(i);
				if(a instanceof AttID){
					newID=annotationTask.getNextID(linkName);
					newdata[i]=newID;
				}
				if((a instanceof AttData) &&
						(attributes.get(i).getName()=="fromID")){
					newdata[i]=from_id;
				}
				if((a instanceof AttData) &&
						(attributes.get(i).getName()=="toID")){
					newdata[i]=to_id;
				}
				if((a instanceof AttData) &&
						(attributes.get(i).getName()=="fromText")){
					newdata[i]=from_text;
				}
				if((a instanceof AttData) &&
						(attributes.get(i).getName()=="toText")){
					newdata[i]=to_text;
				}
			}
			tableModel.addRow(newdata);
			tab.clearSelection();
			tab.setRowSelectionInterval(tableModel.getRowCount()-1,tableModel.getRowCount()-1);
			Rectangle rect =  tab.getCellRect(tableModel.getRowCount()-1, 0, true);
			tab.scrollRectToVisible(rect);

			annotationTask.addToDB(newID,linkName,from_id, from_type,to_id,to_type,true);

			//reset variables
			linkFrom="";
			linkName="";
			linkTo="";

			linkFrame.setVisible(false);  
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
			if (e.getActionCommand() == "fromID"){
				linkFrom = select;
			}
			else if (e.getActionCommand() == "link"){
				linkName = select;
			}
			else if (e.getActionCommand() == "toID"){
				linkTo = select;
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
	private void processTagHash(HashCollection<String,Hashtable<String,String>>  newTags){
		ArrayList<String> elements = newTags.getKeyList();
		//first, add the extent tags
		for (int i = 0;i<elements.size();i++){
			String elemName = elements.get(i);
			Elem elem = annotationTask.getElemHash().get(elemName);
			if(elem instanceof ElemExtent &&
					elementTables.containsKey(elemName)){
				/*for each element type there is a list of tag information*/
				ArrayList<Hashtable<String,String>> tags = newTags.get(elemName);
				
				for(int j=0;j<tags.size();j++){
					Hashtable<String,String> a = tags.get(j);
					if(updateIDandDB(a,elemName)){
						addRowFromHash(a,elemName);
					}
				}
				annotationTask.batchExtents();
			}
		}
		//then, go back and add the link tags (since they rely on the extent tag 
		//info, the need to be added later
		for (int i = 0;i<elements.size();i++){
			String elemName = elements.get(i);
			Elem elem = annotationTask.getElemHash().get(elemName);
			if(elem instanceof ElemLink &&
					elementTables.containsKey(elemName)){
				/*for each element type there is a list of tag information*/
				ArrayList<Hashtable<String,String>> tags = newTags.get(elemName);
				
				for(int j=0;j<tags.size();j++){
					Hashtable<String,String> a = tags.get(j);
					if(updateIDandDB(a,elemName)){
						addRowFromHash(a,elemName);
					}
				}
				annotationTask.batchLinks();
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
		String startString = a.get("start");
		String endString = a.get("end");
		start = Integer.valueOf(startString);
		end = Integer.valueOf(endString);
		if(start>-1){
			for(int i=start;i<end;i++){
				annotationTask.addToDB(i,elemName,newID,false);
			}
		}
		else{
			annotationTask.addToDB(-1,elemName,newID,false);
		}
		start = -1;
		end = -1;
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
		String from_type = annotationTask.getElementByID(from_id);
		String to_type = annotationTask.getElementByID(to_id);
		annotationTask.addToDB(newID,elemName,from_id, from_type,to_id,to_type,false);
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
		Hashtable<String,Elem> elements = annotationTask.getElemHash();
		Elem elem = elements.get(elemName);
		ArrayList<Attrib> attributes = elem.getAttributes();
		for(int i=0;i<attributes.size();i++){
			if(attributes.get(i) instanceof AttID){
				String newID = a.get(attributes.get(i).getName());
				if(annotationTask.idExists(elemName,newID)==false){
					if (elem instanceof ElemExtent){
						addExtentToDBFromHash(a,elemName,newID);
					}
					else if (elem instanceof ElemLink){
						addLinkToDBFromHash(a,elemName,newID);
					}
				}
				else{
					System.out.println("ID " +newID+" already exists.  Skipping this "+elemName+" tag");
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
		JTable tab = elementTables.get(elemName);
		DefaultTableModel tableModel = (DefaultTableModel)tab.getModel();
		String[] newdata = new String[tableModel.getColumnCount()];
		for(int k=0;k<tableModel.getColumnCount();k++){
			String colName = tableModel.getColumnName(k);
			String value = a.get(colName);
			if(value!=null){
				newdata[k]=value;
			}
			else{
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
		ArrayList<String> link_types = links.getKeyList();
		for(int i=0;i<link_types.size();i++){
			Elem elem = annotationTask.getElem(link_types.get(i));
			ArrayList<String> link_ids = links.getList(elem.getName());
			if(elem instanceof ElemLink){
				for(int j=0;j<link_ids.size();j++){
					String id = link_ids.get(j);
					removeTableRows(elem,id);
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
		JTable tab = elementTables.get(elem.getName());
		DefaultTableModel tableModel = (DefaultTableModel)tab.getModel();
		int rows = tableModel.getRowCount();
		//has to go backwards or the wrong rows get deleted
		for (int i=rows-1;i>=0;i--){
			String value = (String)tableModel.getValueAt(i,0);
			if (value.equals(id)){
				//redo color for this text--assumes that lines
				//have already been removed from the DB
				if(elem instanceof ElemExtent){
					int start = Integer.parseInt(((String)tableModel.getValueAt(i,1)));
					int end = Integer.parseInt(((String)tableModel.getValueAt(i,2)));
					assignTextColor(start,end);
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
		JTable tab = elementTables.get(elem);
		DefaultTableModel tableModel = (DefaultTableModel)tab.getModel();
		int rows = tableModel.getRowCount();
		int idCol = -1;
		int textCol = -1;
		int cols = tableModel.getColumnCount();
		for(int i=0;i<cols;i++){
			String colname = tableModel.getColumnName(i);
			if(colname.equalsIgnoreCase("id")){
				idCol = i;
			}
			if(colname.equalsIgnoreCase("text")){
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
		HashCollection<String,String> idHash = annotationTask.getTagsSpan(start,end);
		if (idHash.size()>0){
			ArrayList<String> elems = idHash.getKeyList();
			for(int i=0; i<elems.size();i++){
				String e = elems.get(i);
				ArrayList<String> ids = idHash.get(e);
				for(int j=0;j<ids.size();j++){
					String id = ids.get(j);
					highlightTableRows(e,id);
					HashCollection<String,String> links = new HashCollection<String,String>();
					//returns HashCollection of link ids connected to this
					links = annotationTask.getLinksByExtentID(e,id);
					if(links.size()>0){
						highlightTableRowsHash(links);
					}
				}
			}
		}
	}

	/**
	 * Adds extent tags to the database, one tag per character location
	 * 
	 * @param element the type of tag being added
	 * @param id the ID of the tag being added
	 */
	private void add_tags(String element, String id){
		if(start>-1){
			for(int i=start;i<end;i++){
				annotationTask.addToDB(i,element,id,true);
			}
		}
		else{
			annotationTask.addToDB(-1,element,id,true);
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
		for(int i=0; i<elems.size();i++){
			String e = elems.get(i);
			ArrayList<String> ids = hash.get(e);
			for(int j=0;j<ids.size();j++){
				String id = ids.get(j);
				highlightTableRows(e,id);
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
		HashCollection<String,String>elems = annotationTask.getElementsAllLocs();
		ArrayList<String> locations = elems.getKeyList();
		for (int i=0;i<locations.size();i++) {
			String location = locations.get(i);
			ArrayList<String> elements = elems.getList(location);
			if (elements.size()>1){
				setColorAtLocation(colorTable.get(elements.get(0)),Integer.parseInt(location),1,true);
			}
			else{
				setColorAtLocation(colorTable.get(elements.get(0)),Integer.parseInt(location),1,false);
			}
		}
	}


	/**
	 * 	This method is for coloring/underlining text
     *  in the text window.  It detects overlaps, and
     *  should be called every time a tag is added
     *  or removed.
	 * 
	 * @param beginColor the location of the first character in the extent
	 * @param endColor the location of the last character in the extent
	 */
	private void assignTextColor(int beginColor, int endColor){
		//go through each part of the word being changed and 
        //  find what tags are there, and what color it should be.
		for(int i=0;i<endColor-beginColor;i++){
			ArrayList<String> c = annotationTask.getElemntsLoc(beginColor+i);
			if (c.size()==1){
				//use color of only tag
				setColorAtLocation(colorTable.get(c.get(0)),beginColor+i,1,false);
			}
			else if (c.size()>1){
				//set color to that of first tag also set underline
				setColorAtLocation(colorTable.get(c.get(0)),beginColor+i,1,true);
			}
			else{
				//set color to black, remove underline
				setColorAtLocation(Color.black,beginColor+i,1,false);
			}
		}
	}

	
	/**
	 * Sets the color of a specific span of text.  Called for each
	 * extent tag.
	 * 
	 * @param color The color the text will become.  
	 * Determined by the tag name and colorTable (Hashtable)
	 * @param s the location of the start of the extent
	 * @param e the location of the end of the extent
	 * @param b whether or not the text will be underlined
	 */
	private void setColorAtLocation(Color color, int s, int e, boolean b){
		DefaultStyledDocument styleDoc = 
				(DefaultStyledDocument)displayAnnotation.getStyledDocument();
		SimpleAttributeSet aset = new SimpleAttributeSet();
		StyleConstants.setForeground(aset, color);
		StyleConstants.setUnderline(aset, b);
		styleDoc.setCharacterAttributes(s,e,aset,false);
	}
	
	/**
	 * Retrieves the text between two offsets from the document.
	 * 
	 * @param start start location of the text
	 * @param end end location of the text
	 * @return the text
	 */
	
	private String getText(int start, int end){
		DefaultStyledDocument styleDoc = (DefaultStyledDocument)displayAnnotation.getStyledDocument();
		String text = "";
		try{
			text = styleDoc.getText(start,end-start);
		}catch(Exception e){
			System.out.println(e.toString());
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
	private void showLinkWindow(int loc, int loc2){
		JPanel linkPane = new JPanel(new BorderLayout());
		JPanel boxPane = new JPanel(new GridLayout(3,2));
		linkFrame = new JFrame();

		JComboBox fromList = new JComboBox();
		fromList.addActionListener(new jboxListener());
		fromList.setActionCommand("fromID"); 

		HashCollection<String,String> idHash =  annotationTask.getTagsSpanAndNC(loc,loc+1);
		ArrayList<String> elements = idHash.getKeyList();
		if (elements.size()>0){
			if (elements.size()>1){
				fromList.addItem("");
			}
			for(int i=0; i<elements.size();i++){
				ArrayList<String> tags = idHash.get(elements.get(i));
				for(int j=0;j<tags.size();j++){
					//create the string for the table list
					String puttag = (elements.get(i) + 
							" - " + tags.get(j));
					//get the text for the words by id and element
					String text = getTextByID(elements.get(i),tags.get(j));
					puttag = puttag + " ("+text+")";
					//add string to JComboBox
					fromList.addItem(puttag);
				}
			}
		}

		JComboBox linkList = new JComboBox();
		linkList.setActionCommand("link"); 
		linkList.addActionListener(new jboxListener());

		ArrayList<Elem> taskElements = annotationTask.getElements();
		//create a tab for each element in the annotation task

		ArrayList<String> linkitems = new ArrayList<String>();

		for(int i=0;i<taskElements.size();i++){
			String name = taskElements.get(i).getName();
			if(taskElements.get(i) instanceof ElemLink){
				linkitems.add(name);
			}
		}
		if (linkitems.size()>1){
			linkList.addItem("");
		}
		for(int i=0;i<linkitems.size();i++){
			linkList.addItem(linkitems.get(i));
		}


		JComboBox toList = new JComboBox();
		toList.setActionCommand("toID"); 
		toList.addActionListener(new jboxListener());

		idHash =  annotationTask.getTagsSpanAndNC(loc2,loc2+1);
		elements = idHash.getKeyList();
		if (elements.size()>0){
			if (elements.size()>1){
				toList.addItem("");
			}
			for(int i=0; i<elements.size();i++){
				ArrayList<String> tags = idHash.get(elements.get(i));
				for(int j=0;j<tags.size();j++){
					String puttag = (elements.get(i) + 
							" - " + tags.get(j));
					//get the text for the words by id and element
					String text = getTextByID(elements.get(i),tags.get(j));
					puttag = puttag + " ("+text+")";
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
		linkFrame.setBounds(90,70,400,300);
		linkFrame.add(linkPane);
		linkFrame.setVisible(true);

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

		elementTables.put(e.getName(),table);
		table.addMouseListener(new JTableListener());
		//go through element attributes and add colums
		ArrayList<Attrib> attributes = e.getAttributes();
		//for some reason, it's necessary to add the columns first,
		//then go back and add the cell renderers.
		for (int i=0;i<attributes.size();i++){
			model.addColumn(attributes.get(i).getName());
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
		tabbedElementsPane.removeAll();
		ArrayList<Elem> elements = annotationTask.getElements();
		//create a tab for each element in the annotation task
		for(int i=0;i<elements.size();i++){
			String name = elements.get(i).getName();
			tabbedElementsPane.addTab(name, makeTablePanel(elements.get(i)));
		}
	}

	
	/**
	 * Creates the menu with the option to remove selected table rows
	 * 
	 * @return GUI menu
	 */
	private JPopupMenu removePopup(){
		JPopupMenu jp = new JPopupMenu();
		int index = tabbedElementsPane.getSelectedIndex();
		String title = tabbedElementsPane.getTitleAt(index);
		String action = "Remove selected " + title + " rows";
		JMenuItem menuItem = new JMenuItem(action);
		menuItem.setActionCommand(title);
		menuItem.addActionListener(new removeSelectedTableRows());
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
		JTable tab = elementTables.get(elem);
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
		for (Enumeration<String> tables = elementTables.keys(); tables.hasMoreElements();){
			JTable tab = elementTables.get(tables.nextElement());
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
		JOptionPane.showMessageDialog(frame, text);
	}


	/**
	 * Shows information about MAE
	 */
	private void showAboutDialog(){
		JOptionPane about = new JOptionPane();
		about.setLocation(100,100);
		about.setAlignmentX(Component.CENTER_ALIGNMENT);
		about.setAlignmentY(Component.CENTER_ALIGNMENT);
		about.setMessage("MAE \n Multi-purpose Annotation Editor \nVersion 0.9.6 \n\n" +
				"Copyright Amber Stubbs\nastubbs@cs.brandeis.edu \n Lab for " +
				"Linguistics and Computation, Brandeis University 2010-2012." + 
				"\n\nThis distribution of MAE (the software and the source code) \n" +
				" is covered under the GNU General Public License version 3.\n" +
				"http://www.gnu.org/licenses/");
		JDialog dialog = about.createDialog(frame, "About MAE");
		dialog.setVisible(true);
		//about.showMessageDialog(frame, text);
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

		int message = JOptionPane.showConfirmDialog(frame, 
				text, "Warning!", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		
		if (message==0){
			return true;
		}
		return false;
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
	 * Create a menuitem for each element in the annotation task
	 * when a section of the text is highlighted and right-clicked.
	 * 
	 * @return a pop-up menu with all extent tags listed, as well 
	 * as information about existing tags at the selected location
	 */
	private JPopupMenu populatePopup(){
		JPopupMenu jp = new JPopupMenu();
		ArrayList<Elem> elements = annotationTask.getElements();
		for(int i=0;i<elements.size();i++){
			String name = elements.get(i).getName();
			JMenuItem menuItem = new JMenuItem(name);
			menuItem.addActionListener(new MakeTagListener());
			if (elements.get(i) instanceof ElemExtent){
				jp.add(menuItem);
			}
		}
		//get a hash collection of the element type and id- add info to 
		//the action command for that menuItem
		//this is only for extent tags
		HashCollection<String,String> idHash = annotationTask.getTagsSpan(start,end);
		if (idHash.size()>0){
			jp.addSeparator();
			ArrayList<String> elems = idHash.getKeyList();
			for(int i=0; i<elems.size();i++){
				ArrayList<String> ids = idHash.get(elems.get(i));
				for(int j=0;j<ids.size();j++){
					String name = "Remove " + ids.get(j);
					JMenuItem menuItem = new JMenuItem(name);
					menuItem.setActionCommand(elems.get(i)+ ", "+ids.get(j));
					menuItem.addActionListener(new RemoveExtentTag());
					jp.add(menuItem);
				}
			}
		}
		return jp;
	}

	/**
	 * assigns colors to the elements in the DTD
	 */
	private void assignColors(){
		ArrayList<String> elements = annotationTask.getExtentElements();
		for (int i=0;i<elements.size();i++){
			int l = colors.length;
			int k = i;
			if (i>=l){
				k = i%l;
			}
			colorTable.put(elements.get(i),colors[k]);
		}
	}



	/**
	 * Refreshes the GUI menus when a new DTD or file is added
	 */
	private void updateMenus(){
		mb.remove(nc_tags);
		mb.remove(display);
		mb.remove(optionMenu);
		mb.remove(helpMenu);
		nc_tags = createNCMenu();
		optionMenu = createFileMenu();
		mb.add(optionMenu);
		display = createDisplayMenu();
		mb.add(display);
		mb.add(nc_tags);
		mb.add(helpMenu);
		mb.updateUI();

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
		if(annotationTask.hasDTD()==false){
			loadFile.setEnabled(false);
		}
		else{
			loadFile.setEnabled(true);
		}
		menu.add(loadFile);

		menu.addSeparator();
		JMenuItem saveFileRTF = new JMenuItem("Create RTF");
		saveFileRTF.setActionCommand("Save RTF");
		saveFileRTF.addActionListener(new getFile());
		if(hasFile==false){
			saveFileRTF.setEnabled(false);
		}
		else{
			saveFileRTF.setEnabled(true);
		}
		menu.add(saveFileRTF);
		menu.addSeparator();

		JMenuItem saveFileXML = new JMenuItem("Save File As XML");
		saveFileXML.setActionCommand("Save XML");
		saveFileXML.addActionListener(new getFile());
		if(hasFile==false){
			saveFileXML.setEnabled(false);
		}
		else{
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

		if(annotationTask.hasDTD()){
			menu.addSeparator();
			JMenu linkDisplay = new JMenu("Show linked extents");
			ArrayList<String> links = annotationTask.getLinkElements();
			for(int i=0;i<links.size();i++){
				String e = links.get(i);

				JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(e);
				menuItem.addActionListener(new DisplayLinkListener());
				String command = "displayLinks:"+e;
				menuItem.setActionCommand(command);
				linkDisplayStatus.put(command,0);
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
		if(annotationTask.hasDTD()){
			ArrayList<Elem> nc = annotationTask.getNCElements();
			for(int i=0;i<nc.size();i++){
				Elem e= nc.get(i);
				JMenuItem menuItem = new JMenuItem(e.getName());
				menuItem.addActionListener(new MakeTagListener());
				menuItem.setActionCommand("addN-C:"+e.getName());
				if(hasFile==false){
					menuItem.setEnabled(false);
				}
				menu.add(menuItem);
			}
		}
		else{
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
	 * Creates the GUI
	 */
	private static void createAndShowGUI() {
		JFrame.setDefaultLookAndFeelDecorated(true);

		//Create and set up the window.
		frame = new JFrame("MAE");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//Create and set up the content pane.
		JComponent newContentPane = new MaeGui();
		newContentPane.setOpaque(true); //content panes must be opaque
		frame.setContentPane(newContentPane);

		//Display the window.
		frame.pack();
		frame.setSize(900,500);
		frame.setVisible(true);
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
}