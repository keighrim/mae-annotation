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
import edu.brandeis.cs.nlp.mae.model.ArgumentType;
import edu.brandeis.cs.nlp.mae.model.LinkTag;
import edu.brandeis.cs.nlp.mae.model.TagType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Called when the user selects the option to delete the highlighted rows from
 * the table in view.  Rows are removed both from the database and the table.
 */
public class SetArgument extends MenuActionI {

    public SetArgument(String text, ImageIcon icon, KeyStroke hotkey, Integer mnemonic, MaeMainController controller) {
        super(text, icon, hotkey, mnemonic, controller);
    }

    @Override
    public void actionPerformed(ActionEvent event) {

        try {
            String tid = event.getActionCommand();
            SetArgumentOptionPanel options = new SetArgumentOptionPanel();
            int result = JOptionPane.showConfirmDialog(getMainController().getRootPane(),
                    options,
                    String.format("Setting %s an argument of", tid),
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                LinkTag linker = options.getSelectedLinkTag();
                ArgumentType argType = options.getSelectedArgumentType();
                getMainController().surgicallyUpdateCell(linker, argType.getName() + MaeStrings.ARG_IDCOL_SUF, tid);
            }
        } catch (MaeDBException e) {
            catchException(e);
        }
    }

    class SetArgumentOptionPanel extends JPanel {

        final MaeDriverI driver = getMainController().getDriver();
        final JComboBox<TagType> linkTypes = new JComboBox<>();
        final JComboBox<ArgumentType> argTypes = new JComboBox<>();
        final JComboBox<LinkTag> linkTags = new JComboBox<>();

        public SetArgumentOptionPanel() throws MaeDBException {
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

            // TODO: 2016-01-22 23:45:07EST encapsulate these strings
            add(new JLabel("Select Link Type"));
            add(linkTypes);
            add(new JLabel("Select Argument Type"));
            add(argTypes);
            add(new JLabel("Select Link Tag"));
            add(linkTags);

        }

        public LinkTag getSelectedLinkTag() {
            return (LinkTag) linkTags.getSelectedItem();

        }
        public ArgumentType getSelectedArgumentType() {
            return (ArgumentType) argTypes.getSelectedItem();

        }

    }

}

