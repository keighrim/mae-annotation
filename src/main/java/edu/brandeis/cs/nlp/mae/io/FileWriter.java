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

import java.io.*;

/**
 * Created by krim on 2/18/2016.
 */
public class FileWriter {

    public static void writeTextToEmptyXML(String text, String task, File file) throws MaeIOException {
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            Writer output = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(
                            file), "UTF-8"));
            output.write(String.format(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n<%s>\n<TEXT><![CDATA[%s]]></TEXT>\n</%s>",
                            task, text, task));
            output.close();
        } catch (IOException e) {
            throw new MaeIOException("Cannot create a new file!", e);
        }
    }

}
