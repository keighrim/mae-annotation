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
import edu.brandeis.cs.nlp.mae.database.MaeDBException;
import edu.brandeis.cs.nlp.mae.database.MaeDriverI;
import edu.brandeis.cs.nlp.mae.model.ExtentTag;
import edu.brandeis.cs.nlp.mae.model.TagType;
import edu.brandeis.cs.nlp.mae.util.ColorHandler;
import edu.brandeis.cs.nlp.mae.util.SpanHandler;
import edu.brandeis.cs.nlp.mae.view.TextPanelView;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by krim on 12/31/2015.
 */
public class TextPanelController extends MaeControllerI{

    TextPanelView view;

    private int[] selected;
    private List<int[]> selectionHistory;

    public TextPanelController(MaeMainController mainController) throws MaeDBException {
        super(mainController);
        view = new TextPanelView();
        selectionHistory = new LinkedList<>();
        reset();
    }

    public boolean isTextOpen() {
        return getView().getCurrentTab() == -1;
    }

    protected void setSelection(int[] spans) {
        selected = spans;
        mainController.getStatusBar().reset();
    }

    public void clearSelection() {
        selectionHistory.clear();
        setSelection(null);
    }

    public void addSelection(int[] contiguousSpan) {
        selectionHistory.add(0, contiguousSpan);
        setSelection(SpanHandler.convertPairsToArray(selectionHistory));
    }

    public void undoSelection() {
        selectionHistory.remove(0);
        setSelection(SpanHandler.convertPairsToArray(selectionHistory));
    }

    public void addDocument(String documentTitle, String documentText) {
        getView().addDocument(documentTitle, documentText);
    }

    public void closeDocument(int i) {
        // TODO: 1/4/2016 need tests if this works well with tab switch listener to properly change current tab as well
        getView().getTabs().remove(i);
    }

    @Override
    protected TextPanelView getView() {
        return view;
    }

    @Override
    void reset() throws MaeDBException {
        getView().clear();
        if (!getMainController().isTaskLoaded()) {
            getView().addDocument(MaeStrings.NO_TASK_IND, MaeStrings.NO_TASK_GUIDE);
        } else if (!getMainController().isAnnotationOn()) {
            getView().addDocument(MaeStrings.NO_FILE_IND, MaeStrings.NO_FILE_GUIDE);
        }
        selectionHistory.clear();
        if (getMainController().isAnnotationOn()) {
            unassignAllColors();
            removeAllHighlights();
        }

    }

    @Override
    void addListeners() {
        // TODO: 1/3/2016 replace these with new listeners
        getView().getDocumentPane().addCaretListener(new TextPanelCaretListener());
        getView().getDocumentPane().addMouseListener(new TextPanelMouseListener());
        getView().getTabs().addChangeListener(new TextPanelTabSwitchListener());
    }

    public void removeAllHighlights() {
        getView().getDocumentPane().getHighlighter().removeAllHighlights();

    }

    /**
     * krim: This method is for removing all color/underline highlighting from the
     * whole text windows. It is called when toggling all_extents
     */
    public void unassignAllColors() throws MaeDBException {
        List<Integer> anchorLocations = getDriver().getAllAnchors();
        for (Integer location : anchorLocations) {
            setColorAtLocation(Color.black, location, false);
        }
        // TODO: 1/3/2016 should we need emptyActiveTags(), or so here?
    }

    /**
     * Sets the color of a specific span of text.  Called for each extent tag.
     *
     * @param color The color the text will become. Determined by the tag name and
     *              colorTable (Hashtable)
     * @param location the location of the start of the extent
     * @param underline whether or not the text will be underlined, in which case two or more tags are associated with the location
     */
    private void setColorAtLocation(Color color, int location, boolean underline) {
        DefaultStyledDocument styleDoc = getDocument();
        SimpleAttributeSet attributeSet = new SimpleAttributeSet();
        StyleConstants.setForeground(attributeSet, color);
        StyleConstants.setUnderline(attributeSet, underline);
        styleDoc.setCharacterAttributes(location, 1, attributeSet, false);
    }

    private DefaultStyledDocument getDocument() {
        return getView().getDocument();
    }

    private MaeDriverI getDriver() {
        return getMainController().getDriver();
    }

    public void selectTab(int tabId) {
        // TODO: 1/4/2016 finish this for multi file support

        getView().setCurrentTab(tabId);
        getMainController().switchAnnotationTab(tabId);
    }

    public int selectedTab() {
        return getView().getTabs().getSelectedIndex();
    }

    public Boolean isTextSelected() {
        return this.selected == null || this.selected.length == 0;
    }

    public void assignTextColorOver(int...locations) throws MaeDBException {
        for (int location : locations) {
            assignTextColorAt(location);
        }
    }

    /**
     * This method is for coloring/underlining text in the text window.  It detects
     * overlaps, and should be called every time a tag is added or removed.
     */
    private void assignTextColorAt(int location) throws MaeDBException {
        boolean singular = false;
        boolean plural = false;
        Color c = Color.black; // default color is black
        Set<TagType> activeTags = getMainController().getActiveExtentTags();

        // exclude unactivated elements
        for (TagType type : getDriver().getTagsByTypesAt(location).keySet()) {
            if (activeTags.contains(type)) {
                if (!singular) {
                    c = mainController.getHighlightColor(type);
                    singular = true;
                } else {
                    plural = true;
                    break;
                }

            }
        }
        setColorAtLocation(c, location, plural);
    }

