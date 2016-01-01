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

package edu.brandeis.cs.nlp.mae.model;

import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by krim on 12/9/2015.
 */
public class LinkTagTest extends ExtentTagTest {

    TagType semanticRole;
    ArgumentType pred;
    ArgumentType agent;


    @Before
    public void setUp() throws Exception {

        cs = new JdbcConnectionSource(DATABASE_URL);
        setupDatabase(cs);
    }

    @Override
    protected void setupDatabase(ConnectionSource source) throws Exception {
        super.setupDatabase(source);

        semanticRole = new TagType("AGENT", "A", true);
        tagTypeDao.create(semanticRole);

        pred = new ArgumentType(semanticRole, "predicate");
        agent = new ArgumentType(semanticRole, "agent");
        argTypeDao.create(pred);
        argTypeDao.create(agent);

    }

    @Override
    protected void dropAllTables(ConnectionSource source) throws Exception {
        super.dropAllTables(source);
    }

    @After
    public void tearDown() throws Exception {
        // destroy the data source which should close underlying connections
        if (cs != null) {
            dropAllTables(cs);
            cs.close();
        }
    }

    @Test
    public void canSaveLinkTagWithArguments() throws Exception {
        ExtentTag nTag = createTag("N01", noun, "Crown", new int[]{0, 1, 2, 3, 4});
        ExtentTag vTag = createTag("V01", verb, "own", new int[]{2, 3, 4});

        LinkTag link = new LinkTag("A01", semanticRole, "filename");
        Argument agentArg = new Argument(link, agent, nTag);
        Argument predArg = new Argument(link, pred, vTag);
        argDao.create(agentArg);
        argDao.create(predArg);
        lTagDao.create(link);

        assertEquals(
                "Expected 1 link tag in DB, found " + lTagDao.countOf(),
                1, lTagDao.countOf()
        );
        LinkTag retrievedTag = lTagDao.queryBuilder().where().
                eq(DBSchema.TAB_TAG_COL_TID, "A01").queryForFirst();
        assertEquals(
                "Expected retrieved link tag has 2 arguments, found " + retrievedTag.getArguments().size(),
                2, retrievedTag.getArguments().size());

        Map<String, String> arguments = retrievedTag.getArgumentTidsWithNames();

        List<String> argTypes = new LinkedList<>(arguments.keySet());
        List<String> goldTypes = Arrays.asList("agent", "predicate");
        assertTrue(
                "Expected tag to have \"agent\" and \"predicate\" arg types, found " + argTypes,
                argTypes.containsAll(goldTypes) && goldTypes.containsAll(argTypes)
        );
        assertTrue(
                "Expected argument names and values properly mapped, found " + arguments.toString(),
                arguments.get("agent").equals("N01") && arguments.get("predicate").equals("V01")
        );

        Argument retrievedAgent = retrievedTag.getArgumentByTypeName("agent");
        assertTrue(
                "Expected agent argument can return id of its argument tag, found: " + retrievedAgent.getArgumentId(),
                retrievedAgent.getArgumentId().equals("N01")
        );
        assertTrue(
                "Expected agent argument can return text of its argument tag, found: " + retrievedAgent.getArgumentText(),
                retrievedAgent.getArgumentText().equals("Crown")
        );

        Argument retrievedPred = retrievedTag.getArgumentByTypeName("predicate");
        assertTrue(
                "Expected predicate argument can return id of its argument tag, found: " + retrievedPred.getArgumentId(),
                retrievedPred.getArgumentId().equals("V01")
        );
        assertTrue(
                "Expected predicate argument can return text of its argument tag, found: " + retrievedPred.getArgumentType(),
                retrievedPred.getArgumentText().equals("own")
        );

    }
}

