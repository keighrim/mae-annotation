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

package edu.brandeis.cs.nlp.mae.util;

import edu.brandeis.cs.nlp.mae.io.MaeIOException;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by krim on 4/12/16.
 */
public class FileHandler {

    public static String ANNOTATOR_SUFFIX_DELIM = "_";
    public static String XML_EXT = ".xml";
    public static FileFilter XML_FILTER  = new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        return pathname.getName().toLowerCase().endsWith(".xml");
                    } };

    public static String getFileBaseName(File file) {
        return getFileBaseName(file.getAbsolutePath());
    }

    public static String getFileBaseName(String fileFullName) {
        String[] pathTokens = fileFullName.split("[\\\\|/]");
        return pathTokens[pathTokens.length - 1];
    }

    public static List<File> getAllFileIn(File directory) throws MaeIOException {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles(XML_FILTER);
            if (files.length > 0) {
                return new LinkedList<>(Arrays.asList(files));
            } else {
                throw new MaeIOException("No XML found in the directory: " + directory.getName());
            }
        } else {
            throw new MaeIOException("Not a directory: " + directory.getName());
        }
    }


    private static String[] splitSuffix(String string, String delimiter) {
        int splitPoint = string.lastIndexOf(delimiter);
        String rest = string.substring(0, splitPoint);
        String suffix = string.substring(splitPoint + delimiter.length(), string.length());
        return new String[]{rest, suffix};
    }

    public static String getFileNameWithoutExtension(String fileBaseName) {
        return splitSuffix(fileBaseName, ".")[0];

    }

    public static String[] splitAnnotationAnnotator(String fileNameWOExt) {
        return splitSuffix(fileNameWOExt, ANNOTATOR_SUFFIX_DELIM);

    }

    public static boolean containsBaseName(String baseName, List<File> files) {
        for (File f : files) {
            if (getFileBaseName(f).equals(baseName)) {
                return true;
            }
        }
        return false;
    }


}
