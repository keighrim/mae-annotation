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
import edu.brandeis.cs.nlp.mae.model.ArgumentType;
import edu.brandeis.cs.nlp.mae.model.ExtentTag;
import edu.brandeis.cs.nlp.mae.model.LinkTag;
import edu.brandeis.cs.nlp.mae.model.TagType;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * All popups and supplement sub windows are managed within this class
 *
 * Created by krim on 1/1/2016.
 */
class DialogController {
    JFrame parent;
    MaeMainController mainController;
    JFrame dialog;
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

    void showError(String errorMessage) {
        // TODO: 1/1/2016 maybe can implement "send error log to dev" button
        JOptionPane.showMessageDialog(getParent(), errorMessage, MaeStrings.ERROR_POPUP_TITLE, JOptionPane.WARNING_MESSAGE);

    }

    File showFileChooseDialogAndSelect(String defaultName, boolean saveFile) {
        if (defaultName.length() > 0) {
            fileChooser.setSelectedFile(new File(defaultName));
        }
        // TODO: 1/1/2016 implement multi selection for multi file support

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
        int result = JOptionPane.showConfirmDialog(getMainController().getRootPane(),
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
        int result = JOptionPane.showConfirmDialog(getMainController().getRootPane(),
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
