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

package edu.brandeis.cs.nlp.mae.database;

import edu.brandeis.cs.nlp.mae.MaeStrings;
import edu.brandeis.cs.nlp.mae.model.*;
import edu.brandeis.cs.nlp.mae.util.MappedSet;
import edu.brandeis.cs.nlp.mae.util.SpanHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
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
    AttributeType nounType;
    ArgumentType pred;
    ArgumentType agent;


    @Before
    public void setUp() throws Exception {
        driver = new LocalSqliteDriverImpl(MaeStrings.TEST_DB_FILE);
        driver.setAnnotationFileName("TEST_SAMPLE");

        noun = driver.createTagType("NOUN", "N", false);
        verb = driver.createTagType("VERB", "V", false);
        semanticRole = driver.createTagType("SR", "S", true);
        nounType = driver.createAttributeType(noun, "type");
        driver.setAttributeTypeDefaultValue(nounType, "person");

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
        Collection<ExtentTag> retrievedTags = (Collection<ExtentTag>) driver.getAllTagsOfType(noun);
        ExtentTag retrievedTag = retrievedTags.iterator().next();
        assertEquals(
                "Expected 1 extent tag is retrieved by generic query, found: " + retrievedTags.size(),
                1, retrievedTags.size());
        Map<String, String> retrievedAtts =  driver.getAttributeMapOfTag(retrievedTag);
        assertEquals(
                "Expected 1 attribute is automatically populated as a default, found: " + retrievedAtts.size(),
                1, retrievedAtts.size());
        assertEquals(
                "Expected \"type\" attribute populated, found: " + retrievedAtts.keySet().iterator().next(),
                "type",  retrievedAtts.keySet().iterator().next());
        assertEquals(
                "Expected \"type\" attribute is set to \"person\" by default, found: " + retrievedAtts.values().iterator().next(),
                "person",  retrievedAtts.values().iterator().next());
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
    public void canUpdateTag() throws Exception {
        int[] span = new int[]{5,6,7,8,9};
        ExtentTag tag = driver.createExtentTag("N01", noun, "jenny", span);
        assertEquals(
                "Expected the span is set accurately, found: " + tag.getSpansAsString(),
                SpanHandler.convertArrayToString(span), tag.getSpansAsString()
        );

        int[] newSpan = new int[]{5,6,7,8,9,13,14,15,16};
        driver.updateTagSpans(tag, newSpan);
        assertEquals(
                "Expected the span is updated, found: " + tag.getSpansAsString(),
                SpanHandler.convertArrayToString(newSpan), tag.getSpansAsString()
        );

        driver.updateTagText(tag, "JANE");
        assertEquals(
                "Expected the text is updated, found: " + tag.getText(),
                "JANE", tag.getText()
        );

    }

    @Test
    public void canDeleteTag() throws Exception {
        ExtentTag tag = driver.createExtentTag("N01", noun, "jenny", 5,6,7,8,9);
        Collection<ExtentTag> retrievedTags = (Collection<ExtentTag>) driver.getAllTagsOfType(noun);
        assertEquals(
                "Expected 1 extent tag is retrieved by generic query, found: " + retrievedTags.size(),
                1, retrievedTags.size());

        driver.deleteTag(tag);
        retrievedTags = (Collection<ExtentTag>) driver.getAllTagsOfType(noun);
        assertEquals(
                "Expected 1 extent tag is successfully deleted, found: " + retrievedTags.size(),
                0, retrievedTags.size());
    }

    @Test
    public void canRetrieveExtentTagsByType() throws Exception {
        driver.createExtentTag("N01", noun, "jenny", 5,6,7,8,9);

        Collection<ExtentTag> retrievedTags = (Collection<ExtentTag>) driver.getAllTagsOfType(noun);
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
    public void canRetrieveAllExtentTagsByTypesInSpan() throws Exception {
        ExtentTag nTag3 = driver.createExtentTag("N03", noun, "jimmy", 16,17,18,19,20);
        ExtentTag nTag4 = driver.createExtentTag("N04", noun, "jim", 16,17,18);
        ExtentTag vTag = driver.createExtentTag("V01", verb, "loves", 11, 12, 13, 14, 15, 16);

        List<ExtentTag> retrievedTags = driver.getTagsOfTypeAt(noun, 16);
        assertEquals(
                "Expected 2 noun tags are stored, found: " + retrievedTags.size(),
                2, retrievedTags.size());
        assertTrue(
                "Expected N03 and N04 are anchored on the testing offset",
                retrievedTags.contains(nTag3) && retrievedTags.contains(nTag4)
        );

        retrievedTags = driver.getTagsOfTypeAt(verb, 16);
        assertEquals(
                "Expected 1 verb tag is stored, found: " + retrievedTags.size(),
                1, retrievedTags.size());
        assertTrue(
                "Expected V01 is anchored on the testing offset",
                retrievedTags.contains(vTag)
        );

    }

    @Test
    public void canRetrieveAllNCTagsByTypes() throws Exception {
        ExtentTag nTag3 = driver.createExtentTag("N03", noun, "jimmy", 16,17,18,19,20);
        ExtentTag nTag4 = driver.createExtentTag("N04", noun, "jim", 16,17,18);
        ExtentTag vTag = driver.createExtentTag("V01", verb, "loves", 11, 12, 13, 14, 15, 16);
        ExtentTag ncNoun = driver.createExtentTag("N01", noun, null, null);
        ExtentTag ncVerb = driver.createExtentTag("V02", verb, null, null);

        List<ExtentTag> retrievedTags = driver.getAllNCTagsOfType(noun);
        assertEquals(
                "Expected 1 noun NC tag is stored, found: " + retrievedTags.size(),
                1, retrievedTags.size());
        assertTrue(
                "Expected N03 and N04 are anchored on the testing offset",
                retrievedTags.contains(ncNoun)
        );

        retrievedTags = driver.getAllNCTagsOfType(verb);
        assertEquals(
                "Expected 1 verb NC tag is stored, found: " + retrievedTags.size(),
                1, retrievedTags.size());
        assertTrue(
                "Expected V01 is anchored on the testing offset",
                retrievedTags.contains(ncVerb)
        );

    }

    @Test
    public void canUpdateAttribute() throws Exception {
        ExtentTag nTag = driver.createExtentTag("N01", noun, "jenny", 5,6,7,8,9);
        AttributeType proper = driver.createAttributeType(noun, "proper");
        driver.addAttribute(nTag, proper, "true");

        assertTrue(
                "Expected an attribute and its type are created, found: " + nTag.getAttributesWithNames().toString(),
                nTag.getAttributes().size() == 2
                        && (new ArrayList<>(nTag.getAttributesWithNames().keySet())).get(1).equals("proper")
                        && (new ArrayList<>(nTag.getAttributesWithNames().values())).get(1).equals("true")
        );

        driver.updateAttribute(nTag, proper, "false");

        assertTrue(
                "Expected an attribute is updated, found: " + nTag.getAttributesWithNames().toString(),
                nTag.getAttributes().size() == 2
                        && (new ArrayList<>(nTag.getAttributesWithNames().keySet())).get(1).equals("proper")
                        && (new ArrayList<>(nTag.getAttributesWithNames().values())).get(1).equals("false")
        );

    }

    @Test
    public void canRetrieveLinkTagsByType() throws Exception {
        ExtentTag nTag = driver.createExtentTag("N01", noun, "jenny", 5,6,7,8,9);
        ExtentTag vTag = driver.createExtentTag("V01", verb, "loves", 11, 12, 13, 14, 15);

        LinkTag link = driver.createLinkTag("A01", semanticRole);
        driver.addArgument(link, agent, nTag);
        driver.addArgument(link, pred, vTag);


        Collection<LinkTag> retrievedTags = (Collection<LinkTag>) driver.getAllTagsOfType(semanticRole);
        assertEquals(
                "Expected 1 link tag is retrieved by generic query, found: " + retrievedTags.size(),
                1, retrievedTags.size());

        retrievedTags = driver.getAllLinkTagsOfType(semanticRole);
        assertEquals(
                "Expected 1 link tag is retrieved by link-only query, found: " + retrievedTags.size(),
                1, retrievedTags.size());

        LinkTag retrievedTag = retrievedTags.iterator().next();
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
        driver.addAttribute(nTag, proper, "true");

        LinkTag link = driver.createLinkTag("A01", semanticRole);
        driver.addArgument(link, agent, nTag);
        driver.addArgument(link, pred, vTag);

        assertTrue(
                "Expected an attribute and it type are created, found: " + nTag.getAttributesWithNames().toString(),
                nTag.getAttributes().size() == 2
                        && (new ArrayList<>(nTag.getAttributesWithNames().keySet())).get(1).equals("proper")
                        && (new ArrayList<>(nTag.getAttributesWithNames().values())).get(1).equals("true")
        );

        LinkTag retrievedTag = driver.getAllLinkTagsOfType(semanticRole).iterator().next();
        assertEquals(
                "Expected 2 arguments associated with the link, found: " + retrievedTag.getArguments().size(),
                2, retrievedTag.getArguments().size());

        driver.emptyAnnotations();

        Collection<ExtentTag> nouns = (Collection<ExtentTag>) driver.getAllTagsOfType(noun);
        Collection<ExtentTag> verbs = (Collection<ExtentTag>) driver.getAllTagsOfType(verb);
        Collection<LinkTag> roles = (Collection<LinkTag>) driver.getAllTagsOfType(semanticRole);
        assertTrue(
                "Expected all tags are wiped out, found tags: " + (nouns.size() + verbs.size() + roles.size()),
                nouns.size() + verbs.size() + roles.size() == 0);
        // cannot continue test on atts/args, because of lack of methods in driver to get atts/args without referencing tag

    }

    @Test
    public void canRetriveTagWithNullAttributes() throws Exception {
        ExtentTag vTag = driver.createExtentTag("V01", verb, "loves", 11, 12, 13, 14, 15);
        assertTrue(vTag != null);

        assertTrue(
                "Expected no attributes are associated, found something. ",
                vTag.getAttributesWithNames().size() == 0
        );

        ExtentTag retrievedTag = (ExtentTag) driver.getTagByTid("V01");
        Map<String, String> retrievedAttMap = retrievedTag.getAttributesWithNamesWithoutChecking();
        assertTrue(
                "Expected no attributes are associated, found: " + retrievedAttMap.toString(),
                retrievedAttMap.size() == 0
        );

    }

    @Test
    public void measureGetAttributesWithName() throws Exception {
        AttributeType properNoun1 = driver.createAttributeType(noun, "isProper1");        
        AttributeType properNoun2 = driver.createAttributeType(noun, "isProper2");        
        AttributeType properNoun3 = driver.createAttributeType(noun, "isProper3");        
        AttributeType properNoun4 = driver.createAttributeType(noun, "isProper4");        
        AttributeType properNoun5 = driver.createAttributeType(noun, "isProper5");        
        AttributeType properNoun6 = driver.createAttributeType(noun, "isProper6");        
        AttributeType properNoun7 = driver.createAttributeType(noun, "isProper7");        
        AttributeType properNoun8 = driver.createAttributeType(noun, "isProper8");        
        AttributeType properNoun9 = driver.createAttributeType(noun, "isProper9");        
        AttributeType properNouna = driver.createAttributeType(noun, "isPropera");        
        AttributeType properNounb = driver.createAttributeType(noun, "isProperb");        
        AttributeType properNounc = driver.createAttributeType(noun, "isProperc");        
        AttributeType properNound = driver.createAttributeType(noun, "isProperd");        
        AttributeType properNoune = driver.createAttributeType(noun, "isPropere");        
        AttributeType properNounf = driver.createAttributeType(noun, "isProperf");        
        AttributeType properNoung = driver.createAttributeType(noun, "isProperg");        
        AttributeType properNounh = driver.createAttributeType(noun, "isProperh");        
        AttributeType properNouni = driver.createAttributeType(noun, "isProperi");        
        AttributeType properNounj = driver.createAttributeType(noun, "isProperj");        
        AttributeType properNounk = driver.createAttributeType(noun, "isProperk");        
        AttributeType properNounl = driver.createAttributeType(noun, "isProperl");        
        AttributeType properNounm = driver.createAttributeType(noun, "isProperm");        
        AttributeType properNounn = driver.createAttributeType(noun, "isPropern");        
        AttributeType properNouno = driver.createAttributeType(noun, "isPropero");        
        AttributeType properNounp = driver.createAttributeType(noun, "isProperp");        
        AttributeType properNounq = driver.createAttributeType(noun, "isProperq");        
        AttributeType properNounr = driver.createAttributeType(noun, "isProperr");        
        AttributeType properNouns = driver.createAttributeType(noun, "isPropers");        
        AttributeType properNount = driver.createAttributeType(noun, "isPropert");        
        AttributeType properNounu = driver.createAttributeType(noun, "isProperu");        
        AttributeType properNounv = driver.createAttributeType(noun, "isProperv");        
        AttributeType properNounw = driver.createAttributeType(noun, "isProperw");        
        AttributeType properNounx = driver.createAttributeType(noun, "isProperx");        
        AttributeType properNouny = driver.createAttributeType(noun, "isPropery");        
        AttributeType properNounz = driver.createAttributeType(noun, "isProperz");        
        ExtentTag nTag = driver.createExtentTag("N01", noun, "John", new int[]{0,1,2,3,4});
        for (AttributeType type : new AttributeType[]{properNoun1,
                properNoun2, properNoun3, properNoun4, properNoun5, properNoun6,
                properNoun7, properNoun8, properNoun9}) {
            driver.addAttribute(nTag, type, Boolean.toString(true));
        }

        for (AttributeType type : new AttributeType[]{properNounb,
                properNound, properNoune, properNoung, properNounh, properNounl,
                properNounn, properNounr, properNounw}) {
            driver.addAttribute(nTag, type, Boolean.toString(false));
        }

        long begin = System.nanoTime();
        ExtentTag tag = (ExtentTag) driver.getTagByTid("N01");
        int repeat = 100;
        for (int i=0; i<repeat; i++) {
            tag.getAttributesWithNames();
        }
        long end = System.nanoTime();
        System.out.println("Tag::getAttributesWithNames repeated " + repeat + " getting: " + (end - begin) / 1e9 + " s");

        begin = System.nanoTime();
        for (int i=0; i<repeat; i++) {
            tag.getAttributesWithNamesWithoutChecking();
        }
        end = System.nanoTime();
        System.out.println("Tag::getAttributesWithNamesWithoutChecking repeated " + repeat + " getting: " + (end - begin) / 1e9 + " s");

        begin = System.nanoTime();
        for (int i=0; i<repeat; i++) {
            driver.getAttributeMapOfTag(tag);
        }
        end = System.nanoTime();
        System.out.println("DriverI::getAttributeMapOfTag repeated " + repeat + " getting: " + (end - begin) / 1e9 + " s");
    }
}