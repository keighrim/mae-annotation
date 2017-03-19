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

package edu.brandeis.cs.nlp.mae.controller.textpanel;

import edu.brandeis.cs.nlp.mae.MaeStrings;
import edu.brandeis.cs.nlp.mae.controller.MaeControlException;
import edu.brandeis.cs.nlp.mae.controller.MaeControllerI;
import edu.brandeis.cs.nlp.mae.controller.MaeMainController;
import edu.brandeis.cs.nlp.mae.database.MaeDBException;
import edu.brandeis.cs.nlp.mae.database.MaeDriverI;
import edu.brandeis.cs.nlp.mae.model.ExtentTag;
import edu.brandeis.cs.nlp.mae.model.LinkTag;
import edu.brandeis.cs.nlp.mae.model.Tag;
import edu.brandeis.cs.nlp.mae.model.TagType;
import edu.brandeis.cs.nlp.mae.util.ColorHandler;
import edu.brandeis.cs.nlp.mae.util.FontHandler;
import edu.brandeis.cs.nlp.mae.util.MappedSet;
import edu.brandeis.cs.nlp.mae.util.SpanHandler;
import edu.brandeis.cs.nlp.mae.view.DocumentTabTitle;
import edu.brandeis.cs.nlp.mae.view.TextPanelView;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.text.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by krim on 12/31/2015.
 */
public class TextPanelController extends MaeControllerI {

    TextPanelView view;

    private int[] selected;
    private List<int[]> selectionHistory;
    public static final int DEFAULT_FONT_SIZE = 14;
    public static final Color DEFAULT_FONT_COLOR = Color.BLACK;
    public static final String DEFAULT_FONT_FAMILY = Font.MONOSPACED;
    private int currentFontSize = DEFAULT_FONT_SIZE;


    public TextPanelController(MaeMainController mainController) throws MaeDBException {
        super(mainController);
        view = new TextPanelView();
        selectionHistory = new LinkedList<>();
        selected = new int[0];
        addGuideTab();
    }

