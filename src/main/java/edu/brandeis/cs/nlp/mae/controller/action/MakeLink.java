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

package edu.brandeis.cs.nlp.mae.controller.action;

import edu.brandeis.cs.nlp.mae.MaeStrings;
import edu.brandeis.cs.nlp.mae.controller.MaeMainController;
import edu.brandeis.cs.nlp.mae.database.MaeDBException;
import edu.brandeis.cs.nlp.mae.database.MaeDriverI;
import edu.brandeis.cs.nlp.mae.model.*;


import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Map;

/**
 * Called when the user selects the option to delete the highlighted rows from
 * the table in view.  Rows are removed both from the database and the table.
 */
public class MakeLink extends MenuActionI {

    public MakeLink(String text, ImageIcon icon, KeyStroke hotkey, Integer mnemonic, MaeMainController controller) {
        super(text, icon, hotkey, mnemonic, controller);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        try {
            TagType linkType = getMainController().getDriver().getTagTypeByName(event.getActionCommand());
            CreateLinkOptionPanel options = new CreateLinkOptionPanel(linkType);
            int result = JOptionPane.showConfirmDialog(getMainController().getRootPane(),
                    options,
                    String.format("Create a new link: %s ", event.getActionCommand()),
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                LinkTag linker = (LinkTag) getMainController().createTagFromTextContextMenu(linkType);
                Map<ArgumentType, ExtentTag> arguments = options.getSelectedArguments();
                for (ArgumentType argType : arguments.keySet()) {
                    String argTypeName = argType.getName();
                    String argTid = arguments.get(argType).getTid();
                    getMainController().surgicallyUpdateCell(linker, argTypeName + MaeStrings.ARG_IDCOL_SUF, argTid);
                }

            }
        } catch (MaeDBException e) {
            catchException(e);
        }

    }

    class CreateLinkOptionPanel extends JPanel {

        private Map<ArgumentType, ExtentTag> argumentsMap;
        // selected arguments should not be null or empty by now (checked in menu controller)
        final List<ExtentTag> selectedTags = getMainController().getSelectedArguments();
        final Border etched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);


        public CreateLinkOptionPanel(TagType linkType) throws MaeDBException {
            super();

            List<ArgumentType> argTypes = new ArrayList<>(linkType.getArgumentTypes());
            setLayout(new GridLayout(1, argTypes.size()));

            argumentsMap = new LinkedHashMap<>();

            int typeNum = 0;
            for (final ArgumentType type : argTypes) {
                final JComboBox<ExtentTag> candidates = new JComboBox<>();
                for (ExtentTag tag : selectedTags) {
                    candidates.addItem(tag);
                }
                candidates.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        argumentsMap.put(type, (ExtentTag) candidates.getSelectedItem());
                    }
                });
                candidates.setSelectedIndex(typeNum++ % selectedTags.size());
                JPanel comboPanel = new JPanel();
                comboPanel.add(candidates);
                TitledBorder titledBorder = BorderFactory.createTitledBorder(
                        etched, type.getName());
                titledBorder.setTitleJustification(TitledBorder.CENTER);
                comboPanel.setBorder(titledBorder);
                add(comboPanel);
            }

        }
        public Map<ArgumentType, ExtentTag> getSelectedArguments() {
            return argumentsMap;

        }
    }

}

