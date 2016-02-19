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

import edu.brandeis.cs.nlp.mae.MaeHotKeys;
import edu.brandeis.cs.nlp.mae.MaeStrings;
import edu.brandeis.cs.nlp.mae.database.MaeDBException;
import edu.brandeis.cs.nlp.mae.database.MaeDriverI;
import edu.brandeis.cs.nlp.mae.io.FileWriter;
import edu.brandeis.cs.nlp.mae.io.MaeIOException;
import edu.brandeis.cs.nlp.mae.io.MaeIOXMLException;
import edu.brandeis.cs.nlp.mae.io.XMLLoader;
import edu.brandeis.cs.nlp.mae.model.*;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import java.util.List;

/**
 * All popups and supplement sub windows are managed within this class
 *
 * Created by krim on 1/1/2016.
 */
class DialogController {
    JFrame parent;
    MaeMainController mainController;
    JFileChooser fileChooser;

    DialogController(MaeMainController mainController) {
        this.mainController = mainController;
        this.parent = getMainController().getMainWindow();

        this.fileChooser = new JFileChooser(".");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

    }

    JFileChooser getFileChooser() {
        return fileChooser;
    }

    JFrame getParent() {
        return parent;
    }

    MaeMainController getMainController() {
        return mainController;
    }

    int showWarning(String warnMessage) {
        return JOptionPane.showConfirmDialog(getParent(), warnMessage, MaeStrings.WARN_POPUP_TITLE, JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
    }

    void showError(Exception e) {
        // TODO: 1/1/2016 maybe can implement "send error log to dev" button
        String errorTitle = e.getClass().getName();
        String errorMessage = e.getMessage();
        JOptionPane.showMessageDialog(getParent(), errorMessage, errorTitle, JOptionPane.WARNING_MESSAGE);

    }

    void showError(String message, Exception e) {
        String errorTitle = e.getClass().getName();
        String errorMessage = String.format("%s: %s", message, e.getMessage());
        JOptionPane.showMessageDialog(getParent(), errorMessage, errorTitle, JOptionPane.WARNING_MESSAGE);

    }

    void showError(String message) {
        JOptionPane.showMessageDialog(getParent(), message, MaeStrings.ERROR_POPUP_TITLE, JOptionPane.WARNING_MESSAGE);

    }

    File showFileChooseDialogAndSelect(String defaultName, boolean saveFile) {
        if (defaultName.length() > 0) {
            fileChooser.setSelectedFile(new File(defaultName));
        }

        if (saveFile) {
            if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
                return fileChooser.getSelectedFile();
            }
        } else {
            if (fileChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
                return fileChooser.getSelectedFile();
            }
        }
        return null;
    }

    void setAsArgument(String argumentTid) throws MaeDBException {
        SetArgumentOptionPanel options = new SetArgumentOptionPanel();
        int result = JOptionPane.showConfirmDialog(getMainController().getMainWindow(),
                options,
                String.format("Setting %s an argument of", argumentTid),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            LinkTag linker = options.getSelectedLinkTag();
            ArgumentType argType = options.getSelectedArgumentType();
            getMainController().surgicallyUpdateCell(linker, argType.getName() + MaeStrings.ARG_IDCOL_SUF, argumentTid);
        }

    }

    LinkTag createLink(TagType linkType, List<ExtentTag> candidates) throws MaeDBException {
        CreateLinkOptionPanel options = new CreateLinkOptionPanel(linkType, candidates);
        int result = JOptionPane.showConfirmDialog(getMainController().getMainWindow(),
                options,
                String.format("Create a new link: %s ", linkType.getName()),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            Map<ArgumentType, ExtentTag> arguments = options.getSelectedArguments();
            LinkTag linker = (LinkTag) getMainController().createTagFromMenu(linkType);
            for (ArgumentType argType : arguments.keySet()) {
                String argTypeName = argType.getName();
                String argTid = arguments.get(argType).getTid();
                getMainController().surgicallyUpdateCell(linker, argTypeName + MaeStrings.ARG_IDCOL_SUF, argTid);
            }
            return linker;

        }
        return null;
    }

    boolean showIncompleteTagsWarning(Set<Tag> incomplete, boolean simplyWarn) {
        if (incomplete == null || incomplete.size() < 1) {
            return true;
        }
        IncompleteTagsWarningOptionPanel options = new IncompleteTagsWarningOptionPanel(incomplete, simplyWarn);
        options.setVisible(true);
        switch (options.getResponse()) {
            case JOptionPane.YES_OPTION:
                options.dispose();
                return true;
            case JOptionPane.CANCEL_OPTION:
                getMainController().selectTagAndTable(options.getSelectedTag());
            default:
                options.dispose();
                return false;
        }
    }

