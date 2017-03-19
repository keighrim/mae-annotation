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

package edu.brandeis.cs.nlp.mae.controller.tablepanel;

import edu.brandeis.cs.nlp.mae.model.TagType;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.*;

/**
 * Created by krim on 1/11/2017.
 */
class AdjudicationCellRenderer extends AnnotationCellRenderer {

    private TablePanelController tablePanelController;
    AdjudicationTableModelI dataModel;
    TagType associatedType;
    final int indicatorThickness = 1;
    final Border absMinorityIndicator = BorderFactory.createLineBorder(Color.RED, indicatorThickness);
    final Border minorityIndicator = BorderFactory.createLineBorder(Color.ORANGE, indicatorThickness);
    final Border majorityIndicator = BorderFactory.createLineBorder(Color.YELLOW, indicatorThickness);
    final Border absMajorityIndicator = BorderFactory.createLineBorder(Color.GREEN, indicatorThickness);
    // this needs to match to what's returned from getValueDistribution() in terms of order
    final Border[] borders = new Border[]{absMinorityIndicator, minorityIndicator, majorityIndicator, absMajorityIndicator};

    private Color nonGoldRowBackground = Color.LIGHT_GRAY;
    private Color adjudicationSelectionForeground = Color.BLUE;

    public AdjudicationCellRenderer(TablePanelController tablePanelController, TagTableModel model) {
        this.tablePanelController = tablePanelController;
        associatedType = model.getAssociatedTagType();
        dataModel = (AdjudicationTableModelI) model;
    }

    @Override
    Color getCellForeground(boolean isSelected) {
        if (isSelected) {
            return adjudicationSelectionForeground;
        } else {
            return UIManager.getColor("Table.foreground");
        }

    }


    @Override
    public JTextComponent getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {

        JTextComponent renderer = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

        renderer.setBackground(dataModel.isGoldTagRow(table.convertRowIndexToModel(row)) ? getBackground() : nonGoldRowBackground);
        int lastMinimalColumn = associatedType.isExtent() ? TablePanelController.TEXT_COL : Collections.max(((TagTableModel) dataModel).getTextColumns());

        if (!dataModel.isGoldTagRow(table.convertRowIndexToModel(row))) {
            if (col == TablePanelController.SRC_COL) {
                renderer.setForeground(tablePanelController.getMainController().getDocumentColor((String) value));
                renderer.setBorder(null);
            } else if (((TagTableModel) dataModel).isTextColumn(col) || col > lastMinimalColumn) {
                java.util.List[] distribution = getValueDistribution(dataModel, col);
                if (distribution != null) {
                    renderer.setBorder(getAgreementIndicator(distribution, (String) value));
                }
            } else {
                renderer.setBorder(null);
            }
        } else {
            renderer.setBackground(UIManager.getColor("Table.background")); // do not change background on selection
        }

        return renderer;
    }

    private Border getAgreementIndicator(java.util.List[] distribution, String value) {
        for (int i = 0; i < distribution.length; i++) {
            if (distribution[i].contains(value)) return borders[i];
        }
        return null;
    }

    public java.util.List[] getValueDistribution(AdjudicationTableModelI model, int col) {
        // will return array of lists of values
        // [0] -> the absolute majority (over 0.5) (green)
        // [1] -> majority (yellow)
        // [2] -> minority (not the least, not major) (orange)
        // [3] -> the absolute minority (red)
        // if values are equally distributed, all are considered as minor
        // if values are binomialy distributed, the more gets the abs majority, the less gets the abs minority
        // if values are diverged over three or more values
        //   only when the most frequent one is occurred more than 50%, it gets the abs majority, otherwise gets simple majority
        //   the least frequent one and only one always gets the abs minority
        //   the rest gets minorities
        java.util.List<String> values = new ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            if (!model.isGoldTagRow(i)) {
                values.add((String) model.getValueAt(i, col));
            }
        }
        if (values.size() <= 1) { // single or no line
            tablePanelController.logger.debug("att value agreement: single or no tag selected.");
            return null;
        }
        java.util.List<String> absMinor = new ArrayList<>();
        java.util.List<String> minor = new ArrayList<>();
        java.util.List<String> major = new ArrayList<>();
        java.util.List<String> absMajor = new ArrayList<>();
        java.util.List[] distribution = new java.util.List[]{absMinor, minor, major, absMajor};

        Map<String, Integer> valueCounts = new HashMap<>();

        for (String value : values) {
            Integer count = valueCounts.get(value);
            if (count == null) count = 0;
            count++;
            valueCounts.put(value, count);
        }

        Set<String> valueSet = valueCounts.keySet();
        Set<Integer> counts = new HashSet<>(valueCounts.values());
        if (valueSet.size() == 1 && counts.size() == 1) { // unanimity
            tablePanelController.logger.debug("att value agreement: unanimity to " + valueSet.iterator().next());
            absMajor.addAll(valueCounts.keySet());
            return distribution;
        } else if (valueSet.size() > 1 && counts.size() == 1) { // uniform dist
            tablePanelController.logger.debug("att value agreement: uniform distribution of " + valueSet);
            minor.addAll(valueCounts.keySet());
            return distribution;
        } else {
            Map<Integer, java.util.List<String>> countsToValues = new HashMap<>();
            for (Map.Entry<String, Integer> entry : valueCounts.entrySet()) {
                java.util.List<String> valuesOccued = countsToValues.get(entry.getValue());
                if (valuesOccued == null) valuesOccued = new ArrayList<>();
                valuesOccued.add(entry.getKey());
                countsToValues.put(entry.getValue(), valuesOccued);
            }
            java.util.List<Integer> frequencyDist = new ArrayList<>(countsToValues.keySet());
            Collections.sort(frequencyDist);  // ascending sort

            if (valueSet.size() == 2 && counts.size() == 2) { // binomial dist
                tablePanelController.logger.debug("att value agreement: binomial distribution of " + valueSet);
                absMajor.addAll(countsToValues.get(frequencyDist.get(1)));
                absMinor.addAll(countsToValues.get(frequencyDist.get(0)));
                return distribution;
            }

            int majorExist = frequencyDist.size() - 1;
            int theMostOccurenece = frequencyDist.get(majorExist);
            java.util.List<String> theMost = countsToValues.get(theMostOccurenece);
            if (theMost.size() == 1) {
                tablePanelController.logger.debug("att value agreement: diverging, and found majority: " + theMost.get(0));
                if (theMostOccurenece > (values.size() / 2)) {
                    absMajor.addAll(theMost);
                    tablePanelController.logger.debug("att value agreement: it was the absolute majority: " + theMost.get(0));
                } else {
                    major.addAll(theMost);
                    tablePanelController.logger.debug("att value agreement: it did not absolutely majored: " + theMost.get(0));
                }
                majorExist--;
            }

            int minorExist = 0;
            int theLeastOccurenece = frequencyDist.get(minorExist);
            java.util.List<String> theLeast = countsToValues.get(theLeastOccurenece);
            if (theLeast.size() == 1) {
                absMinor.addAll(theLeast);
                tablePanelController.logger.debug("att value agreement: diverging, and found the absolute minority: " + theLeast.get(0));
                minorExist++;
            }

            for (int i = majorExist; i <= minorExist; i++) {
                minor.addAll(countsToValues.get(frequencyDist.get(i)));
            }
            return distribution;
        }

    }
}
