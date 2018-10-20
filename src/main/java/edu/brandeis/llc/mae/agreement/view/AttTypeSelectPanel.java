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

package edu.brandeis.llc.mae.agreement.view;

import edu.brandeis.llc.mae.util.MappedSet;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static edu.brandeis.llc.mae.agreement.MaeAgreementStrings.GUI_ATT_SELECT_GUIDE;

/**
 * Created by krim on 4/24/2016.
 */
class AttTypeSelectPanel extends JPanel {

    private MappedSet<String, String> tagsAndAtts;
    private Map<String, AttTypeSelectList> listMap;

    AttTypeSelectPanel(MappedSet<String, String> tagsAndAtts) {
        this.tagsAndAtts = tagsAndAtts;

        prepareAttLists();
        initUI();
    }

    private void prepareAttLists() {
        listMap = new LinkedHashMap<>();
        listMap.put("-", new AttTypeSelectList("-", new String[0]));

        for (String tagTypeName : tagsAndAtts.keySet()) {
            List<String> attTypeNamesList = tagsAndAtts.getAsList(tagTypeName);
            String[] attTypeNames = new String[attTypeNamesList.size()];
            attTypeNamesList.toArray(attTypeNames);
            listMap.put(tagTypeName, new AttTypeSelectList(tagTypeName, attTypeNames));
        }
    }

    private void initUI() {

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(new VerboseTextArea(GUI_ATT_SELECT_GUIDE));

        final JComboBox<String> tagTypeCombo = new JComboBox<>();
        final JPanel listPanel = new JPanel(new CardLayout());
        listPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createRaisedBevelBorder(),
                BorderFactory.createLoweredBevelBorder()));

        for (String tagTypeName : listMap.keySet()) {
            tagTypeCombo.addItem(tagTypeName);
            AttTypeSelectList attList = listMap.get(tagTypeName);

            JScrollPane listScroller = new JScrollPane(attList);
            listScroller.setBorder(BorderFactory.createEmptyBorder());
            listPanel.add(listScroller, tagTypeName);
        }


        tagTypeCombo.addItemListener(e ->
                ((CardLayout) listPanel.getLayout()).show(listPanel, (String) e.getItem()));
        tagTypeCombo.setSelectedIndex(0);
        ((JLabel) tagTypeCombo.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

        add(tagTypeCombo);
        add(listPanel);
        setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 12));


    }

    public List<String> getSelectedAttTypes(String tagTypeName) {
        return this.listMap.get(tagTypeName).getSelectedAttTypes();
    }
}
