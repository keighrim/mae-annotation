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

import edu.brandeis.cs.nlp.mae.MaeStrings;
import edu.brandeis.cs.nlp.mae.database.MaeDBException;
import edu.brandeis.cs.nlp.mae.database.MaeDriverI;
import edu.brandeis.cs.nlp.mae.model.ExtentTag;
import edu.brandeis.cs.nlp.mae.model.LinkTag;
import edu.brandeis.cs.nlp.mae.model.Tag;
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

/**
 * Created by krim on 12/31/2015.
 */
class TextPanelController extends MaeControllerI{

    TextPanelView view;

    private int[] selected;
    private List<int[]> selectionHistory;

    TextPanelController(MaeMainController mainController) throws MaeDBException {
        super(mainController);
        view = new TextPanelView();
        selectionHistory = new LinkedList<>();
        selected = new int[0];
        noTaskGuide();
//        reset();
    }

    @Override
    protected TextPanelView getView() {
        return view;
    }

    public void clearColoring() throws MaeDBException {
        unassignAllFGColor();
        removeAllBGColors();
    }

    void repaintBGColor() {
        try {
            removeAllBGColors();
            addBGColorOver(selected, ColorHandler.getDefaultHighlighter());
        } catch (MaeControlException ignored) {
            // possible MaeException chained from BadLocationException is ignored
        }
    }

    void repaintFGColor(Tag tag) throws MaeDBException {
        if (tag.getTagtype().isExtent()) {
            assignFGColorOver(((ExtentTag) tag).getSpansAsList());
        } else {
            for (ExtentTag arg : ((LinkTag) tag).getArgumentTags()) {
                assignFGColorOver(arg.getSpansAsList());
            }
        }
    }

    private void addGuideTab(String guideTitle, String guideText) {
        disableTabSwitchListener();
        getView().initTabs();
        getView().addTextTab(guideTitle, guideText);
    }

    public void noTaskGuide() {
        addGuideTab(MaeStrings.NO_TASK_IND, MaeStrings.NO_TASK_GUIDE);
    }

    public void noDocumentGuide() {
        addGuideTab(MaeStrings.NO_FILE_IND, MaeStrings.NO_FILE_GUIDE);
    }

    @Override
    void addListeners() {
        getView().getDocumentPane().addCaretListener(new TextPanelCaretListener());
        getView().getDocumentPane().addMouseListener(new TextPanelMouseListener());
    }

    void disableTabSwitchListener() {

        JTabbedPane tabs = getView().getTabs();
        for (ChangeListener listener : tabs.getChangeListeners()) {
            if (listener instanceof TextPanelTabSwitchListener) {
                tabs.removeChangeListener(listener);
            }
        }

    }

    protected void setSelection(int[] spans) {
        selected = spans;
    }

    void clearSelection() {
        selectionHistory.clear();
        setSelection(new int[0]);
        removeAllBGColors();
    }

    void clearCaret() {
        getView().getDocumentPane().setCaretPosition(0);

    }

    boolean validateNewSpanPair(int[] newSpanPair) throws MaeDBException {
        if (intPairCollectionContains(selectionHistory, newSpanPair)) {
            return false;
        }
        int[] newSpanArray = SpanHandler.range(newSpanPair[0], newSpanPair[1]);
        if (getMainController().getMode() == MaeMainController.MODE_ARG_SEL) {
            return getMainController().getExtentTagsIn(newSpanArray).size() > 0;
        }
        return true;
    }

    boolean intPairCollectionContains(Collection<int[]> c, int[] pair) {
        for (int[] inCollection : c) {
            if (Arrays.equals(pair, inCollection)) {
                return true;
            }
        }
        return false;
    }

    void addSelection(int[] newSpanPair) throws MaeDBException {
        if (validateNewSpanPair(newSpanPair)) {
            selectionHistory.add(0, newSpanPair);
        }
        setSelection(SpanHandler.convertPairsToArray(selectionHistory));
    }

    int[] getLatestSelection() {
        if (selectionHistory.size() > 0) {
            return selectionHistory.get(0);
        } else {
            return null;
        }

    }