    public File showStartAdjudicationDialog() throws MaeControlException, MaeDBException, MaeIOException {
        Object[] options = {"Yes", "No, Load Gold Standard file", "Cancel"};
        int response = JOptionPane.showOptionDialog(getParent(),
                "Start adjudication with an empty Gold Standard file?",
                "Start adjudication",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);
        switch (response) {
            case JOptionPane.YES_OPTION:
                return getNewGoldstandardFile();
            case JOptionPane.NO_OPTION:
                return getExistingGoldstandardFile();
            default:
                return null;
        }
    }

    File getExistingGoldstandardFile() throws MaeIOXMLException, MaeDBException {
        File existingGS = showFileChooseDialogAndSelect("goldstandard.xml", false);
        XMLLoader xmlLoader = new XMLLoader(getMainController().getDriver());
        if (existingGS != null && xmlLoader.isFileMatchesCurrentWork(existingGS)) {
            return existingGS;
        }
        return null;
    }

    File getNewGoldstandardFile() throws MaeIOException, MaeDBException {
        File newGS = showFileChooseDialogAndSelect("goldstandard.xml", true);
        if (newGS != null) {
            FileWriter.writeTextOnEmptyFile(getMainController().getDriver().getPrimaryText(), newGS);
            return newGS;
        }
        return null;
    }

    class IncompleteTagsWarningOptionPanel extends JDialog {
        JList<String> incompleteTags;
        int response;

