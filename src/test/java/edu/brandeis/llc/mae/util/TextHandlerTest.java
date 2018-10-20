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

package edu.brandeis.llc.mae.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author krim
 * @since 3/20/2018
 */
public class TextHandlerTest {

    @Test
    public void canTruncateLongTexts() {
        String longtext = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. ";
        String longwords = "Loremipsumdolorsitamet, consecteturadipiscingelit. ";
        String longword = "Loremipsumdolorsitamet,consecteturadipiscingelit. ";
        assertEquals(TextHandler.truncateLongText(longtext), "Lorem ... elit.");
        assertEquals(TextHandler.truncateLongText(longwords), "Lorem.. ... ..elit.");
        assertEquals(TextHandler.truncateLongText(longword), "Lorem....elit.");
    }
}