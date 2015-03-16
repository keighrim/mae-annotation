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

package mae;

import java.util.Calendar;

/**
 * Contains string resources for MAE main
 * Created by krim on 2/17/2015.
 * @author Keigh Rim
 *
 */
public class MaeStrings {
    
    /*
    External information
     */
    final static String PROJECT_WEBPAGE
            = "https://github.com/keighrim/mae-annotation";
    final static String CUR_YEAR
            = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
    final static String VERSION = "0.13.1.0";
    final static String TITLE_PREFIX = "MAE " + VERSION;

    /*
    Internal data structures and actionEvents
     */
    final static String COMBO_DELIMITER = " - ";
    final static String SPANDELIMITER = "~";
    final static String SPANSEPARATOR = ",";
    final static String SPANTEXTTRUNC = " ... ";
    final static String LONGTEXTTRUNC = " … ";
    final static String ADD_NC_COMMAND = "ADDNC:";
    final static String ADD_LINK_COMMAND = "ADDLINK:";
    final static String ADD_LINK_WITH_ARGS_COMMAND = "ADDLINKARGS:";
    final static String ALL_TABLE_TAB_BACK_NAME = new Object().toString();
    final static String ALL_TABLE_TAB_FRONT_NAME = "All Extents";
    final static String SRC_COL_NAME = "source";
    final static String ID_COL_NAME = "id";
    final static String SPANS_COL_NAME = "spans";
    final static String TEXT_COL_NAME = "text";
    final static String SEP = "@#";
    final static String ID_SUF = "ID";


    /*
    Status bar messages
     */
    final static String SB_NODTD = "No DTD loaded.";
    final static String SB_NOFILE = "No file loaded.";
    final static String SB_TEXT = "Selected: ";
    final static String SB_NOTEXT = "No Text Selected";
    final static String SB_TAG= " %d Tags Selected: %s";
    final static String SB_NOTAG = "No Tags Selected";
    final static String SB_NORM_MODE 
            = "Now in normal mode! Click anywhere to continue.";
    final static String SB_MSPAN_MODE = "[Multi-span] ";
    final static String SB_MSPAN_TEXT = SB_MSPAN_MODE + SB_TEXT;
    final static String SB_MSPAN_NOTEXT = SB_MSPAN_MODE + SB_NOTEXT;
    final static String SB_MARGS_MODE = "[Arguments select] ";
    final static String SB_MARGS_TAG = SB_MARGS_MODE + SB_TAG;
    final static String SB_MARGS_NOTAG = SB_MARGS_MODE + SB_NOTAG;

    
    /*
    menu items 
    */
    final static String MENU_NOTEXT = SB_NOTEXT;
    
    
    /*
    general messages
     */
    final static String NO_TASK_IND = "No DTD";
    final static String NO_TASK_GUIDE = "Start a new task by opening a DTD";
}
