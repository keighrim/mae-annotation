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

package edu.brandeis.llc.mae.io;

import edu.brandeis.llc.mae.MaeStrings;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Contains helper functions to create or modify files.
 */
public class MaeFileWriter {

    public static void writeTextToEmptyXML(File utf8file, String task, File xmlOutFile)
            throws MaeIOException {
        try {
            writeTextToEmptyXML(new BufferedReader(new InputStreamReader(
                    new FileInputStream(utf8file), StandardCharsets.UTF_8)), task, xmlOutFile);
        } catch (FileNotFoundException e) {
            throw new MaeIOException(e.getMessage());
        }
    }

    public static void writeTextToEmptyXML(String utf8Text, String task, File xmlOutFile)
            throws MaeIOException {
        writeTextToEmptyXML(new BufferedReader(new StringReader(utf8Text)), task, xmlOutFile);
    }

    public static void writeTextToEmptyXML(BufferedReader utf8BufReader, String task, File xmlOutFile)
            throws MaeIOException {

        try (PrintWriter outputWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(xmlOutFile), StandardCharsets.UTF_8)))) {
            if (!xmlOutFile.exists()) xmlOutFile.createNewFile();
            outputWriter.print(String.format(
                    MaeStrings.maeXMLHeader, task));

            int c;
            while ((c = utf8BufReader.read()) > -1) {
                outputWriter.print((char) c);
            }
            outputWriter.print(String.format(MaeStrings.maeXMLFooter, task));
            utf8BufReader.close();
        } catch (IOException e) {
            throw new MaeIOException("Cannot create a new file!", e);
        }
    }
}
