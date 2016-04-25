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

import java.awt.event.*;
import java.util.List;
import javax.swing.*;
import java.awt.*;

import static edu.brandeis.cs.nlp.mae.agreement.MaeAgreementStrings.*;

/**
 * Created by krim on 4/24/2016.
 */
class AgreementTypeSelectPanel extends JPanel {
    private String tagTypeName;
    private JComboBox<String> scopeCombo;
    private JComboBox<String> metricTypeCombo;

    public AgreementTypeSelectPanel(String tagTypeName) {
        this.tagTypeName = tagTypeName;
        this.initUI();
    }

    private void initUI() {
        this.setLayout(new GridLayout(1, 2));

        JLabel tagTypeNameLabel = new JLabel(this.tagTypeName);
        tagTypeNameLabel.setHorizontalAlignment(JLabel.CENTER);
        add(tagTypeNameLabel);

        prepareScopeCombobox();
        prepareMetricsCombobox();
        scopeCombo.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                super.focusGained(e);
                Component component = e.getComponent();
                scrollToComponent(component);
            }
        });

        metricTypeCombo.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                super.focusGained(e);
                Component component = e.getComponent();
                scrollToComponent(component);
            }
        });

        add(scopeCombo);
        add(metricTypeCombo);
        setMaximumSize(new Dimension(450, 32));
        setPreferredSize(new Dimension(450, 28));
    }

    private void scrollToComponent(Component component) {
        this.scrollRectToVisible(component.getBounds());
    }

    private void prepareLabelingMetricsCombobox() {
        prepareMetricsCombobox(LABELING_METRIC_TYPES_STRINGS);
    }

    private void prepareSegmentationMetricsCombobox() {
        prepareMetricsCombobox(SEGMENTATION_METRIC_TYPES_STRINGS);
    }

    private void prepareMetricsCombobox() {
        if (metricTypeCombo == null) {
            metricTypeCombo = new JComboBox<>();
        }
        metricTypeCombo.removeAllItems();
        metricTypeCombo.setEnabled(false);
    }

    private void prepareMetricsCombobox(List<String> items) {
        if (metricTypeCombo == null) {
            metricTypeCombo = new JComboBox<>();
        }
        populateCombobox(metricTypeCombo, items);
        metricTypeCombo.setEnabled(true);
        metricTypeCombo.setSelectedIndex(0);
    }

    private static void populateCombobox(JComboBox<String> metricTypeCombo, List<String> items) {
        metricTypeCombo.removeAllItems();
        items.forEach(metricTypeCombo::addItem);
    }

    private void prepareScopeCombobox() {
        scopeCombo = new JComboBox<>();
        for (String SCOPE : SCOPE_TYPE_STRINGS) {
            scopeCombo.addItem(SCOPE);
        }
        scopeCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (((String) scopeCombo.getSelectedItem()).contains(SCOPE_LABELING_STRING)) {
                    prepareLabelingMetricsCombobox();
                    updateUI();
                } else if (((String) scopeCombo.getSelectedItem()).contains(SCOPE_UNITIZING_STRING)) {
                    prepareSegmentationMetricsCombobox();
                    updateUI();
                } else {
                    prepareMetricsCombobox();
                    updateUI();
                }
            }
        });
        scopeCombo.setSelectedIndex(0);
    }

    public String getTagTypeName() {
        return tagTypeName;
    }

    public String getSelectedScope() {
        return (String) scopeCombo.getSelectedItem();
    }

    public String getSelectedMetric() {
        return (String) metricTypeCombo.getSelectedItem();
    }

    public boolean isIgnored() {
        return getSelectedScope().equals(SCOPE_IGNORE_STRING);
    }

    public boolean isGlobalScope() {
        return !isIgnored() && (getSelectedScope().contains(SCOPE_CROSSTAG_STRING));
    }
}
