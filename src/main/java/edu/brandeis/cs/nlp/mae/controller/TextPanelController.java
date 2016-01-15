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
import edu.brandeis.cs.nlp.mae.util.MappedSet;
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
import java.util.*;
import java.util.List;

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
        selected = new int[0];
        reset();
    }

    @Override
    protected TextPanelView getView() {
        return view;
    }

    @Override
    void reset() throws MaeDBException {
        if (!getMainController().isTaskLoaded()) {
            addGuideTab(MaeStrings.NO_TASK_IND, MaeStrings.NO_TASK_GUIDE);
        } else if (!getMainController().isDocumentOpen()) {
            addGuideTab(MaeStrings.NO_FILE_IND, MaeStrings.NO_FILE_GUIDE);
        } else {
            selectionHistory.clear();
            if (getMainController().isDocumentOpen()) {
                clearColoring();
                addListeners();
                assignAllFGColors();
            }
        }

    }

    private void clearColoring() throws MaeDBException {
        unassignAllFGColors();
        removeAllBGColors();
    }

    private void addGuideTab(String guideTitle, String guideText) {
        getView().clearAllTabs();
        getView().addTextTab(guideTitle, guideText);
    }

    @Override
    void addListeners() {
        getView().getDocumentPane().addCaretListener(new TextPanelCaretListener());
        getView().getDocumentPane().addMouseListener(new TextPanelMouseListener());
        // TODO: 2016-01-11 20:50:41EST this listener is used 4MF
//        getView().getTabs().addChangeListener(new TextPanelTabSwitchListener());
    }

    protected void setSelection(int[] spans) {
        selected = spans;
    }

    public void clearSelection() {
        selectionHistory.clear();
        setSelection(new int[0]);
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
        // adding a new document wll wipe any existing tabs, either guide or documents
        // TODO: 2016-01-11 20:47:48EST fix this 4MF
//        if (!getView().isAnyDocumentOpen()) {
        getView().clearAllTabs();
//        }
        getView().addTextTab(documentTitle, documentText);
        if (!getView().isAnyDocumentOpen()) {
            addListeners();
        }
        getView().setDocumentOpen(true);

    }

    private DefaultStyledDocument getDocument() {
        return getView().getDocument();
    }

    public void closeDocument(int i) {
        // TODO: 1/4/2016 need tests if this works well with tab switch listener to properly change current tab as well
        getView().getTabs().remove(i);
    }

    public void selectTab(int tabId) {
        // TODO: 1/4/2016 finish this for multi file support
        getView().selectTab(tabId);
        getMainController().switchAnnotationTab(tabId);
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

    public int getSelectedTabIndex() {
        return getView().getTabs().getSelectedIndex();
    }

    public Boolean isTextSelected() {
        return this.selected.length > 0;
    }

    public int[] getSelected() {
        return selected;
    }

    public List<int[]> getSelectedAsPairs() {
        return SpanHandler.convertArrayToPairs(selected);
    }

    public String getPrimaryText() {
        return getView().getDocumentPane().getText();
    }

    public String getSelectedText() throws MaeControlException {
        return getTextIn(selected, true).replace("\n", " ");
    }

    public List<ExtentTag> getSelectedArgumentsInOrder() throws MaeDBException {
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

    public String getTextIn(int[] spans, boolean trimWhitespaces) throws MaeControlException {
        return getTextIn(SpanHandler.convertArrayToPairs(spans), trimWhitespaces);
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

    public void removeAllBGColors() {
        getView().getHighlighter().removeAllHighlights();

    }

    public void assignAllFGColors() throws MaeDBException {
        assignFGColorOver(getDriver().getAllAnchors());

    }

    public void unassignAllFGColors() throws MaeDBException {
        List<Integer> anchorLocations = getDriver().getAllAnchors();
        for (Integer location : anchorLocations) {
            setFGColorAtLocation(Color.black, location, false, false);
        }
    }

    /**
     * Sets the color of a specific span of text.  Called for each extent tag.
     *
     * @param color The color the text will become. Determined by the tag name and
     *              colorTable (Hashtable)
     * @param location the location of the start of the extent
     * @param underline whether or not the text will be underlined, in which case two or more tags are associated with the location
     */
    private void setFGColorAtLocation(Color color, int location, boolean underline, boolean italic) {
        // TODO: 2016-01-13 10:52:12EST continue from here, changing color is not wokring
        DefaultStyledDocument styleDoc = getDocument();
        SimpleAttributeSet attributeSet = new SimpleAttributeSet();
        StyleConstants.setForeground(attributeSet, color);
        StyleConstants.setUnderline(attributeSet, underline);
        StyleConstants.setItalic(attributeSet, italic);
        styleDoc.setCharacterAttributes(location, 1, attributeSet, false);
    }

    public void assignFGColorOver(int...locations) throws MaeDBException {
        for (int location : locations) {
            assignFGColorAt(location);
        }
    }

    public void assignFGColorOver(List<Integer> locations) throws MaeDBException {
        for (Integer location : locations) {
            assignFGColorAt(location);
        }
    }

    /**
     * This method is for coloring/underlining text in the text window.  It detects
     * overlaps, and should be called every time a tag is added or removed.
     */
    private void assignFGColorAt(int location) throws MaeDBException {
        boolean singular = false;
        boolean plural = false;
        Color c = Color.black; // default color is black
        Set<TagType> activeTags = getMainController().getActiveExtentTags();
        Set<TagType> activeLinks = getMainController().getActiveLinkTags();

        // exclude unactivated elements
        MappedSet<TagType, ExtentTag> allTags = getDriver().getTagsByTypesAt(location);
        for (TagType type : allTags.keySet()) {
            if (activeTags.contains(type)) {
                if (!singular) {
                    c = getMainController().getFGColor(type);
                    singular = true;
                } else {
                    plural = true;
                    break;
                }

            }
        }
        // TODO: 2016-01-14 20:16:49EST find a way to properly set italic
        boolean argument = false;
        setFGColorAtLocation(c, location, plural, argument);
    }

    /**
     * Highlight given spans with given highlighter and painter(color)
     *
     * @param spans   - desired text spans to be highlighted
     * @param painter - highlighter OBJ with color
     */
    public void addBGColorOver(int[] spans, Highlighter.HighlightPainter painter) throws MaeControlException {
        if (spans.length == 0) {
            return;
        }
        Highlighter hl = getView().getHighlighter();
        try {
            for (int anchor : spans) {
                hl.addHighlight(anchor, anchor, painter);
            }
            getView().getDocumentPane().scrollRectToVisible(getView().getDocumentPane().modelToView(spans[0]));
        } catch (BadLocationException e) {
            throw catchViewException("failed to fetch text region: ", e);
        }
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
//            Highlighter hl = getView().getHighlighter();
//            hl.removeAllHighlights();

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
                switch (getMainController().getMode()) {
                    case MaeMainController.MODE_NORMAL:
                        clearSelection();
                        break;
                    case MaeMainController.MODE_ARG_SEL:
                        // in arg sel mode, allow users to select arguments with single clicks
                        // TODO: 1/4/2016 need a test to make sure this is safe
                        addSelection(new int[]{start, end});
                        break;

                }
            }
            try {
                addBGColorOver(selected, ColorHandler.getDefaultHighlighter());
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

