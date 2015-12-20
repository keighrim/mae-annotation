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

import edu.brandeis.cs.nlp.mae.MaeStrings;
import edu.brandeis.cs.nlp.mae.model.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by krim on 12/13/2015.
 */
public class DatabaseDriverTest {

    private DatabaseDriver driver;

    TagType noun;
    TagType verb;
    TagType semanticRole;
    ArgumentType pred;
    ArgumentType agent;


    @Before
    public void setUp() throws Exception {
        driver = new DatabaseDriver(MaeStrings.TEST_DB_URL);

        noun = driver.createTagType("NOUN", "N");
        verb = driver.createTagType("VERB", "V");
        semanticRole = driver.createTagType("SR", "S");

        pred = driver.createArgumentType(semanticRole, "predicate");
        agent = driver.createArgumentType(semanticRole, "agent");

    }

    @After
    public void tearDown() throws Exception {
        driver.destroy();

    }

    @Test
    public void canRetrieveExtentTagsByType() throws SQLException {
        driver.createExtentTag("N01", noun, "jenny", new int[]{5,6,7,8,9});
        driver.createExtentTag("V01", verb, "loves", new int[]{11, 12, 13, 14, 15});

        List<ExtentTag> retrievedTags = (List<ExtentTag>) driver.getAllTagsOfType(noun);
        assertEquals(
                "Expected 1 extent tag is retrieved by generic query, found: " + retrievedTags.size(),
                1, retrievedTags.size());

        retrievedTags = driver.getAllExtentTagsOfType(noun);
        assertEquals(
                "Expected 1 extent tag is retrieved by only extent query, found: " + retrievedTags.size(),
                1, retrievedTags.size());

    }

    @Test
    public void canRetrieveLinkTagsByType() throws SQLException {
        ExtentTag nTag = driver.createExtentTag("N01", noun, "jenny", new int[]{5,6,7,8,9});
        ExtentTag vTag = driver.createExtentTag("V01", verb, "loves", new int[]{11, 12, 13, 14, 15});

        LinkTag link = driver.createLinkTag("A01", semanticRole);
        driver.addArgument(link, agent, nTag);
        driver.addArgument(link, pred, vTag);


        List<LinkTag> retrievedTags = (List<LinkTag>) driver.getAllTagsOfType(semanticRole);
        assertEquals(
                "Expected 1 link tag is retrieved by generic query, found: " + retrievedTags.size(),
                1, retrievedTags.size());

        retrievedTags = driver.getAllLinkTagsOfType(semanticRole);
        assertEquals(
                "Expected 1 link tag is retrieved by link-only query, found: " + retrievedTags.size(),
                1, retrievedTags.size());

        LinkTag retrievedTag = retrievedTags.get(0);
        assertEquals(
                "Expected 2 arguments associated with the link, found: " + retrievedTag.getArguments().size(),
                2, retrievedTag.getArguments().size());

        Map<String, String> arguments = retrievedTag.getArgumentTidsWithNames();
        assertTrue(
                "Expected argument names and values properly mapped, found " + arguments.toString(),
                arguments.get("agent").equals("N01") && arguments.get("predicate").equals("V01")
        );

    }

}