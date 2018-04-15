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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

/**
 * Created by krim on 3/10/17.
 */
public class MaeFileWriterTest {

    private String TEST_TEMP_FILE;
    final private String TEST_TASK_NAME = "TEST-TASK";

    String header = String.format(MaeStrings.maeXMLHeader, TEST_TASK_NAME);
    String footer = String.format(MaeStrings.maeXMLFooter, TEST_TASK_NAME);
    int headerLen = header.length();
    int footerLen = footer.length();
    int baseLines = numNewLines(header) + numNewLines(footer);

    private int numNewLines(String s) {
        int count = 0;
        for (char c : s.toCharArray()) {
            if (c == '\n') {
                count++;
            }
        }
        return count;
    }

    private class FileObj {
        public String text = "";
        public int lines;
        public int chars;

        public void addChar(int c) {
            text += (char) c;
            chars++;
            if (c == (int) '\n') {
                lines++;
            }
        }
    }

    private FileObj readFile(String fileName) {

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(fileName), StandardCharsets.UTF_8))) {
            FileObj o = new FileObj();
            int c;
            while ((c = reader.read()) > -1) {
                o.addChar(c);
            }
            return o;
        } catch (IOException ignored) {
            // write test carefully so that the subject file is always available!
        }
        return null;
    }

    private void writeToFile(String text) throws Exception {
        File testOutput = new File(TEST_TEMP_FILE);
        if (!testOutput.exists()) {
            testOutput.createNewFile();
        }
        MaeFileWriter.writeTextToEmptyXML(new BufferedReader(new StringReader(text)),
                TEST_TASK_NAME, testOutput);
    }

    @Before
    public void setUp() throws Exception {
        File tempFile = File.createTempFile("mae-test-out-", ".xml");
        TEST_TEMP_FILE = tempFile.getAbsolutePath();
        System.out.println(TEST_TEMP_FILE + " is created.");

    }

    @After
    public void tearDown() throws Exception {
        new File(TEST_TEMP_FILE).delete();
        System.out.println(TEST_TEMP_FILE + " is deleted.");
    }

    @Test
    public void canWriteTextToEmptyXML() throws Exception {
        String test = "test";
        writeToFile(test);
        FileObj result = readFile(TEST_TEMP_FILE);
        System.out.println(result.text);
        assertEquals(test.length(), result.chars - headerLen - footerLen);
        assertEquals(Integer.toString(result.lines - baseLines), numNewLines(test), result.lines - baseLines);
    }

    @Test
    public void canWriteUnicodeTextToEmptyXML() throws Exception {
        String unicodeString = "\uD83C\uDFA4\n" +
                       "\uD83C\uDFB6\n" +
                       "\uD83C\uDF5F\n" +
                       "\uD83C\uDF55\n";
        writeToFile(unicodeString);
        FileObj result = readFile(TEST_TEMP_FILE);
        System.out.println(result.text);
        assertEquals(12, result.chars - headerLen - footerLen);
        assertEquals(Integer.toString(result.lines - baseLines), 4, result.lines - baseLines);
    }
}