    int[] leavingLatestSelection() throws MaeDBException {
        int[] latest = getLatestSelection();
        if (latest == null) {
            return new int[0];
        }
        clearSelection();
        addSelection(latest);
        return SpanHandler.range(latest[0], latest[1]);
    }

    int[] undoSelection() {
        if (selectionHistory.size() > 0) {
            int[] undoed = selectionHistory.remove(0);
            setSelection(SpanHandler.convertPairsToArray(selectionHistory));
            return SpanHandler.range(undoed[0], undoed[1]);
        } else {
            return null;
        }
    }

    void addDocumentTab(String documentTitle, String documentText) {
        if (!getView().isAnyDocumentOpen()) {
            getView().initTabs();
        }
        JTabbedPane tabs = getView().getTabs();
        TextPanelView.DocumentTabTitle title = new TextPanelView.DocumentTabTitle(documentTitle, tabs);
        title.addCloseListener(new DocumentCloseListener());
        getView().addTextTab(title, documentText);
        addListeners();
        if (!getView().isAnyDocumentOpen()) {
            getView().getTabs().addChangeListener(new TextPanelTabSwitchListener());
            getView().setDocumentOpen(true);
        }

    }

    void addAdjudicationTab(String goldTitle, String goldText) throws MaeDBException {
        JTabbedPane tabs = getView().getTabs();
        TextPanelView.DocumentTabTitle title = new TextPanelView.DocumentTabTitle(goldTitle, tabs);
        title.addCloseListener(new DocumentCloseListener());
        getView().addAdjudicationTab(title, goldText);
        addListeners();
        for (int i = 1; i < tabs.getTabCount(); i++) {
            tabs.getTabComponentAt(i).setEnabled(false);
            tabs.setEnabledAt(i, false);

        }
        updateTabTitles(true);

    }

    void removeAdjudicationTab() throws MaeDBException {
        JTabbedPane tabs = getView().getTabs();
        System.out.println(((TextPanelView.DocumentTabTitle) tabs.getTabComponentAt(0)).getLabel());
        tabs.remove(0);

        for (int i = 0; i < tabs.getTabCount(); i++) {
            tabs.getTabComponentAt(i).setEnabled(true);
            tabs.setEnabledAt(i, true);
        }
        updateTabTitles(false);

    }

    private DefaultStyledDocument getDocument() {
        return getView().getDocument();
    }

    void closeDocumentTab(int i) {
        getView().getTabs().remove(i);
    }

    void resetFontSize() {
        getView().setTextFont(new Font(TextPanelView.DEFAULT_FONT_FAMILY, Font.PLAIN, TextPanelView.DEFAULT_FONT_SIZE));

    }

    void increaseFontSize() {
        Font original = getView().getTextFont();
        Font increased = new Font(original.getName(), original.getStyle(), original.getSize() + 1);
        getView().setTextFont(increased);

    }

    void decreaseFontSize() {
        Font original = getView().getTextFont();
        Font decreased = new Font(original.getName(), original.getStyle(), original.getSize() - 1);
        getView().setTextFont(decreased);

    }

    void veryLargeFonts() {
        getView().setTextFont(new Font(TextPanelView.DEFAULT_FONT_FAMILY, Font.PLAIN, TextPanelView.VERYLARGE_FONT_SIZE));

    }

    public int getCurrentTab() {
        return getView().getTabs().getSelectedIndex();
    }

    public int getOpenTabCount() {
        return getView().getTabs().getTabCount();
    }

    /**
     * add asterisk to windows title when file is changed
     */
    void updateTabTitles(boolean colorToo) throws MaeDBException {
        JTabbedPane tabs = getView().getTabs();
        for (int i = 0; i <tabs.getTabCount(); i++) {
            MaeDriverI driver = getMainController().getDriverAt(i);
            TextPanelView.DocumentTabTitle title = (TextPanelView.DocumentTabTitle) tabs.getTabComponentAt(i);
            title.setLabel(driver.getAnnotationFileBaseName());
            title.setChanged(driver.isAnnotationChanged());
            if (colorToo) {
                title.setLabelColor(getMainController().getDocumentColor(i));
            } else {
                title.setLabelColor(Color.BLACK);
            }

        }
    }

