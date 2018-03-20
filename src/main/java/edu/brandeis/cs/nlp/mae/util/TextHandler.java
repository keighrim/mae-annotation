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

/**
 * @author krim
 * @since 3/20/2018
 */
public class TextHandler {

    public static final int longWordLen = 10;
    public static final int longTextLen = 20;

    public static String truncateLongWord(String word, boolean fromRight) {
        if (word.length() > longWordLen) {
            if (fromRight) {
                return word.substring(0, longWordLen / 2) + "..";
            } else {
                return ".." + word.substring(word.length() - longWordLen / 2);
            }
        }
        return word;
    }

    public static String truncateLongText(String text) {
        text = text.trim();
        if (text.length() > longTextLen) {
            if (text.contains(" ")) {
                String firstWord = truncateLongWord(text.substring(0, text.indexOf(" ")), true);
                String lastWord = truncateLongWord(text.substring(text.lastIndexOf(" ") + 1), false);
                return String.format("%s ... %s", firstWord, lastWord);
            } else {
                return truncateLongWord(text, true) + truncateLongWord(text, false);
            }

        }
        return text;
    }

}
