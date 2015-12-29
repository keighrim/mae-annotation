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

package edu.brandeis.cs.nlp.mae.io;

import edu.brandeis.cs.nlp.mae.MaeStrings;
import edu.brandeis.cs.nlp.mae.database.LocalSqliteDriverImpl;
import edu.brandeis.cs.nlp.mae.database.MaeDBException;
import edu.brandeis.cs.nlp.mae.model.ExtentTag;
import edu.brandeis.cs.nlp.mae.model.LinkTag;
import edu.brandeis.cs.nlp.mae.model.TagType;
import edu.brandeis.cs.nlp.mae.util.HashedSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;

/**
 * Created by krim on 12/28/2015.
 */
public class XMLLoaderTest {

    private LocalSqliteDriverImpl driver;
    private NewDTDLoader dtdLoader;
    private NewXMLLoader xmlLoader;
    final private String SAMPLE_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
            "<NounVerbTask>\n";
    final private String SAMPLE_END = "</NounVerbTask>\n";
    final private String SAMPLE_TEXT = "  <TEXT><![CDATA[\n" +
            "Mrs Miller wants the entire house repainted.\n" +
            "]]></TEXT>\n";
    final private String SAMPLE_TAGS_START = "  <TAGS>\n";
    final private String SAMPLE_TAGS_END = " </TAGS>\n";

    final private String SAMPLE_TAG_N0 = "    <NOUN id=\"N0\" spans=\"1~11\" text=\"Mrs Miller\" type=\"other\" comment=\"default value\" /> ";
    final private String SAMPLE_TAG_N1 = "    <NOUN id=\"N1\" start=\"29\" end=\"34\" text=\"house\" type=\"other\" comment=\"default value\" />\n";
    final private String SAMPLE_TAG_N2 = "    <NOUN id=\"N2\" spans=\"-1~-1\" text=\"\" type=\"other\" comment=\"default value\" />\n";

    final private String SAMPLE_TAG_N4 = "    <NOUN id=\"N4\" start=\"1\" text=\"Mrs\" type=\"other\" comment=\"invalid span\" /> ";

    final private String SAMPLE_TAG_V0 = "    <VERB id=\"V0\" spans=\"12~17\" text=\"wants\" tense=\"\" aspect=\"perfect progressive\" />\n";

    final private String SAMPLE_TAG_A0 = "    <ADJ_ADV id=\"A0\" spans=\"35~44\" text=\"repainted\" type=\"\" />\n";
    final private String SAMPLE_TAG_A1 = "    <ADJ_ADV id=\"A1\" spans=\"22~28\" text=\"entire\" type=\"\" />\n";

    final private String SAMPLE_LTAG_DEF = "    <ACTION id=\"AC1\" fromID=\"N2\" fromText=\"\" toID=\"A0\" toText=\"repainted\" relationship=\"performs\" />";
    final private String SAMPLE_LTAG_OPT = "    <DESCRIPTION id=\"D1\" type=\"\" arg0ID=\"N1\" arg0Text=\"house\" arg1ID=\"A1\" arg1Text=\"entire\" arg2ID=\"A0\" arg2Text=\"repainted\" arg3ID=\"\" arg3Text=\"\" arg4ID=\"\" arg4Text=\"\" relationship=\"described_by\" />";
    final private String SAMPLE_LTAG_OBL = "    <ARGUMENTS id=\"AR0\" agentID=\"N0\" agentText=\"Mrs Miller\" patientID=\"N1\" patientText=\"house\" themeID=\"A0\" themeText=\"repainted\" predicateID=\"V0\" predicateText=\"wants\" has_gap=\"no\" />";

    @After
    public void tearDown() throws Exception {
        driver.destroy();

    }

    @Before
    public void setUp() throws Exception {
        driver = new LocalSqliteDriverImpl(MaeStrings.TEST_DB_FILE);
        dtdLoader = new NewDTDLoader(driver);
        readDTDfile();

        xmlLoader = new NewXMLLoader(driver);

    }

    private void readDTDfile() throws MaeIODTDException, MaeDBException {
        URL sampleFileUrl = Thread.currentThread().getContextClassLoader().getResource("sampleTask.dtd");
        File sampleFile = new File(sampleFileUrl.getPath());
        dtdLoader.read(sampleFile);
    }

    @Test(expected = MaeIOXMLException.class)
    public void canValidateEmptyText() throws MaeIOXMLException, MaeDBException {
        String sample = SAMPLE_HEADER + SAMPLE_END;
        xmlLoader.read(sample);

    }

    @Test
    public void canAddPrimaryText() throws MaeIOXMLException, MaeDBException {
        String sample = SAMPLE_HEADER + SAMPLE_TEXT + SAMPLE_END;
        xmlLoader.read(sample);

        assertEquals(
                "Expected successfully read up the primary document text, found: " + driver.getPrimaryText(),
                "\nMrs Miller wants the entire house repainted.\n", driver.getPrimaryText()
        );

    }

    @Test(expected = MaeIOXMLException.class)
    public void canValidateSpansAttribute() throws MaeIOXMLException, MaeDBException {
        String sample = SAMPLE_HEADER + SAMPLE_TEXT + SAMPLE_TAGS_START + SAMPLE_TAG_N0 + SAMPLE_TAG_N4 + SAMPLE_TAGS_END + SAMPLE_END;
        xmlLoader.read(sample);

    }

