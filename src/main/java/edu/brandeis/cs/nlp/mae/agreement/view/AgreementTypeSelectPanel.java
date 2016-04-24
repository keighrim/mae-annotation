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

package edu.brandeis.cs.nlp.mae.agreement.view;

import javax.swing.*;
import java.awt.*;

import static edu.brandeis.cs.nlp.mae.agreement.MaeAgreementStrings.AGR_TYPES_STRINGS;

/**
 * Created by krim on 4/24/2016.
 */
class AgreementTypeSelectPanel extends JPanel {
    private String tagTypeName;
    private JComboBox<String> selTypeCombo;

    public AgreementTypeSelectPanel(String tagTypeName) {
        this.tagTypeName = tagTypeName;
        this.initUI();
    }

    private void initUI() {
        this.setLayout(new GridLayout(1, 2));

        JLabel tagTypeNameLabel = new JLabel(this.tagTypeName);
        tagTypeNameLabel.setHorizontalAlignment(JLabel.CENTER);
        add(tagTypeNameLabel);
        selTypeCombo = new JComboBox<>();
        for (String AGR_TYPES_STRING : AGR_TYPES_STRINGS) {
            selTypeCombo.addItem(AGR_TYPES_STRING);
        }
        selTypeCombo.setSelectedIndex(0);
        add(selTypeCombo);
        setMaximumSize(new Dimension(400, 32));
        setPreferredSize(new Dimension(400, 28));
    }

    public String getTagTypeName() {
        return tagTypeName;
    }

    public int getSelectedAgrType() {
        return selTypeCombo.getSelectedIndex();
    }
}
