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

package edu.brandeis.cs.nlp.mae.agreement;

import edu.brandeis.cs.nlp.mae.MaeStrings;
import edu.brandeis.cs.nlp.mae.database.LocalSqliteDriverImpl;
import edu.brandeis.cs.nlp.mae.database.MaeDriverI;
import edu.brandeis.cs.nlp.mae.io.DTDLoader;
import edu.brandeis.cs.nlp.mae.util.MappedSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by krim on 4/14/2016.
 */
public class MaeAgreementMainTest {
    private MaeAgreementMain calc;
    private MaeDriverI driver;

    @After
    public void tearDown() throws Exception {
        driver.destroy();

    }

    @Before
    public void setUp() throws Exception {
        driver = new LocalSqliteDriverImpl(MaeStrings.TEST_DB_FILE);
        driver.setAnnotationFileName("TEST_SAMPLE");
        DTDLoader dtdLoader = new DTDLoader(driver);
        URL sampleFileUrl = Thread.currentThread().getContextClassLoader().getResource("iaa_example/iaaSample.dtd");
        File sampleFile = new File(sampleFileUrl.getPath());
        dtdLoader.read(sampleFile);

        calc = new MaeAgreementMain(driver);

        URL exmapleFileUrl = Thread.currentThread().getContextClassLoader().getResource("iaa_example");
        File exampleDir = new File(exmapleFileUrl.getPath());
        calc.loadAnnotationFiles(exampleDir);
    }

    @Test
    public void canValidateTaskNames() throws Exception {
        assertTrue(calc.validateTaskNames("NounVerbTask"));
    }

    @Test
    public void canValidateTextSharing() throws Exception {
        assertTrue(calc.validateTextSharing());

    }

    @Test
    public void testGlobalMultiPiAgreement() throws Exception {
        MappedSet<String, String> sample = new MappedSet<>();
        sample.putCollection("MOOD_DECL", new LinkedList<>());
        sample.putCollection("MOOD_IMPE", new LinkedList<>());
        sample.putCollection("MOOD_SUBJ", new LinkedList<>());
        System.out.println(calc.agreementsToString("GlobalMultiPi", calc.calculateGlobalMultiPi(sample)));
    }

    @Test
    public void testLocalMultiPiAgreement() throws Exception {
        MappedSet<String, String> sample = new MappedSet<>();
        sample.putCollection("NAMED_ENTITY", new LinkedList<String>() {{add("type");}});
        System.out.println(calc.agreementsToString("LocalMultiPi", calc.calculateLocalMultiPi(sample)));
    }

    @Test
    public void testLocalUnitizationAgreement() throws Exception {
        MappedSet<String, String> sample = new MappedSet<>();
        sample.putCollection("NOUN", new LinkedList<String>() {{add("type"); add("comment");}});
        sample.putCollection("VERB", new LinkedList<String>() {{add("tense"); add("aspect");}});
        sample.putCollection("ADJ_ADV", new LinkedList<String>() {{add("type");}});
        System.out.println(calc.agreementsToString("LocalUnitize", calc.calculateLocalAlphaU(sample)));
    }

    @Test
    public void testGlobalUnitizationAgreement() throws Exception {
        MappedSet<String, String> sample = new MappedSet<>();
        sample.putCollection("NOUN", new LinkedList<String>() {{add("type"); add("comment");}});
        sample.putCollection("VERB", new LinkedList<String>() {{add("tense"); add("aspect");}});
        sample.putCollection("ADJ_ADV", new LinkedList<String>() {{add("type");}});
        System.out.println(calc.agreementsToString("GlobalUnitize: " + sample, calc.calculateGlobalAlphaU(sample)));
    }
}