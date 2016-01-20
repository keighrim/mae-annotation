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
import edu.brandeis.cs.nlp.mae.util.MappedSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by krim on 12/13/2015.
 */
public class LocalSqliteDriverImplTest {

    private LocalSqliteDriverImpl driver;

    TagType noun;
    TagType verb;
    TagType semanticRole;
    ArgumentType pred;
    ArgumentType agent;


    @Before
    public void setUp() throws Exception {
        driver = new LocalSqliteDriverImpl(MaeStrings.TEST_DB_FILE);

        noun = driver.createTagType("NOUN", "N", false);
        verb = driver.createTagType("VERB", "V", false);
        semanticRole = driver.createTagType("SR", "S", true);

        pred = driver.createArgumentType(semanticRole, "predicate");
        agent = driver.createArgumentType(semanticRole, "agent");

    }

    @After
    public void tearDown() throws Exception {
        driver.destroy();

    }

    @Test
    public void canCreateExtentTagType() throws Exception {
        TagType adjective = driver.createTagType("ADJ", "AD", false);
        assertFalse(
                "Expected newly created tag type to be extent",
                adjective.isLink());

        TagType retrieved = driver.getTagTypeByName("ADJ");
        assertTrue(
                "Expected retrieved tag type also to be extent",
                retrieved.isExtent());

    }

    @Test
    public void canCreateTag() throws Exception {
        ExtentTag tag = driver.createExtentTag("N01", noun, "jenny", 5,6,7,8,9);
        List<ExtentTag> retrievedTags = (List<ExtentTag>) driver.getAllTagsOfType(noun);
        assertEquals(
                "Expected 1 extent tag is retrieved by generic query, found: " + retrievedTags.size(),
                1, retrievedTags.size());

        ExtentTag retrievedTag = retrievedTags.get(0);
        assertEquals(
                "Expected Obj and Rel share the same ID, found: " + retrievedTag.getTid(),
                tag.getTid(), retrievedTag.getTid()
        );
        assertEquals(
                "Expected Obj and Rel share the same text, found: " + retrievedTag.getText(),
                tag.getText(), retrievedTag.getText()
        );
        assertEquals(
                "Expected Obj and Rel share the same span, found: " + retrievedTag.getSpansAsString(),
                tag.getSpansAsString(), retrievedTag.getSpansAsString()
        );

    }

    @Test
    public void canDeleteTag() throws Exception {
        ExtentTag tag = driver.createExtentTag("N01", noun, "jenny", 5,6,7,8,9);
        List<ExtentTag> retrievedTags = (List<ExtentTag>) driver.getAllTagsOfType(noun);
        assertEquals(
                "Expected 1 extent tag is retrieved by generic query, found: " + retrievedTags.size(),
                1, retrievedTags.size());

        driver.deleteTag(tag);
        retrievedTags = (List<ExtentTag>) driver.getAllTagsOfType(noun);
        assertEquals(
                "Expected 1 extent tag is successfully deleted, found: " + retrievedTags.size(),
                0, retrievedTags.size());
    }

