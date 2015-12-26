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
import edu.brandeis.cs.nlp.mae.database.DatabaseDriver;
import edu.brandeis.cs.nlp.mae.model.ArgumentType;
import edu.brandeis.cs.nlp.mae.model.AttributeType;
import edu.brandeis.cs.nlp.mae.model.TagType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by krim on 12/26/2015.
 */
public class DTDLoaderTest {

    private DatabaseDriver driver;
    private NewDTDLoader dtdLoader;

    @After
    public void tearDown() throws Exception {
        driver.destroy();

    }

    @Before
    public void setUp() throws Exception {
        driver = new DatabaseDriver(MaeStrings.TEST_DB_URL);
        dtdLoader = new NewDTDLoader(driver);

    }

    @Test
    public void canCreateTagTypeFromDTDString() throws Exception {
        String tagTypeDefinitions = "<!ENTITY name \"NounVerbTask\">\n" +
                "\n" +
                "   <!ELEMENT NOUN ( #PCDATA ) >\n" +
                "<!ELEMENT VERB ( #PCDATA ) >   \n" +
                "<!ELEMENT \n ACTION EMPTY >\n";
        this.dtdLoader.read(tagTypeDefinitions);

        List<TagType> types = driver.getAllTagTypes();
        List<String> typeNames = new ArrayList<>();
        for (TagType type : types) {
            typeNames.add(type.getName());
        }
        assertTrue(
                "Expected DTD name is successfully read, found: " + driver.getDtdName(),
                driver.getDtdName().equals("NounVerbTask")
        );
        assertEquals(
                "Expected 3 tag types defined from DTD, found: " + types.size(),
                3, types.size()
        );
        assertEquals(
                "Expected only 2 of 3 tag types are extent, found: " + driver.getExtentTagTypes().size(),
                2, driver.getExtentTagTypes().size()
        );
        assertEquals(
                "Expected only 1 of 3 tag types are link, found: " + driver.getLinkTagTypes().size(),
                1, driver.getLinkTagTypes().size()
        );
        assertTrue(
                "Expected NOUN, VERB, ACTION is defined, found: " + typeNames.toString(),
                typeNames.contains("NOUN") && typeNames.contains("VERB") && typeNames.contains("ACTION")
        );
    }

    @Test
    public void canCreateTagTypeAndAttributes() throws Exception {
        String tagTypeDefinitions = "<!ENTITY name \"NounVerbTask\">\n" +
                "\n" +
                "<!ELEMENT NOUN ( #PCDATA ) >\n" +
                "<!ATTLIST NOUN spans #IMPLIED >\n" +
                "<!ELEMENT VERB ( #PCDATA ) >\n" +
                "<!ATTLIST VERB tense ( past | present | future | none ) #IMPLIED \"none\" >\n";
        this.dtdLoader.read(tagTypeDefinitions);

        TagType noun = driver.getTagTypeByName("NOUN");
        assertTrue(
                "Expected NOUN to be a extent tag, found: " + noun.isExtent(),
                noun.isExtent()
        );
        assertTrue(
                "Expected NOUN to be non-consuming, found: " + noun.isNonConsuming(),
                noun.isNonConsuming()
        );
        TagType verb = driver.getTagTypeByName("VERB");
        List<AttributeType> verbAtts = new ArrayList<>(verb.getAttributeTypes());
        assertEquals(
                "Expected VERB to have only attribute, found: " + verbAtts.size(),
                1, verbAtts.size()
        );
        AttributeType att = verbAtts.get(0);
        assertEquals(
                "Expected VERB to have only attribute, \"tense\", found: " + att.getName(),
                "tense", att.getName()
        );
        assertEquals(
                "Expected \"tense\" to have \"none\" as its default, found: " + att.getDefaultValue(),
                "none", att.getDefaultValue()
        );
        List<String> values = att.getValuesetAsList();
        assertTrue(
                "Expected \"tense\" to have 4 possible pre-defined values, found: " + values.toString(),
                values.contains("past") && values.contains("present") && values.contains("future") && values.contains("none")
        );
    }

    @Test
    public void canCreateLinkTagTypeWithoutArgSpec() throws Exception {
        String tagTypeDefinitions = "<!ENTITY name \"NounVerbTask\">\n" +
                "\n" +
                "<!ELEMENT ACTION EMPTY >\n" +
                "";
        this.dtdLoader.read(tagTypeDefinitions);

       List<ArgumentType> actionArgs = driver.getArgumentTypesOfLinkTagType(driver.getTagTypeByName("ACTION"));

        assertEquals(
                "Expected an underspecified link tag to have 2 default argument types, found: " + actionArgs.size(),
                2, actionArgs.size()
        );

        List<String> actionArgNames = new ArrayList<>();
        for (ArgumentType actionArg : actionArgs) {
            actionArgNames.add(actionArg.getName());
        }
        assertTrue(
                "Expected the default argument types of an underspecified link tag to be \"from\" and \"to\", found: " + actionArgNames.toString(),
                actionArgNames.contains("from") && actionArgNames.contains("to")
        );
        List<ArgumentType> actionReqArgs = new ArrayList<>();
        for (ArgumentType actionArg : actionArgs) {
            if (actionArg.isRequired()) {
                actionReqArgs.add(actionArg);
            }
        }
        assertTrue(
                "Expected both default arg types (\"from\", \"to\") are not required, found: " + actionReqArgs.size(),
                actionReqArgs.size() == 0
        );
    }

