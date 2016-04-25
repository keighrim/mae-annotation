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
import java.util.List;

/**
 * Created by krim on 4/23/2016.
 */
public class MaeAgreementStrings {

    public final static String SPAN_ATT = "-";
    public final static String UNMARKED_CAT = "#UNMARKED#";
    public final static String TAG_ATT_DELIM = "::";
    public final static String GLOBAL_ALPHAU_CALC_STRING = "Cross-tag segmentation";
    public final static String LOCAL_ALPHAU_CALC_STRING = "Tag-level segmentation";
    public final static String GLOBAL_ALPHA_CALC_STRING = "Cross-tag labeling";
    public final static String LOCAL_ALPHA_CALC_STRING = "Tag-level labeling";
    public final static String IGNORE_CACL_CALC_STRING = "Ignore this tag";

    public final static String GUI_ATT_SELECT_GUIDE = "Select tag type first and select att type to calculate attribute types in the list. Use ctrl/cmd and/or shift keys to select multiple items.";

    public final static ArrayList<String> AGR_TYPES_STRINGS = new ArrayList<String>() {{
            add(GLOBAL_ALPHAU_CALC_STRING);
            add(LOCAL_ALPHAU_CALC_STRING);
            add(GLOBAL_ALPHA_CALC_STRING);
            add(LOCAL_ALPHA_CALC_STRING);
            add(IGNORE_CACL_CALC_STRING);
    }};
    public final static int GLOBAL_ALPHAU_CALC_IDX = 0;
    public final static int LOCAL_ALPHAU_CALC_IDX = 1;
    public final static int GLOBAL_ALPHA_CALC_IDX = 2;
    public final static int LOCAL_ALPHA_CALC_IDX = 3;


}