    @Test
    public void canAddExtentTagsAndAttributes() throws MaeIOXMLException, MaeDBException {
        String sample = SAMPLE_HEADER + SAMPLE_TEXT + SAMPLE_TAGS_START + SAMPLE_TAG_N0 + SAMPLE_TAG_N1 + SAMPLE_TAG_N2 + SAMPLE_TAG_V0 + SAMPLE_TAGS_END + SAMPLE_END;
        xmlLoader.read(sample);

        HashedSet<TagType, ExtentTag> allTags = driver.getAllExtentTagsByTypes(false);

        for (TagType type : allTags.keySet()) {
            if (type.getName().equals("NOUN")) {
                assertEquals(
                        "Expected three NOUN tags are loaded, found: " + allTags.get(type).size(),
                        3, allTags.get(type).size()
                );
                int nonconsuming = 0;
                for (ExtentTag tag : allTags.get(type)) {
                    if (!tag.isConsuming()) {
                        nonconsuming++;
                    }
                }
                assertEquals(
                        "Expected one of NOUN tags are non-consuming, found: " + nonconsuming,
                        1, nonconsuming
                );
            } else if (type.getName().equals("VERB")) {
                assertEquals(
                        "Expected only one VERB tag is loaded, found: " + allTags.get(type).size(),
                        1, allTags.get(type).size()
                );
                ExtentTag v0 = allTags.getAsList(type).get(0);
                assertEquals(
                        "Expected the span of v0 is 12~17 and associated text is \"wants\", found: " + v0.getSpansAsString(),
                        String.format("%d%s%d", 12, MaeStrings.SPANDELIMITER, 17), v0.getSpansAsString()
                );
                assertEquals(
                        "Expected v0's associated text is \"wants\", found: " + v0.getText(),
                        "wants", v0.getText()
                );
                Map<String, String> retrievedAttributes = v0.getAttbutesWithNames();
                assertFalse(
                        "Expected v0 has no tense: ", retrievedAttributes.keySet().contains("tense")
                );
                assertEquals(
                        "Expected the aspect of v0 is \"perfect progressive\", found: " + retrievedAttributes.get("aspect"),
                        "perfect progressive", retrievedAttributes.get("aspect")
                );
            }
        }
    }

    @Test
    public void canAddLinkTagsAndArguments() throws MaeIOXMLException, MaeDBException {
        String sample = SAMPLE_HEADER + SAMPLE_TEXT +
                SAMPLE_TAGS_START +
                SAMPLE_TAG_N0 + SAMPLE_TAG_N1 + SAMPLE_TAG_N2 +
                SAMPLE_TAG_V0 +
                SAMPLE_TAG_A0 + SAMPLE_TAG_A1 +
                SAMPLE_LTAG_DEF + // default from/to arguments
                SAMPLE_LTAG_OPT + // only one(arg0) of 5 argument is required, only 3 (0~2) are annotated
                SAMPLE_LTAG_OBL + // all 4 arguments are required (all of them have custom names)
                SAMPLE_TAGS_END + SAMPLE_END;
        xmlLoader.read(sample);

        List<TagType> linkTypes = driver.getLinkTagTypes();
        for (TagType type : linkTypes) {
            List<LinkTag> retrievedLinkers = driver.getAllLinkTagsOfType(type);
            if (type.getName().equals("ACTION")) {
                assertEquals(
                        "Expected only one action tag is loaded up, found: " + retrievedLinkers.size(),
                        1, retrievedLinkers.size()
                );

                LinkTag ac0 = retrievedLinkers.get(0);
                assertEquals(
                        "Expected ac0 to have two argument types: ", ac0.getTagtype().getArgumentTypes().size(),
                        2, ac0.getTagtype().getArgumentTypes().size()
                );

                Map<String, String> retrievedArguments = ac0.getArgumentTidsWithNames();
                assertEquals(
                        "Expected ac0 to have two actual arguments: ", retrievedArguments.size(),
                        2, retrievedArguments.size()
                );
                assertEquals(
                        "Expected argument 'to' of ac0 is \"A0\", found: " + retrievedArguments.get("to"),
                        "A0", retrievedArguments.get("to")
                );
                assertEquals(
                        "Expected argument 'from' of ac0 is \"N2\", found: " + retrievedArguments.get("from"),
                        "N2", retrievedArguments.get("from")
                );
            }
            if (type.getName().equals("DESCRIPTION")) {
                assertEquals(
                        "Expected only one description tag is loaded up, found: " + retrievedLinkers.size(),
                        1, retrievedLinkers.size()
                );

                LinkTag d1 = retrievedLinkers.get(0);
                assertEquals(
                        "Expected d1 to have five argument types: ", d1.getTagtype().getArgumentTypes().size(),
                        5, d1.getTagtype().getArgumentTypes().size()
                );
                Map<String, String> retrievedArguments = d1.getArgumentTidsWithNames();
                assertEquals(
                        "Expected d1 to have three actual arguments: ", retrievedArguments.size(),
                        3, retrievedArguments.size()
                );
            }

            if (type.getName().equals("ARGUMENTS")) {
                assertEquals(
                        "Expected only one arguments tag is loaded up, found: " + retrievedLinkers.size(),
                        1, retrievedLinkers.size()
                );
                LinkTag ar0 = retrievedLinkers.get(0);
                assertEquals(
                        "Expected ar0 to have four argument types: ", ar0.getTagtype().getArgumentTypes().size(),
                        4, ar0.getTagtype().getArgumentTypes().size()
                );
                Map<String, String> retrievedArguments = ar0.getArgumentTidsWithNames();
                assertEquals(
                        "Expected ar0 to have four actual arguments: ", retrievedArguments.size(),
                        4, retrievedArguments.size()
                );
                Set<String> retrievedArgumentNames = retrievedArguments.keySet();
                assertTrue(
                        "Expected arguments of ar0 to have custom names: ",
                        retrievedArgumentNames.contains("agent") &&
                                retrievedArgumentNames.contains("patient") &&
                                retrievedArgumentNames.contains("theme") &&
                                retrievedArgumentNames.contains("predicate")
                );
            }
        }
    }

}