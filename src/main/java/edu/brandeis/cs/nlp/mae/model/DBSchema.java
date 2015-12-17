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

package edu.brandeis.cs.nlp.mae.model;

/**
 * Created by krim on 12/12/2015.
 */
public class DBSchema {

    public final static String TAB_CI = "char_index";
    public final static String TAB_CI_COL_ID = "id";
    public final static String TAB_CI_COL_LOCATION = "location";
    public final static String TAB_CI_FCOL_ETAG = "tag_fid";

    public final static String TAB_TT = "tag_type";
    public final static String TAB_TT_COL_NAME = "name";
    public final static String TAB_TT_COL_COLOR = "color";
    public final static String TAB_TT_COL_PREFIX = "prefix";

    public final static String TAB_TAG_COL_TID = "tid";
    public final static String TAB_TAG_COL_FN = "filename";
    public final static String TAB_TAG_FCOL_TT = "tag_type_fid";

    public final static String TAB_ETAG = "extent_tag";
    public final static String TAB_ETAG_COL_TEXT = "text";

    public final static String TAB_LTAG = "link_tag";

    public final static String TAB_AT = "att_type";
    public final static String TAB_AT_COL_ID = "id";
    public final static String TAB_AT_FCOL_TT = "tag_type_fid";
    public final static String TAB_AT_COL_NAME = "name";
    public final static String TAB_AT_COL_VALUESET = "value_set";
    public final static String TAB_AT_COL_DEFVALUE = "def_value";
    public final static String TAB_AT_COL_IDREF = "idref";
    public final static String TAB_AT_COL_REQ = "required";

    public final static String TAB_ATT = "att";
    public final static String TAB_ATT_COL_ID = "id";
    public final static String TAB_ATT_FCOL_AT = "att_type_fid";
    public final static String TAB_ATT_COL_TID = "tid";
    public final static String TAB_ATT_FCOL_ETAG = "extent_tag_fid";
    public final static String TAB_ATT_FCOL_LTAG = "link_tag_fid";
    public final static String TAB_ATT_COL_VALUE = "value";

    public final static String TAB_ARG = "arg";
    public final static String TAB_ARG_COL_ID = "id";
    public final static String TAB_ARG_FCOL_LTAG = "link_tag_fid";
    public final static String TAB_ARG_FCOL_ETAG = "extent_tag_fid";
    public final static String TAB_ARG_FCOL_ART = "arg_type_fid";

    public final static String TAB_ART = "arg_type";
    public final static String TAB_ART_COL_ID = "id";
    public final static String TAB_ART_FCOL_TT = "tag_type_fid";
    public final static String TAB_ART_COL_NAME = "name";
    public final static String TAB_ART_COL_REQ = "required";
}