    int getSelectedTabIndex() {
        return getView().getTabs().getSelectedIndex();
    }

    Boolean isTextSelected() {
        return this.selected.length > 0;
    }

    int[] getSelected() {
        return selected;
    }

    List<int[]> getSelectedAsPairs() {
        return SpanHandler.convertArrayToPairs(selected);
    }

    String getPrimaryText() {
        return getView().getDocumentPane().getText();
    }

    String getSelectedText() throws MaeControlException {
        if (selected.length > 0) {
            return getTextIn(selected, true).replace("\n", " ");
        } else {
            return "NO-TEXT-SELECTED";
        }
    }

    List<ExtentTag> getSelectedArgumentsInOrder() throws MaeDBException {
        LinkedList<ExtentTag> argsInOrder = new LinkedList<>();
        List<ExtentTag> surplusArgs = new LinkedList<>();

        for (int[] span : selectionHistory) {
            // since getTagsIn() returns a sorted list (by tid),
            // if tags of the same type was found from different selection,
            // they will be aggregated in front of the argInOrder list.
            List<ExtentTag> tagsIn = getDriver().getTagsIn(span);
            if (tagsIn != null) {
                tagsIn.removeAll(argsInOrder);
                tagsIn.removeAll(surplusArgs);
                if (tagsIn.size() > 0) {
                    // add to first since we are looping backward through selection history
                    argsInOrder.addFirst(tagsIn.remove(0));
                    surplusArgs.addAll(tagsIn);
                }
            }
        }

        argsInOrder.addAll(surplusArgs);
        return argsInOrder;

    }

    String getTextIn(int[] spans, boolean trimWhitespaces) throws MaeControlException {
        if (spans != null && spans.length > 0) {
            return getTextIn(SpanHandler.convertArrayToPairs(spans), trimWhitespaces);
        }
        return null;

    }