    @Test
    public void canCreateLinkTagTypeWithSpecificArgTypes() throws Exception {
        String tagTypeDefinitions = "<!ENTITY name \"NounVerbTask\">\n" +
                "<!ELEMENT DESCRIPTION EMPTY >\n" +
                "<!ATTLIST DESCRIPTION arg0 IDREF #REQUIRED>\n" +
                "<!ATTLIST DESCRIPTION arg1 IDREF >\n" +
                "<!ELEMENT SRL EMPTY >\n";
        this.dtdLoader.read(tagTypeDefinitions);

        List<ArgumentType> descArgs = driver.getArgumentTypesOfLinkTagType(driver.getTagTypeByName("DESCRIPTION"));
        assertEquals(
                "Expected an \"DESCRIPTION\" to have 2 argument types, found: " + descArgs.size(),
                2, descArgs.size()
        );

        List<String> descArgNames = new ArrayList<>();
        for (ArgumentType descArg : descArgs) {
            descArgNames.add(descArg.getName());
        }
        assertTrue(
                "Expected arguments without prefixes specified have names of argN, found: " + descArgNames.toString(),
                descArgNames.contains("arg0") && descArgNames.contains("arg1")
        );


        for (ArgumentType descArg : descArgs) {
            switch (descArg.getName()) {
                case "arg0":
                    assertTrue(
                            "Expected arg0 of \"DESCRIPTION\" is required as specified, found: " + descArg.isRequired(),
                            descArg.isRequired()
                    );
                    break;
                case "arg1":
                    assertTrue(
                            "Expected arg1 of \"DESCRIPTION\" is required as unspecified, found: " + descArg.isRequired(),
                            !descArg.isRequired()
                    );
                    break;
                default:
                    assertTrue(
                            "Unexpected argument found: " + descArg.getName(),
                            0 == 1
                    );
            }
        }
    }

    @Test
    public void canCreateLinkTagTypeWithCustomArgTypes() throws Exception {
        String tagTypeDefinitions = "<!ENTITY name \"NounVerbTask\">\n" +
                "<!ELEMENT SRL EMPTY >\n" +
                "<!ATTLIST SRL arg0 IDREF prefix=\"agent\" #REQUIRED>\n" +
                "<!ATTLIST SRL arg1 IDREF prefix=\"patient\" #REQUIRED>\n" +
                "<!ATTLIST SRL arg2 IDREF prefix=\"theme\" #REQUIRED>\n" +
                "<!ATTLIST SRL arg3 IDREF prefix=\"predicate\" #REQUIRED>\n";
        this.dtdLoader.read(tagTypeDefinitions);

        List<ArgumentType> srlArgs = driver.getArgumentTypesOfLinkTagType(driver.getTagTypeByName("SRL"));
        assertEquals(
                "Expected an \"SRL\" to have 4 argument types, found: " + srlArgs.size(),
                4, srlArgs.size()
        );

        List<String> srlArgNames = new ArrayList<>();
        for (ArgumentType srlArg : srlArgs) {
            srlArgNames.add(srlArg.getName());
        }
        assertTrue(
                "Expected arguments to have custom names of 'agent', 'patient', 'theme', and 'predicate', found: " + srlArgNames.toString(),
                srlArgNames.contains("agent") && srlArgNames.contains("patient") && srlArgNames.contains("theme") && srlArgNames.contains("predicate")
        );

        List<ArgumentType> srlReqArgs = new ArrayList<>();
        for (ArgumentType actionArg : srlArgs) {
            if (actionArg.isRequired()) {
                srlReqArgs.add(actionArg);
            }
        }
        assertEquals(
                "Expected all 4 arguments of \"SRL\"  are required as specified, found: " + srlReqArgs.size(),
                4, srlReqArgs.size()
        );
    }

    @Test
    public void canReadFile() throws Exception {
        URL sampleFileUrl = Thread.currentThread().getContextClassLoader().getResource("sampleTask.dtd");
        File sampleFile = new File(sampleFileUrl.getPath());
        dtdLoader.read(sampleFile);

    }

    @Test
    public void canValidateDTDFormat() throws Exception {
        // TODO 151226 more tests for validating DTD input, load DTD from file, etc.

    }

}