    /**
     * This method is for coloring/underlining text in the entire text window.  It
     * is called when a new file is loaded or toggling all_extents
     */
    public void assignColorsAllActiveTags() throws MaeDBException {
        assignTextColorOver(getDriver().getAllAnchors());
    }

    private void assignTextColorOver(List<Integer> locations) throws MaeDBException {
        for (Integer location : locations) {
            assignTextColorAt(location);
        }
    }

    /**
     * Highlight given spans with given highlighter and painter(color)
     *
     * @param spans   - desired text spans to be highlighted
     * @param painter - highlighter OBJ with color
     */
    public void highlightTextSpans(int[] spans, Highlighter.HighlightPainter painter) throws MaeControlException {
        Highlighter hl = getView().getHighligher();
        for (int span : spans) {
            try {
                // TODO: 1/3/2016 make sure second offset is not exclusive
                hl.addHighlight(span, span, painter);
            } catch (BadLocationException e) {
                throw catchViewException("failed to fetch text region: ", e);
            }
        }
    }

    /**
     * add asterisk to windows title when file is changed
     */
    public void updateTabTitles() throws MaeDBException {
        JTabbedPane tabs = getView().getTabs();
        for (int i = 0; i <tabs.getTabCount(); i++) {
            MaeDriverI driver = getMainController().getDriverAt(i);
            String suffix = "";
            int boldness = Font.PLAIN;
            if (driver.isAnnotationChanged()) {
                suffix = MaeStrings.UNSAVED_SUFFIX;
                boldness = Font.BOLD;
            }
            tabs.setTitleAt(i, driver.getAnnotationFileName() + suffix);
            Component title = tabs.getComponentAt(i);
            title.setFont(title.getFont().deriveFont(boldness));

        }
    }

    public List<ExtentTag> getPotentialArgsInSelectedOrder() throws MaeDBException {
        List<ExtentTag> argsInOrder = new LinkedList<>();
        List<ExtentTag> surplusArgs = new LinkedList<>();

        for (int[] span : selectionHistory) {
            // since getTagsIn() returns a sorted list (by tid),
            // if tags of the same type was found from different selection,
            // they will be aggregated in front of the argInOrder list.
            List<ExtentTag> tagsIn = getDriver().getTagsIn(span);
            argsInOrder.add(tagsIn.remove(0));
            surplusArgs.addAll(tagsIn);
        }

        argsInOrder.addAll(surplusArgs);
        return argsInOrder;

    }

    public String getTextInSelected() throws MaeControlException {
        return getTextIn(selected, true).replace("\n", " ");
    }

    public String getTextIn(int[] spans, boolean trimWhitespaces) throws MaeControlException {
        return getTextIn(SpanHandler.convertArrayToPairs(spans), trimWhitespaces);
    }

    public String getText() {
        return getView().getDocumentPane().getText();
    }

    /**
     * Retrieves the text between two offsets from the document. krim: take a string
     * representing span(s), not 2 integers
     *
     * @param spans text spans
     * @return the text of the tag spans
     */
    public String getTextIn(List<int[]> spans, boolean trimWhitespaces) throws MaeControlException {
        String text = "";
        Iterator<int[]> iter = spans.iterator();
        while (iter.hasNext()) {
            int[] nextPair = iter.next();
            text += getTextBetween(nextPair[0], nextPair[1], trimWhitespaces);
            if (iter.hasNext()) {
                text += MaeStrings.SPANTEXTTRUNC;
            }
        }
        return text;
    }

    /**
     * Retrieves the text between two offsets from the document.
     *
     * @param start start location of the text
     * @param end   end location of the text
     * @return the text
     */
    public String getTextBetween(int start, int end, boolean trimWhitespaces) throws MaeControlException {
        DefaultStyledDocument document = getDocument();
        String text;
        try {
            text = document.getText(start, end - start);
        } catch (BadLocationException e) {
            throw catchViewException("failed to fetch text region: ", e);
        }
        if (trimWhitespaces) {
            text = text.trim();
        }
        return text;
    }

    public List<int[]> getSelectedAsPairs() {
        return SpanHandler.convertArrayToPairs(selected);
    }

    public int[] getSelected() {
        return selected;
    }

    private class TextPanelMouseListener extends MouseAdapter {

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()) {
                getMainController().createTextContextMenu().show(e.getComponent(), e.getX(), e.getY());
            }
        }

    }

    private class TextPanelCaretListener implements CaretListener {

        @Override
        public void caretUpdate(CaretEvent e) {
            Highlighter hl = getView().getDocumentPane().getHighlighter();
            hl.removeAllHighlights();

            int start = Math.min(e.getDot(), e.getMark());
            int end = Math.max(e.getDot(), e.getMark()) + 1; // because dot and mark are inclusive

            if (start != end - 1) {
                // that is, mouse is dragged and text is selected
                if (getMainController().getMode() == MaeMainController.MODE_NORMAL) {
                    // in normal mode, clear selection before adding a new selection
                    clearSelection();
                }
                addSelection(new int[]{start, end});
            } else {
                // in arg sel mode, allow users to select arguments with single clicks
                // TODO: 1/4/2016 need a test to make sure this is safe
                if (getMainController().getMode() == MaeMainController.MODE_ARG_SEL) {
                    addSelection(new int[]{start, end});
                }
            }
            try {
                highlightTextSpans(selected, ColorHandler.getDefaultHighlighter());
            } catch (MaeControlException ignored) {
                // possible MaeException chained from BadLocationException is ignored
            }
            getMainController().propagateSelectionFromTextPanel();
        }
    }

    private class TextPanelTabSwitchListener implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            // TODO: 1/4/2016  finish this for multi file support

        }
    }
}

