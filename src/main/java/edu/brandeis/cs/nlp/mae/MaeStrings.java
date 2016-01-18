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

package edu.brandeis.cs.nlp.mae;

import edu.brandeis.cs.nlp.mae.controller.MaeMainController;

import java.io.IOException;
import java.util.Calendar;
import java.util.Properties;

/**
 * Contains string resources for MAE main
 * Created by krim on 2/17/2015.
 * @author Keigh Rim
 *
 */
public class MaeStrings {
    
    /* External information */
    public final static String PROJECT_WEBPAGE = "https://github.com/keighrim/mae-annotation";
    public final static String CUR_YEAR = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
    public final static String VERSION = "v1.0-snapshot";
    public final static String TITLE_PREFIX = "MAE " + VERSION;
    public final static String DB_DRIVER = "jdbc:sqlite:";
    public final static String ANN_DB_FILE = "mae.db";
    public final static String ANN_DB_URL = DB_DRIVER + ANN_DB_FILE;
    public final static String ADJ_DB_FILE = "mai.db";
    public final static String ADJ_DB_URL = DB_DRIVER + ADJ_DB_FILE;
    public final static String TEST_DB_FILE = "test.db";
    public final static String TEST_DB_URL = DB_DRIVER + TEST_DB_FILE;

    /* Internal data structures and actionEvents */
    public final static String COMBO_DELIMITER = " - ";
    public final static String SPANDELIMITER = "~";
    public final static String SPANSEPARATOR = ",";
    public final static int NC_START = -1;
    public final static int NC_END = -1;
    public final static String NCSPAN_PLACEHOLDER = String.format("%d%s%d",
            MaeStrings.NC_START, MaeStrings.SPANDELIMITER, MaeStrings.NC_END);

    public final static String ATT_VALUESET_SEPARATOR = ":::";
    // TODO 151209 are these two below safe?
    public final static String SPANTEXTTRUNC = " ... ";
    public final static String LONGTEXTTRUNC = " … ";
    public final static String ADD_NC_COMMAND = "ADDNC:";
    public final static String ADD_LINK_COMMAND = "ADDLINK:";
    public final static String ADD_LINK_WITH_ARGS_COMMAND = "ADDLINKARGS:";
    public final static String ALL_TABLE_TAB_BACK_NAME = new Object().toString();
    public final static String ALL_TABLE_TAB_PREFIX = "@ALL@";
    public final static String ALL_TABLE_TAB_FRONT_NAME = "All Extents";
    public final static String SRC_COL_NAME = "@source";
    public final static String ID_COL_NAME = "id";
    public final static String SPANS_COL_NAME = "spans";
    public final static String TEXT_COL_NAME = "text";
    public final static String SEP = "@#";
    public final static String ARG_IDCOL_SUF = "ID";
    public final static String ARG_TEXTCOL_SUF = "Text";


    /* Status bar messages */
    public static final String WAIT_MESSAGE = "Processing...";
    public final static String SB_NODTD = "No DTD loaded.";
    public final static String SB_NOFILE = "No file loaded.";
    public final static String SB_TEXT = "Selected: ";
    public final static String SB_NOTEXT = "No Text Selected";
    public final static String SB_TAG= " %d Tags Selected: %s";
    public final static String SB_NOTAG = "No Tags Selected";
    public final static String SB_NORM_MODE_NOTI = "Now in normal mode! Click anywhere to continue.";
    public final static String SB_NEWTASK = "New task is successfully loaded! Click anywhere to continue.";
    public final static String SB_MSPAN_MODE_PREFIX = "[Multi-span] ";
    public final static String SB_MSPAN_TEXT = SB_MSPAN_MODE_PREFIX + SB_TEXT;
    public final static String SB_MSPAN_NOTEXT = SB_MSPAN_MODE_PREFIX + SB_NOTEXT;
    public final static String SB_MARGS_MODE_PREFIX = "[Arguments select] ";
    public final static String SB_MARGS_TAG = SB_MARGS_MODE_PREFIX + SB_TAG;
    public final static String SB_MARGS_NOTAG = SB_MARGS_MODE_PREFIX + SB_NOTAG;


    /* menus */
    public final static String MENU_FILE = "File";
    public final static String MENU_FILE_ITEM_LOADTASK = "New Task Definition";
    public final static String MENU_FILE_ITEM_OPENFILE = "Open Document";
    public final static String MENU_FILE_ITEM_SAVEXML = "Save Annotation As XML";
    public final static String MENU_FILE_ITEM_SAVERTF = "Export Annotation as RTF";
    public final static String MENU_FILE_ITEM_LOADGS = "Load Gold Standard File";
    public final static String MENU_FILE_ITEM_CLOSEFILE = "Close Annotation";
    public final static String MENU_MODE = "Mode";
    public final static String MENU_DISPLAY = "Display";
    public final static String MENU_HELP = "Help";
    public final static String MENU_NOTEXT = SB_NOTEXT;

    /* action commands */
    public final static String DELETE_TAG_SINGLE = "Remove ";
    public final static String DELETE_TAG_PLURAL = "Remove %s tags: %s";
    public final static String DELETE_TAG_SUBMENU = "";

    /* general messages */
    public final static String NO_TASK_IND = "No DTD";
    public final static String NO_FILE_IND = "No File";
    public final static String NO_TASK_GUIDE = "Start a new task by loading a DTD.";
    public final static String NO_FILE_GUIDE = "Start a new annotation by opening a file. \n\nFile can be a plain text file that contains the primary document, or a XML document with stand-alone annotations.";
    public static final String UNSAVED_SUFFIX = " *";

    /* popup messages */
    public static final String WARN_POPUP_TITLE = "Attention, Please";
    public static final String ERROR_POPUP_TITLE = "Aagh";

}
