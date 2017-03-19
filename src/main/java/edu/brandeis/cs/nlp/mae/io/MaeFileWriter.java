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

package edu.brandeis.cs.nlp.mae.io;

import edu.brandeis.cs.nlp.mae.MaeStrings;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Contains helper functions to create or modify files.
 */
public class MaeFileWriter {

    public static void writeTextToEmptyXML(String utf8Text, String task, File file) throws MaeIOException {
        writeTextToEmptyXML(new BufferedReader(new StringReader(utf8Text)), task, file);
    }

    public static void writeTextToEmptyXML(BufferedReader utf8StreamReader, String task, File file) throws MaeIOException {

        try (BufferedWriter output = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))){
            if (!file.exists()) file.createNewFile();
            output.write(String.format(
                    MaeStrings.maeXMLHeader, task));
            String line = utf8StreamReader.readLine();
            if (line == null) {
                return;
            }
            output.write(line);
            while ((line = utf8StreamReader.readLine()) != null) {
                output.newLine();
                output.write(line);
            }
            output.write(String.format(MaeStrings.maeXMLFooter, task));
            utf8StreamReader.close();
        } catch (IOException e) {
            throw new MaeIOException("Cannot create a new file!", e);
        }
    }

}