    @Override
    public TextPanelView getView() {
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

    public void repaintFGColor(Tag tag) throws MaeDBException {
        if (tag.getTagtype().isExtent()) {
            assignFGColorOver(((ExtentTag) tag).getSpansAsList());
        } else {
            for (ExtentTag arg : ((LinkTag) tag).getArgumentTags()) {
                assignFGColorOver(arg.getSpansAsList());
            }
        }
    }

    public void addGuideTab() {
        if (!getMainController().isDocumentOpen()) {
            disableTabSwitchListener();
            getView().initTabs();
            if (!getMainController().isTaskLoaded()) {
                getView().addTextTab(MaeStrings.NO_TASK_IND, MaeStrings.NO_TASK_GUIDE);
            } else {
                getView().addTextTab(MaeStrings.NO_FILE_IND, MaeStrings.NO_FILE_GUIDE);
            }
        }
    }

    @Override
    protected void addListeners() {
        getView().getDocumentPane().addCaretListener(
                new TextPanelCaretListener(this, this.getMainController()));
        getView().getDocumentPane().addMouseListener(
                new TextPanelMouseListener(this.getMainController()));
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

    public void clearSelection() {
        selectionHistory.clear();
        setSelection(new int[0]);
        removeAllBGColors();
    }

    public void clearCaret() {
        getView().getDocumentPane().setCaretPosition(0);

    }

    boolean validateNewSpanPair(int[] newSpanPair) throws MaeDBException {
        if (intPairCollectionContains(selectionHistory, newSpanPair)) {
            return false;
        }
        int[] newSpanArray = SpanHandler.range(newSpanPair[0], newSpanPair[1]);
        return getMainController().getMode() != MaeMainController.MODE_ARG_SEL
                || getMainController().getExtentTagsIn(newSpanArray).size() > 0;
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

    public int[] undoSelection() {
        if (selectionHistory.size() > 0) {
            int[] undoed = selectionHistory.remove(0);
            setSelection(SpanHandler.convertPairsToArray(selectionHistory));
            return SpanHandler.range(undoed[0], undoed[1]);
        } else {
            return null;
        }
    }

    public void addDocumentTab(String documentTitle, String documentText) throws MaeDBException {
        if (!getView().isAnyDocumentOpen()) {
            getView().initTabs();
        }
        JTabbedPane tabs = getView().getTabs();
        DocumentTabTitle title = new DocumentTabTitle(documentTitle, tabs);
        title.addCloseListener(new DocumentCloseListener(this.getMainController()));
        getView().addTextTab(title, documentText, currentFontSize, !getMainController().isAdjudicating());
        addListeners();
        if (!getView().isAnyDocumentOpen()) {
            getView().getTabs().addChangeListener(new TextPanelTabSwitchListener(this.getMainController()));
            getView().setDocumentOpen(true);
        }
        if (getMainController().isAdjudicating()) {
            int newTab = tabs.getTabCount() - 1;
            tabs.getTabComponentAt(newTab).setEnabled(false);
            tabs.setEnabledAt(newTab, false);
            updateTabTitles(true);
        }

    }

    public void addAdjudicationTab(String goldTitle, String goldText) throws MaeDBException {
        JTabbedPane tabs = getView().getTabs();
        DocumentTabTitle title = new DocumentTabTitle(goldTitle, tabs);
        title.addCloseListener(new DocumentCloseListener(this.getMainController()));
        getView().addAdjudicationTab(title, goldText, currentFontSize);
        addListeners();
        for (int i = 1; i < tabs.getTabCount(); i++) {
            tabs.getTabComponentAt(i).setEnabled(false);
            tabs.setEnabledAt(i, false);

        }
        updateTabTitles(true);

    }

    public void removeAdjudicationTab() throws MaeDBException {
        JTabbedPane tabs = getView().getTabs();
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

    public void closeDocumentTab(int i) {
        getView().getTabs().remove(i);
    }

    public void resetFontSize() {
        SimpleAttributeSet attributeSet = new SimpleAttributeSet();
        StyleConstants.setFontSize(attributeSet, DEFAULT_FONT_SIZE);
        currentFontSize = DEFAULT_FONT_SIZE;
        getView().setTextFont(attributeSet);

    }

    public void increaseFontSize() {
        SimpleAttributeSet attributeSet = new SimpleAttributeSet();
        StyleConstants.setFontSize(attributeSet, ++currentFontSize);
        getView().setTextFont(attributeSet);

    }

    public void decreaseFontSize() {
        SimpleAttributeSet attributeSet = new SimpleAttributeSet();
        StyleConstants.setFontSize(attributeSet, --currentFontSize);
        getView().setTextFont(attributeSet);

    }

    public void bigFontSize() {
        SimpleAttributeSet attributeSet = new SimpleAttributeSet();
        StyleConstants.setFontSize(attributeSet, 36);
        getView().setTextFont(attributeSet);

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
    public void updateTabTitles(boolean colorToo) throws MaeDBException {
        JTabbedPane tabs = getView().getTabs();
        for (int i = 0; i <tabs.getTabCount(); i++) {
            MaeDriverI driver = getMainController().getDriverAt(i);
            DocumentTabTitle title = (DocumentTabTitle) tabs.getTabComponentAt(i);
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

    public Boolean isTextSelected() {
        return this.selected.length > 0;
    }

    public int[] getSelected() {
        return selected;
    }

    List<int[]> getSelectedAsPairs() {
        return SpanHandler.convertArrayToPairs(selected);
    }

    String getPrimaryText() {
        return getView().getDocumentPane().getText();
    }

    public String getSelectedText() throws MaeControlException {
        if (selected.length > 0) {
            return getTextIn(selected, false).replace("\n", " ");
        } else {
            return "NO-TEXT-SELECTED";
        }
    }

    public List<ExtentTag> getSelectedArgumentsInOrder() throws MaeDBException {
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

    public String getTextIn(int[] spans, boolean trimWhitespaces) throws MaeControlException {
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

    public void removeAllBGColors() {
        getView().getHighlighter().removeAllHighlights();

    }

    void unassignAnchoredFGColors() throws MaeDBException {
        List<Integer> anchorLocations = getDriver().getAllAnchorLocations();
        int anchorIndex = 0;
        while (anchorIndex < anchorLocations.size()) {
            anchorIndex += setFGColorAtLocation(DEFAULT_FONT_COLOR, anchorLocations.get(anchorIndex), false, false);
        }
    }

    void unassignAllFGColor() throws MaeDBException {
        int caretPos = getView().getDocumentPane().getCaretPosition();
        getView().getDocumentPane().setStyledDocument(
                FontHandler.stringToSimpleStyledDocument(getDriver().getPrimaryText(), DEFAULT_FONT_FAMILY, currentFontSize, Color.BLACK)
        );
        getView().getDocumentPane().setCaretPosition(caretPos);
        try {
            Rectangle rect = getView().getDocumentPane().modelToView(caretPos);
            if (rect != null) {
                getView().getDocumentPane().scrollRectToVisible(getView().getDocumentPane().modelToView(caretPos));
            }
        } catch (BadLocationException ignored) {
        }
    }

    private int setFGColorAtLocation(Color color, int location, boolean fullOverlap, boolean partialOverlap) {
        try {
            DefaultStyledDocument styleDoc = getDocument();
            SimpleAttributeSet attributeSet = new SimpleAttributeSet();
            StyleConstants.setForeground(attributeSet, color);
            StyleConstants.setUnderline(attributeSet, fullOverlap);
            StyleConstants.setItalic(attributeSet, partialOverlap);
            int length = Character.isHighSurrogate(styleDoc.getText(location, 1).charAt(0)) ? 2 : 1;
            styleDoc.setCharacterAttributes(location, length, attributeSet, false);
            return length;
        } catch (BadLocationException ignored) {
        }
        return 0;
    }

    void assignOverlappingColorOver(List<Integer> locations, Color srcColor, boolean fullOverlap) {
        int locIndex = 0;
        while (locIndex < locations.size()) {
            locIndex += setFGColorAtLocation(srcColor, locations.get(locIndex), fullOverlap, false);
        }
    }

    public void assignOverlappingColorAt(Integer location, Color srcColor, boolean fullOverlap) {
        DefaultStyledDocument styleDoc = getDocument();
        try {
            if (location == 0 || !Character.isHighSurrogate(styleDoc.getText(location - 1, 1).charAt(0))) {
                setFGColorAtLocation(srcColor, location, fullOverlap, false);
            }
        } catch (BadLocationException ignored) {
        }
    }

    public void assignAllFGColor() throws MaeDBException {
        massivelyAssignFGColors(getDriver().getAllAnchorLocations());

    }

    void assignFGColorOf(TagType type) throws MaeDBException {
        massivelyAssignFGColors(getDriver().getAllAnchorLocationsOfTagType(type));
    }

    public void massivelyAssignFGColors(List<Integer> largeSpan) throws MaeDBException {
        int locIndex = 0;
        Set<TagType> activeTags = getMainController().getActiveExtentTags();
        Set<TagType> activeLinks = getMainController().getActiveLinkTags();

        MappedSet<Integer, TagType> existingAnchors = new MappedSet<>();
        for (TagType tagType : activeTags) {
            for (Integer anchor : getDriver().getAllAnchorLocationsOfTagType(tagType)) {
                existingAnchors.putItem(anchor, tagType);
            }
        }

        MappedSet<Integer, TagType> existingArgumentAnchors = new MappedSet<>();
        for (TagType tagType : activeLinks) {
            for (Integer anchor : getDriver().getAllAnchorLocationsOfTagType(tagType)) {
                existingArgumentAnchors.putItem(anchor, tagType);
            }
        }

        while (locIndex < largeSpan.size()) {
            Integer location = largeSpan.get(locIndex);
            boolean plural = false;
            boolean argument = false;
            Color c = DEFAULT_FONT_COLOR;

            if (existingAnchors.containsKey(location)) {
                List<TagType> types = new ArrayList<>(existingAnchors.get(location));
                if (types.size() > 0) {
                    c = getMainController().getFGColor(types.get(0));
                }
                if (types.size() > 1) {
                    plural = true;
                }
            }
            if (existingArgumentAnchors.containsKey(location)) {
                argument = true;
            }

            locIndex += setFGColorAtLocation(c, location, plural, argument);
        }
    }


    public void assignFGColorOver(List<Integer> locations) throws MaeDBException {
        int locIndex = 0;
        while (locIndex < locations.size()) {
            locIndex += assignFGColorAt(locations.get(locIndex));
        }
    }

    private int assignFGColorAt(int location) throws MaeDBException {
        boolean singular = false;
        boolean plural = false;
        boolean argument = false;
        Color c = DEFAULT_FONT_COLOR;
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

    public void addBGColorOver(int[] spans, Highlighter.HighlightPainter painter) throws MaeControlException {
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

}

