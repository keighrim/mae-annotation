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

package edu.brandeis.cs.nlp.mae.database;

import com.j256.ormlite.field.DatabaseField;
import edu.brandeis.cs.nlp.mae.MaeStrings;
import edu.brandeis.cs.nlp.mae.model.TagType;
import org.junit.Before;
import org.junit.Test;
import org.omg.CORBA.IDLTypeHelper;

import java.sql.SQLException;

import static org.junit.Assert.*;

/**
 * Created by krim on 12/16/15.
 */
public class IdHandlerTest {

    private static DatabaseDriver driver;
    private IdHandler handler;

    @Before
    public void setUp() throws Exception {
        driver = new DatabaseDriver(MaeStrings.TEST_DB_URL);
        handler = new IdHandler();

    }

    @Test
    public void canGenerateProperNextId() throws SQLException {
        TagType noun = driver.createTagType("NOUN", "N");
        handler.addId(noun, 0);
        String nextId = handler.getNextID(noun);
        assertEquals(
                "Expected N1 to be generated with only 0 in tracker, found: " + nextId,
                "N1", nextId
        );

        handler.addId(noun, "N04");
        nextId = handler.getNextID(noun);
        assertEquals(
                "Expected N1 to be generated with 0 & 4 in tracker, found: " + nextId,
                "N1", nextId
        );
    }

}