    @Test
    public void canRetrieveExtentTagsByType() throws Exception {
        driver.createExtentTag("N01", noun, "jenny", 5,6,7,8,9);

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
    public void canRetrieveAllExtentTagsByTypes() throws Exception {
        driver.createExtentTag("N01", noun, "jenny", 5, 6, 7, 8, 9);
        driver.createExtentTag("V01", verb, "loves", 11, 12, 13, 14, 15);

        MappedSet<TagType, ExtentTag> retrievedTags = driver.getAllExtentTagsByTypes(false);
        assertEquals(
                "Expected 2 types of tags are stored, found: " + retrievedTags.size(),
                2, retrievedTags.size());

        for (TagType type : retrievedTags.keySet()) {
//        List<TagType> types = driver.getAllTagTypes();
//        for (TagType type : types) { // <-- this doesn't work
            if (type.getName().equals("NOUN")) {
                assertEquals(
                        "Expected 1 noun tag is store, found: " + retrievedTags.get(type).size(),
                        1, retrievedTags.get(type).size());
            } else if (type.getName().equals("VERB")) {
                assertEquals(
                        "Expected 1 verb tag is store, found: " + retrievedTags.get(type).size(),
                        1, retrievedTags.get(type).size());
            }
        }
    }

    @Test
    public void canUpdateAttribute() throws Exception {
        ExtentTag nTag = driver.createExtentTag("N01", noun, "jenny", 5,6,7,8,9);
        AttributeType proper = driver.createAttributeType(noun, "proper");
        driver.addOrUpdateAttribute(nTag, proper, "true");

        assertTrue(
                "Expected an attribute and it type are created, found: " + nTag.getAttributesWithNames().toString(),
                nTag.getAttributes().size() == 1
                        && (new ArrayList<>(nTag.getAttributesWithNames().keySet())).get(0).equals("proper")
                        && (new ArrayList<>(nTag.getAttributesWithNames().values())).get(0).equals("true")
        );

        driver.addOrUpdateAttribute(nTag, proper, "false");

        assertTrue(
                "Expected an attribute is updated, found: " + nTag.getAttributesWithNames().toString(),
                nTag.getAttributes().size() == 1
                        && (new ArrayList<>(nTag.getAttributesWithNames().keySet())).get(0).equals("proper")
                        && (new ArrayList<>(nTag.getAttributesWithNames().values())).get(0).equals("false")
        );

    }

    @Test
    public void canRetrieveLinkTagsByType() throws Exception {
        ExtentTag nTag = driver.createExtentTag("N01", noun, "jenny", 5,6,7,8,9);
        ExtentTag vTag = driver.createExtentTag("V01", verb, "loves", 11, 12, 13, 14, 15);

        LinkTag link = driver.createLinkTag("A01", semanticRole);
        driver.addOrUpdateArgument(link, agent, nTag);
        driver.addOrUpdateArgument(link, pred, vTag);


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

    @Test
    public void canOnlyDropTagTables() throws Exception {
        ExtentTag nTag = driver.createExtentTag("N01", noun, "jenny", 5,6,7,8,9);
        ExtentTag vTag = driver.createExtentTag("V01", verb, "loves", 11, 12, 13, 14, 15);

        AttributeType proper = driver.createAttributeType(noun, "proper");
        driver.addOrUpdateAttribute(nTag, proper, "true");

        LinkTag link = driver.createLinkTag("A01", semanticRole);
        driver.addOrUpdateArgument(link, agent, nTag);
        driver.addOrUpdateArgument(link, pred, vTag);

        assertTrue(
                "Expected an attribute and it type are created, found: " + nTag.getAttributesWithNames().toString(),
                nTag.getAttributes().size() == 1
                        && (new ArrayList<>(nTag.getAttributesWithNames().keySet())).get(0).equals("proper")
                        && (new ArrayList<>(nTag.getAttributesWithNames().values())).get(0).equals("true")
        );

        LinkTag retrievedTag = driver.getAllLinkTagsOfType(semanticRole).get(0);
        assertEquals(
                "Expected 2 arguments associated with the link, found: " + retrievedTag.getArguments().size(),
                2, retrievedTag.getArguments().size());

        driver.emptyAnnotations();

        List<ExtentTag> nouns = (List<ExtentTag>) driver.getAllTagsOfType(noun);
        List<ExtentTag> verbs = (List<ExtentTag>) driver.getAllTagsOfType(verb);
        List<LinkTag> roles = (List<LinkTag>) driver.getAllTagsOfType(semanticRole);
        assertTrue(
                "Expected all tags are wiped out, found tags: " + (nouns.size() + verbs.size() + roles.size()),
                nouns.size() + verbs.size() + roles.size() == 0);
        // cannot continue test on atts/args, because of lack of methods in driver to get atts/args without referencing tag

    }
}