    /**
     * Retrieves the text between two offsets from the document. krim: take a string
     * representing span(s), not 2 integers
     *
     * @param spans text spans
     * @return the text of the tag spans
     */
    String getTextIn(List<int[]> spans, boolean trimWhitespaces) throws MaeControlException {
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
    String getTextBetween(int start, int end, boolean trimWhitespaces) throws MaeControlException {
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

    void removeAllBGColors() {
        getView().getHighlighter().removeAllHighlights();

    }

    void assignAllFGColors() throws MaeDBException {
        assignFGColorOver(getDriver().getAllAnchors());

    }

    void unassignAnchoredFGColors() throws MaeDBException {
        List<Integer> anchorLocations = getDriver().getAllAnchors();
        int anchorIndex = 0;
        while (anchorIndex < anchorLocations.size()) {
            anchorIndex += setFGColorAtLocation(Color.black, anchorLocations.get(anchorIndex), false, false);
        }
    }

    void unassignAllFGColor() throws MaeDBException {
        int[] entireDocument = SpanHandler.range(0, getDriver().getPrimaryText().length());
        for (int anchor : entireDocument) {
            setFGColorAtLocation(Color.black, anchor, false, false);

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
    private int setFGColorAtLocation(Color color, int location, boolean underline, boolean italic) {
        DefaultStyledDocument styleDoc = getDocument();
        SimpleAttributeSet attributeSet = new SimpleAttributeSet();
        StyleConstants.setForeground(attributeSet, color);
        StyleConstants.setUnderline(attributeSet, underline);
        StyleConstants.setItalic(attributeSet, italic);
        int length = 0;
        try {
            length = Character.isHighSurrogate(styleDoc.getText(location, 1).charAt(0)) ? 2 : 1;
        } catch (BadLocationException ignored) {
        }
        styleDoc.setCharacterAttributes(location, length, attributeSet, false);
        return length;
    }

    void assignOverlappingColorAt(Integer location, Color srcColor, boolean fullOverlap) {
        setFGColorAtLocation(srcColor, location, fullOverlap, false);
    }

    void assignFGColorOver(List<Integer> locations) throws MaeDBException {
//        int locIndex = 0;
//        while (locIndex < locations.size()) {
//            locIndex += assignFGColorAt(locations.get(locIndex));
//        }
        for (Integer location : locations) {
            assignFGColorAt(location);
        }
    }

    /**
     * This method is for coloring/underlining text in the text window.  It detects
     * overlaps, and should be called every time a tag is added or removed.
     */
    private int assignFGColorAt(int location) throws MaeDBException {
        boolean singular = false;
        boolean plural = false;
        boolean argument = false;
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
            // then, italicize where any link is associated
            for (ExtentTag tag : allTags.get(type)) {
                for (LinkTag linker : getDriver().getLinksHasArgumentTag(tag)) {
                    if (activeLinks.contains(linker.getTagtype())) {
                        argument = true;
                        break;
                    }
                }
            }
        }
        return setFGColorAtLocation(c, location, plural, argument);
    }

    void addBGColorOver(int[] spans, Highlighter.HighlightPainter painter) throws MaeControlException {
        if (spans.length == 0) {
            return;
        }
        Highlighter hl = getView().getHighlighter();
        try {
            for (int anchor : spans) {
                hl.addHighlight(anchor, anchor+1, painter);
            }
            getView().getDocumentPane().scrollRectToVisible(getView().getDocumentPane().modelToView(spans[0]));
        } catch (BadLocationException e) {
            throw catchViewException("failed to fetch a text region: ", e);
        }
    }

    private class TextPanelMouseListener extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {
            if (e.isPopupTrigger()) {
                createAndShowContextMenu(e);
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()) {
                createAndShowContextMenu(e);
            }
        }

        void createAndShowContextMenu(MouseEvent e) {
            getMainController().createTextContextMenu().show(e.getComponent(), e.getX(), e.getY());

        }

    }

    private class TextPanelCaretListener implements CaretListener {

        boolean acceptingSingleClick() {
            return getMainController().getMode() == MaeMainController.MODE_ARG_SEL;

        }

        @Override
        public void caretUpdate(CaretEvent e) {

            try {
            if (e.getDot() != e.getMark()) { // that is, mouse is dragged and text is selected
                addDraggedSelection(e.getDot(), e.getMark());
            } else if (getMainController().getMode() == MaeMainController.MODE_MULTI_SPAN) {
                // MSPAN mode always ignore single click
            } else {
                if (getMainController().getMode() == MaeMainController.MODE_NORMAL) {
                    clearSelection(); // single click will clear out prev selection
                }
                if (acceptingSingleClick()) {
                        addSelection(new int[]{e.getDot(), e.getDot() + 1});
                }
            }
            } catch (MaeDBException ex) {
                getMainController().showError(ex);
            }
            repaintBGColor();
            getMainController().propagateSelectionFromTextPanel();
        }

        void addDraggedSelection(int dot, int mark) throws MaeDBException {
            int start = Math.min(dot, mark);
            int end = Math.max(dot, mark);
            if (getMainController().getMode() == MaeMainController.MODE_NORMAL) {
                // in normal mode, clear selection before adding a new selection
                clearSelection();
            }
            addSelection(new int[]{start, end});
        }
    }

    private class DocumentCloseListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            TextPanelView.DocumentTabTitle title = getProperParent((Component) e.getSource());
            if (title.isEnabled()) {
                getView().getTabs().setSelectedIndex(title.getTabIndex());
                if (getMainController().showUnsavedChangeWarning()) {
                    getMainController().closeCurrentDocument();
                }
            }

        }
        TextPanelView.DocumentTabTitle getProperParent(Component component) {
            if (component instanceof TextPanelView.DocumentTabTitle) {
                return (TextPanelView.DocumentTabTitle) component;
            } else {
                return getProperParent(component.getParent());
            }
        }
    }

    private class TextPanelTabSwitchListener implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            getMainController().switchAnnotationDocument(((JTabbedPane) e.getSource()).getSelectedIndex());

        }
    }
}

