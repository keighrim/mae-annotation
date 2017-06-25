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

package edu.brandeis.cs.nlp.mae.agreement;

import java.util.ArrayList;

/**
 * Created by krim on 4/23/2016.
 */
public class MaeAgreementStrings {

    public final static String SPAN_ATT = "-";
    public final static String UNMARKED_CAT = "#UNMARKED#";
    public final static String TAG_ATT_DELIM = "::";
    public final static String GUI_ATT_SELECT_GUIDE = "Choose a tag using dropdown and then select attributes.\nUse ctrl/cmd or shift keys to select items.";

    public final static String ANNOTATOR_CONFIG_PANEL_TITLE = "Select Annotators";
    public final static String SCOPE_CONFIG_PANEL_TITLE = "Select Tags and Matrices";
    public final static String ATTS_CONFIG_PANEL_TITLE = "Select Attributes";

    public final static String SCOPE_CROSSTAG_STRING = "Cross-tag";
    public final static String SCOPE_LOCAL_STRING = "Tag-level";
    public final static String SCOPE_UNITIZING_STRING = "segmentation";
    public final static String SCOPE_LABELING_STRING = "labeling";
    public final static String SCOPE_CROSSTAG_UNITIZING_STRING = SCOPE_CROSSTAG_STRING + " " + SCOPE_UNITIZING_STRING;
    public final static String SCOPE_LOCAL_UNITIZING_STRING = SCOPE_LOCAL_STRING + " " + SCOPE_UNITIZING_STRING;
    public final static String SCOPE_CROSSTAG_LABELING_STRING = SCOPE_CROSSTAG_STRING + " " + SCOPE_LABELING_STRING;
    public final static String SCOPE_LOCAL_LABELING_STRING = SCOPE_LOCAL_STRING + " " + SCOPE_LABELING_STRING;
    public final static String SCOPE_IGNORE_STRING = "Ignore this tag";

    public final static String ALPHAU_CALC_STRING = "Alpha-U (Krippendorf's)";
    public final static String MULTIPI_CALC_STRING = "Multi-Pi (Fleiss' Kappa)";
    public final static String MULTIKAPPA_CALC_STRING = "Multi-Kappa (Huberts' Kappa)";
    public final static String ALPHA_CALC_STRING = "Alpha (Krippendorf's)";

    public final static ArrayList<String> SCOPE_TYPE_STRINGS = new ArrayList<String>() { {
        add(SCOPE_IGNORE_STRING);
        add(SCOPE_CROSSTAG_LABELING_STRING);
        add(SCOPE_LOCAL_LABELING_STRING);
        add(SCOPE_CROSSTAG_UNITIZING_STRING);
        add(SCOPE_LOCAL_UNITIZING_STRING);
    }};


    public final static ArrayList<String> LABELING_METRIC_TYPES_STRINGS = new ArrayList<String>() {{
        add(MULTIPI_CALC_STRING);
        add(MULTIKAPPA_CALC_STRING);
        add(ALPHA_CALC_STRING);
    }};

    public final static ArrayList<String> SEGMENTATION_METRIC_TYPES_STRINGS = new ArrayList<String>() {{
        add(ALPHAU_CALC_STRING);
    }};

    public final static ArrayList<String> ALL_METRIC_TYPE_STRINGS = new ArrayList<String>() {{
        addAll(LABELING_METRIC_TYPES_STRINGS);
        addAll(SEGMENTATION_METRIC_TYPES_STRINGS);
    }};
}
