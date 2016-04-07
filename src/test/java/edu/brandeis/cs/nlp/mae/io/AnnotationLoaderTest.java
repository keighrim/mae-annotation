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

package edu.brandeis.cs.nlp.mae.io;

import edu.brandeis.cs.nlp.mae.MaeStrings;
import edu.brandeis.cs.nlp.mae.database.LocalSqliteDriverImpl;
import edu.brandeis.cs.nlp.mae.database.MaeDBException;
import edu.brandeis.cs.nlp.mae.model.ExtentTag;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by krim on 4/6/16.
 */
public class AnnotationLoaderTest {
    private LocalSqliteDriverImpl driver;
    private DTDLoader dtdLoader;
    private AnnotationLoader loader;

    @After
    public void tearDown() throws Exception {
        driver.destroy();

    }

    @Before
    public void setUp() throws Exception {
        driver = new LocalSqliteDriverImpl(MaeStrings.TEST_DB_FILE);
        driver.setAnnotationFileName("TEST_SAMPLE");
        dtdLoader = new DTDLoader(driver);

    }

    private void readDTDfile(String resName) throws MaeIODTDException, MaeDBException {
        URL sampleFileUrl = Thread.currentThread().getContextClassLoader().getResource(resName);
        File sampleFile = new File(sampleFileUrl.getPath());
        dtdLoader.read(sampleFile);
    }

    private void readSimpleDTD() throws MaeIODTDException, MaeDBException {
        readDTDfile("xml_samples/sampleTask.dtd");
    }

    private void readComplexDTD() throws MaeIODTDException, MaeDBException {
        readDTDfile("complexTask.dtd");
    }

    @Test
    public void canDistinguishXML() throws MaeIOException {
        URL sampleFileUrl = Thread.currentThread().getContextClassLoader().getResource("xml_samples/sampleTask.xml");
        File sampleFile = new File(sampleFileUrl.getPath());
        assertTrue(AnnotationLoader.isXml(sampleFile));
        sampleFileUrl = Thread.currentThread().getContextClassLoader().getResource("xml_samples/sampleTask.dtd");
        sampleFile = new File(sampleFileUrl.getPath());
        assertFalse(AnnotationLoader.isXml(sampleFile));
    }

    @Test
    public void canMatchTaskName() throws MaeIOException {
        URL sampleFileUrl = Thread.currentThread().getContextClassLoader().getResource("xml_samples/sampleTask.xml");
        File sampleFile = new File(sampleFileUrl.getPath());
        assertTrue(AnnotationLoader.isTaskNameMatching(sampleFile, "NounVerbTask"));
        assertFalse(AnnotationLoader.isTaskNameMatching(sampleFile, "nounvERBtAsk"));
    }

    @Test
    public void canMatchText() throws MaeIOException {
        URL sampleFileUrl = Thread.currentThread().getContextClassLoader().getResource("xml_samples/sampleTask.xml");
        File sampleFile = new File(sampleFileUrl.getPath());
        assertTrue(AnnotationLoader.isPrimaryTextMatching(sampleFile, "\nMrs Miller wants the entire house repainted.\n"));
        assertFalse(AnnotationLoader.isPrimaryTextMatching(sampleFile, "NounVerbTask"));

    }

    @Test
    public void canReadSimpleXML() throws IOException, SAXException, MaeIOException, MaeDBException {
        readSimpleDTD();
        loader = new AnnotationLoader(driver);
        URL sampleFileUrl = Thread.currentThread().getContextClassLoader().getResource("xml_samples/sampleTask.xml");
        File sampleFile = new File(sampleFileUrl.getPath());
        loader.readAsXml(sampleFile);
    }

    @Ignore
    public void canReadComplexXML() throws IOException, SAXException, MaeIOException, MaeDBException {
        readComplexDTD();
        loader = new AnnotationLoader(driver);
        URL sampleFileUrl = Thread.currentThread().getContextClassLoader().getResource("xml_samples/complexTask.xml");
        File sampleFile = new File(sampleFileUrl.getPath());
        loader.readAsXml(sampleFile);
    }

    @Ignore
    public void canReadUnicodeHighSurrogateXML() throws IOException, SAXException, MaeIOException, MaeDBException {
        readSimpleDTD();
        loader = new AnnotationLoader(driver);
        URL sampleFileUrl = Thread.currentThread().getContextClassLoader().getResource("xml_samples/emj_sampleTask.xml");
        File sampleFile = new File(sampleFileUrl.getPath());
        loader.readAsXml(sampleFile);
        ExtentTag n2 = (ExtentTag) driver.getTagByTid("N2");
        System.out.println(n2.getText());
    }



}