        IncompleteTagsWarningOptionPanel(Set<Tag> incomplete, boolean simplyWarn) {
            super(getMainController().getMainWindow(), "Missing Something", true);
            setSize(100, 200);

            final JButton yes = makeYesButton();
            final JButton no = makeNoButton();
            final JButton see = makeSeeButton();
            see.setEnabled(false);


            String[] incompleteTagsDetail = getMissingDetails(incomplete);
            incompleteTags = new JList<>(incompleteTagsDetail);
            addListenersToList(see);
            JPanel buttons = new JPanel(new FlowLayout());
            buttons.add(yes);
            if (!simplyWarn) buttons.add(no);
            buttons.add(see);

            String tag = incomplete.size() == 1? "tag" : "tags";
            add(new JLabel(String.format(
                    "<html><p align=\"center\">You have %d underspecified %s! <br/> Continue?</p></html>", incomplete.size(), tag), SwingConstants.CENTER),
                    BorderLayout.NORTH);
            add(new JScrollPane(incompleteTags), BorderLayout.CENTER);
            add(buttons, BorderLayout.SOUTH);
            setLocationRelativeTo(getMainController().getMainWindow());
            pack();
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            this.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    super.windowClosing(e);
                    response = JOptionPane.NO_OPTION;

                }
            });
            KeyStroke stroke   = MaeHotKeys.ksESC;

            getRootPane().registerKeyboardAction(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    no();
                }
            }, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
            getRootPane().setDefaultButton(yes);

        }

        private void yes() {
            response = JOptionPane.YES_OPTION;
            setVisible(false);
        }

        private void no() {
            response = JOptionPane.NO_OPTION;
            setVisible(false);
        }
        private void see() {
            response = JOptionPane.CANCEL_OPTION;
            setVisible(false);
        }

        private void addListenersToList(final JButton see) {
            incompleteTags.addListSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent e) {
                    see.setEnabled(true);
                    see.requestFocusInWindow();
                }
            });
            incompleteTags.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    super.mouseClicked(e);
                    if (e.getClickCount() == 2) {
                        see();
                    }
                }
            });
        }

        private String[] getMissingDetails(Set<Tag> incomplete) {
            String[] incompleteTagsDetail = new String[incomplete.size()];
            int i = 0;
            for (Tag tag : incomplete) {
                incompleteTagsDetail[i++] = String.format("%s - missing: %s", tag.getId(), tag.getUnderspec());
            }
            return incompleteTagsDetail;
        }

        private JButton makeYesButton() {
            return makeButton("Yes", MaeHotKeys.mnYES_BUTTON, JOptionPane.YES_OPTION);
        }

        private JButton makeNoButton() {
            return makeButton("No", MaeHotKeys.mnNO_BUTTON, JOptionPane.NO_OPTION);
        }

        private JButton makeSeeButton() {
            return makeButton("See", MaeHotKeys.mnSEE_BUTTON, JOptionPane.CANCEL_OPTION);
        }

        private JButton makeButton(String label, int mnemonic, final int responsevalue) {
            final JButton button = new JButton(label);
            button.setMnemonic(mnemonic);
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    response = responsevalue;
                    setVisible(false);
                }
            });
            return button;
        }

        public Tag getSelectedTag() {
            String tid = incompleteTags.getSelectedValue().split(" - ")[0];
            return getMainController().getTagByTid(tid);
        }

        public int getResponse() {
            return response;
        }
    }


    class SetArgumentOptionPanel extends JPanel {

        final MaeDriverI driver = getMainController().getDriver();
        final JComboBox<TagType> linkTypes = new JComboBox<>();
        final JComboBox<ArgumentType> argTypes = new JComboBox<>();
        final JComboBox<LinkTag> linkTags = new JComboBox<>();

        SetArgumentOptionPanel() throws MaeDBException {
            super(new GridLayout(6, 1));

            argTypes.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    TagType type = (TagType) linkTypes.getSelectedItem();
                    if (argTypes.getSelectedItem() != null) {
                        String argTypeName = ((ArgumentType) argTypes.getSelectedItem()).getName();
                        try {
                            List<LinkTag> prioritized = getPrioritizedLinkTags(type, argTypeName);

                            linkTags.removeAllItems();
                            for (LinkTag link : prioritized) {
                                linkTags.addItem(link);
                            }

                        } catch (MaeDBException ex) {
                            getMainController().showError(ex);
                        }
                    }

                }

                List<LinkTag> getPrioritizedLinkTags(TagType type, String argTypeName) throws MaeDBException {
                    List<LinkTag> prioritized = new ArrayList<>();
                    for (LinkTag link : driver.getAllLinkTagsOfType(type)) {
                        if (link.getArgumentByTypeName(argTypeName) == null) {
                            prioritized.add(0, link);
                        } else {
                            prioritized.add(link);
                        }
                    }
                    return prioritized;
                }
            });

            linkTypes.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    TagType type = (TagType) linkTypes.getSelectedItem();

                    try {
                        argTypes.removeAllItems();
                        for (ArgumentType argType : driver.getArgumentTypesOfLinkTagType(type)) {
                            argTypes.addItem(argType);
                        }
                    } catch (MaeDBException ex) {
                        getMainController().showError(ex);
                    }

                }
            });

            for (TagType type : driver.getLinkTagTypes()) {
                linkTypes.addItem(type);
            }

            add(new JLabel(MaeStrings.SETARG_SEL_TAGTYPE));
            add(linkTypes);
            add(new JLabel(MaeStrings.SETARG_SEL_ARGTYPE));
            add(argTypes);
            add(new JLabel(MaeStrings.SETARG_SEL_TAG));
            add(linkTags);

        }

        LinkTag getSelectedLinkTag() {
            return (LinkTag) linkTags.getSelectedItem();

        }
        ArgumentType getSelectedArgumentType() {
            return (ArgumentType) argTypes.getSelectedItem();

        }

    }

    class CreateLinkOptionPanel extends JPanel {

        private Map<ArgumentType, ExtentTag> argumentsMap;
        // selected arguments should not be null or empty by now (checked in menu controller)
        final Border etched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);


        CreateLinkOptionPanel(TagType linkType, List<ExtentTag> argumentCandidates) throws MaeDBException {
            super();

            List<ArgumentType> argTypes = new ArrayList<>(linkType.getArgumentTypes());
            setLayout(new GridLayout(1, argTypes.size()));

            argumentsMap = new LinkedHashMap<>();

            int typeNum = 0;
            for (final ArgumentType type : argTypes) {
                final JComboBox<ExtentTag> candidates = new JComboBox<>();
                for (ExtentTag tag : argumentCandidates) {
                    candidates.addItem(tag);
                }
                candidates.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        argumentsMap.put(type, (ExtentTag) candidates.getSelectedItem());
                    }
                });
                candidates.setSelectedIndex(typeNum++ % argumentCandidates.size());
                JPanel comboPanel = new JPanel();
                comboPanel.add(candidates);
                TitledBorder titledBorder = BorderFactory.createTitledBorder(
                        etched, type.getName());
                titledBorder.setTitleJustification(TitledBorder.CENTER);
                comboPanel.setBorder(titledBorder);
                add(comboPanel);
            }

        }

        Map<ArgumentType, ExtentTag> getSelectedArguments() {
            return argumentsMap;

        }
    }


}
